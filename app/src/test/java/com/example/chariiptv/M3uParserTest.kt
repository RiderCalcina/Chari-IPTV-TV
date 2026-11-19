package com.example.chariiptv

import com.example.chariiptv.domain.Channel
import com.example.chariiptv.domain.StreamSource
import com.example.chariiptv.domain.provider.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Test

class M3uParserTest {

    @Test
    fun parse_extractsExtVlcOptAndInlineHeaders() {
        val sample = """
            #EXTM3U
            #EXTINF:-1 tvg-id="SampleSports.pe@SD" tvg-logo="https://example.com/logo.png" http-referrer="https://inline.example/" group-title="Sports",Sample Sports (1080p)
            #EXTVLCOPT:http-referrer=https://vlc.example/
            #EXTVLCOPT:http-user-agent=CustomAgent/1.0
            https://cdn.example.com/live/index.m3u8
            #EXTINF:-1 tvg-id="PipeChannel.es@SD" group-title="General",Pipe Channel
            https://cdn2.example.com/stream.m3u8|User-Agent=PipeUA/2.0&Referer=https://pipe.example/
        """.trimIndent()

        val channels = M3uParser.parse(sample)
        assertEquals(2, channels.size)

        val first = channels[0]
        assertEquals("Sample Sports (1080p)", first.name)
        assertEquals("https://cdn.example.com/live/index.m3u8", first.url)
        assertEquals("https://vlc.example/", first.referer)
        assertEquals("CustomAgent/1.0", first.userAgent)

        val second = channels[1]
        assertEquals("https://cdn2.example.com/stream.m3u8", second.url)
        assertEquals("PipeUA/2.0", second.userAgent)
        assertEquals("https://pipe.example/", second.referer)
    }

    @Test
    fun allSources_deduplicatesAndPreservesOrder() {
        val ch = Channel(
            name = "Sports HD",
            logo = "",
            url = "https://primary.example/live.m3u8",
            userAgent = "UA1",
            alternateSources = listOf(
                StreamSource("https://primary.example/live.m3u8"),
                StreamSource("https://backup1.example/live.m3u8", referer = "https://ref.example/"),
                StreamSource("https://backup2.example/live.m3u8")
            )
        )

        val all = ch.allSources()
        assertEquals(3, all.size)
        assertEquals("https://primary.example/live.m3u8", all[0].url)
        assertEquals("UA1", all[0].userAgent)
        assertEquals("https://backup1.example/live.m3u8", all[1].url)
        assertEquals("https://ref.example/", all[1].referer)
        assertEquals("https://backup2.example/live.m3u8", all[2].url)
    }
}
