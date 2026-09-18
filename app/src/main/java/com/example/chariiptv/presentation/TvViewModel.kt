package com.example.chariiptv.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.domain.ChannelRepository
import com.example.chariiptv.domain.RssRepository
import com.example.chariiptv.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    init {
        viewModelScope.launch {
            repository.getFavoritesFlow().collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
                if (_uiState.value.selectedCountryCode == "fav") {
                    val channels = repository.getFavoriteChannels(favs)
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
                } else {
                    // Log error if needed, avoiding raw exception swallow
                    // Timber.e(result.exceptionOrNull())
                }
                delay(60 * 60 * 1000L) // 1 hour delay
            }
        }
        
        loadChannels("pe")
    }

    fun selectCountry(countryCode: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.selectedCountryCode == countryCode && _uiState.value.channels.isNotEmpty()) {
            return
        }
        _uiState.update { it.copy(selectedCountryCode = countryCode) }
        loadChannels(countryCode, forceRefresh)
    }

    private fun loadChannels(countryCode: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, channels = emptyList(), errorMessage = null) }
            try {
                val channels = if (countryCode == "fav") {
                    repository.getFavoriteChannels(_uiState.value.favorites)
                } else {
                    repository.getChannelsByCountry(countryCode, forceRefresh)
                }
                _uiState.update { it.copy(isLoading = false, channels = channels) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error loading channels") }
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

