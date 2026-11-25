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
import com.example.chariiptv.MainActivity
import com.example.chariiptv.R
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.domain.GhibliData
import com.example.chariiptv.domain.GhibliPlaybackStore
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
    @com.example.chariiptv.di.MediaClient fun okHttpClient(): OkHttpClient
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
    val activity = context as? MainActivity
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

    val sources = remember(channel) { channel.allSources() }
    var currentSourceIndex by remember(channel.url) { mutableIntStateOf(0) }
    val activeSource = sources.getOrElse(currentSourceIndex) { sources.first() }

    // Identificación exclusiva para el apartado Studio Ghibli (25 películas)
    val isGhibli = remember(channel.category, categoryTitle, channel.name) {
        channel.category.equals("ghibli", ignoreCase = true) ||
        categoryTitle.contains("ghibli", ignoreCase = true) ||
        GhibliData.getChronologicalIndex(channel.name) in 1..25
    }

    val savedPlaybackInfo = remember(channel.name, isGhibli) {
        if (isGhibli) {
            GhibliPlaybackStore.getSavedPosition(context, channel.name)
        } else null
    }

    var showResumeDialog by remember(channel.name, isGhibli) {
        mutableStateOf(savedPlaybackInfo != null && savedPlaybackInfo.first >= GhibliPlaybackStore.MIN_RESUME_THRESHOLD_MS)
    }

    val resumePositionMs = remember(savedPlaybackInfo) {
        savedPlaybackInfo?.first ?: 0L
    }

    val resumeDurationMs = remember(savedPlaybackInfo) {
        savedPlaybackInfo?.second ?: 0L
    }

    val resumeFocusRequester = remember { FocusRequester() }

    var isBuffering by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var retryCount by remember { mutableIntStateOf(0) }
    var autoRetryCount by remember(channel.url, currentSourceIndex) { mutableIntStateOf(0) }
    var useProxyFallback by remember(channel.url, currentSourceIndex) { mutableStateOf(false) }
    var useAutoReferer by remember(channel.url, currentSourceIndex) { mutableStateOf(false) }
    val retryFocusRequester = remember { FocusRequester() }
    val latestSources by rememberUpdatedState(sources)
    val latestProxyServer by rememberUpdatedState(localProxyServer)

    // Cajón lateral de canales rápidos (Zapping sin salir del reproductor)
    var isSideDrawerVisible by remember { mutableStateOf(false) }
    var drawerInteractionTrigger by remember { mutableLongStateOf(0L) }
    var navigatedFromDrawer by remember { mutableStateOf(false) }

    // HUD interactivo con auto-ocultado tras 5 segundos
    var isHudVisible by remember { mutableStateOf(true) }
    var hudInteractionTrigger by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val appVersion = remember(context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName ?: "1.2"}"
        } catch (_: Exception) {
            "v1.2"
        }
    }

    val channelsList = remember(categoryChannels, channel) {
        if (categoryChannels.isNotEmpty()) categoryChannels.distinctBy { it.url } else listOf(channel)
    }

    val isStaticVod = remember(channel.url, channel.category) {
        channel.url.endsWith(".mp4", ignoreCase = true) ||
        channel.url.contains(".mp4?", ignoreCase = true) ||
        channel.url.endsWith(".mkv", ignoreCase = true) ||
        channel.url.contains(".mkv?", ignoreCase = true) ||
        channel.category.equals("ani", ignoreCase = true) ||
        channel.category.equals("ghibli", ignoreCase = true)
    }
    var isLiveStream by remember { mutableStateOf(!isStaticVod) }
    val isVod = isStaticVod || !isLiveStream

    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(true) }

    val formatMs: (Long) -> String = remember {
        { ms ->
            if (ms <= 0L) "00:00"
            else {
                val totalSec = ms / 1000
                val s = totalSec % 60
                val m = (totalSec / 60) % 60
                val h = totalSec / 3600
                if (h > 0) String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
                else String.format(Locale.getDefault(), "%02d:%02d", m, s)
            }
        }
    }

    // Mantener pantalla encendida exclusivamente durante la reproducción (evita burn-in OLED en menús)
    DisposableEffect(activity) {
        val window = activity?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val activeStreamUrl = remember(activeSource, useProxyFallback, useAutoReferer, retryCount) {
        if (useProxyFallback && localProxyServer != null) {
            localProxyServer.buildProxyUrl(
                streamUrl = activeSource.url,
                referer = activeSource.referer,
                userAgent = activeSource.userAgent,
                autoReferer = useAutoReferer && activeSource.referer.isBlank()
            )
        } else {
            activeSource.url
        }
    }

    // 1. Instancia limpia y aislada de ExoPlayer por cada cambio de URL o reintento
    val exoPlayer = remember(activeStreamUrl, retryCount) {
        PlayerBuilder.build(context, okHttpClient).apply {
            setMediaItem(MediaItem.fromUri(activeStreamUrl))
            prepare()
            playWhenReady = !showResumeDialog
        }
    }

    // Interceptor directo a nivel de actividad para eventos del control remoto
    // Garantiza navegación fluida tanto en TV en directo como en Cine On-Demand (VOD)
    DisposableEffect(activity, isSideDrawerVisible, isError, showResumeDialog, channelsList, channel.url, isVod, durationMs, exoPlayer, navigatedFromDrawer) {
        activity?.playerKeyHandler = { event ->
            val keyCode = event.keyCode
            val isActionDown = event.action == KeyEvent.ACTION_DOWN

            if (showResumeDialog) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    try {
                        exoPlayer.stop()
                        exoPlayer.clearMediaItems()
                    } catch (_: Exception) {}
                    onClose()
                    true
                } else {
                    false
                }
            } else if (isSideDrawerVisible) {
                // El cajón lateral de canales está abierto
                // Cualquier interacción con el control remoto reinicia la cuenta regresiva de 4 segundos
                if (isActionDown) {
                    drawerInteractionTrigger = System.currentTimeMillis()
                }

                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (isActionDown && event.repeatCount == 0) {
                            isSideDrawerVisible = false
                        }
                        true
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        if (event.action == KeyEvent.ACTION_UP) {
                            isSideDrawerVisible = false
                        }
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        true // Consumir para no rebotar
                    }
                    // Permitir que D-Pad Arriba, Abajo, y OK/Enter fluyan al LazyColumn del cajón para navegar
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                        false
                    }
                    else -> false
                }
            } else if (isError) {
                // Cuando hay error en pantalla: si el usuario pulsa D-Pad Izquierda (◀), reabrir el menú lateral de canales
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (isActionDown && event.repeatCount == 0 && channelsList.isNotEmpty()) {
                            isSideDrawerVisible = true
                            drawerInteractionTrigger = System.currentTimeMillis()
                            true
                        } else {
                            false
                        }
                    }
                    // Permitir que D-Pad Arriba, Abajo, Derecha y OK/Enter fluyan a los botones "Reintentar" y "Volver a Canales"
                    else -> false
                }
            } else if (isVod) {
                when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER,
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            if (isActionDown && event.repeatCount == 0) {
                                if (exoPlayer.isPlaying) {
                                    exoPlayer.pause()
                                    isPlaying = false
                                } else {
                                    exoPlayer.play()
                                    isPlaying = true
                                }
                                isHudVisible = true
                                hudInteractionTrigger = System.currentTimeMillis()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            if (isActionDown && event.repeatCount == 0) {
                                exoPlayer.play()
                                isPlaying = true
                                isHudVisible = true
                                hudInteractionTrigger = System.currentTimeMillis()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            if (isActionDown && event.repeatCount == 0) {
                                exoPlayer.pause()
                                isPlaying = false
                                isHudVisible = true
                                hudInteractionTrigger = System.currentTimeMillis()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                            if (isActionDown) {
                                val maxDur = if (durationMs > 0L) durationMs else Long.MAX_VALUE
                                val target = (exoPlayer.currentPosition + 15_000L).coerceAtMost(maxDur)
                                exoPlayer.seekTo(target)
                                currentPositionMs = target
                                isHudVisible = true
                                hudInteractionTrigger = System.currentTimeMillis()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_MEDIA_REWIND -> {
                            if (isActionDown) {
                                val target = (exoPlayer.currentPosition - 15_000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(target)
                                currentPositionMs = target
                                isHudVisible = true
                                hudInteractionTrigger = System.currentTimeMillis()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (isActionDown && event.repeatCount == 0) {
                                if (channelsList.isNotEmpty()) {
                                    isSideDrawerVisible = true
                                    isHudVisible = false
                                    drawerInteractionTrigger = System.currentTimeMillis()
                                }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (isActionDown && event.repeatCount == 0) {
                                onToggleFavorite()
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    // Modo En Vivo (Live TV)
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (isActionDown && event.repeatCount == 0) {
                                if (channelsList.isNotEmpty()) {
                                    isSideDrawerVisible = true
                                    isHudVisible = false
                                    drawerInteractionTrigger = System.currentTimeMillis()
                                }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (isActionDown && event.repeatCount == 0) {
                                isHudVisible = false
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (isActionDown && event.repeatCount == 0) {
                                onToggleFavorite()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            if (isActionDown && event.repeatCount == 0) {
                                isHudVisible = !isHudVisible
                                hudInteractionTrigger = System.currentTimeMillis()
                            }
                            true
                        }
                        else -> false
                    }
                }
            }

        onDispose {
            activity?.playerKeyHandler = null
        }
    }

    LaunchedEffect(hudInteractionTrigger) {
        isHudVisible = true
        delay(5000)
        isHudVisible = false
    }

    // Auto-ocultado automático del cajón lateral de canales tras 4 segundos de inactividad
    LaunchedEffect(isSideDrawerVisible, drawerInteractionTrigger) {
        if (isSideDrawerVisible) {
            delay(4000)
            isSideDrawerVisible = false
        }
    }

    LaunchedEffect(activeStreamUrl, retryCount) {
        isBuffering = true
        isError = false
        errorMessage = ""
    }

    LaunchedEffect(showResumeDialog) {
        if (showResumeDialog) {
            delay(150)
            try {
                resumeFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // Respaldo de navegación con la tecla Atrás
    BackHandler(enabled = showResumeDialog) {
        try {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        } catch (_: Exception) {}
        onClose()
    }

    BackHandler(enabled = isSideDrawerVisible && !showResumeDialog) {
        isSideDrawerVisible = false
    }

    BackHandler(enabled = !isSideDrawerVisible && !showResumeDialog) {
        try {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        } catch (_: Exception) {}
        onClose()
    }

    // 2. Liberación garantizada de audio y red al desmontar el reproductor
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                    isPlaying = player.isPlaying
                }
                if (events.contains(Player.EVENT_TIMELINE_CHANGED) || events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                    isLiveStream = player.isCurrentMediaItemLive
                    val dur = player.duration
                    if (dur > 0L) durationMs = dur
                }
            }

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

                val httpCode = extractHttpStatusCode(error)
                val isHardHttpFailure = httpCode in setOf(401, 403, 404, 410, 451)

                // Reintento automático: mínimo 3 intentos de reconexión ante inestabilidad de la señal antes de mostrar error
                if (!isHardHttpFailure && autoRetryCount < 3) {
                    autoRetryCount++
                    isBuffering = true
                    isError = false
                    try {
                        exoPlayer.seekToDefaultPosition()
                        exoPlayer.prepare()
                        exoPlayer.play()
                    } catch (_: Exception) {}
                    return
                }

                val isGeoblocked = isGeoBlockedError(error)
                val proxy = latestProxyServer

                if (!useProxyFallback && proxy != null && isGeoblocked) {
                    useProxyFallback = true
                    useAutoReferer = true
                    autoRetryCount = 0
                    isBuffering = true
                    isError = false
                    return
                }

                // Saltar automáticamente al siguiente servidor espejo (mirror) si existe
                val availableSources = latestSources
                if (currentSourceIndex + 1 < availableSources.size) {
                    currentSourceIndex++
                    useProxyFallback = false
                    useAutoReferer = false
                    autoRetryCount = 0
                    isBuffering = true
                    isError = false
                    return
                }

                isBuffering = false
                isError = true
                try {
                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                } catch (_: Exception) {}
                errorMessage = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                        "No se pudo conectar con el servidor emisor de la señal tras múltiples intentos. Verifica tu conexión a internet."
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                        "El servidor del canal rechazó la conexión (HTTP ${httpCode ?: "error"}: posible geobloqueo o señal fuera de línea)."
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                    PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                        "El formato de la transmisión no es compatible con el reproductor."
                    else ->
                        "La señal del canal no está disponible en este momento tras varios intentos. Puede estar en pausa o fuera de emisión."
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            if (isGhibli && !showResumeDialog) {
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration
                if (pos >= GhibliPlaybackStore.MIN_RESUME_THRESHOLD_MS) {
                    GhibliPlaybackStore.savePosition(context, channel.name, pos, dur)
                }
            }
            exoPlayer.removeListener(listener)
            try {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
            } catch (_: Exception) {}
            Thread {
                try {
                    exoPlayer.release()
                } catch (_: Exception) {}
            }.start()
        }
    }

    // 3. Ciclo de Vida de la Actividad (pausa/reanuda sin doble release)
    DisposableEffect(lifecycleOwner, exoPlayer, isGhibli, showResumeDialog) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                    if (isGhibli && !showResumeDialog) {
                        val pos = exoPlayer.currentPosition
                        val dur = exoPlayer.duration
                        if (pos >= GhibliPlaybackStore.MIN_RESUME_THRESHOLD_MS) {
                            GhibliPlaybackStore.savePosition(context, channel.name, pos, dur)
                        }
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!isError && !showResumeDialog) exoPlayer.play()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Actualización reactiva de posición y duración para Cine On-Demand (VOD)
    LaunchedEffect(exoPlayer, isHudVisible, isBuffering, showResumeDialog) {
        var lastPeriodicSave = 0L
        while (true) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            val dur = exoPlayer.duration
            if (dur > 0L) durationMs = dur
            isPlaying = exoPlayer.isPlaying

            // Guardar posición periódicamente para Ghibli (cada 3 segundos durante reproducción activa)
            if (isGhibli && isPlaying && !showResumeDialog && currentPositionMs >= GhibliPlaybackStore.MIN_RESUME_THRESHOLD_MS) {
                val now = System.currentTimeMillis()
                if (now - lastPeriodicSave >= 3000L) {
                    lastPeriodicSave = now
                    GhibliPlaybackStore.savePosition(context, channel.name, currentPositionMs, durationMs)
                }
            }

            delay(500)
        }
    }

    // 4. Watchdog reactivo: 15s para TV en directo, 35s para Cine VOD / Ghibli; reintenta hasta 3 veces o salta al siguiente mirror
    LaunchedEffect(isBuffering, isError, activeStreamUrl, retryCount, currentSourceIndex) {
        if (isBuffering && !isError) {
            val watchdogTimeout = if (isVod) 35_000L else 15_000L
            delay(watchdogTimeout)
            if (isBuffering && !isError) {
                if (autoRetryCount < 3) {
                    autoRetryCount++
                    try {
                        exoPlayer.seekToDefaultPosition()
                        exoPlayer.prepare()
                        exoPlayer.play()
                    } catch (_: Exception) {}
                } else if (currentSourceIndex + 1 < sources.size) {
                    currentSourceIndex++
                    useProxyFallback = false
                    useAutoReferer = false
                    autoRetryCount = 0
                } else {
                    isBuffering = false
                    isError = true
                    try {
                        exoPlayer.stop()
                        exoPlayer.clearMediaItems()
                    } catch (_: Exception) {}
                    errorMessage = "El servidor emisor no respondió a tiempo tras varios intentos. La señal podría estar temporalmente saturada o fuera de emisión."
                }
            }
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

    val hudLogoRequest = remember(channel.logo, context) {
        ImageRequest.Builder(context)
            .data(channel.logo)
            .size(128, 128)
            .crossfade(false)
            .error(R.drawable.default_tv)
            .fallback(R.drawable.default_tv)
            .build()
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
                if (view.player !== exoPlayer) {
                    view.player = exoPlayer
                }
            },
            onRelease = { view ->
                view.player = null
            }
        )

        // HUD SUPERIOR: Datos reales de la emisión (Logo, Nombre, Directo, Proxy, Reloj)
        AnimatedVisibility(
            visible = isHudVisible && !isError && !isSideDrawerVisible && !showResumeDialog,
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
                                model = hudLogoRequest,
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
                            // Badge Directo o CINE VOD
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isVod) TvPrimaryContainer.copy(alpha = 0.5f) else TvLiveRedContainer)
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
                                            .background(if (isVod) TvPrimary else TvLiveRed)
                                    )
                                    Text(
                                        text = if (isVod) "CINE VOD" else "DIRECTO",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Estado de conexión real (Directa, VOD o Proxy Loopback)
                        val modeLabel = when {
                            isVod -> "Cine On-Demand · Archivo Multimedia"
                            useProxyFallback && (activeSource.referer.isNotBlank() || activeSource.userAgent.isNotBlank()) ->
                                "Modo: Proxy con Cabeceras"
                            useProxyFallback -> "Modo: Proxy con Referer"
                            else -> "Modo: Conexión Directa"
                        }
                        val sourceSuffix = if (!isVod && sources.size > 1) " · Fuente ${currentSourceIndex + 1}/${sources.size}" else ""
                        Text(
                            text = modeLabel + sourceSuffix,
                            color = if (useProxyFallback) TvSecondary else TvOnSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Lado Derecho: Reloj aislado (no recompone el reproductor ni el HUD completo)
                PlayerClockBadge()
            }
        }

        // HUD INFERIOR: Barra de tiempo interactiva (VOD) + Atajos y Versión de la App
        AnimatedVisibility(
            visible = isHudVisible && !isError && !isSideDrawerVisible && !showResumeDialog,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xD9090D16))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                // Barra de progreso y tiempo interactiva para Cine On-Demand (VOD)
                if (isVod) {
                    val progress = if (durationMs > 0L) {
                        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        // Track de la barra de progreso
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0x55FFFFFF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(TvPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Tiempos: Tiempo actual / Duración total y estado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TvPrimaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isPlaying) "▶ EN REPRODUCCIÓN" else "⏸ EN PAUSA",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = formatMs(currentPositionMs),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = if (durationMs > 0L) formatMs(durationMs) else "--:--",
                                color = TvOnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Fila de Atajos y Versión de la App
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (isVod) {
                            // Atajos específicos para VOD
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(TvPrimary).padding(horizontal = 6.dp, vertical = 1.dp)) {
                                    Text(text = "OK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = if (isPlaying) "Pausar" else "Reanudar", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(TvSurfaceContainerHigh).padding(horizontal = 6.dp, vertical = 1.dp)) {
                                    Text(text = "◀ / ▶", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = "±15s", color = Color.White, fontSize = 11.5.sp)
                            }

                            if (channelsList.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(TvSurfaceContainerHigh).padding(horizontal = 6.dp, vertical = 1.dp)) {
                                        Text(text = "▼", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(text = "Catálogo", color = TvOnSurfaceVariant, fontSize = 11.5.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(TvSurfaceContainerHigh).padding(horizontal = 5.dp, vertical = 1.dp)) {
                                    Text(text = "ATRÁS", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = "Volver a Menú", color = TvOnSurfaceVariant, fontSize = 11.5.sp)
                            }
                        } else {
                            // Atajos para TV En Vivo
                            if (channelsList.isNotEmpty()) {
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
                            Text(text = "Chari-IPTV", color = TvOnSurfaceVariant, fontSize = 10.sp)
                            Text(text = appVersion, color = TvPrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Overlay: Estado de carga / Buffering
        AnimatedVisibility(
            visible = isBuffering && !isError && !showResumeDialog,
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
                    text = if (autoRetryCount > 0) "Reconectando señal (intento $autoRetryCount de 3)..."
                           else "Conectando con ${channel.name}...",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (sources.size > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Probando fuente ${currentSourceIndex + 1} de ${sources.size}",
                        color = TvOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
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
                            model = hudLogoRequest,
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
                                currentSourceIndex = 0
                                autoRetryCount = 0
                                useAutoReferer = false
                                useProxyFallback = false
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
                            onClick = {
                                if (navigatedFromDrawer && channelsList.isNotEmpty()) {
                                    isSideDrawerVisible = true
                                    drawerInteractionTrigger = System.currentTimeMillis()
                                } else {
                                    try {
                                        exoPlayer.stop()
                                        exoPlayer.clearMediaItems()
                                    } catch (_: Exception) {}
                                    onClose()
                                }
                            },
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

        // Overlay: Diálogo Exclusivo Ghibli - Reanudar o Comenzar de Nuevo
        AnimatedVisibility(
            visible = showResumeDialog && !isError,
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
                        .widthIn(min = 480.dp, max = 620.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(TvSurface)
                        .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(20.dp))
                        .padding(horizontal = 32.dp, vertical = 26.dp)
                ) {
                    // Badge Superior Ghibli
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TvPrimaryContainer.copy(alpha = 0.7f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🎬 STUDIO GHIBLI · CONTINUAR REPRODUCCIÓN",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val timeText = if (resumeDurationMs > 0L) {
                        "Dejaste la película en ${formatMs(resumePositionMs)} de ${formatMs(resumeDurationMs)}"
                    } else {
                        "Dejaste la película en ${formatMs(resumePositionMs)}"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TvSurfaceContainerHigh)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            androidx.compose.material3.Text("⏱️", fontSize = 12.sp)
                            Text(
                                text = timeText,
                                color = TvPrimaryLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "¿Deseas continuar la reproducción donde la dejaste o comenzar desde el principio?",
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
                        // Botón 1: Continuar (Enfocado automáticamente)
                        Surface(
                            onClick = {
                                exoPlayer.seekTo(resumePositionMs)
                                exoPlayer.play()
                                showResumeDialog = false
                            },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                            border = ClickableSurfaceDefaults.border(
                                border = Border(BorderStroke(1.dp, Color(0x33FFFFFF))),
                                focusedBorder = Border(BorderStroke(2.5.dp, Color.White))
                            ),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = TvPrimary,
                                focusedContainerColor = TvPrimaryFocused
                            ),
                            modifier = Modifier
                                .focusRequester(resumeFocusRequester)
                                .height(46.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                androidx.compose.material3.Text("▶", fontSize = 13.sp, color = Color.White)
                                Text(
                                    text = "Continuar (${formatMs(resumePositionMs)})",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Botón 2: Comenzar de nuevo
                        Surface(
                            onClick = {
                                GhibliPlaybackStore.clearPosition(context, channel.name)
                                exoPlayer.seekTo(0L)
                                exoPlayer.play()
                                showResumeDialog = false
                            },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                            border = ClickableSurfaceDefaults.border(
                                border = Border(BorderStroke(1.dp, Color(0x33FFFFFF))),
                                focusedBorder = Border(BorderStroke(2.5.dp, Color.White))
                            ),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = TvSurfaceContainerHigh,
                                focusedContainerColor = TvPrimaryContainer
                            ),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                androidx.compose.material3.Text("⏮", fontSize = 13.sp, color = Color.White)
                                Text(
                                    text = "Comenzar de nuevo",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guía D-Pad
                    Text(
                        text = "Presiona OK para seleccionar · ATRÁS para volver al catálogo",
                        color = TvOutline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // CAJÓN LATERAL FLOTANTE DE CANALES (Zapping instantáneo en la misma sección - en capa superior)
        AnimatedVisibility(
            visible = isSideDrawerVisible,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            QuickChannelDrawer(
                channels = channelsList,
                currentChannelUrl = channel.url,
                categoryTitle = categoryTitle,
                favorites = favorites,
                onChannelSelected = { newChannel ->
                    navigatedFromDrawer = true
                    isSideDrawerVisible = false
                    if (newChannel.url == channel.url) {
                        if (isError) {
                            currentSourceIndex = 0
                            autoRetryCount = 0
                            useAutoReferer = false
                            useProxyFallback = false
                            isError = false
                            isBuffering = true
                            retryCount++
                        }
                    } else {
                        isError = false
                        isBuffering = true
                        onChannelChange(newChannel)
                    }
                },
                onClose = {
                    isSideDrawerVisible = false
                },
                onUserActivity = {
                    drawerInteractionTrigger = System.currentTimeMillis()
                }
            )
        }
    }
}

@Composable
private fun PlayerClockBadge() {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var currentTime by remember { mutableStateOf(formatter.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = formatter.format(Date())
            delay(15_000)
        }
    }

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

private fun extractHttpStatusCode(error: PlaybackException): Int? {
    var cause: Throwable? = error.cause
    while (cause != null) {
        if (cause is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException) {
            return cause.responseCode
        }
        cause = cause.cause
    }
    return null
}

private fun isGeoBlockedError(error: PlaybackException): Boolean {
    val code = extractHttpStatusCode(error)
    if (code == 403 || code == 451) {
        return true
    }
    val msg = (error.message ?: "").lowercase()
    val causeMsg = (error.cause?.message ?: "").lowercase()
    return msg.contains("403") || msg.contains("geoblock") || msg.contains("geo-block") ||
            causeMsg.contains("403") || causeMsg.contains("geoblock") || causeMsg.contains("geo-block")
}
