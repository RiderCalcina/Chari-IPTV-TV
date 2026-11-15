package com.example.chariiptv.domain

import com.example.chariiptv.data.local.FavoriteChannel
import com.example.chariiptv.data.local.FavoriteDao
import com.example.chariiptv.data.local.CachedChannel
import com.example.chariiptv.data.local.CachedChannelDao
import com.example.chariiptv.domain.provider.IptvOrgProvider
import com.example.chariiptv.domain.provider.PlutoTvProvider
import com.example.chariiptv.domain.provider.TdtChannelsProvider
import com.example.chariiptv.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val iptvOrgProvider: IptvOrgProvider,
    private val tdtChannelsProvider: TdtChannelsProvider,
    private val plutoTvProvider: PlutoTvProvider,
    private val favoriteDao: FavoriteDao,
    private val cachedChannelDao: CachedChannelDao,
    private val okHttpClient: OkHttpClient,
    @Named("pingOkHttpClient") private val pingOkHttpClient: OkHttpClient
) {

    private val channelsCache = mutableMapOf<String, List<Channel>>()

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

    suspend fun getChannelsByCountry(countryCode: String, forceRefresh: Boolean = false): List<Channel> = withContext(Dispatchers.IO) {
        val normalizedCode = countryCode.lowercase()
        if (forceRefresh) {
            channelsCache.remove(normalizedCode)
            cachedChannelDao.deleteChannelsByCountry(normalizedCode)
            tdtChannelsProvider.clearCache()
            plutoTvProvider.clearCache()
            iptvOrgProvider.clearCache()
        } else {
            channelsCache[normalizedCode]?.let { return@withContext it }
            val cachedDbChannels = cachedChannelDao.getChannelsByCountry(normalizedCode)
            if (cachedDbChannels.isNotEmpty()) {
                val channels = cachedDbChannels.map { Channel(it.name, it.logo, it.url, it.countryCode) }
                channelsCache[normalizedCode] = channels
                return@withContext channels
            }
        }

        val resultChannels = mutableListOf<Channel>()

        if (normalizedCode == "es") {
            // Para España: TDTChannels oficiales + Pluto TV España + IPTV-Org España
            val tdtDeferred = async { tdtChannelsProvider.fetchChannels() }
            val plutoDeferred = async { plutoTvProvider.fetchChannels() }
            val orgDeferred = async { iptvOrgProvider.fetchChannelsByCountry("es") }

            val tdt = tdtDeferred.await()
            val pluto = plutoDeferred.await()
            val org = orgDeferred.await()

            resultChannels.addAll(tdt)
            resultChannels.addAll(pluto)
            resultChannels.addAll(org)
        } else {
            // Para cualquier otro país (Ecuador, Rep. Dominicana, Perú, México, etc.):
            // Canal oficial exclusivo del país sin mezclas extranjeras
            val countryChannels = iptvOrgProvider.fetchChannelsByCountry(normalizedCode)
            resultChannels.addAll(countryChannels)
        }

        // Filtrar canales no hispanos, geobloqueados o cadenas panregionales de cable y desduplicar
        val distinctList = resultChannels
            .filter { ch -> 
                !Constants.isForeignOrBlocked(ch.name, ch.url) &&
                !Constants.isPanRegionalThematicChannel(ch.name, ch.url)
            }
            .distinctBy { Constants.normalizeChannelName(it.name) }
            .sortedWith(
                compareBy<Channel> { ch ->
                    // Priorizar transmisiones 24/7 activas al inicio de la lista
                    if (ch.name.contains("[not 24/7]", ignoreCase = true) || ch.name.contains("(not 24/7)", ignoreCase = true)) 1 else 0
                }.thenBy { it.name }
            )

        if (distinctList.isNotEmpty()) {
            val entitiesToSave = distinctList.map { 
                CachedChannel(it.url, it.name, it.logo, normalizedCode)
            }
            cachedChannelDao.insertChannels(entitiesToSave)
            channelsCache[normalizedCode] = distinctList
        }

        return@withContext distinctList
    }

    suspend fun getChannelsByCategory(categoryKey: String, forceRefresh: Boolean = false): List<Channel> = withContext(Dispatchers.IO) {
        val normalizedKey = categoryKey.lowercase()
        if (forceRefresh) {
            channelsCache.remove(normalizedKey)
            cachedChannelDao.deleteChannelsByCountry(normalizedKey)
            tdtChannelsProvider.clearCache()
            plutoTvProvider.clearCache()
            iptvOrgProvider.clearCache()
        } else {
            channelsCache[normalizedKey]?.let { return@withContext it }
            val cachedDbChannels = cachedChannelDao.getChannelsByCountry(normalizedKey)
            if (cachedDbChannels.isNotEmpty()) {
                val channels = cachedDbChannels.map { Channel(it.name, it.logo, it.url, it.countryCode) }
                channelsCache[normalizedKey] = channels
                return@withContext channels
            }
        }

        fun matchesCategory(ch: Channel, catKey: String): Boolean {
            val cat = ch.category.lowercase()
            val name = ch.name.lowercase()

            return when (catKey) {
                "series" -> {
                    // Excluir canales generalistas de TV nacional, noticias, radios, deportes o religión
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
                                name.contains("gol") || name.contains("tudn") || name.contains("espn") ||
                                name.contains("fox sports") || name.contains("racing") || name.contains("motor") ||
                                name.contains("laliga") || name.contains("dsports") || name.contains("directv") ||
                                name.contains("tyc sports") || name.contains("claro sports")
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

        // Carga enriquecida: todo el catálogo en español (spa.m3u) + canales temáticos de TDT y Pluto
        val spanishAllDeferred = async { iptvOrgProvider.fetchSpanishChannels() }
        val tdtDeferred = async { tdtChannelsProvider.fetchChannels() }
        val plutoDeferred = async { plutoTvProvider.fetchChannels() }

        val spanishChannels = spanishAllDeferred.await()
        val tdtChannels = tdtDeferred.await()
        val plutoChannels = plutoDeferred.await()

        val allCandidates = ArrayList<Channel>(spanishChannels.size + tdtChannels.size + plutoChannels.size)

        // Canales temáticos filtrados rigurosamente por su categoría real
        allCandidates.addAll(tdtChannels.filter { matchesCategory(it, normalizedKey) })
        allCandidates.addAll(plutoChannels.filter { matchesCategory(it, normalizedKey) })
        allCandidates.addAll(spanishChannels.filter { matchesCategory(it, normalizedKey) })

        val filtered = allCandidates.filter { ch ->
            !Constants.isForeignOrBlocked(ch.name, ch.url) && ch.url.isNotBlank()
        }
        .distinctBy { it.url.trim().lowercase() }
        .distinctBy { Constants.normalizeChannelName(it.name) }
        .sortedWith(
            compareBy<Channel> { ch ->
                if (ch.name.contains("[not 24/7]", ignoreCase = true) || ch.name.contains("(not 24/7)", ignoreCase = true)) 1 else 0
            }.thenBy { it.name }
        )

        if (filtered.isNotEmpty()) {
            val entitiesToSave = filtered.map { 
                CachedChannel(it.url, it.name, it.logo, normalizedKey)
            }
            cachedChannelDao.insertChannels(entitiesToSave)
            channelsCache[normalizedKey] = filtered
        }

        return@withContext filtered
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
        } catch (_: Exception) {
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
            } catch (_: Exception) {}
        }
        return@withContext null
    }
}
