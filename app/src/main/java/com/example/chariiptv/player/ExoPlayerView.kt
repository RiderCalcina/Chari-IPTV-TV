package com.example.chariiptv.player

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.theme.TvPrimary
import com.example.chariiptv.theme.TvPrimaryFocused
import kotlinx.coroutines.delay

@Composable
fun ExoPlayerView(
    channel: Channel,
    channelsList: List<Channel> = emptyList(),
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado de Canal Principal
    var isBuffering by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var retryCount by remember { mutableIntStateOf(0) }
    val retryFocusRequester = remember { FocusRequester() }

    // Estado de Dual View (Segundo Canal)
    var secondChannel by remember { mutableStateOf<Channel?>(null) }
    var isSecondAudioActive by remember { mutableStateOf(false) }
    var showDualViewPicker by remember { mutableStateOf(false) }
    var secondBuffering by remember { mutableStateOf(false) }

    // Si Dual View está activo, la tecla Atrás primero cierra el segundo canal
    BackHandler {
        if (showDualViewPicker) {
            showDualViewPicker = false
        } else if (secondChannel != null) {
            secondChannel = null
            isSecondAudioActive = false
        } else {
            onClose()
        }
    }

    // 1. Instancia optimizada de ExoPlayer para Canal Principal
    val exoPlayer = remember(channel.url, retryCount) {
        PlayerBuilder.build(context).apply {
            setMediaItem(MediaItem.fromUri(channel.url))
            prepare()
            playWhenReady = true
        }
    }

    // 2. Instancia optimizada de ExoPlayer para Segundo Canal (Dual View)
    val secondPlayer = remember(secondChannel?.url) {
        secondChannel?.let { sec ->
            PlayerBuilder.build(context).apply {
                setMediaItem(MediaItem.fromUri(sec.url))
                volume = 0f // Inicia en silencio
                prepare()
                playWhenReady = true
            }
        }
    }

    // Control de volumen / foco de audio entre Canal 1 y Canal 2
    LaunchedEffect(isSecondAudioActive, secondPlayer) {
        if (secondPlayer != null) {
            if (isSecondAudioActive) {
                exoPlayer.volume = 0f
                secondPlayer.volume = 1f
            } else {
                exoPlayer.volume = 1f
                secondPlayer.volume = 0f
            }
        } else {
            exoPlayer.volume = 1f
        }
    }

    // Listener para Canal Principal
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> isBuffering = true
                    Player.STATE_READY -> {
                        isBuffering = false
                        isError = false
                    }
                    Player.STATE_ENDED -> isBuffering = false
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                isBuffering = false
                isError = true
                errorMessage = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                        "No se pudo conectar con el servidor emisor de la señal. Verifica tu conexión a internet."
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                        "El servidor del canal rechazó la conexión (posible geobloqueo o transmisión fuera de línea)."
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                    PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                        "El formato de la transmisión no es compatible con el reproductor."
                    else ->
                        "La señal del canal no está disponible en este momento. Puede estar en pausa o fuera de emisión."
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
    }

    // Listener para Segundo Canal (Dual View)
    DisposableEffect(secondPlayer) {
        val secListener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                secondBuffering = (state == Player.STATE_BUFFERING)
            }
        }
        secondPlayer?.addListener(secListener)

        onDispose {
            secondPlayer?.let { p ->
                p.removeListener(secListener)
                p.stop()
                p.clearMediaItems()
                p.release()
            }
        }
    }

    // Ciclo de vida de la Actividad
    DisposableEffect(lifecycleOwner, exoPlayer, secondPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                    secondPlayer?.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!isError) exoPlayer.play()
                    secondPlayer?.play()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.stop()
                    exoPlayer.release()
                    secondPlayer?.stop()
                    secondPlayer?.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Watchdog: detección de buffering infinito
    LaunchedEffect(channel.url, retryCount) {
        isBuffering = true
        isError = false
        errorMessage = ""
        delay(14_000)
        if (isBuffering && !isError) {
            isBuffering = false
            isError = true
            errorMessage = "El servidor emisor no respondió a tiempo. La señal podría estar temporalmente saturada o fuera de emisión."
        }
    }

    LaunchedEffect(isError) {
        if (isError) {
            delay(100)
            try {
                retryFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ==========================================
        // VISTA PRINCIPAL: SIMPLE O PANTALLA DIVIDIDA
        // ==========================================
        if (secondChannel == null) {
            // MODO PANTALLA COMPLETA
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = exoPlayer
                        useController = true
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                    view.setOnKeyListener { _, keyCode, event ->
                        if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && event.action == android.view.KeyEvent.ACTION_UP) {
                            onToggleFavorite()
                            true
                        } else {
                            false
                        }
                    }
                }
            )
        } else {
            // MODO DUAL VIEW (PANTALLA DIVIDIDA 50% - 50%)
            Row(modifier = Modifier.fillMaxSize()) {
                // Panel Izquierdo: Canal Principal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(
                            width = if (!isSecondAudioActive) 3.dp else 1.dp,
                            color = if (!isSecondAudioActive) Color(0xFF3B82F6) else Color(0x33FFFFFF)
                        )
                        .clickable { isSecondAudioActive = false }
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                this.player = exoPlayer
                                useController = false
                            }
                        },
                        update = { it.player = exoPlayer }
                    )
                    // Etiqueta Canal 1 con indicador de audio
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0B1329))
                            .border(BorderStroke(1.dp, if (!isSecondAudioActive) Color(0xFF60A5FA) else Color(0x33FFFFFF)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (!isSecondAudioActive) "🔊 ${channel.name}" else "🔇 ${channel.name}",
                            color = if (!isSecondAudioActive) Color.White else Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Panel Derecho: Segundo Canal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(
                            width = if (isSecondAudioActive) 3.dp else 1.dp,
                            color = if (isSecondAudioActive) Color(0xFF3B82F6) else Color(0x33FFFFFF)
                        )
                        .clickable { isSecondAudioActive = true }
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                this.player = secondPlayer
                                useController = false
                            }
                        },
                        update = { it.player = secondPlayer }
                    )
                    // Etiqueta Canal 2 con indicador de audio
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0B1329))
                            .border(BorderStroke(1.dp, if (isSecondAudioActive) Color(0xFF60A5FA) else Color(0x33FFFFFF)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSecondAudioActive) "🔊 ${secondChannel!!.name}" else "🔇 ${secondChannel!!.name}",
                            color = if (isSecondAudioActive) Color.White else Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // ==========================================
        // BARRA SUPERIOR DE ACCIONES RÁPIDAS
        // ==========================================
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Dual View (Pantalla Dividida)
            if (channelsList.isNotEmpty()) {
                Surface(
                    onClick = {
                        if (secondChannel == null) {
                            showDualViewPicker = !showDualViewPicker
                        } else {
                            // Salir de Dual View
                            secondChannel = null
                            isSecondAudioActive = false
                        }
                    },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (secondChannel != null) Color(0xFF2563EB) else Color(0x990F172A),
                        focusedContainerColor = TvPrimaryFocused
                    ),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(BorderStroke(1.dp, Color(0x44FFFFFF))),
                        focusedBorder = Border(BorderStroke(2.dp, Color.White))
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (secondChannel == null) "🔀 Pantalla Dividida (Dual View)" else "✕ Cerrar 2do Canal",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Alternar Foco de Audio en Dual View
            if (secondChannel != null) {
                Surface(
                    onClick = { isSecondAudioActive = !isSecondAudioActive },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color(0x990F172A),
                        focusedContainerColor = TvPrimaryFocused
                    ),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(BorderStroke(1.dp, Color(0x44FFFFFF))),
                        focusedBorder = Border(BorderStroke(2.dp, Color.White))
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (!isSecondAudioActive) "🎧 Audio: Canal Izquierdo (1)" else "🎧 Audio: Canal Derecho (2)",
                            color = Color(0xFF67E8F9),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ==========================================
        // SELECTOR DE CANALES PARA DUAL VIEW (DRAWER)
        // ==========================================
        AnimatedVisibility(
            visible = showDualViewPicker,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xEE090D16))
                    .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔀 Elige un 2do canal para ver en Pantalla Dividida:",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pulsa Atrás para cancelar",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val candidateChannels = channelsList.filter { it.url != channel.url }
                    items(candidateChannels) { ch ->
                        Surface(
                            onClick = {
                                secondChannel = ch
                                isSecondAudioActive = false
                                showDualViewPicker = false
                            },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.10f),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color(0xFF1E293B),
                                focusedContainerColor = TvPrimaryFocused
                            ),
                            border = ClickableSurfaceDefaults.border(
                                border = Border(BorderStroke(1.dp, Color(0x33FFFFFF))),
                                focusedBorder = Border(BorderStroke(2.dp, Color.White))
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                androidx.compose.material3.Text("📺", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ch.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // OVERLAY: ESTADO DE CARGA / BUFFERING
        // ==========================================
        AnimatedVisibility(
            visible = isBuffering && !isError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC090D16))
                    .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(16.dp))
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Conectando con ${channel.name}...",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cargando señal con decodificador de audio universal",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }

        // ==========================================
        // OVERLAY: ERROR AMIGABLE Y REINTENTO
        // ==========================================
        AnimatedVisibility(
            visible = isError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE6080C14)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(min = 480.dp, max = 640.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF111827))
                        .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(20.dp))
                        .padding(horizontal = 36.dp, vertical = 28.dp)
                ) {
                    if (channel.logo.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(channel.logo)
                                .crossfade(true)
                                .build(),
                            contentDescription = channel.name,
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Fit
                        ) {
                            val state = painter.state
                            if (state is AsyncImagePainter.State.Success) {
                                SubcomposeAsyncImageContent()
                            } else {
                                androidx.compose.material3.Text("📡", fontSize = 48.sp)
                            }
                        }
                    } else {
                        androidx.compose.material3.Text("📡", fontSize = 48.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33EF4444))
                            .border(BorderStroke(1.dp, Color(0x66EF4444)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚠️ Señal Temporalmente No Disponible",
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = errorMessage.ifEmpty {
                            "Este canal se encuentra fuera de transmisión en este momento o tiene restricciones geográficas en tu zona."
                        },
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = {
                                isError = false
                                isBuffering = true
                                retryCount++
                            },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                            border = ClickableSurfaceDefaults.border(
                                border = Border(BorderStroke(1.dp, Color(0x33FFFFFF))),
                                focusedBorder = Border(BorderStroke(2.dp, Color.White))
                            ),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = TvPrimary,
                                focusedContainerColor = TvPrimaryFocused
                            ),
                            modifier = Modifier.focusRequester(retryFocusRequester)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                androidx.compose.material3.Text("🔄", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reintentar",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            onClick = onClose,
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                            border = ClickableSurfaceDefaults.border(
                                border = Border(BorderStroke(1.dp, Color(0x33FFFFFF))),
                                focusedBorder = Border(BorderStroke(2.dp, Color.White))
                            ),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color(0xFF1E293B),
                                focusedContainerColor = Color(0xFF334155)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                androidx.compose.material3.Text("⬅️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Volver a Canales",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
