package com.example.chariiptv.domain

import com.example.chariiptv.data.local.FavoriteChannel
import com.example.chariiptv.data.local.FavoriteDao
import com.example.chariiptv.data.local.CachedChannel
import com.example.chariiptv.data.local.CachedChannelDao
import com.example.chariiptv.data.remote.M3uApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val apiService: M3uApiService,
    private val favoriteDao: FavoriteDao,
    private val cachedChannelDao: CachedChannelDao,
    private val okHttpClient: OkHttpClient
) {

    private var cachedM3uContent: String? = null
    private val channelsCache = mutableMapOf<String, List<Channel>>()

    fun getFavoritesFlow(): Flow<List<String>> = favoriteDao.getFavorites()

    suspend fun toggleFavorite(channel: Channel) {
        val isFav = favoriteDao.isFavorite(channel.url)
        if (isFav) {
            favoriteDao.deleteFavorite(channel.url)
        } else {
            favoriteDao.insertFavorite(FavoriteChannel(url = channel.url))
        }
    }

    suspend fun getFavoriteChannels(urls: List<String>): List<Channel> = withContext(Dispatchers.IO) {
        if (urls.isEmpty()) return@withContext emptyList()
        val cachedDbChannels = cachedChannelDao.getChannelsByUrls(urls)
        return@withContext cachedDbChannels.map { Channel(it.name, it.logo, it.url) }
    }

    suspend fun getChannelsByCountry(countryCode: String, forceRefresh: Boolean = false): List<Channel> = withContext(Dispatchers.IO) {
        if (forceRefresh) {
            channelsCache.remove(countryCode)
            cachedChannelDao.deleteChannelsByCountry(countryCode)
        } else {
            channelsCache[countryCode]?.let { return@withContext it }
        }

        val cachedDbChannels = cachedChannelDao.getChannelsByCountry(countryCode)
        if (cachedDbChannels.isNotEmpty()) {
            val channels = cachedDbChannels.map { Channel(it.name, it.logo, it.url) }
            channelsCache[countryCode] = channels
            return@withContext channels
        }

        val m3uContent = cachedM3uContent ?: try {
            val response = apiService.getGlobalM3u()
            if (response.isSuccessful) {
                val body = response.body()?.string()
                cachedM3uContent = body
                body
            } else null
        } catch (e: Exception) {
            null
        }

        m3uContent ?: return@withContext emptyList()

        val parsedChannels = mutableListOf<Channel>()
        var currentName = ""
        var currentLogo = ""
        var isCountryChannel = false

        m3uContent.lines().forEach { line ->
            if (line.startsWith("#EXTINF:-1")) {
                isCountryChannel = false
                
                val logoRegex = "tvg-logo=\"([^\"]+)\"".toRegex()
                currentLogo = logoRegex.find(line)?.groupValues?.get(1) ?: ""
                
                val countryRegex = "tvg-country=\"([^\"]+)\"".toRegex()
                val tvgCountry = countryRegex.find(line)?.groupValues?.get(1)?.lowercase() ?: ""
                
                val idRegex = "tvg-id=\"([^\"]+)\"".toRegex()
                val tvgId = idRegex.find(line)?.groupValues?.get(1)?.lowercase() ?: ""
                val cleanId = tvgId.substringBefore("@")
                
                if (tvgCountry == countryCode || (tvgCountry.isEmpty() && cleanId.endsWith(".$countryCode"))) {
                    isCountryChannel = true
                }
                currentName = line.substringAfterLast(",").trim()
            } else if (line.startsWith("http") && isCountryChannel) {
                parsedChannels.add(Channel(currentName, currentLogo, line.trim()))
            }
        }

        val uniqueChannels = parsedChannels.distinctBy { it.url }

        val workingChannels = uniqueChannels.map { channel ->
            async {
                if (isStreamWorking(channel.url)) channel else null
            }
        }.awaitAll().filterNotNull()

        val entitiesToSave = workingChannels.map { 
            CachedChannel(it.url, it.name, it.logo, countryCode)
        }
        cachedChannelDao.insertChannels(entitiesToSave)

        channelsCache[countryCode] = workingChannels
        return@withContext workingChannels
    }

    private fun isStreamWorking(urlString: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(urlString)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Range", "bytes=0-500")
                .get()
                .build()
            val response = okHttpClient.newCall(request).execute()
            val code = response.code
            val contentType = response.header("Content-Type") ?: ""
            response.close()
            val isSuccess = response.isSuccessful || code == 206 || code == 302 || code == 301
            isSuccess && !contentType.contains("text/html", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}
