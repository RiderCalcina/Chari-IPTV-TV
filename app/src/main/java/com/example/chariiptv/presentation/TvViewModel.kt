package com.example.chariiptv.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.domain.ChannelRepository
import com.example.chariiptv.domain.RssRepository
import com.example.chariiptv.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val rssRepository: RssRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TvUiState())
    val uiState: StateFlow<TvUiState> = _uiState.asStateFlow()

    private var hasUserManuallySelectedCountry = false

    init {
        // Detección inicial instantánea mediante configuración regional del sistema
        val systemCountry = java.util.Locale.getDefault().country.lowercase()
        val initialCountry = if (Constants.SPANISH_COUNTRIES.contains(systemCountry)) {
            systemCountry
        } else {
            "pe"
        }
        _uiState.update { it.copy(selectedCountryCode = initialCountry) }

        // Detección precisa en segundo plano vía GeoIP por IP de conexión
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

        viewModelScope.launch {
            repository.getFavoritesFlow().collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
                if (_uiState.value.selectedFilterId == "fav") {
                    val channels = repository.getFavoriteChannels()
                    _uiState.update { it.copy(channels = channels) }
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
        
        loadFilter("fav")
    }

    fun selectFilter(filterId: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.selectedFilterId == filterId && _uiState.value.channels.isNotEmpty()) {
            return
        }

        if (filterId == "countries") {
            _uiState.update { 
                it.copy(
                    selectedFilterId = "countries",
                    isCountrySubMenuVisible = true
                ) 
            }
            loadChannelsByCountry(_uiState.value.selectedCountryCode, forceRefresh)
            return
        }

        _uiState.update { 
            it.copy(
                selectedFilterId = filterId,
                isCountrySubMenuVisible = false
            ) 
        }
        loadFilter(filterId, forceRefresh)
    }

    fun selectCountry(countryCode: String, forceRefresh: Boolean = false) {
        hasUserManuallySelectedCountry = true
        if (!forceRefresh && _uiState.value.selectedCountryCode == countryCode && _uiState.value.selectedFilterId == "countries" && _uiState.value.channels.isNotEmpty()) {
            return
        }
        _uiState.update { 
            it.copy(
                selectedFilterId = "countries",
                selectedCountryCode = countryCode,
                isCountrySubMenuVisible = true
            ) 
        }
        loadChannelsByCountry(countryCode, forceRefresh)
    }

    private fun loadFilter(filterId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, channels = emptyList(), errorMessage = null) }
            try {
                val channels = when (filterId) {
                    "fav" -> repository.getFavoriteChannels()
                    else -> repository.getChannelsByCategory(filterId, forceRefresh)
                }
                _uiState.update { it.copy(isLoading = false, channels = channels) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar canales") }
            }
        }
    }

    private fun loadChannelsByCountry(countryCode: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, channels = emptyList(), errorMessage = null) }
            try {
                val channels = repository.getChannelsByCountry(countryCode, forceRefresh)
                _uiState.update { it.copy(isLoading = false, channels = channels) }
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
    
    fun playChannel(channel: Channel?) {
        _uiState.update { it.copy(currentChannel = channel) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
