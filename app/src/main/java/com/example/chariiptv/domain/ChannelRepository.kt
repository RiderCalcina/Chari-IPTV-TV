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
import javax.inject.Named
import javax.inject.Singleton

private data class RawParsedChannel(
    val name: String,
    val logo: String,
    val url: String,
    val country: String,
    val cleanId: String,
    val groupTitle: String
)

@Singleton
class ChannelRepository @Inject constructor(
    private val apiService: M3uApiService,
    private val favoriteDao: FavoriteDao,
    private val cachedChannelDao: CachedChannelDao,
    private val okHttpClient: OkHttpClient,
    @Named("pingOkHttpClient") private val pingOkHttpClient: OkHttpClient
) {

    private var cachedSpanishM3uContent: String? = null
    private var cachedRawChannels: List<RawParsedChannel>? = null
    private val channelsCache = mutableMapOf<String, List<Channel>>()
    private val pingSemaphore = Semaphore(16)

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

    private fun extractAttribute(line: String, attr: String): String {
        val key = "$attr=\""
        val startIdx = line.indexOf(key)
        if (startIdx == -1) return ""
        val valStart = startIdx + key.length
        val endIdx = line.indexOf('"', valStart)
        if (endIdx == -1) return ""
        return line.substring(valStart, endIdx).trim()
    }

    private suspend fun getSpanishRawChannels(): List<RawParsedChannel> {
        cachedRawChannels?.let { return it }
        val m3uContent = fetchSpanishM3u() ?: return emptyList()

        val list = ArrayList<RawParsedChannel>(1200)
        var currentName = ""
        var currentLogo = ""
        var currentCountry = ""
        var currentCleanId = ""
        var currentGroup = ""
        var hasExtInf = false

        m3uContent.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.startsWith("#EXTINF:-1")) {
                currentLogo = extractAttribute(line, "tvg-logo")
                currentCountry = extractAttribute(line, "tvg-country").lowercase()
                val tvgId = extractAttribute(line, "tvg-id").lowercase()
                currentCleanId = tvgId.substringBefore("@")
                currentGroup = extractAttribute(line, "group-title").lowercase()

                if (currentLogo.isBlank() && currentCleanId.isNotBlank()) {
                    currentLogo = "https://iptv-org.github.io/logos/symbols/$currentCleanId.png"
                }

                currentName = line.substringAfterLast(",").trim()
                hasExtInf = true
            } else if (hasExtInf && line.startsWith("http")) {
                list.add(
                    RawParsedChannel(
                        name = currentName,
                        logo = currentLogo,
                        url = line,
                        country = currentCountry,
                        cleanId = currentCleanId,
                        groupTitle = currentGroup
                    )
                )
                hasExtInf = false
            }
        }

        cachedRawChannels = list
        return list
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

        val rawChannels = getSpanishRawChannels()
        if (rawChannels.isEmpty()) return@withContext emptyList()

        val matchingChannels = rawChannels.filter { raw ->
            raw.country == countryCode || (raw.country.isEmpty() && raw.cleanId.endsWith(".$countryCode"))
        }.map { Channel(it.name, it.logo, it.url) }.distinctBy { it.url }

        val workingChannels = matchingChannels.map { channel ->
            async {
                if (isStreamWorking(channel.url)) channel else null
            }
        }.awaitAll().filterNotNull()

        val finalChannels = if (workingChannels.isNotEmpty()) workingChannels else matchingChannels

        val entitiesToSave = finalChannels.map { 
            CachedChannel(it.url, it.name, it.logo, countryCode)
        }
        cachedChannelDao.insertChannels(entitiesToSave)

        channelsCache[countryCode] = finalChannels
        return@withContext finalChannels
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

        val rawChannels = getSpanishRawChannels()
        if (rawChannels.isEmpty()) return@withContext emptyList()

        val categoryKeywords = when (categoryKey) {
            "sports" -> listOf("sports", "deporte")
            "news" -> listOf("news", "noticia")
            "movies" -> listOf("movie", "cine", "película", "pelicula", "film")
            "series" -> listOf("series", "serie")
            "animation" -> listOf("kids", "animation", "infantil", "dibujos", "family")
            "music" -> listOf("music", "música", "musica")
            else -> listOf(categoryKey)
        }

        val matchingChannels = rawChannels.filter { raw ->
            val isForeign = isForeignChannel(raw.name)
            !isForeign && categoryKeywords.any { raw.groupTitle.contains(it) }
        }.map { Channel(it.name, it.logo, it.url) }.distinctBy { it.url }

        val workingChannels = matchingChannels.map { channel ->
            async {
                if (isStreamWorking(channel.url)) channel else null
            }
        }.awaitAll().filterNotNull()

        val finalChannels = if (workingChannels.isNotEmpty()) workingChannels else matchingChannels

        val entitiesToSave = finalChannels.map { 
            CachedChannel(it.url, it.name, it.logo, categoryKey)
        }
        cachedChannelDao.insertChannels(entitiesToSave)

        channelsCache[categoryKey] = finalChannels
        return@withContext finalChannels
    }

    private fun isForeignChannel(name: String): Boolean {
        return name.contains("(Sweden)", ignoreCase = true) ||
                name.contains("(India)", ignoreCase = true) ||
                name.contains("(Pakistan)", ignoreCase = true) ||
                name.contains("(Turkiye)", ignoreCase = true) ||
                name.contains("(Turkey)", ignoreCase = true) ||
                name.contains("(Arabic)", ignoreCase = true) ||
                name.contains("(Germany)", ignoreCase = true) ||
                name.contains("(Italy)", ignoreCase = true) ||
                name.contains("(United Kingdom)", ignoreCase = true) ||
                name.contains("(France)", ignoreCase = true) ||
                name.contains("(Russia)", ignoreCase = true) ||
                name.contains("(Japan)", ignoreCase = true) ||
                name.contains("(Korea)", ignoreCase = true) ||
                name.contains("(China)", ignoreCase = true)
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
            val response = pingOkHttpClient.newCall(request).execute()
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
