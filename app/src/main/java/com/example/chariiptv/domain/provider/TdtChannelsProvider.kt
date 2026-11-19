package com.example.chariiptv.domain.provider

import com.example.chariiptv.domain.Channel
import com.example.chariiptv.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TdtChannelsProvider @Inject constructor(
    private val client: OkHttpClient
) : IptvProvider {

    companion object {
        private val OTHER_COUNTRY_TOKENS = listOf(
            "perú", "peru", "chequia", "czech", "usa",
            "alemania", "germany", "francia", "france",
            "italia", "italy", "portugal", "rusia", "russia",
            "uk", "reino unido", "china", "japan", "japón",
            "méxico", "mexico", "colombia", "argentina", "chile", "ecuador"
        )
    }

    override val id: String = "tdt_channels"
    override val name: String = "TDT España Oficial"
    override val priority: Int = 2

    @Volatile
    private var cachedChannels: List<Channel>? = null
    private val fetchMutex = Mutex()

    override suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        cachedChannels?.let { return@withContext it }

        fetchMutex.withLock {
            cachedChannels?.let { return@withContext it }

            val url = "https://www.tdtchannels.com/lists/tv.m3u8"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val content = response.body?.string() ?: ""
                        // Filtrado riguroso: solo canales oficiales de España
                        val spanishOnly = M3uParser.parse(content, defaultCountry = "es")
                            .asSequence()
                            .filter { ch ->
                                val catLower = ch.category.lowercase()
                                val isInternationalGroup = catLower.startsWith("int.") ||
                                        catLower.contains("internacional") ||
                                        catLower.contains("deportivos int")
                                if (isInternationalGroup) return@filter false

                                val nameLower = ch.name.lowercase()
                                OTHER_COUNTRY_TOKENS.none { nameLower.contains(it) } &&
                                !Constants.isForeignOrBlocked(ch.name, ch.url)
                            }
                            .distinctBy { it.url.trim().lowercase() }
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
