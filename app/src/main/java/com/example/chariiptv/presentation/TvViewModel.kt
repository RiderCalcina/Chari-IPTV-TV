package com.example.chariiptv.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.domain.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class TvViewModel @Inject constructor(
    private val repository: ChannelRepository
) : ViewModel() {

    private val _selectedCountryCode = MutableStateFlow("pe")
    val selectedCountryCode: StateFlow<String> = _selectedCountryCode

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel

    val favorites: StateFlow<List<String>> = repository.getFavoritesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _rssNews = MutableStateFlow<String>("")
    val rssNews: StateFlow<String> = _rssNews

    init {
        viewModelScope.launch {
            favorites.collect { favs ->
                if (_selectedCountryCode.value == "fav") {
                    _channels.value = repository.getFavoriteChannels(favs)
                }
            }
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val titles = fetchRssFeed("https://qwertyaserty.com/rblog/rss")
                    if (titles.isNotEmpty()) {
                        val separator = "   ★   www.qwertyaserty.com   ★   "
                        _rssNews.value = titles.joinToString(separator) { it } + separator
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(60 * 60 * 1000L) // 1 hour delay
            }
        }
        
        loadChannels("pe")
    }

    fun selectCountry(countryCode: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && _selectedCountryCode.value == countryCode && _channels.value.isNotEmpty()) {
            return
        }
        _selectedCountryCode.value = countryCode
        loadChannels(countryCode, forceRefresh)
    }

    private fun loadChannels(countryCode: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _channels.value = emptyList() // Always clear when loading
            if (countryCode == "fav") {
                _channels.value = repository.getFavoriteChannels(favorites.value)
            } else {
                _channels.value = repository.getChannelsByCountry(countryCode, forceRefresh)
            }
            _isLoading.value = false
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
        }
    }
    
    fun playChannel(channel: Channel?) {
        _currentChannel.value = channel
    }

    private fun fetchRssFeed(urlString: String): List<String> {
        val titles = mutableListOf<String>()
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 10000
            urlConnection.readTimeout = 10000
            
            val inputStream: InputStream = urlConnection.inputStream
            val textContent = inputStream.bufferedReader().use { it.readText() }.trimStart()
            
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(java.io.StringReader(textContent))

            var eventType = parser.eventType
            var isInsideItem = false
            
            while (eventType != XmlPullParser.END_DOCUMENT && titles.size < 5) {
                val tagName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            isInsideItem = true
                        } else if (tagName.equals("title", ignoreCase = true) && isInsideItem) {
                            val titleText = parser.nextText()
                            titles.add(titleText.trim())
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            isInsideItem = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } finally {
            urlConnection?.disconnect()
        }
        return titles
    }
}
