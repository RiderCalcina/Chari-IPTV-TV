package com.example.chariiptv.player

import android.content.Context
import android.os.Handler
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink

/**
 * Decodificador de audio por software (FFmpeg):
 * Permite decodificar formatos de audio como AC3, E-AC3 (Dolby Digital Plus), DTS o TrueHD
 * incluso cuando el hardware del televisor o los auriculares Bluetooth no disponen de decodificador nativo.
 */
object SoftAudio {
    private var checked = false
    private var isAvailable = false

    fun available(ctx: Context): Boolean {
        if (checked) return isAvailable
        checked = true
        isAvailable = try {
            val clazz = Class.forName("androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer")
            val constructor = clazz.getConstructor(
                Handler::class.java,
                AudioRendererEventListener::class.java,
                AudioSink::class.java
            )
            constructor.newInstance(null, null, DefaultAudioSink.Builder(ctx.applicationContext).build())
            true
        } catch (_: Throwable) {
            false
        }
        return isAvailable
    }

    fun factory(ctx: Context): DefaultRenderersFactory =
        DefaultRenderersFactory(ctx)
            .setExtensionRendererMode(
                if (available(ctx)) DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
                else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
            )
            .setEnableDecoderFallback(true)
}

