package com.example.chariiptv.player

import android.content.Context
import androidx.media3.exoplayer.DefaultRenderersFactory

/**
 * Decodificador de audio por software (FFmpeg):
 * Permite decodificar formatos de audio como AC3, E-AC3 (Dolby Digital Plus), DTS, TrueHD,
 * AAC-LATM y MP2 directamente por software, asegurando que todos los canales tengan audio
 * incluso en televisores y TV Boxes sin licencia de hardware Dolby.
 */
object SoftAudio {

    fun factory(ctx: Context): DefaultRenderersFactory =
        DefaultRenderersFactory(ctx)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)
}
