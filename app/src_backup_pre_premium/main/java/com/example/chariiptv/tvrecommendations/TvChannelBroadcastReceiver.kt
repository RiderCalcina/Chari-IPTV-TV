package com.example.chariiptv.tvrecommendations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.chariiptv.domain.ChannelRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TvReceiverEntryPoint {
    fun channelRepository(): ChannelRepository
}

class TvChannelBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "android.media.tv.action.INITIALIZE_PROGRAMS" ||
            intent?.action == Intent.ACTION_BOOT_COMPLETED
        ) {
            val pendingResult = goAsync()
            val appContext = context.applicationContext
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        appContext,
                        TvReceiverEntryPoint::class.java
                    )
                    val channelRepository = entryPoint.channelRepository()
                    val channels = channelRepository.getFavoriteChannels().ifEmpty {
                        channelRepository.getChannelsByCountry("es")
                    }
                    TvRecommendationManager.updateHomeScreenChannels(appContext, channels)
                } catch (_: Exception) {
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
