package com.example.chariiptv

import com.example.chariiptv.domain.Channel
import com.example.chariiptv.presentation.TvUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TvUiStateTest {

    @Test
    fun defaultState_isInitializedCorrectly() {
        val state = TvUiState()
        assertTrue(state.channels.isEmpty())
        assertTrue(state.favorites.isEmpty())
        assertFalse(state.isLoading)
        assertEquals("fav", state.selectedFilterId)
        assertEquals("pe", state.selectedCountryCode)
        assertFalse(state.isCountrySubMenuVisible)
        assertNull(state.currentChannel)
        assertEquals("", state.rssNews)
        assertNull(state.errorMessage)
    }

    @Test
    fun stateCopy_updatesCorrectly() {
        val channel = Channel("Canal 1", "https://logo.png", "https://stream.m3u8")
        val state = TvUiState(
            channels = listOf(channel),
            isLoading = true,
            selectedFilterId = "sports",
            selectedCountryCode = "ar"
        )

        assertEquals(1, state.channels.size)
        assertEquals("Canal 1", state.channels[0].name)
        assertTrue(state.isLoading)
        assertEquals("sports", state.selectedFilterId)
        assertEquals("ar", state.selectedCountryCode)
    }
}
