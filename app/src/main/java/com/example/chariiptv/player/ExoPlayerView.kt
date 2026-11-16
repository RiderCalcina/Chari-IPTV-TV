package com.example.chariiptv.player

import android.content.Context
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.chariiptv.R
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.presentation.components.QuickChannelDrawer
import com.example.chariiptv.theme.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PlayerEntryPoint {
    fun localHlsProxyServer(): LocalHlsProxyServer
    fun okHttpClient(): OkHttpClient
}

@Composable
fun ExoPlayerView(
    channel: Channel,
    isFavorite: Boolean,
    categoryChannels: List<Channel> = emptyList(),
    categoryTitle: String = "",
    favorites: Set<String> = emptySet(),
    onChannelChange: (Channel) -> Unit = {},
    onToggleFavorite: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val playerDependencies = remember(context) {
        try {
            EntryPointAccessors.fromApplication(context.applicationContext, PlayerEntryPoint::class.java)
        } catch (_: Exception) {
            null
        }
    }
    val okHttpClient = playerDependencies?.okHttpClient()
    val localProxyServer = playerDependencies?.localHlsProxyServer()

    var isBuffering by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var retryCount by remember { mutableIntStateOf(0) }
    var autoRetryCount by remember(channel.url) { mutableIntStateOf(0) }
    var useProxyFallback by remember(channel.url) { mutableStateOf(false) }
    val retryFocusRequester = remember { FocusRequester() }

    // Cajón lateral de canales rápidos (Zapping sin salir del reproductor)
    var isSideDrawerVisible by remember { mutableStateOf(false) }

    // HUD interactivo con auto-ocultado tras 5 segundos
    var isHudVisible by remember { mutableStateOf(true) }
    var hudInteractionTrigger by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    val appVersion = remember(context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName ?: "1.1"}"
        } catch (_: Exception) {
            "v1.1"
        }
    }

    LaunchedEffect(hudInteractionTrigger) {
        isHudVisible = true
        delay(5000)
        isHudVisible = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(15_000)
        }
    }

    // Si el drawer lateral está abierto, la tecla Atrás cierra el drawer en vez de salir de la app
    BackHandler(enabled = isSideDrawerVisible) {
        isSideDrawerVisible = false
    }

    BackHandler(enabled = !isSideDrawerVisible) {
        onClose()
    }

    val activeStreamUrl = remember(channel.url, useProxyFallback, retryCount) {
        if (useProxyFallback && localProxyServer != null) {
            localProxyServer.buildProxyUrl(channel.url)
        } else {
            channel.url
        }
    }

    // 1. Instancia optimizada de ExoPlayer con FFmpeg y política de reintentos
    val exoPlayer = remember(activeStreamUrl, retryCount) {
        PlayerBuilder.build(context, okHttpClient).apply {
            setMediaItem(MediaItem.fromUri(activeStreamUrl))
            prepare()
            playWhenReady = true
        }
    }

    // 2. Liberación garantizada de audio y red al cambiar de canal o desmontar
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> isBuffering = true
                    Player.STATE_READY -> {
                        isBuffering = false
                        isError = false
                        autoRetryCount = 0
                    }
                    Player.STATE_ENDED -> isBuffering = false
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    exoPlayer.seekToDefaultPosition()
                    exoPlayer.prepare()
                    exoPlayer.play()
                    return
                }

                if (autoRetryCount < 2) {
                    autoRetryCount++
                    isBuffering = true
                    try {
                        exoPlayer.seekToDefaultPosition()
                        exoPlayer.prepare()
                        exoPlayer.play()
                    } catch (_: Exception) {}
                    return
                }

                val isGeoblocked = isGeoBlockedError(error)

                if (!useProxyFallback && localProxyServer != null && isGeoblocked) {
                    useProxyFallback = true
                    isBuffering = true
                    isError = false
                    return
                }

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

    // 3. Ciclo de Vida de la Actividad
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!isError) exoPlayer.play()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.stop()
                    exoPlayer.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 4. Watchdog: detección de buffering congelado
    LaunchedEffect(channel.url, retryCount) {
        isBuffering = true
        isError = false
        errorMessage = ""
        delay(20_000)
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
        // Vista a pantalla completa de ExoPlayer
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = exoPlayer
                    useController = false
                }
            },
            update = { view ->
                view.player = exoPlayer
                view.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_UP) {
                        hudInteractionTrigger = System.currentTimeMillis()
                        when (keyCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                if (categoryChannels.isNotEmpty()) {
                                    isSideDrawerVisible = !isSideDrawerVisible
                                    true
                                } else {
                                    false
                                }
                            }
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                onToggleFavorite()
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                isHudVisible = !isHudVisible
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
            }
        )

        // HUD SUPERIOR: Datos reales de la emisión (Logo, Nombre, Directo, Proxy, Reloj)
        AnimatedVisibility(
            visible = isHudVisible && !isError && !isSideDrawerVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xD9090D16))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Lado Izquierdo: Logo y Nombre del Canal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TvSurfaceContainerHigh)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (channel.logo.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(channel.logo)
                                    .crossfade(true)
                                    .error(R.drawable.default_tv)
                                    .fallback(R.drawable.default_tv)
                                    .build(),
                                contentDescription = channel.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_tv),
                                contentDescription = channel.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = channel.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // Badge Directo
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TvLiveRedContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(TvLiveRed)
                                    )
                                    Text(
                                        text = "DIRECTO",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Estado de conexión real (Directa o Proxy Loopback)
                        Text(
                            text = if (useProxyFallback) "Modo: Proxy Anti-Geobloqueo Activo" else "Modo: Conexión Directa HLS",
                            color = if (useProxyFallback) TvSecondary else TvOnSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Lado Derecho: Reloj
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(TvSurfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentTime,
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // HUD INFERIOR: Atajos y Versión de la App en la parte inferior derecha
        AnimatedVisibility(
            visible = isHudVisible && !isError && !isSideDrawerVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xD9090D16))
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Atajos en el reproductor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Atajo Lista de Canales [◀]
                    if (categoryChannels.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TvPrimary)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(text = "◀", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(text = "Lista de Canales", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TvSurfaceContainerHigh)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(text = "ATRÁS", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "Volver a Menú", color = TvOnSurfaceVariant, fontSize = 11.5.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TvSurfaceContainerHigh)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(text = "▲", color = TvSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (isFavorite) "Quitar Favorito" else "Añadir Favorito",
                            color = TvOnSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Versión en la parte inferior derecha
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TvSurfaceContainerHigh.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Chari-IPTV TV", color = TvOnSurfaceVariant, fontSize = 10.sp)
                        Text(text = appVersion, color = TvPrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // CAJÓN LATERAL FLOTANTE DE CANALES (Zapping instantáneo en la misma sección)
        AnimatedVisibility(
            visible = isSideDrawerVisible && !isError,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            QuickChannelDrawer(
                channels = categoryChannels,
                currentChannelUrl = channel.url,
                categoryTitle = categoryTitle,
                favorites = favorites,
                onChannelSelected = { newChannel ->
                    onChannelChange(newChannel)
                },
                onClose = {
                    isSideDrawerVisible = false
                }
            )
        }

        // Overlay: Estado de carga / Buffering
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
                    color = TvPrimaryFocused,
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
            }
        }

        // Overlay: Error amigable y reintento
        AnimatedVisibility(
            visible = isError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF0080C14)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(min = 480.dp, max = 640.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(TvSurface)
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
                            .background(TvLiveRedContainer)
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
                                containerColor = TvSurfaceContainerHigh,
                                focusedContainerColor = TvSurfaceFocused
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

private fun isGeoBlockedError(error: PlaybackException): Boolean {
    var cause: Throwable? = error.cause
    while (cause != null) {
        if (cause is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException) {
            val code = cause.responseCode
            if (code == 403 || code == 451) {
                return true
            }
        }
        cause = cause.cause
    }
    val msg = (error.message ?: "").lowercase()
    val causeMsg = (error.cause?.message ?: "").lowercase()
    return msg.contains("403") || msg.contains("geoblock") || msg.contains("geo-block") ||
            causeMsg.contains("403") || causeMsg.contains("geoblock") || causeMsg.contains("geo-block")
}
