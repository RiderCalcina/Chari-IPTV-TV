package com.example.chariiptv.domain.provider

import com.example.chariiptv.domain.Channel
import com.example.chariiptv.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IptvOrgProvider @Inject constructor(
    private val client: OkHttpClient
) : IptvProvider {

    override val id: String = "iptv_org"
    override val name: String = "IPTV-Org"
    override val priority: Int = 1

    @Volatile
    private var cachedSpanishChannels: List<Channel>? = null
    private val spanishMutex = Mutex()
    private val countryCache = ConcurrentHashMap<String, List<Channel>>()
    private val countryMutexes = ConcurrentHashMap<String, Mutex>()

    suspend fun fetchSpanishChannels(): List<Channel> = withContext(Dispatchers.IO) {
        cachedSpanishChannels?.let { return@withContext it }

        spanishMutex.withLock {
            cachedSpanishChannels?.let { return@withContext it }

            val url = "https://iptv-org.github.io/iptv/languages/spa.m3u"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val content = response.body?.string() ?: ""
                        val filtered = M3uParser.parse(content)
                            .asSequence()
                            .filter { ch -> !Constants.isForeignOrBlocked(ch.name, ch.url) }
                            .toList()
                        if (filtered.isNotEmpty()) {
                            cachedSpanishChannels = filtered
                        }
                        return@withContext filtered
                    }
                }
            } catch (_: Exception) {}
            return@withContext emptyList()
        }
    }

    suspend fun fetchChannelsByCountry(countryCode: String): List<Channel> = withContext(Dispatchers.IO) {
        val code = countryCode.lowercase()
        countryCache[code]?.let { return@withContext it }

        val mutex = countryMutexes.computeIfAbsent(code) { Mutex() }
        mutex.withLock {
            countryCache[code]?.let { return@withContext it }

            val url = "https://iptv-org.github.io/iptv/countries/$code.m3u"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val content = response.body?.string() ?: ""
                        val filtered = M3uParser.parse(content, defaultCountry = code)
                            .asSequence()
                            .filter { ch -> !Constants.isForeignOrBlocked(ch.name, ch.url) }
                            .distinctBy { it.url.trim().lowercase() }
                            .toList()

                        if (filtered.isNotEmpty()) {
                            countryCache[code] = filtered
                        }
                        return@withContext filtered
                    }
                }
            } catch (_: Exception) {}
            return@withContext emptyList()
        }
    }

    override suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        return@withContext fetchChannelsByCountry("es")
    }

    fun clearCache() {
        cachedSpanishChannels = null
        countryCache.clear()
    }
}
