package com.example.chariiptv.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanRemoteEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<LanRemoteEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<LanRemoteEvent> = _events.asSharedFlow()

    fun emit(event: LanRemoteEvent) {
        _events.tryEmit(event)
    }
}
