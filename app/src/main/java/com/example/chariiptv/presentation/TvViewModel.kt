package com.example.chariiptv.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.domain.ChannelRepository
import com.example.chariiptv.domain.RssRepository
import com.example.chariiptv.tvrecommendations.TvRecommendationManager
import com.example.chariiptv.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class TvViewModel @Inject constructor(
    private val repository: ChannelRepository,
    private val rssRepository: RssRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("chari_iptv_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(TvUiState())
    val uiState: StateFlow<TvUiState> = _uiState.asStateFlow()

    private var hasUserManuallySelectedCountry = false
    private var activeLoadJob: Job? = null
    private var hasPublishedInitialHomeChannels = false

    init {
        // Cargar última pestaña recordada (por defecto 'fav' para el primer inicio)
        val savedFilter = prefs.getString("last_filter_id", "fav") ?: "fav"
        val savedCountry = prefs.getString("last_country_code", null)

        val systemCountry = java.util.Locale.getDefault().country.lowercase()
        val defaultCountry = if (Constants.SPANISH_COUNTRIES.contains(systemCountry)) systemCountry else "pe"
        val initialCountry = savedCountry ?: defaultCountry

        _uiState.update { 
            it.copy(
                selectedFilterId = savedFilter,
                selectedCountryCode = initialCountry,
                isCountrySubMenuVisible = (savedFilter == "countries")
            ) 
        }

        if (savedCountry != null) {
            hasUserManuallySelectedCountry = true
        }

        // Detección precisa en segundo plano vía GeoIP por IP de conexión si no ha seleccionado país
        viewModelScope.launch {
            val detectedCountry = repository.detectCountryFromIp()
            if (detectedCountry != null && Constants.SPANISH_COUNTRIES.contains(detectedCountry)) {
                if (!hasUserManuallySelectedCountry) {
                    _uiState.update { it.copy(selectedCountryCode = detectedCountry) }
                    if (_uiState.value.selectedFilterId == "countries") {
                        loadChannelsByCountry(detectedCountry)
                    }
                }
            }
        }

        // Purgar proactivamente cualquier residuo de canal de prueba en base de datos local
        viewModelScope.launch(Dispatchers.IO) {
            repository.purgeTestFavorites()
        }

        // Observación única de favoritos (evita doble consulta SQLite)
        viewModelScope.launch {
            repository.getFavoriteChannelsFlow().collect { favChannels ->
                val favUrls = favChannels.mapTo(HashSet(favChannels.size)) { it.url }
                _uiState.update { state ->
                    if (state.selectedFilterId == "fav") {
                        state.copy(favorites = favUrls, channels = favChannels, isLoading = false)
                    } else {
                        state.copy(favorites = favUrls)
                    }
                }
                if (favChannels.isNotEmpty()) {
                    hasPublishedInitialHomeChannels = true
                    TvRecommendationManager.updateHomeScreenChannels(context, favChannels)
                }
            }
        }
        
        viewModelScope.launch {
            while (true) {
                val result = rssRepository.fetchRssFeed()
                if (result.isSuccess) {
                    val titles = result.getOrNull() ?: emptyList()
                    if (titles.isNotEmpty()) {
                        val formattedNews = titles.joinToString(Constants.RSS_SEPARATOR) { it } + Constants.RSS_SEPARATOR
                        _uiState.update { it.copy(rssNews = formattedNews) }
                    }
                }
                delay(60 * 60 * 1000L) // 1 hour delay
            }
        }
        
        if (savedFilter == "countries") {
            loadChannelsByCountry(initialCountry)
        } else {
            loadFilter(savedFilter)
        }
    }

    fun selectFilter(filterId: String, forceRefresh: Boolean = false) {
        prefs.edit().putString("last_filter_id", filterId).apply()
        if (!forceRefresh && _uiState.value.selectedFilterId == filterId && _uiState.value.channels.isNotEmpty()) {
            return
        }

        if (filterId == "countries") {
            _uiState.update { 
                it.copy(
                    selectedFilterId = "countries",
                    isCountrySubMenuVisible = true,
                    channels = emptyList(),
                    isLoading = true,
                    errorMessage = null
                ) 
            }
            loadChannelsByCountry(_uiState.value.selectedCountryCode, forceRefresh)
            return
        }

        _uiState.update { 
            it.copy(
                selectedFilterId = filterId,
                isCountrySubMenuVisible = false,
                channels = emptyList(),
                isLoading = true,
                errorMessage = null
            ) 
        }
        loadFilter(filterId, forceRefresh)
    }

    fun selectCountry(countryCode: String, forceRefresh: Boolean = false) {
        hasUserManuallySelectedCountry = true
        prefs.edit().putString("last_filter_id", "countries").putString("last_country_code", countryCode).apply()
        if (!forceRefresh && _uiState.value.selectedCountryCode == countryCode && _uiState.value.selectedFilterId == "countries" && _uiState.value.channels.isNotEmpty()) {
            return
        }
        _uiState.update { 
            it.copy(
                selectedFilterId = "countries",
                selectedCountryCode = countryCode,
                isCountrySubMenuVisible = true,
                channels = emptyList(),
                isLoading = true,
                errorMessage = null
            ) 
        }
        loadChannelsByCountry(countryCode, forceRefresh)
    }

    private fun loadFilter(filterId: String, forceRefresh: Boolean = false) {
        activeLoadJob?.cancel()
        activeLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val channels = when (filterId) {
                    "fav" -> repository.getFavoriteChannels()
                    else -> repository.getChannelsByCategory(filterId, forceRefresh)
                }
                _uiState.update { it.copy(isLoading = false, channels = channels) }
                if (!hasPublishedInitialHomeChannels && channels.isNotEmpty()) {
                    hasPublishedInitialHomeChannels = true
                    TvRecommendationManager.updateHomeScreenChannels(context, channels)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar canales") }
            }
        }
    }

    private fun loadChannelsByCountry(countryCode: String, forceRefresh: Boolean = false) {
        activeLoadJob?.cancel()
        activeLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val channels = repository.getChannelsByCountry(countryCode, forceRefresh)
                _uiState.update { it.copy(isLoading = false, channels = channels) }
                if (!hasPublishedInitialHomeChannels && channels.isNotEmpty()) {
                    hasPublishedInitialHomeChannels = true
                    TvRecommendationManager.updateHomeScreenChannels(context, channels)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar canales") }
            }
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
        }
    }

    fun syncFavorites(favoriteUrls: List<String>) {
        viewModelScope.launch {
            repository.syncFavorites(favoriteUrls)
        }
    }

    fun playChannel(channel: Channel?) {
        _uiState.update { it.copy(currentChannel = channel) }
        if (channel != null) {
            viewModelScope.launch {
                TvRecommendationManager.recordWatchedChannel(context, channel)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun prewarmChannel(channel: Channel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.prewarmStream(channel.url)
        }
    }

    suspend fun searchChannelsGlobally(query: String): List<Channel> {
        return repository.searchChannelsGlobally(query)
    }
}
