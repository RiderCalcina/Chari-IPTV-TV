package com.example.chariiptv.domain.provider

import com.example.chariiptv.domain.Channel
import com.example.chariiptv.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IptvOrgProvider @Inject constructor(
    private val client: OkHttpClient
) : IptvProvider {

    override val id: String = "iptv_org"
    override val name: String = "IPTV-Org"
    override val priority: Int = 1

    private var cachedSpanishChannels: List<Channel>? = null
    private val countryCache = mutableMapOf<String, List<Channel>>()

    suspend fun fetchSpanishChannels(): List<Channel> = withContext(Dispatchers.IO) {
        cachedSpanishChannels?.let { return@withContext it }

        val url = "https://iptv-org.github.io/iptv/languages/spa.m3u"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val content = response.body?.string() ?: ""
                response.close()
                val parsed = M3uParser.parse(content)
                val filtered = parsed.filter { ch ->
                    !Constants.isForeignOrBlocked(ch.name, ch.url)
                }
                if (filtered.isNotEmpty()) {
                    cachedSpanishChannels = filtered
                }
                return@withContext filtered
            }
            response.close()
        } catch (_: Exception) {}
        return@withContext emptyList()
    }

    suspend fun fetchChannelsByCountry(countryCode: String): List<Channel> = withContext(Dispatchers.IO) {
        val code = countryCode.lowercase()
        countryCache[code]?.let { return@withContext it }

        val url = "https://iptv-org.github.io/iptv/countries/$code.m3u"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val content = response.body?.string() ?: ""
                response.close()
                val parsed = M3uParser.parse(content, defaultCountry = code)
                val filtered = parsed.filter { ch ->
                    !Constants.isForeignOrBlocked(ch.name, ch.url)
                }.distinctBy { Constants.normalizeChannelName(it.name) }

                if (filtered.isNotEmpty()) {
                    countryCache[code] = filtered
                }
                return@withContext filtered
            }
            response.close()
        } catch (_: Exception) {}
        return@withContext emptyList()
    }

    override suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        return@withContext fetchChannelsByCountry("es")
    }

    fun clearCache() {
        cachedSpanishChannels = null
        countryCache.clear()
    }
}
