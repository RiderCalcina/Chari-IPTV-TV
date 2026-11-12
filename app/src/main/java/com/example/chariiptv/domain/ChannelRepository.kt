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
import kotlinx.coroutines.sync.Semaphore
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

    private var cachedSpanishM3uContent: String? = null
    private val channelsCache = mutableMapOf<String, List<Channel>>()
    private val pingSemaphore = Semaphore(12)

    fun getFavoritesFlow(): Flow<List<String>> = favoriteDao.getFavorites()

    suspend fun toggleFavorite(channel: Channel) {
        val isFav = favoriteDao.isFavorite(channel.url)
        if (isFav) {
            favoriteDao.deleteFavorite(channel.url)
        } else {
            favoriteDao.insertFavorite(
                FavoriteChannel(
                    url = channel.url,
                    name = channel.name,
                    logo = channel.logo
                )
            )
        }
    }

    suspend fun getFavoriteChannels(): List<Channel> = withContext(Dispatchers.IO) {
        val favEntities = favoriteDao.getFavoriteChannels()
        if (favEntities.isEmpty()) return@withContext emptyList()
        return@withContext favEntities.map { Channel(it.name, it.logo, it.url) }
    }

    suspend fun getFavoriteChannels(urls: List<String>): List<Channel> = getFavoriteChannels()

    private suspend fun fetchSpanishM3u(): String? {
        cachedSpanishM3uContent?.let { return it }
        val response = try {
            apiService.getSpanishM3u()
        } catch (e: Exception) {
            null
        }
        val body = if (response?.isSuccessful == true) response.body()?.string() else null
        if (!body.isNullOrEmpty()) {
            cachedSpanishM3uContent = body
        }
        return body
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

        val m3uContent = fetchSpanishM3u() ?: return@withContext emptyList()

        val parsedChannels = mutableListOf<Channel>()
        var currentName = ""
        var currentLogo = ""
        var isCountryChannel = false

        val logoRegex = "tvg-logo=\"([^\"]*)\"".toRegex()
        val countryRegex = "tvg-country=\"([^\"]*)\"".toRegex()
        val idRegex = "tvg-id=\"([^\"]*)\"".toRegex()

        m3uContent.lines().forEach { line ->
            if (line.startsWith("#EXTINF:-1")) {
                currentLogo = logoRegex.find(line)?.groupValues?.get(1)?.trim() ?: ""
                val tvgCountry = countryRegex.find(line)?.groupValues?.get(1)?.lowercase()?.trim() ?: ""
                val tvgId = idRegex.find(line)?.groupValues?.get(1)?.lowercase()?.trim() ?: ""
                val cleanId = tvgId.substringBefore("@")

                if (currentLogo.isBlank() && cleanId.isNotBlank()) {
                    currentLogo = "https://iptv-org.github.io/logos/symbols/$cleanId.png"
                }

                isCountryChannel = (tvgCountry == countryCode || (tvgCountry.isEmpty() && cleanId.endsWith(".$countryCode")))
                currentName = line.substringAfterLast(",").trim()
            } else if (line.startsWith("http") && isCountryChannel) {
                parsedChannels.add(Channel(currentName, currentLogo, line.trim()))
                isCountryChannel = false
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

    suspend fun getChannelsByCategory(categoryKey: String, forceRefresh: Boolean = false): List<Channel> = withContext(Dispatchers.IO) {
        if (forceRefresh) {
            channelsCache.remove(categoryKey)
            cachedChannelDao.deleteChannelsByCountry(categoryKey)
        } else {
            channelsCache[categoryKey]?.let { return@withContext it }
        }

        val cachedDbChannels = cachedChannelDao.getChannelsByCountry(categoryKey)
        if (cachedDbChannels.isNotEmpty()) {
            val channels = cachedDbChannels.map { Channel(it.name, it.logo, it.url) }
            channelsCache[categoryKey] = channels
            return@withContext channels
        }

        val m3uContent = fetchSpanishM3u() ?: return@withContext emptyList()

        val parsedChannels = mutableListOf<Channel>()
        var currentName = ""
        var currentLogo = ""
        var isTargetCategory = false

        val logoRegex = "tvg-logo=\"([^\"]*)\"".toRegex()
        val groupRegex = "group-title=\"([^\"]*)\"".toRegex()

        val categoryKeywords = when (categoryKey) {
            "sports" -> listOf("sports", "deporte")
            "news" -> listOf("news", "noticia")
            "movies" -> listOf("movie", "cine", "película", "pelicula", "film")
            "series" -> listOf("series", "serie")
            "animation" -> listOf("kids", "animation", "infantil", "dibujos", "family")
            "music" -> listOf("music", "música", "musica")
            else -> listOf(categoryKey)
        }

        val idRegex = "tvg-id=\"([^\"]*)\"".toRegex()

        m3uContent.lines().forEach { line ->
            if (line.startsWith("#EXTINF:-1")) {
                currentLogo = logoRegex.find(line)?.groupValues?.get(1)?.trim() ?: ""
                val groupTitle = groupRegex.find(line)?.groupValues?.get(1)?.lowercase()?.trim() ?: ""
                val tvgId = idRegex.find(line)?.groupValues?.get(1)?.lowercase()?.trim() ?: ""
                val cleanId = tvgId.substringBefore("@")

                if (currentLogo.isBlank() && cleanId.isNotBlank()) {
                    currentLogo = "https://iptv-org.github.io/logos/symbols/$cleanId.png"
                }
                
                isTargetCategory = categoryKeywords.any { groupTitle.contains(it) }
                currentName = line.substringAfterLast(",").trim()

                // Exclude any non-Spanish channels that might have foreign language tags
                val isForeign = currentName.contains("(Sweden)", ignoreCase = true) ||
                                currentName.contains("(India)", ignoreCase = true) ||
                                currentName.contains("(Pakistan)", ignoreCase = true) ||
                                currentName.contains("(Turkiye)", ignoreCase = true) ||
                                currentName.contains("(Turkey)", ignoreCase = true) ||
                                currentName.contains("(Arabic)", ignoreCase = true) ||
                                currentName.contains("(Germany)", ignoreCase = true) ||
                                currentName.contains("(Italy)", ignoreCase = true) ||
                                currentName.contains("(United Kingdom)", ignoreCase = true) ||
                                currentName.contains("(France)", ignoreCase = true) ||
                                currentName.contains("(Russia)", ignoreCase = true) ||
                                currentName.contains("(Japan)", ignoreCase = true) ||
                                currentName.contains("(Korea)", ignoreCase = true) ||
                                currentName.contains("(China)", ignoreCase = true)

                if (isForeign) {
                    isTargetCategory = false
                }
            } else if (line.startsWith("http") && isTargetCategory) {
                parsedChannels.add(Channel(currentName, currentLogo, line.trim()))
                isTargetCategory = false
            }
        }

        val uniqueChannels = parsedChannels.distinctBy { it.url }

        val workingChannels = uniqueChannels.map { channel ->
            async {
                if (isStreamWorking(channel.url)) channel else null
            }
        }.awaitAll().filterNotNull()

        val entitiesToSave = workingChannels.map { 
            CachedChannel(it.url, it.name, it.logo, categoryKey)
        }
        cachedChannelDao.insertChannels(entitiesToSave)

        channelsCache[categoryKey] = workingChannels
        return@withContext workingChannels
    }

    private suspend fun isStreamWorking(urlString: String): Boolean = withContext(Dispatchers.IO) {
        pingSemaphore.acquire()
        try {
            val isHls = urlString.contains(".m3u8", ignoreCase = true)
            val requestBuilder = Request.Builder()
                .url(urlString)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
            
            if (!isHls) {
                requestBuilder.header("Range", "bytes=0-1024")
            }
            
            val request = requestBuilder.get().build()
            val response = okHttpClient.newCall(request).execute()
            val code = response.code
            val isSuccess = response.isSuccessful || code == 206 || code in 300..308
            response.close()
            isSuccess
        } catch (e: Exception) {
            false
        } finally {
            pingSemaphore.release()
        }
    }

    suspend fun detectCountryFromIp(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.country.is/")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                response.close()
                val match = "\"country\"\\s*:\\s*\"([A-Za-z]{2})\"".toRegex().find(body)
                val code = match?.groupValues?.get(1)?.lowercase()
                if (!code.isNullOrBlank()) return@withContext code
            } else {
                response.close()
            }
        } catch (e: Exception) {
            try {
                val request2 = Request.Builder()
                    .url("http://ip-api.com/json/?fields=countryCode")
                    .header("User-Agent", "Mozilla/5.0")
                    .build()
                val response2 = okHttpClient.newCall(request2).execute()
                if (response2.isSuccessful) {
                    val body2 = response2.body?.string() ?: ""
                    response2.close()
                    val match2 = "\"countryCode\"\\s*:\\s*\"([A-Za-z]{2})\"".toRegex().find(body2)
                    return@withContext match2?.groupValues?.get(1)?.lowercase()
                } else {
                    response2.close()
                }
            } catch (e2: Exception) {}
        }
        return@withContext null
    }
}
