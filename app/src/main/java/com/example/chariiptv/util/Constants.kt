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

    const val RSS_URL = "https://qwertyaserty.com/rblog/rss"
    const val RSS_SEPARATOR = "   ★   www.qwertyaserty.com   ★   "
}
