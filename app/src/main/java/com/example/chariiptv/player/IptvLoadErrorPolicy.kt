package com.example.chariiptv.player

import androidx.media3.common.C
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy

/**
 * Política inteligente de reintentos para transmisiones IPTV.
 * En conexiones Wi-Fi o transmisiones en directo, pequeños microcortes de red provocaban
 * que el reproductor se detuviera de inmediato. Esta política realiza hasta 6 reintentos
 * con retroceso progresivo antes de declarar un error irrecuperable.
 */
class IptvLoadErrorPolicy(
    private val maxRetries: Int = 6
) : DefaultLoadErrorHandlingPolicy(maxRetries) {

    companion object {
        const val DEFAULT_MAX_RETRIES = 6

        /** Códigos HTTP definitivos donde no tiene sentido reintentar (errores de cliente excepto timeout y rate limit). */
        fun isFinalStatus(code: Int): Boolean = code in 400..499 && code != 408 && code != 429
    }

    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
        val exception = loadErrorInfo.exception
        if (exception is HttpDataSource.InvalidResponseCodeException && isFinalStatus(exception.responseCode)) {
            return C.TIME_UNSET
        }
        return super.getRetryDelayMsFor(loadErrorInfo)
    }
}
