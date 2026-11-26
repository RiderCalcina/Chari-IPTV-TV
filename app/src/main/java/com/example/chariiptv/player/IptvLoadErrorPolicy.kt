package com.example.chariiptv.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy

/**
 * Política inteligente de reintentos para transmisiones IPTV en directo.
 * - Ante códigos HTTP definitivos (4xx excepto 408/429) falla rápido tras 1 reintento
 *   para activar inmediatamente el proxy de cabeceras o saltar al siguiente mirror.
 * - Ante microcortes de red o jitter de segmentos HLS, reintenta de forma acotada
 *   sin superar el watchdog del reproductor.
 */
@OptIn(UnstableApi::class)
class IptvLoadErrorPolicy(
    private val maxRetries: Int = 6
) : DefaultLoadErrorHandlingPolicy(maxRetries) {

    companion object {
        fun isFinalStatus(statusCode: Int): Boolean {
            return statusCode in 400..499 && statusCode != 408 && statusCode != 429
        }
    }

    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
        val exception = loadErrorInfo.exception
        if (exception is HttpDataSource.InvalidResponseCodeException) {
            val code = exception.responseCode
            if (isFinalStatus(code) && loadErrorInfo.errorCount >= 3) {
                return C.TIME_UNSET
            }
        }
        return super.getRetryDelayMsFor(loadErrorInfo).coerceAtMost(2000L)
    }
}
