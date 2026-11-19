package com.example.chariiptv.util

data class CategoryItem(
    val id: String,
    val name: String,
    val icon: String
)

object Constants {
    val SUPPORTED_CATEGORIES = listOf(
        CategoryItem("sports", "Deportes", "⚽"),
        CategoryItem("news", "Noticias", "📰"),
        CategoryItem("movies", "Películas", "🎬"),
        CategoryItem("series", "Series", "📺"),
        CategoryItem("animation", "Infantil", "👶"),
        CategoryItem("music", "Música", "🎵")
    )

    val SPANISH_COUNTRIES = setOf(
        "ar", "bo", "cl", "co", "cr", "cu", "do", "ec",
        "es", "gt", "hn", "mx", "ni", "pa", "pe", "pr",
        "py", "sv", "uy", "ve"
    )

    val COUNTRY_FLAGS = mapOf(
        "ar" to "🇦🇷", "bo" to "🇧🇴", "cl" to "🇨🇱", "co" to "🇨🇴",
        "cr" to "🇨🇷", "cu" to "🇨🇺", "do" to "🇩🇴", "ec" to "🇪🇨",
        "es" to "🇪🇸", "gt" to "🇬🇹", "hn" to "🇭🇳", "mx" to "🇲🇽",
        "ni" to "🇳🇮", "pa" to "🇵🇦", "pe" to "🇵🇪", "pr" to "🇵🇷",
        "py" to "🇵🇾", "sv" to "🇸🇻", "uy" to "🇺🇾", "ve" to "🇻🇪"
    )

    val SUPPORTED_COUNTRIES = mapOf(
        "fav" to "Favoritos",
        "ar" to "Argentina", "bo" to "Bolivia", "cl" to "Chile", "co" to "Colombia",
        "cr" to "Costa Rica", "cu" to "Cuba", "do" to "Rep. Dominicana", "ec" to "Ecuador",
        "es" to "España", "gt" to "Guatemala", "hn" to "Honduras", "mx" to "México",
        "ni" to "Nicaragua", "pa" to "Panamá", "pe" to "Perú", "pr" to "Puerto Rico",
        "py" to "Paraguay", "sv" to "El Salvador", "uy" to "Uruguay", "ve" to "Venezuela"
    )

    fun getFilterDisplayName(filterKey: String): String {
        if (filterKey == "fav") return "Favoritos"
        if (filterKey == "countries") return "Países"
        SUPPORTED_CATEGORIES.firstOrNull { it.id == filterKey }?.let { return it.name }
        return SUPPORTED_COUNTRIES[filterKey] ?: filterKey
    }

    private val PAN_REGIONAL_HINTS = listOf(
        "latin america", "latinamerica", "panregional", "central america",
        "sudamerica", "suramerica"
    )

    private val THEMATIC_BRANDS = listOf(
        "axn", "cinecanal", "cinemax", "hbo", "universal tv", "studio universal",
        "warner", "sony channel", "sony movies", "star channel", "paramount network",
        "space", "golden", "de pelicula", "tnt series", "tnt novela", "tnt",
        "discovery channel", "discovery kids", "discovery science", "discovery turbo", "discovery home",
        "animal planet", "national geographic", "nat geo", "history", "h2",
        "disney channel", "disney jr", "cartoon network", "cartoonito", "boomerang",
        "nickelodeon", "nick jr", "tooncarrusel",
        "espn", "fox sports", "dsports", "directv sports", "tyc sports", "claro sports", "goltv",
        "comedy central", "mtv", "e! entertainment", "a&e", "lifetime", "tlc"
    )

    private val SHORT_BRAND_REGEX = Regex(
        "\\b(?:${THEMATIC_BRANDS.filter { it.length <= 4 }.joinToString("|") { Regex.escape(it) }})\\b"
    )

    private val LONG_THEMATIC_BRANDS = THEMATIC_BRANDS.filter { it.length > 4 }

    private val GEO_BLOCKED_TOKENS = listOf(
        "[geo-blocked]", "(geo-blocked)", "[geoblocked]", "(geoblocked)",
        "[geo]", "(geo)"
    )

    private val GEO_TAG_REGEX = Regex("\\bgeo\\b", RegexOption.IGNORE_CASE)

    private val FOREIGN_TOKENS = listOf(
        "(sweden)", "(suecia)", "(india)", "(pakistan)", "(pakistán)",
        "(turkiye)", "(turkey)", "(turquía)", "(arabic)", "(árabe)",
        "(germany)", "(alemania)", "alemania", "(italy)", "(italia)",
        "(united kingdom)", "(reino unido)", "(uk)", "(france)", "(francia)",
        "(russia)", "(rusia)", "(japan)", "(japón)", "(korea)", "(corea)",
        "(china)", "(poland)", "(polonia)", "(greece)", "(grecia)",
        "(romania)", "(rumania)", "(netherlands)", "(holanda)",
        "(denmark)", "(dinamarca)", "(norway)", "(noruega)",
        "(finland)", "(finlandia)", "chequia", "czech",
        "das erste", "zdf", "kika", "failarmy", "fifa+",
        "ocko", "óčko", "english", "[eng]", "(eng)", "(vo)", "(vose)"
    )

    private val NORMALIZE_REGEX = Regex(
        "\\(\\d+p\\)|\\[(?:not 24/7|geo-blocked|geoblocked)]|\\((?:geo-blocked|sd|hd|fhd)\\)",
        RegexOption.IGNORE_CASE
    )

    fun isPanRegionalThematicChannel(name: String, url: String = ""): Boolean {
        val lowerName = name.lowercase()

        // Canales con nombres explícitos de transmisión panregional o satelital
        for (hint in PAN_REGIONAL_HINTS) {
            if (lowerName.contains(hint)) return true
        }

        // Marcas panregionales de cine, series, infantiles o deportes de pago
        for (brand in LONG_THEMATIC_BRANDS) {
            if (lowerName.contains(brand)) return true
        }
        return SHORT_BRAND_REGEX.containsMatchIn(lowerName)
    }

    fun isForeignOrBlocked(name: String, url: String = ""): Boolean {
        val lowerName = name.lowercase()
        val lowerUrl = url.lowercase()

        // Excluir canales explícitamente geobloqueados o redes de intranet privadas / servidores caídos
        if (lowerUrl.contains("bantel-cdn1.iptvperu.tv")) return true
        if (lowerUrl.contains("barinaslive1.boldmss.com")) return true

        for (token in GEO_BLOCKED_TOKENS) {
            if (lowerName.contains(token)) return true
        }

        // Excluir canales de TDTChannels marcados con GEO o GEO CAT (ej: Esport3 GEO, Teledeporte GEO)
        // preservando canales válidos de documentales como Nat Geo o National Geographic
        if (GEO_TAG_REGEX.containsMatchIn(lowerName) && !lowerName.contains("nat geo")) {
            return true
        }

        // Excluir emisiones en idiomas extranjeros no hispanos
        for (token in FOREIGN_TOKENS) {
            if (lowerName.contains(token)) return true
        }
        return false
    }

    fun normalizeChannelName(rawName: String): String {
        return NORMALIZE_REGEX.replace(rawName, "").trim().lowercase()
    }

    const val RSS_URL = "https://qwertyaserty.com/rblog/rss"
    const val RSS_SEPARATOR = "   ★   www.qwertyaserty.com   ★   "
}
