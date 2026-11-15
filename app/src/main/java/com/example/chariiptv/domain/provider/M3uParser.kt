package com.example.chariiptv.domain.provider

import com.example.chariiptv.domain.Channel
import com.example.chariiptv.util.Constants

object M3uParser {

    fun parse(m3uContent: String, defaultCountry: String = ""): List<Channel> {
        val list = ArrayList<Channel>(1500)
        var currentName = ""
        var currentLogo = ""
        var currentCountry = defaultCountry
        var currentCategory = ""
        var hasExtInf = false

        m3uContent.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.startsWith("#EXTINF:-1")) {
                currentLogo = extractAttribute(line, "tvg-logo")
                val countryAttr = extractAttribute(line, "tvg-country").lowercase()
                val tvgId = extractAttribute(line, "tvg-id").lowercase()
                val cleanId = tvgId.substringBefore("@")
                
                currentCountry = if (countryAttr.isNotBlank()) {
                    countryAttr
                } else if (cleanId.contains(".")) {
                    val potential = cleanId.substringAfterLast(".").take(2)
                    if (Constants.SPANISH_COUNTRIES.contains(potential)) {
                        potential
                    } else if (defaultCountry.isNotBlank()) {
                        defaultCountry
                    } else {
                        potential
                    }
                } else {
                    defaultCountry
                }

                currentCategory = extractAttribute(line, "group-title").lowercase()

                if (currentLogo.isBlank() && cleanId.isNotBlank()) {
                    currentLogo = "https://iptv-org.github.io/logos/symbols/$cleanId.png"
                }

                currentName = line.substringAfterLast(",").trim()
                hasExtInf = true
            } else if (hasExtInf && line.startsWith("http")) {
                list.add(
                    Channel(
                        name = currentName,
                        logo = currentLogo,
                        url = line,
                        country = currentCountry,
                        category = currentCategory
                    )
                )
                hasExtInf = false
            }
        }
        return list
    }

    private fun extractAttribute(line: String, attr: String): String {
        val key = "$attr=\""
        val startIdx = line.indexOf(key)
        if (startIdx == -1) return ""
        val valStart = startIdx + key.length
        val endIdx = line.indexOf('"', valStart)
        if (endIdx == -1) return ""
        return line.substring(valStart, endIdx).trim()
    }
}
