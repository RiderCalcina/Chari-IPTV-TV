package com.example.chariiptv.remote

import com.example.chariiptv.domain.Channel

sealed class LanRemoteEvent {
    data class PlayChannel(val channel: Channel) : LanRemoteEvent()
    data class SendKeyEvent(val keyCode: Int) : LanRemoteEvent()
    data class SetSearchQuery(val query: String) : LanRemoteEvent()
    data class SyncFavorites(val favoriteUrls: List<String>) : LanRemoteEvent()
    data object StopPlayback : LanRemoteEvent()
}
