package com.example.chariiptv.domain

import com.example.chariiptv.data.local.CachedChannel
import com.example.chariiptv.data.local.CachedChannelDao
import com.example.chariiptv.data.local.FavoriteChannel
import com.example.chariiptv.data.local.FavoriteDao
import com.example.chariiptv.domain.provider.AniVodProvider
import com.example.chariiptv.domain.provider.IptvOrgProvider
import com.example.chariiptv.domain.provider.PlutoTvProvider
import com.example.chariiptv.domain.provider.TdtChannelsProvider
import com.example.chariiptv.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val iptvOrgProvider: IptvOrgProvider,
    private val tdtChannelsProvider: TdtChannelsProvider,
    private val plutoTvProvider: PlutoTvProvider,
    private val aniVodProvider: AniVodProvider,
    private val favoriteDao: FavoriteDao,
    private val cachedChannelDao: CachedChannelDao,
    private val okHttpClient: OkHttpClient
) {

    companion object {
        private const val CACHE_TTL_MS = 4L * 60L * 60L * 1000L // 4 horas de vigencia automática
        private const val MAX_ALTERNATE_SOURCES = 6
        private val GEO_COUNTRY_REGEX = Regex("\"country\"\\s*:\\s*\"([A-Za-z]{2})\"")
        private val GEO_CODE_REGEX = Regex("\"countryCode\"\\s*:\\s*\"([A-Za-z]{2})\"")
        private val RAW_IP_HOST_REGEX = Regex("^https?://\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d+)?(?:/|$)", RegexOption.IGNORE_CASE)
        private val SHORT_SPORTS_REGEX = Regex("\\b(?:l1|l2|gol|bein|dazn)\\b", RegexOption.IGNORE_CASE)
    }

    private data class CachedEntry(
        val channels: List<Channel>,
        val updatedAt: Long
    )

    private val channelsCache = ConcurrentHashMap<String, CachedEntry>()

    fun getFavoritesFlow(): Flow<List<String>> = favoriteDao.getFavorites()

    fun getFavoriteChannelsFlow(): Flow<List<Channel>> =
        favoriteDao.getFavoriteChannelsFlow().map { favEntities ->
            favEntities.map { it.toDomainChannel() }
        }

    suspend fun toggleFavorite(channel: Channel) {
        val isFav = favoriteDao.isFavorite(channel.url)
        if (isFav) {
            favoriteDao.deleteFavorite(channel.url)
        } else {
            favoriteDao.insertFavorite(
                FavoriteChannel(
                    url = channel.url,
                    name = channel.name,
                    logo = channel.logo,
                    userAgent = channel.userAgent,
                    referer = channel.referer,
                    alternateSourcesJson = serializeAlternateSources(channel.alternateSources)
                )
            )
        }
    }

    suspend fun getFavoriteChannels(): List<Channel> = withContext(Dispatchers.IO) {
        val favEntities = favoriteDao.getFavoriteChannels()
        if (favEntities.isEmpty()) return@withContext emptyList()
        return@withContext favEntities.map { it.toDomainChannel() }
    }

    suspend fun syncFavorites(favoriteUrls: List<String>) = withContext(Dispatchers.IO) {
        val currentFavs = favoriteDao.getFavoriteChannels().map { it.url }.toSet()
        val missingUrls = favoriteUrls.filter { !currentFavs.contains(it) }
        if (missingUrls.isNotEmpty()) {
            val cached = cachedChannelDao.getChannelsByUrls(missingUrls)
            for (ch in cached) {
                favoriteDao.insertFavorite(
                    FavoriteChannel(
                        url = ch.url,
                        name = ch.name,
                        logo = ch.logo,
                        userAgent = ch.userAgent,
                        referer = ch.referer,
                        alternateSourcesJson = ch.alternateSourcesJson
                    )
                )
            }
        }
    }

    suspend fun getFavoriteChannels(urls: List<String>): List<Channel> = getFavoriteChannels()

    suspend fun searchChannelsGlobally(query: String): List<Channel> = withContext(Dispatchers.IO) {
        val q = query.trim().lowercase()
        if (q.isBlank()) return@withContext emptyList()

        val dbDeferred = async {
            try {
                cachedChannelDao.searchCachedChannels(q).map { it.toDomainChannel() }
            } catch (_: Exception) {
                emptyList()
            }
        }

        val favDeferred = async {
            try {
                favoriteDao.getFavoriteChannels()
                    .map { it.toDomainChannel() }
                    .filter { it.name.lowercase().contains(q) }
            } catch (_: Exception) {
                emptyList()
            }
        }

        val spanishDeferred = async {
            try {
                iptvOrgProvider.fetchSpanishChannels()
                    .filter { it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }
            } catch (_: Exception) {
                emptyList()
            }
        }

        val tdtDeferred = async {
            try {
                tdtChannelsProvider.fetchChannels()
                    .filter { it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }
            } catch (_: Exception) {
                emptyList()
            }
        }

        val plutoDeferred = async {
            try {
                plutoTvProvider.fetchChannels()
                    .filter { it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }
            } catch (_: Exception) {
                emptyList()
            }
        }

        val ghibliDeferred = async {
            try {
                aniVodProvider.fetchGhibliChannels()
                    .filter { it.name.lowercase().contains(q) }
            } catch (_: Exception) {
                emptyList()
            }
        }

        val combined = sequenceOf(
            favDeferred.await(),
            dbDeferred.await(),
            spanishDeferred.await(),
            tdtDeferred.await(),
            plutoDeferred.await(),
            ghibliDeferred.await()
        ).flatten().filter { ch ->
            ch.url.isNotBlank() &&
            (ch.name.lowercase().contains(q) || ch.category.lowercase().contains(q)) &&
            !Constants.isForeignOrBlocked(ch.name, ch.url) &&
            !ch.url.contains("PlaylistM3UCL", ignoreCase = true) &&
            !ch.url.contains("telechancho", ignoreCase = true) &&
            !ch.url.contains("archive.org", ignoreCase = true)
        }

        mergeChannelsWithMirrors(combined)
    }

    suspend fun getChannelsByCountry(countryCode: String, forceRefresh: Boolean = false): List<Channel> = withContext(Dispatchers.IO) {
        val normalizedCode = countryCode.lowercase()
        val now = System.currentTimeMillis()
        var staleFallback: List<Channel> = emptyList()

        if (forceRefresh) {
            channelsCache.remove(normalizedCode)
            tdtChannelsProvider.clearCache()
            plutoTvProvider.clearCache()
            iptvOrgProvider.clearCache()
            aniVodProvider.clearCache()
        } else {
            channelsCache[normalizedCode]?.let { entry ->
                if ((now - entry.updatedAt) < CACHE_TTL_MS) {
                    return@withContext entry.channels
                }
                staleFallback = entry.channels
            }
            val cachedDbChannels = cachedChannelDao.getChannelsByCountry(normalizedCode)
            if (cachedDbChannels.isNotEmpty()) {
                val dbChannels = cachedDbChannels
                    .map { it.toDomainChannel() }
                    .filter { ch ->
                        !Constants.isForeignOrBlocked(ch.name, ch.url) &&
                        !ch.url.contains("PlaylistM3UCL", ignoreCase = true) &&
                        !ch.url.contains("telechancho", ignoreCase = true) &&
                        !ch.url.contains("archive.org", ignoreCase = true)
                    }
                val newestUpdate = cachedDbChannels.maxOfOrNull { it.updatedAt } ?: 0L
                if (newestUpdate > 0L && (now - newestUpdate) < CACHE_TTL_MS) {
                    channelsCache[normalizedCode] = CachedEntry(dbChannels, newestUpdate)
                    return@withContext dbChannels
                }
                // Caché expirada por TTL: refrescar proveedores en memoria para traer URLs nuevas
                staleFallback = dbChannels
                tdtChannelsProvider.clearCache()
                plutoTvProvider.clearCache()
                iptvOrgProvider.clearCache()
                aniVodProvider.clearCache()
            }
        }

        val resultChannels = mutableListOf<Channel>()

        if (normalizedCode == "es") {
            val tdtDeferred = async { tdtChannelsProvider.fetchChannels() }
            val plutoDeferred = async { plutoTvProvider.fetchChannels() }
            val orgDeferred = async { iptvOrgProvider.fetchChannelsByCountry("es") }

            resultChannels.addAll(tdtDeferred.await())
            resultChannels.addAll(plutoDeferred.await())
            resultChannels.addAll(orgDeferred.await())
        } else {
            val orgDeferred = async { iptvOrgProvider.fetchChannelsByCountry(normalizedCode) }
            resultChannels.addAll(orgDeferred.await())
        }

        val filteredSequence = resultChannels
            .asSequence()
            .filter { ch ->
                ch.url.isNotBlank() &&
                !Constants.isForeignOrBlocked(ch.name, ch.url) &&
                !Constants.isPanRegionalThematicChannel(ch.name, ch.url) &&
                !ch.url.contains("PlaylistM3UCL", ignoreCase = true) &&
                !ch.url.contains("telechancho", ignoreCase = true) &&
                !ch.url.contains("archive.org", ignoreCase = true)
            }

        val mergedList = mergeChannelsWithMirrors(filteredSequence)

        if (mergedList.isNotEmpty()) {
            val savedAt = System.currentTimeMillis()
            val entitiesToSave = mergedList.map { it.toCachedEntity(normalizedCode, savedAt) }
            cachedChannelDao.replaceChannelsForCountry(normalizedCode, entitiesToSave)
            channelsCache[normalizedCode] = CachedEntry(mergedList, savedAt)
            return@withContext mergedList
        }

        // Si falló la red durante forceRefresh o expiración de TTL, conservar respaldo previo
        if (staleFallback.isNotEmpty()) {
            return@withContext staleFallback
        }
        val fallbackDb = cachedChannelDao.getChannelsByCountry(normalizedCode)
        if (fallbackDb.isNotEmpty()) {
            val fallback = fallbackDb
                .map { it.toDomainChannel() }
                .filter { ch ->
                    !Constants.isForeignOrBlocked(ch.name, ch.url) &&
                    !ch.url.contains("PlaylistM3UCL", ignoreCase = true) &&
                    !ch.url.contains("telechancho", ignoreCase = true) &&
                    !ch.url.contains("archive.org", ignoreCase = true)
                }
            channelsCache[normalizedCode] = CachedEntry(fallback, now)
            return@withContext fallback
        }

        return@withContext mergedList
    }

    suspend fun getGhibliChannels(forceRefresh: Boolean = false): List<Channel> = withContext(Dispatchers.IO) {
        val cacheKey = "ghibli"
        val now = System.currentTimeMillis()
        var staleFallback: List<Channel> = emptyList()

        if (forceRefresh) {
            channelsCache.remove(cacheKey)
            aniVodProvider.clearCache()
        } else {
            channelsCache[cacheKey]?.let { entry ->
                val isComplete = entry.channels.size >= 25
                if (isComplete && (now - entry.updatedAt) < CACHE_TTL_MS) {
                    return@withContext entry.channels
                }
                staleFallback = entry.channels
                channelsCache.remove(cacheKey)
                aniVodProvider.clearCache()
            }
            val cachedDbChannels = cachedChannelDao.getChannelsByCountry(cacheKey)
            if (cachedDbChannels.isNotEmpty()) {
                val isComplete = cachedDbChannels.size >= 25
                if (isComplete) {
                    val dbChannels = cachedDbChannels
                        .map { it.toDomainChannel() }
                        .sortedBy { GhibliData.getChronologicalIndex(it.name) }
                    val newestUpdate = cachedDbChannels.maxOfOrNull { it.updatedAt } ?: 0L
                    if (newestUpdate > 0L && (now - newestUpdate) < CACHE_TTL_MS) {
                        channelsCache[cacheKey] = CachedEntry(dbChannels, newestUpdate)
                        return@withContext dbChannels
                    }
                    staleFallback = dbChannels
                }
                aniVodProvider.clearCache()
            }
        }

        val ghibliChannels = aniVodProvider.fetchGhibliChannels()
        if (ghibliChannels.isNotEmpty()) {
            val savedAt = System.currentTimeMillis()
            val entitiesToSave = ghibliChannels.map { it.toCachedEntity(cacheKey, savedAt) }
            cachedChannelDao.replaceChannelsForCountry(cacheKey, entitiesToSave)
            channelsCache[cacheKey] = CachedEntry(ghibliChannels, savedAt)
            return@withContext ghibliChannels
        }

        if (staleFallback.isNotEmpty()) {
            return@withContext staleFallback
        }
        val fallbackDb = cachedChannelDao.getChannelsByCountry(cacheKey)
        if (fallbackDb.isNotEmpty()) {
            val fallback = fallbackDb
                .map { it.toDomainChannel() }
                .sortedBy { GhibliData.getChronologicalIndex(it.name) }
            channelsCache[cacheKey] = CachedEntry(fallback, now)
            return@withContext fallback
        }

        return@withContext emptyList()
    }

    suspend fun getChannelsByCategory(categoryKey: String, forceRefresh: Boolean = false): List<Channel> = withContext(Dispatchers.IO) {
        val normalizedKey = categoryKey.lowercase()
        val now = System.currentTimeMillis()
        var staleFallback: List<Channel> = emptyList()

        if (normalizedKey == "ghibli") {
            return@withContext getGhibliChannels(forceRefresh)
        }

        if (forceRefresh) {
            channelsCache.remove(normalizedKey)
            tdtChannelsProvider.clearCache()
            plutoTvProvider.clearCache()
            iptvOrgProvider.clearCache()
            aniVodProvider.clearCache()
        } else {
            channelsCache[normalizedKey]?.let { entry ->
                if ((now - entry.updatedAt) < CACHE_TTL_MS) {
                    return@withContext entry.channels
                }
                staleFallback = entry.channels
            }
            val cachedDbChannels = cachedChannelDao.getChannelsByCountry(normalizedKey)
            if (cachedDbChannels.isNotEmpty()) {
                val dbChannels = cachedDbChannels
                    .map { it.toDomainChannel() }
                    .filter { !Constants.isForeignOrBlocked(it.name, it.url) }
                val newestUpdate = cachedDbChannels.maxOfOrNull { it.updatedAt } ?: 0L
                if (newestUpdate > 0L && (now - newestUpdate) < CACHE_TTL_MS) {
                    channelsCache[normalizedKey] = CachedEntry(dbChannels, newestUpdate)
                    return@withContext dbChannels
                }
                staleFallback = dbChannels
                tdtChannelsProvider.clearCache()
                plutoTvProvider.clearCache()
                iptvOrgProvider.clearCache()
                aniVodProvider.clearCache()
            }
        }

        fun matchesCategory(ch: Channel, catKey: String): Boolean {
            val cat = ch.category.lowercase()
            val name = ch.name.lowercase()

            return when (catKey) {
                "series" -> {
                    if (cat.contains("general") || cat.contains("news") || cat.contains("noticia") ||
                        cat.contains("sports") || cat.contains("deporte") || cat.contains("music") ||
                        cat.contains("música") || cat.contains("religious") || cat.contains("shop") ||
                        name.contains("radio") || name.contains("estereo") || name.contains("estéreo") || name.contains("fm")
                    ) {
                        false
                    } else {
                        val isSeriesCat = cat.contains("series") || cat.contains("telenovela") || cat.contains("novelas") || cat.contains("drama")
                        val isSeriesName = name.contains("series") || name.contains("telenovela") || name.contains("novela") ||
                                name.contains("tnt series") || name.contains("comedy central") || name.contains("syfy")
                        isSeriesCat || isSeriesName
                    }
                }
                "movies" -> {
                    if (cat.contains("general") || cat.contains("news") || cat.contains("noticia") ||
                        cat.contains("sports") || cat.contains("music") || cat.contains("música") ||
                        cat.contains("religious") || name.contains("radio") || name.contains("estereo")
                    ) {
                        false
                    } else {
                        val isMovieCat = cat.contains("movie") || cat.contains("cine") || cat.contains("pelicula") || cat.contains("película")
                        val isMovieName = name.contains("cine") || name.contains("película") || name.contains("pelicula") ||
                                name.contains("cinema") || name.contains("movies") || name.contains("axn") ||
                                name.contains("cinecanal") || name.contains("cinemax") || name.contains("hbo") ||
                                name.contains("golden") || name.contains("space") || name.contains("star channel") ||
                                name.contains("de pelicula") || name.contains("paramount channel")
                        isMovieCat || isMovieName
                    }
                }
                "sports" -> {
                    if (cat.contains("general") || cat.contains("news") || cat.contains("music") || cat.contains("religious")) {
                        false
                    } else {
                        val isSportsCat = cat.contains("sports") || cat.contains("deport") || cat.contains("futbol") || cat.contains("fútbol")
                        val isSportsName = name.contains("deporte") || name.contains("sports") || name.contains("sport") ||
                                name.contains("futbol") || name.contains("fútbol") || name.contains("teledeporte") ||
                                name.contains("tudn") || name.contains("espn") || name.contains("fox sports") ||
                                name.contains("racing") || name.contains("motor") || name.contains("laliga") ||
                                name.contains("liga ") || name.contains("dsports") || name.contains("directv") ||
                                name.contains("tyc sports") || name.contains("claro sports") ||
                                name.contains("bicolor") || name.contains("win+") || name.contains("tigo") ||
                                name.contains("tnt sports") || name.contains("sky sports") ||
                                name.contains("eurosport") || SHORT_SPORTS_REGEX.containsMatchIn(name)
                        isSportsCat || isSportsName
                    }
                }
                "news" -> {
                    if (cat.contains("general") || cat.contains("music") || cat.contains("sports") || cat.contains("religious")) {
                        false
                    } else {
                        val isNewsCat = cat.contains("news") || cat.contains("noticia") || cat.contains("informativ") || cat.contains("weather")
                        val isNewsName = name.contains("noticia") || name.contains("news") || name.contains("24h") ||
                                name.contains("informativo") || name.contains("noticiero") || name.contains("euronews") ||
                                name.contains("cnn") || name.contains("actualidad")
                        isNewsCat || isNewsName
                    }
                }
                "animation" -> {
                    if (cat.contains("general") || cat.contains("news") || cat.contains("music") || cat.contains("sports") || cat.contains("religious")) {
                        false
                    } else {
                        val isKidsCat = cat.contains("animation") || cat.contains("kids") || cat.contains("infantil") || cat.contains("dibujos")
                        val isKidsName = name.contains("infantil") || name.contains("kids") || name.contains("dibujos") ||
                                name.contains("cartoon") || name.contains("disney") || name.contains("nick") ||
                                name.contains("clan") || name.contains("boing") || name.contains("cartoonito") ||
                                name.contains("boomerang") || name.contains("anime")
                        isKidsCat || isKidsName
                    }
                }
                "music" -> {
                    if (cat.contains("news") || cat.contains("sports") || cat.contains("religious")) {
                        false
                    } else {
                        val isMusicCat = cat.contains("music") || cat.contains("música") || cat.contains("musica")
                        val isMusicName = name.contains("música") || name.contains("musica") || name.contains("music") ||
                                name.contains("hit") || name.contains("hits") || name.contains("radio") ||
                                name.contains("urban") || name.contains("pop") || name.contains("rock") ||
                                name.contains("clip") || name.contains("dance") || name.contains("reggaeton") ||
                                name.contains("mtv") || name.contains("fabulosa estereo")
                        isMusicCat || isMusicName
                    }
                }
                else -> cat.contains(catKey) || name.contains(catKey)
            }
        }

        val spanishAllDeferred = async { iptvOrgProvider.fetchSpanishChannels() }
        val tdtDeferred = async { tdtChannelsProvider.fetchChannels() }
        val plutoDeferred = async { plutoTvProvider.fetchChannels() }

        val spanishChannels = spanishAllDeferred.await()
        val tdtChannels = tdtDeferred.await()
        val plutoChannels = plutoDeferred.await()

        val filteredSequence = sequenceOf(tdtChannels, plutoChannels, spanishChannels)
            .flatten()
            .filter { ch ->
                ch.url.isNotBlank() &&
                matchesCategory(ch, normalizedKey) &&
                !Constants.isForeignOrBlocked(ch.name, ch.url)
            }

        val mergedList = mergeChannelsWithMirrors(filteredSequence)

        if (mergedList.isNotEmpty()) {
            val savedAt = System.currentTimeMillis()
            val entitiesToSave = mergedList.map { it.toCachedEntity(normalizedKey, savedAt) }
            cachedChannelDao.replaceChannelsForCountry(normalizedKey, entitiesToSave)
            channelsCache[normalizedKey] = CachedEntry(mergedList, savedAt)
            return@withContext mergedList
        }

        if (staleFallback.isNotEmpty()) {
            return@withContext staleFallback
        }
        val fallbackDb = cachedChannelDao.getChannelsByCountry(normalizedKey)
        if (fallbackDb.isNotEmpty()) {
            val fallback = fallbackDb
                .map { it.toDomainChannel() }
                .filter { !Constants.isForeignOrBlocked(it.name, it.url) }
            channelsCache[normalizedKey] = CachedEntry(fallback, now)
            return@withContext fallback
        }

        return@withContext mergedList
    }

    /**
     * Agrupa fuentes duplicadas por nombre normalizado preservando hasta [MAX_ALTERNATE_SOURCES]
     * servidores espejo (mirrors) para conmutación automática ante errores 403/404/timeout.
     */
    private fun mergeChannelsWithMirrors(channels: Sequence<Channel>): List<Channel> {
        val grouped = LinkedHashMap<String, MutableList<Channel>>()
        val seenUrls = HashSet<String>()

        for (ch in channels) {
            val urlKey = ch.url.trim().lowercase()
            if (urlKey.isBlank() || !seenUrls.add(urlKey)) continue
            val nameKey = Constants.normalizeChannelName(ch.name).ifBlank { urlKey }
            grouped.getOrPut(nameKey) { ArrayList(2) }.add(ch)
        }

        val merged = ArrayList<Channel>(grouped.size)
        for ((_, candidates) in grouped) {
            val ranked = if (candidates.size == 1) {
                candidates
            } else {
                candidates.sortedByDescending { scoreChannelSource(it) }
            }
            val primary = ranked.first()
            val bestLogo = primary.logo.ifBlank {
                ranked.firstOrNull { it.logo.isNotBlank() }?.logo ?: ""
            }
            val alternates = ranked
                .drop(1)
                .take(MAX_ALTERNATE_SOURCES)
                .map { StreamSource(url = it.url, userAgent = it.userAgent, referer = it.referer) }

            merged.add(
                primary.copy(
                    logo = bestLogo,
                    alternateSources = alternates
                )
            )
        }

        return merged.sortedWith(
            compareBy<Channel> { ch ->
                if (isNot247(ch.name)) 1 else 0
            }.thenBy { it.name.lowercase() }
        )
    }

    private fun scoreChannelSource(ch: Channel): Int {
        var score = 0
        val url = ch.url.trim().lowercase()
        if (!isNot247(ch.name)) score += 100
        if (url.startsWith("https://")) score += 30
        if (!RAW_IP_HOST_REGEX.containsMatchIn(url)) score += 25
        if (url.contains(".m3u8")) score += 15
        if (ch.referer.isBlank() && ch.userAgent.isBlank()) score += 5
        return score
    }

    private fun isNot247(name: String): Boolean {
        return name.contains("[not 24/7]", ignoreCase = true) ||
                name.contains("(not 24/7)", ignoreCase = true)
    }

    private fun Channel.toCachedEntity(bucketKey: String, updatedAt: Long): CachedChannel {
        return CachedChannel(
            url = url,
            name = name,
            logo = logo,
            countryCode = bucketKey,
            userAgent = userAgent,
            referer = referer,
            alternateSourcesJson = serializeAlternateSources(alternateSources),
            updatedAt = updatedAt
        )
    }

    private fun CachedChannel.toDomainChannel(): Channel {
        return Channel(
            name = name,
            logo = logo,
            url = url,
            country = countryCode,
            userAgent = userAgent,
            referer = referer,
            alternateSources = deserializeAlternateSources(alternateSourcesJson)
        )
    }

    private fun FavoriteChannel.toDomainChannel(): Channel {
        return Channel(
            name = name,
            logo = logo,
            url = url,
            userAgent = userAgent,
            referer = referer,
            alternateSources = deserializeAlternateSources(alternateSourcesJson)
        )
    }

    private fun serializeAlternateSources(sources: List<StreamSource>): String {
        if (sources.isEmpty()) return ""
        return try {
            val array = JSONArray()
            for (src in sources) {
                val obj = JSONObject()
                obj.put("u", src.url)
                if (src.userAgent.isNotBlank()) obj.put("ua", src.userAgent)
                if (src.referer.isNotBlank()) obj.put("r", src.referer)
                array.put(obj)
            }
            array.toString()
        } catch (_: Exception) {
            ""
        }
    }

    private fun deserializeAlternateSources(rawJson: String): List<StreamSource> {
        if (rawJson.isBlank()) return emptyList()
        return try {
            val array = JSONArray(rawJson)
            val list = ArrayList<StreamSource>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val url = obj.optString("u", "")
                if (url.isNotBlank()) {
                    list.add(
                        StreamSource(
                            url = url,
                            userAgent = obj.optString("ua", ""),
                            referer = obj.optString("r", "")
                        )
                    )
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun detectCountryFromIp(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.country.is/")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val code = GEO_COUNTRY_REGEX.find(body)?.groupValues?.get(1)?.lowercase()
                    if (!code.isNullOrBlank()) return@withContext code
                }
            }
        } catch (_: Exception) {
            try {
                val request2 = Request.Builder()
                    .url("http://ip-api.com/json/?fields=countryCode")
                    .header("User-Agent", "Mozilla/5.0")
                    .build()
                okHttpClient.newCall(request2).execute().use { response2 ->
                    if (response2.isSuccessful) {
                        val body2 = response2.body?.string() ?: ""
                        return@withContext GEO_CODE_REGEX.find(body2)?.groupValues?.get(1)?.lowercase()
                    }
                }
            } catch (_: Exception) {}
        }
        return@withContext null
    }

    @Volatile
    private var lastPrewarmedHost: String? = null

    /**
     * Pre-calentamiento especulativo de red al enfocar un canal con el control remoto:
     * Resuelve el DNS anticipadamente mediante la pila DoH para que al presionar OK el socket
     * arranque de inmediato sin latencia de resolución de dominio.
     */
    suspend fun prewarmStream(streamUrl: String) = withContext(Dispatchers.IO) {
        try {
            val uri = android.net.Uri.parse(streamUrl)
            val host = uri.host ?: return@withContext
            if (host == lastPrewarmedHost) return@withContext
            lastPrewarmedHost = host

            okHttpClient.dns.lookup(host)
        } catch (_: Exception) {
            // Silencioso: optimización anticipatoria
        }
    }
}
