package com.example.chariiptv.domain.provider

import com.example.chariiptv.domain.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlutoTvProvider @Inject constructor(
    private val client: OkHttpClient
) : IptvProvider {

    override val id: String = "pluto_tv"
    override val name: String = "Pluto TV España"
    override val priority: Int = 3

    private var cachedChannels: List<Channel>? = null

    override suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        cachedChannels?.let { return@withContext it }
        val url = "https://raw.githubusercontent.com/BuddyChewChew/pluto/refs/heads/main/pluto_es.m3u"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val content = response.body?.string() ?: ""
                response.close()
                val parsed = M3uParser.parse(content, defaultCountry = "es")

                // Filtrar canales no hispanos en la grilla de Pluto TV
                val spanishOnly = parsed.filter { ch ->
                    val nameLower = ch.name.lowercase()
                    !nameLower.contains("failarmy") &&
                            !nameLower.contains("fifa+") &&
                            !nameLower.contains("(vo)") &&
                            !nameLower.contains("(vose)") &&
                            !nameLower.contains("english")
                }.distinctBy { it.name.trim().lowercase() }

                cachedChannels = spanishOnly
                return@withContext spanishOnly
            }
            response.close()
        } catch (_: Exception) {}
        return@withContext emptyList()
    }

    fun clearCache() {
        cachedChannels = null
    }
}
