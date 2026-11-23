package com.example.chariiptv.domain.provider

import com.example.chariiptv.domain.Channel
import com.example.chariiptv.domain.GhibliData
import com.example.chariiptv.domain.StreamSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AniVodProvider @Inject constructor(
    private val client: OkHttpClient
) : IptvProvider {

    override val id: String = "ghibli_vod"
    override val name: String = "Studio Ghibli"
    override val priority: Int = 4

    companion object {
        private const val GHIBLI_URL = "https://mametchikitty.github.io/Listas-IPTV/studio-ghibli-latino.m3u"

        private val ARCHIVE_NODE_REGEX = Regex(
            "https?://(?:ia\\d+\\.us|dn\\d+\\.[a-z]{2})\\.archive\\.org/(?:\\d+/)?items/(.+)",
            RegexOption.IGNORE_CASE
        )
    }

    @Volatile
    private var cachedGhibliChannels: List<Channel>? = null
    private val mutex = Mutex()

    suspend fun fetchGhibliChannels(): List<Channel> = withContext(Dispatchers.IO) {
        cachedGhibliChannels?.let { return@withContext it }

        mutex.withLock {
            cachedGhibliChannels?.let { return@withContext it }

            val rawChannels = fetchPlaylist(GHIBLI_URL)
            val cleanList = rawChannels.map { ch ->
                val movieInfo = GhibliData.getMovieInfo(ch.name)
                val (primaryUrl, fallbackUrl) = getArchiveUrls(ch.url)
                val alternates = if (fallbackUrl != null) {
                    listOf(StreamSource(url = fallbackUrl))
                } else {
                    emptyList()
                }
                ch.copy(
                    name = movieInfo.title,
                    url = primaryUrl,
                    logo = rewriteCoverUrl(ch.logo),
                    category = "ghibli",
                    country = "lat",
                    alternateSources = alternates
                )
            }.distinctBy { it.name.trim().lowercase() }

            // Películas faltantes oficiales integradas con Opción A: BitChute principal + Archive.org respaldo
            val nausicaaChannel = Channel(
                name = GhibliData.NAUSICAA.title,
                url = "https://seed306b.bitchute.com/kAlc7YDz7UAT/6tYcn0Pm6UKb.mp4",
                logo = "https://static-3.bitchute.com/live/cover_images/kAlc7YDz7UAT/6tYcn0Pm6UKb_640x360.jpg",
                category = "ghibli",
                country = "lat",
                alternateSources = listOf(
                    StreamSource(url = "https://archive.org/download/ndvdv-1080p-lat-jap/NDVDV%20%5B1080p%5D%5BLat%2BJap%5D.mp4"),
                    StreamSource(url = "https://archive.org/download/nausicaa-del-valle-del-viento-1080/Nausicaa%CC%88%20del%20valle%20del%20viento%201080.mp4")
                )
            )

            val garzaChannel = Channel(
                name = GhibliData.EL_CHICO_Y_LA_GARZA.title,
                url = "https://seed167b.bitchute.com/kAlc7YDz7UAT/BUKWUILaXcVJ.mp4",
                logo = "https://static-3.bitchute.com/live/cover_images/kAlc7YDz7UAT/BUKWUILaXcVJ_640x360.jpg",
                category = "ghibli",
                country = "lat",
                alternateSources = listOf(
                    StreamSource(url = "https://archive.org/download/271353-c-8a-96984-2768-457c-94aa-acc-6fb-06ebfe-mxui-2375428-1fichier/271353--c8a96984-2768-457c-94aa-acc6fb06ebfe--mxui--2375428-1fichier.mp4")
                )
            )

            // Consolidación de la filmografía completa de 25 películas en orden cronológico histórico
            val fullCatalog = (cleanList + listOf(nausicaaChannel, garzaChannel))
                .distinctBy { it.name.trim().lowercase() }
                .sortedBy { GhibliData.getChronologicalIndex(it.name) }

            if (fullCatalog.isNotEmpty()) {
                cachedGhibliChannels = fullCatalog
            }
            return@withContext fullCatalog
        }
    }

    private fun rewriteCoverUrl(rawLogo: String): String {
        var clean = rawLogo.trim()
        if (clean.isBlank()) return ""

        // Corregir repositorio telechancho-iptv -> github.io raíz (HTTP 200)
        clean = clean.replace("telechancho.github.io/telechancho-iptv/covers/", "telechancho.github.io/covers/")
        clean = clean.replace("telechancho.github.io/telechancho-iptv/", "telechancho.github.io/")
        return clean
    }

    private fun fetchPlaylist(url: String): List<Channel> {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val content = response.body?.string() ?: ""
                    M3uParser.parse(content, defaultCountry = "lat")
                } else {
                    emptyList()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun getArchiveUrls(rawUrl: String): Pair<String, String?> {
        val trimmed = rawUrl.trim()
        val match = ARCHIVE_NODE_REGEX.matchEntire(trimmed)
        val path = if (match != null) {
            match.groupValues[1]
        } else if (trimmed.contains("archive.org/download/", ignoreCase = true)) {
            trimmed.substringAfter("archive.org/download/")
        } else if (trimmed.contains("archive.org/items/", ignoreCase = true)) {
            trimmed.substringAfter("archive.org/items/")
        } else {
            null
        }

        if (path != null) {
            val canonicalUrl = "https://archive.org/download/$path"
            val fastStartUrl = if (path.endsWith(".mp4", ignoreCase = true) && !path.endsWith(".ia.mp4", ignoreCase = true)) {
                val withoutExt = path.substring(0, path.length - 4)
                "https://archive.org/download/$withoutExt.ia.mp4"
            } else {
                canonicalUrl
            }
            val fallbackUrl = if (fastStartUrl != canonicalUrl) canonicalUrl else null
            return Pair(fastStartUrl, fallbackUrl)
        }

        val normalized = if (trimmed.startsWith("http://ia", ignoreCase = true)) {
            "https://" + trimmed.substring(7)
        } else {
            trimmed
        }
        return Pair(normalized, null)
    }

    override suspend fun fetchChannels(): List<Channel> = fetchGhibliChannels()

    fun clearCache() {
        cachedGhibliChannels = null
    }
}
