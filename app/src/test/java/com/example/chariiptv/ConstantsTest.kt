package com.example.chariiptv

import com.example.chariiptv.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstantsTest {

    @Test
    fun getFilterDisplayName_returnsCorrectNames() {
        assertEquals("Favoritos", Constants.getFilterDisplayName("fav"))
        assertEquals("Países", Constants.getFilterDisplayName("countries"))
        assertEquals("Deportes", Constants.getFilterDisplayName("sports"))
        assertEquals("Noticias", Constants.getFilterDisplayName("news"))
        assertEquals("Películas", Constants.getFilterDisplayName("movies"))
        assertEquals("Series", Constants.getFilterDisplayName("series"))
        assertEquals("Infantil", Constants.getFilterDisplayName("animation"))
        assertEquals("Música", Constants.getFilterDisplayName("music"))
        assertEquals("Perú", Constants.getFilterDisplayName("pe"))
        assertEquals("España", Constants.getFilterDisplayName("es"))
        assertEquals("México", Constants.getFilterDisplayName("mx"))
        assertEquals("Argentina", Constants.getFilterDisplayName("ar"))
        assertEquals("desconocido", Constants.getFilterDisplayName("desconocido"))
    }

    @Test
    fun supportedCountries_containsTwentySpanishSpeakingCountries() {
        assertEquals(20, Constants.SPANISH_COUNTRIES.size)
        assertTrue(Constants.SPANISH_COUNTRIES.contains("pe"))
        assertTrue(Constants.SPANISH_COUNTRIES.contains("mx"))
        assertTrue(Constants.SPANISH_COUNTRIES.contains("es"))
        assertTrue(Constants.SPANISH_COUNTRIES.contains("ar"))
        assertTrue(Constants.SPANISH_COUNTRIES.contains("cl"))
        assertTrue(Constants.SPANISH_COUNTRIES.contains("co"))
    }

    @Test
    fun countryFlags_mapsAllSpanishCountries() {
        Constants.SPANISH_COUNTRIES.forEach { code ->
            assertTrue("Flag for $code should exist", Constants.COUNTRY_FLAGS.containsKey(code))
        }
    }

    @Test
    fun isForeignOrBlocked_blocksBarinasliveServer() {
        assertTrue(Constants.isForeignOrBlocked("ESPN 2 (1080p)", "https://barinaslive1.boldmss.com/live/espn2.m3u8"))
        assertFalse(Constants.isForeignOrBlocked("ESPN Colombia (1080p)", "https://edge01.othercdn.com/live/espn.m3u8"))
    }

    @Test
    fun isForeignOrBlocked_blocksTdtChannelsGeoTokens() {
        assertTrue(Constants.isForeignOrBlocked("Teledeporte GEO", "https://rtvelivestream.rtve.es/live.m3u8"))
        assertTrue(Constants.isForeignOrBlocked("Esport3 GEO", "https://directes-tv-es.3catdirectes.cat/live.m3u8"))
        assertTrue(Constants.isForeignOrBlocked("Esport3 GEO CAT", "https://directes-tv-cat.3catdirectes.cat/live.m3u8"))
        assertTrue(Constants.isForeignOrBlocked("Clan GEO", "https://rtvelivestream.rtve.es/live.m3u8"))
        assertTrue(Constants.isForeignOrBlocked("Canal Sur (geo-blocked)", "https://example.com/live.m3u8"))
    }

    @Test
    fun isForeignOrBlocked_preservesValidDocumentaryAndStandardChannels() {
        assertFalse(Constants.isForeignOrBlocked("Nat Geo Wild (1080p)", "https://cdn.validstream.tv/live.m3u8"))
        assertFalse(Constants.isForeignOrBlocked("National Geographic", "https://cdn.validstream.tv/live.m3u8"))
        assertFalse(Constants.isForeignOrBlocked("Teledeporte", "https://rtvelivestream.rtve.es/live.m3u8"))
        assertFalse(Constants.isForeignOrBlocked("Esport3", "https://directes-tv-int.3catdirectes.cat/live.m3u8"))
    }

    @Test
    fun isTestOrDummyChannel_blocksTestAndDummyChannels() {
        assertTrue(Constants.isTestOrDummyChannel("Canal Prueba Celular", "https://test.com/stream-cel.m3u8"))
        assertTrue(Constants.isTestOrDummyChannel("Canal de prueba", "https://cdn.validstream.tv/live.m3u8"))
        assertTrue(Constants.isTestOrDummyChannel("Canal Real", "https://example.com/stream.m3u8"))
        assertFalse(Constants.isTestOrDummyChannel("Latina TV (1080p)", "https://live.latina.pe/playlist.m3u8"))
    }
}
