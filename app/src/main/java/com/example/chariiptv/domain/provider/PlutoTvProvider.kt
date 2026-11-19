package com.example.chariiptv.domain.provider

import com.example.chariiptv.domain.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlutoTvProvider @Inject constructor(
    private val client: OkHttpClient
) : IptvProvider {

    companion object {
        private val EXCLUDED_PLUTO_TOKENS = listOf("failarmy", "fifa+", "(vo)", "(vose)", "english")
    }

    override val id: String = "pluto_tv"
    override val name: String = "Pluto TV España"
    override val priority: Int = 3

    @Volatile
    private var cachedChannels: List<Channel>? = null
    private val fetchMutex = Mutex()

    override suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        cachedChannels?.let { return@withContext it }

        fetchMutex.withLock {
            cachedChannels?.let { return@withContext it }

            val url = "https://raw.githubusercontent.com/BuddyChewChew/pluto/refs/heads/main/pluto_es.m3u"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val content = response.body?.string() ?: ""
                        val spanishOnly = M3uParser.parse(content, defaultCountry = "es")
                            .asSequence()
                            .filter { ch ->
                                val nameLower = ch.name.lowercase()
                                EXCLUDED_PLUTO_TOKENS.none { nameLower.contains(it) }
                            }
                            .distinctBy { it.name.trim().lowercase() }
                            .toList()

                        if (spanishOnly.isNotEmpty()) {
                            cachedChannels = spanishOnly
                        }
                        return@withContext spanishOnly
                    }
                }
            } catch (_: Exception) {}
            return@withContext emptyList()
        }
    }

    fun clearCache() {
        cachedChannels = null
    }
}
