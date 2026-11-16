package com.example.chariiptv

import com.example.chariiptv.util.Constants
import org.junit.Assert.assertEquals
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
}
