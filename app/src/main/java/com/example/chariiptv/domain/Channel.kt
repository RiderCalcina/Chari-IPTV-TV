package com.example.chariiptv.domain

import androidx.compose.runtime.Immutable

@Immutable
data class StreamSource(
    val url: String,
    val userAgent: String = "",
    val referer: String = ""
)

@Immutable
data class Channel(
    val name: String,
    val logo: String,
    val url: String,
    val country: String = "",
    val category: String = "",
    val userAgent: String = "",
    val referer: String = "",
    val alternateSources: List<StreamSource> = emptyList()
) {
    fun allSources(): List<StreamSource> {
        val primary = StreamSource(url = url, userAgent = userAgent, referer = referer)
        if (alternateSources.isEmpty()) return listOf(primary)
        val result = ArrayList<StreamSource>(alternateSources.size + 1)
        val seen = HashSet<String>(alternateSources.size + 1)
        val primaryKey = url.trim().lowercase()
        if (primaryKey.isNotBlank()) {
            result.add(primary)
            seen.add(primaryKey)
        }
        for (source in alternateSources) {
            val key = source.url.trim().lowercase()
            if (key.isNotBlank() && seen.add(key)) {
                result.add(source)
            }
        }
        return if (result.isNotEmpty()) result else listOf(primary)
    }
}
