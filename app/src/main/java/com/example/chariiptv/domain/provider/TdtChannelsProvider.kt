package com.example.chariiptv.domain.provider

import com.example.chariiptv.domain.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TdtChannelsProvider @Inject constructor(
    private val client: OkHttpClient
) : IptvProvider {

    override val id: String = "tdt_channels"
    override val name: String = "TDT España Oficial"
    override val priority: Int = 2

    private var cachedChannels: List<Channel>? = null

    override suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        cachedChannels?.let { return@withContext it }
        val url = "https://www.tdtchannels.com/lists/tv.m3u8"
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

                // Filtrado riguroso: solo canales oficiales de España (excluir grupos internacionales y otros países)
                val spanishOnly = parsed.filter { ch ->
                    val catLower = ch.category.lowercase()
                    val nameLower = ch.name.lowercase()

                    val isInternationalGroup = catLower.startsWith("int.") ||
                            catLower.contains("internacional") ||
                            catLower.contains("deportivos int")

                    val isOtherCountry = nameLower.contains("perú") ||
                            nameLower.contains("peru") ||
                            nameLower.contains("chequia") ||
                            nameLower.contains("czech") ||
                            nameLower.contains("usa") ||
                            nameLower.contains("alemania") ||
                            nameLower.contains("germany") ||
                            nameLower.contains("francia") ||
                            nameLower.contains("france") ||
                            nameLower.contains("italia") ||
                            nameLower.contains("italy") ||
                            nameLower.contains("portugal") ||
                            nameLower.contains("rusia") ||
                            nameLower.contains("russia") ||
                            nameLower.contains("uk") ||
                            nameLower.contains("reino unido") ||
                            nameLower.contains("china") ||
                            nameLower.contains("japan") ||
                            nameLower.contains("japón") ||
                            nameLower.contains("méxico") ||
                            nameLower.contains("mexico") ||
                            nameLower.contains("colombia") ||
                            nameLower.contains("argentina") ||
                            nameLower.contains("chile") ||
                            nameLower.contains("ecuador")

                    !isInternationalGroup && !isOtherCountry
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
