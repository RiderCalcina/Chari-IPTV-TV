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

    fun isPanRegionalThematicChannel(name: String, url: String = ""): Boolean {
        val lowerName = name.lowercase()
        val lowerUrl = url.lowercase()

        // Canales con nombres explícitos de transmisión panregional o satelital
        if (lowerName.contains("latin america") || lowerName.contains("latinamerica") ||
            lowerName.contains("panregional") || lowerName.contains("central america") ||
            lowerName.contains("sudamerica") || lowerName.contains("suramerica")
        ) {
            return true
        }

        // Marcas panregionales de cine, series, infantiles o deportes de pago
        val thematicBrands = listOf(
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

        return thematicBrands.any { brand ->
            lowerName.startsWith("$brand ") || lowerName.contains(" $brand ") || lowerName.endsWith(" $brand") || lowerName == brand ||
                    (brand.length > 4 && lowerName.contains(brand))
        }
    }

    fun isForeignOrBlocked(name: String, url: String = ""): Boolean {
        val lowerName = name.lowercase()
        val lowerUrl = url.lowercase()

        // Excluir canales explícitamente geobloqueados o redes de intranet privadas caídas
        if (lowerName.contains("[geo-blocked]") || lowerName.contains("(geo-blocked)") ||
            lowerName.contains("[geoblocked]") || lowerName.contains("(geoblocked)") ||
            lowerUrl.contains("bantel-cdn1.iptvperu.tv")
        ) {
            return true
        }

        // Excluir emisiones en idiomas extranjeros no hispanos
        return lowerName.contains("(sweden)") || lowerName.contains("(suecia)") ||
                lowerName.contains("(india)") ||
                lowerName.contains("(pakistan)") || lowerName.contains("(pakistán)") ||
                lowerName.contains("(turkiye)") || lowerName.contains("(turkey)") || lowerName.contains("(turquía)") ||
                lowerName.contains("(arabic)") || lowerName.contains("(árabe)") ||
                lowerName.contains("(germany)") || lowerName.contains("(alemania)") || lowerName.contains("alemania") ||
                lowerName.contains("(italy)") || lowerName.contains("(italia)") ||
                lowerName.contains("(united kingdom)") || lowerName.contains("(reino unido)") || lowerName.contains("(uk)") ||
                lowerName.contains("(france)") || lowerName.contains("(francia)") ||
                lowerName.contains("(russia)") || lowerName.contains("(rusia)") ||
                lowerName.contains("(japan)") || lowerName.contains("(japón)") ||
                lowerName.contains("(korea)") || lowerName.contains("(corea)") ||
                lowerName.contains("(china)") ||
                lowerName.contains("(poland)") || lowerName.contains("(polonia)") ||
                lowerName.contains("(greece)") || lowerName.contains("(grecia)") ||
                lowerName.contains("(romania)") || lowerName.contains("(rumania)") ||
                lowerName.contains("(netherlands)") || lowerName.contains("(holanda)") ||
                lowerName.contains("(denmark)") || lowerName.contains("(dinamarca)") ||
                lowerName.contains("(norway)") || lowerName.contains("(noruega)") ||
                lowerName.contains("(finland)") || lowerName.contains("(finlandia)") ||
                lowerName.contains("chequia") || lowerName.contains("czech") ||
                lowerName.contains("das erste") || lowerName.contains("zdf") || lowerName.contains("kika") ||
                lowerName.contains("failarmy") || lowerName.contains("fifa+") ||
                lowerName.contains("ocko") || lowerName.contains("óčko") ||
                lowerName.contains("english") || lowerName.contains("[eng]") || lowerName.contains("(eng)") ||
                lowerName.contains("(vo)") || lowerName.contains("(vose)")
    }

    fun normalizeChannelName(rawName: String): String {
        return rawName
            .replace("\\(\\d+p\\)".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\\[not 24/7\\]".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\\[geo-blocked\\]".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\\(geo-blocked\\)".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\\[geoblocked\\]".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\\(sd\\)".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\\(hd\\)".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\\(fhd\\)".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()
            .lowercase()
    }

    const val RSS_URL = "https://qwertyaserty.com/rblog/rss"
    const val RSS_SEPARATOR = "   ★   www.qwertyaserty.com   ★   "
}
