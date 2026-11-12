package com.example.chariiptv.presentation

import com.example.chariiptv.domain.Channel

data class TvUiState(
    val channels: List<Channel> = emptyList(),
    val favorites: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilterId: String = "fav",
    val selectedCountryCode: String = "pe",
    val isCountrySubMenuVisible: Boolean = false,
    val currentChannel: Channel? = null,
    val rssNews: String = "",
    val errorMessage: String? = null
)
