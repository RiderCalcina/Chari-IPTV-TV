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
        var currentUserAgent = ""
        var currentReferer = ""
        var hasExtInf = false

        m3uContent.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
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
                currentUserAgent = extractAttribute(line, "http-user-agent")
                currentReferer = extractAttribute(line, "http-referrer")
                    .ifBlank { extractAttribute(line, "http-referer") }

                if (currentLogo.isBlank() && cleanId.isNotBlank()) {
                    currentLogo = "https://iptv-org.github.io/logos/symbols/$cleanId.png"
                }

                currentName = line.substringAfterLast(",").trim()
                hasExtInf = true
            } else if (hasExtInf && line.startsWith("#EXTVLCOPT:", ignoreCase = true)) {
                val opt = line.substringAfter(":").trim()
                val eqIndex = opt.indexOf('=')
                if (eqIndex > 0) {
                    val key = opt.substring(0, eqIndex).trim().lowercase()
                    val value = opt.substring(eqIndex + 1).trim().trim('"')
                    when (key) {
                        "http-user-agent" -> if (value.isNotBlank()) currentUserAgent = value
                        "http-referrer", "http-referer" -> if (value.isNotBlank()) currentReferer = value
                    }
                }
            } else if (hasExtInf && (line.startsWith("http://", ignoreCase = true) || line.startsWith("https://", ignoreCase = true))) {
                val (cleanUrl, pipeUa, pipeRef) = splitPipeHeaders(line)
                val finalUa = pipeUa.ifBlank { currentUserAgent }
                val finalRef = pipeRef.ifBlank { currentReferer }
                if (cleanUrl.isNotBlank() && !Constants.isTestOrDummyChannel(currentName, cleanUrl)) {
                    list.add(
                        Channel(
                            name = currentName,
                            logo = currentLogo,
                            url = cleanUrl,
                            country = currentCountry,
                            category = currentCategory,
                            userAgent = finalUa,
                            referer = finalRef
                        )
                    )
                }
                hasExtInf = false
                currentUserAgent = ""
                currentReferer = ""
            }
        }
        return list
    }

    private fun splitPipeHeaders(rawUrl: String): Triple<String, String, String> {
        val pipeIdx = rawUrl.indexOf('|')
        if (pipeIdx == -1) return Triple(rawUrl, "", "")
        val cleanUrl = rawUrl.substring(0, pipeIdx).trim()
        val params = rawUrl.substring(pipeIdx + 1)
        var ua = ""
        var ref = ""
        params.split('&').forEach { pair ->
            val eqIdx = pair.indexOf('=')
            if (eqIdx > 0) {
                val k = pair.substring(0, eqIdx).trim().lowercase()
                val v = pair.substring(eqIdx + 1).trim()
                when (k) {
                    "user-agent", "http-user-agent" -> if (v.isNotBlank()) ua = v
                    "referer", "referrer", "http-referrer", "http-referer" -> if (v.isNotBlank()) ref = v
                }
            }
        }
        return Triple(cleanUrl, ua, ref)
    }

    private fun extractAttribute(line: String, attr: String): String {
        val key = "$attr=\""
        val startIdx = line.indexOf(key, ignoreCase = true)
        if (startIdx == -1) return ""
        val valStart = startIdx + key.length
        val endIdx = line.indexOf('"', valStart)
        if (endIdx == -1) return ""
        return line.substring(valStart, endIdx).trim()
    }
}
