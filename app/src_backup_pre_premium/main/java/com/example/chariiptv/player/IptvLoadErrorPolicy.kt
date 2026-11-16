package com.example.chariiptv.player

import androidx.media3.common.C
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy

/**
 * Política inteligente de reintentos para transmisiones IPTV en directo.
 * En transmisiones HLS, pequeños microcortes de red o rotaciones de segmentos en la CDN
 * no deben matar la reproducción de inmediato. Esta política realiza reintentos con
 * retroceso controlado antes de declarar un error irrecuperable.
 */
class IptvLoadErrorPolicy(
    private val maxRetries: Int = 6
) : DefaultLoadErrorHandlingPolicy(maxRetries) {

    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
        val exception = loadErrorInfo.exception
        // Solo si es un 401 o 403 persistente tras varios reintentos abortamos
        if (exception is HttpDataSource.InvalidResponseCodeException) {
            val code = exception.responseCode
            if ((code == 401 || code == 403) && loadErrorInfo.errorCount >= 4) {
                return C.TIME_UNSET
            }
        }
        // Para cualquier otro error (404 transitorio en segmento HLS, timeout, jitter de red),
        // reintentar rápidamente para no congelar la reproducción
        return super.getRetryDelayMsFor(loadErrorInfo).coerceAtMost(2500L)
    }
}

