package com.example.chariiptv.tvrecommendations

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.tvprovider.media.tv.PreviewChannel
import androidx.tvprovider.media.tv.PreviewChannelHelper
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import androidx.tvprovider.media.tv.WatchNextProgram
import com.example.chariiptv.MainActivity
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TvRecommendationManager {
    private const val TAG = "TvRecommendationMgr"
    private const val CHANNEL_KEY = "chari_iptv_destacados"

    @Volatile
    private var lastPublishedSignature: String? = null

    /**
     * Sincroniza la fila de recomendaciones en la pantalla de inicio de Android TV.
     * Evita ráfagas IPC redundantes si la selección superior no ha cambiado.
     */
    suspend fun updateHomeScreenChannels(context: Context, channels: List<Channel>) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@withContext
        val topChannels = channels.filter { !Constants.isTestOrDummyChannel(it.name, it.url) }.take(15)
        val signature = topChannels.joinToString("|") { it.url }
        if (signature.isEmpty() || signature == lastPublishedSignature) return@withContext

        try {
            val helper = PreviewChannelHelper(context)
            val allChannels = helper.allChannels
            var myChannel = allChannels.firstOrNull { it.internalProviderId == CHANNEL_KEY }

            if (myChannel == null) {
                val newChannel = PreviewChannel.Builder()
                    .setDisplayName("Chari-IPTV: Canales Destacados")
                    .setDescription("Tus canales de televisión favoritos y en directo")
                    .setAppLinkIntentUri(Uri.parse("chariiptv://home"))
                    .setInternalProviderId(CHANNEL_KEY)
                    .build()
                val channelId = helper.publishChannel(newChannel)
                TvContractCompat.requestChannelBrowsable(context, channelId)
                myChannel = helper.allChannels.firstOrNull { it.id == channelId }
            }

            val channelId = myChannel?.id ?: return@withContext

            // Limpiar programas obsoletos anteriores del canal
            try {
                context.contentResolver.delete(
                    TvContractCompat.buildPreviewProgramsUriForChannel(channelId),
                    null,
                    null
                )
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron borrar programas previos", e)
            }

            // Publicar hasta 15 canales destacados
            for (ch in topChannels) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("chariiptv://play?url=${Uri.encode(ch.url)}&name=${Uri.encode(ch.name)}")
                    putExtra("channel_url", ch.url)
                    putExtra("channel_name", ch.name)
                    putExtra("channel_logo", ch.logo)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

                val programBuilder = PreviewProgram.Builder()
                    .setChannelId(channelId)
                    .setType(TvContractCompat.PreviewPrograms.TYPE_CHANNEL)
                    .setTitle(ch.name)
                    .setDescription("Ver señal en vivo en Chari-IPTV")
                    .setIntent(intent)
                    .setInternalProviderId(ch.url)
                    .setLive(true)

                if (ch.logo.isNotBlank()) {
                    programBuilder.setPosterArtUri(Uri.parse(ch.logo))
                }

                try {
                    helper.publishPreviewProgram(programBuilder.build())
                } catch (e: Exception) {
                    Log.w(TAG, "Error publicando canal en Home: ${ch.name}", e)
                }
            }
            lastPublishedSignature = signature
            Log.i(TAG, "Fila de Android TV actualizada con ${topChannels.size} canales")
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando canales de Android TV", e)
        }
    }

    /**
     * Registra o actualiza (sin duplicar) el canal recién reproducido en la fila "Continuar viendo" (Watch Next)
     */
    suspend fun recordWatchedChannel(context: Context, channel: Channel) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@withContext
        try {
            val helper = PreviewChannelHelper(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("chariiptv://play?url=${Uri.encode(channel.url)}&name=${Uri.encode(channel.name)}")
                putExtra("channel_url", channel.url)
                putExtra("channel_name", channel.name)
                putExtra("channel_logo", channel.logo)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val programBuilder = WatchNextProgram.Builder()
                .setType(TvContractCompat.WatchNextPrograms.TYPE_CHANNEL)
                .setWatchNextType(TvContractCompat.WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE)
                .setTitle(channel.name)
                .setDescription("Visto recientemente en Chari-IPTV")
                .setIntent(intent)
                .setInternalProviderId(channel.url)
                .setLastEngagementTimeUtcMillis(System.currentTimeMillis())

            if (channel.logo.isNotBlank()) {
                programBuilder.setPosterArtUri(Uri.parse(channel.logo))
            }

            val program = programBuilder.build()
            val existingId = findWatchNextProgramId(context, channel.url)
            if (existingId != null) {
                helper.updateWatchNextProgram(program, existingId)
                Log.i(TAG, "Canal actualizado en Watch Next (sin duplicar): ${channel.name}")
            } else {
                helper.publishWatchNextProgram(program)
                Log.i(TAG, "Canal agregado a Watch Next en Android TV: ${channel.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error agregando a Watch Next", e)
        }
    }

    private fun findWatchNextProgramId(context: Context, internalProviderId: String): Long? {
        return try {
            context.contentResolver.query(
                TvContractCompat.WatchNextPrograms.CONTENT_URI,
                arrayOf(
                    TvContractCompat.WatchNextPrograms._ID,
                    TvContractCompat.WatchNextPrograms.COLUMN_INTERNAL_PROVIDER_ID
                ),
                null,
                null,
                null
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(TvContractCompat.WatchNextPrograms._ID)
                val providerIdx = cursor.getColumnIndex(TvContractCompat.WatchNextPrograms.COLUMN_INTERNAL_PROVIDER_ID)
                while (cursor.moveToNext()) {
                    if (providerIdx >= 0 && cursor.getString(providerIdx) == internalProviderId) {
                        return if (idIdx >= 0) cursor.getLong(idIdx) else null
                    }
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}

