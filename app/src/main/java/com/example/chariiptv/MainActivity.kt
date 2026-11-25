package com.example.chariiptv

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import javax.inject.Inject
import com.example.chariiptv.remote.TvLanServer
import com.example.chariiptv.remote.TvNsdBroadcaster
import com.example.chariiptv.remote.LanRemoteEventBus
import com.example.chariiptv.remote.LanRemoteEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.player.ExoPlayerView
import com.example.chariiptv.presentation.TvViewModel
import com.example.chariiptv.presentation.components.*
import com.example.chariiptv.theme.*
import com.example.chariiptv.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val tvViewModel: TvViewModel by viewModels()

    @Inject lateinit var tvLanServer: TvLanServer
    @Inject lateinit var tvNsdBroadcaster: TvNsdBroadcaster
    @Inject lateinit var lanEventBus: LanRemoteEventBus

    // Interceptor directo a nivel de actividad para eventos del control remoto en reproducción
    var playerKeyHandler: ((KeyEvent) -> Boolean)? = null

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (playerKeyHandler?.invoke(event) == true) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        // Inicializar Servidor LAN Micro-HTTP y difusión mDNS DNS-SD para control remoto
        tvLanServer.currentChannelProvider = { tvViewModel.uiState.value.currentChannel }
        tvLanServer.start()
        tvNsdBroadcaster.start()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                lanEventBus.events.collect { event ->
                    handleLanRemoteEvent(event)
                }
            }
        }

        setContent {
            TVDROIDTheme {
                val viewModel: TvViewModel = tvViewModel
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                var notificationMessage by remember { mutableStateOf<String?>(null) }
                // Estados de scroll elevados para conservar posición al entrar/salir del reproductor
                val gridState = rememberLazyGridState()
                val mainRowState = rememberLazyListState()
                val countryRowState = rememberLazyListState()

                LaunchedEffect(notificationMessage) {
                    if (notificationMessage != null) {
                        delay(2500)
                        notificationMessage = null
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TvBackground)
                ) {
                    if (uiState.currentChannel != null) {
                        // Reproductor a Pantalla Completa (desmonta la grilla y marquee de fondo para liberar GPU/CPU)
                        val currentCategoryTitle = if (uiState.selectedFilterId == "countries") {
                            Constants.SUPPORTED_COUNTRIES[uiState.selectedCountryCode] ?: "Países"
                        } else {
                            Constants.getFilterDisplayName(uiState.selectedFilterId)
                        }

                        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                            ExoPlayerView(
                                channel = uiState.currentChannel!!,
                                isFavorite = uiState.favorites.contains(uiState.currentChannel!!.url),
                                categoryChannels = uiState.channels,
                                categoryTitle = currentCategoryTitle,
                                favorites = uiState.favorites,
                                onChannelChange = { newChannel ->
                                    viewModel.playChannel(newChannel)
                                },
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(uiState.currentChannel!!)
                                    notificationMessage = if (uiState.favorites.contains(uiState.currentChannel!!.url)) {
                                        "☆ Eliminado de favoritos"
                                    } else {
                                        "⭐ Añadido a favoritos"
                                    }
                                },
                                onClose = {
                                    viewModel.playChannel(null)
                                }
                            )
                        }
                    } else {
                        ChannelGridScreen(
                            selectedFilterId = uiState.selectedFilterId,
                            selectedCountryCode = uiState.selectedCountryCode,
                            isCountrySubMenuVisible = uiState.isCountrySubMenuVisible,
                            rssNews = uiState.rssNews,
                            gridState = gridState,
                            mainRowState = mainRowState,
                            countryRowState = countryRowState,
                            onFilterSelected = { filterId, forceRefresh -> viewModel.selectFilter(filterId, forceRefresh) },
                            onCountrySelected = { code, forceRefresh -> viewModel.selectCountry(code, forceRefresh) },
                            channels = uiState.channels,
                            favorites = uiState.favorites,
                            isLoading = uiState.isLoading,
                            onChannelClick = { viewModel.playChannel(it) },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(it)
                                notificationMessage = if (uiState.favorites.contains(it.url)) {
                                    "☆ Eliminado de favoritos"
                                } else {
                                    "⭐ Añadido a favoritos"
                                }
                            },
                            onChannelFocused = { viewModel.prewarmChannel(it) }
                        )
                    }

                    // Notificación Flotante Estilizada (Snackbar TV)
                    AnimatedVisibility(
                        visible = notificationMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TvSurfaceContainerHigh.copy(alpha = 0.95f))
                                .border(BorderStroke(1.dp, Color(0x44FFFFFF)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = notificationMessage ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (uiState.errorMessage != null) {
                        AlertDialog(
                            onDismissRequest = { viewModel.clearError() },
                            title = { androidx.compose.material3.Text("Aviso", color = Color.White) },
                            text = { androidx.compose.material3.Text(uiState.errorMessage!!, color = Color.LightGray) },
                            containerColor = TvSurface,
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.clearError() },
                                    colors = ButtonDefaults.colors(containerColor = TvPrimary)
                                ) {
                                    Text("Aceptar", color = Color.White)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val url = intent.getStringExtra("channel_url") ?: intent.data?.getQueryParameter("url")
        val name = intent.getStringExtra("channel_name") ?: intent.data?.getQueryParameter("name") ?: "Canal TV"
        val logo = intent.getStringExtra("channel_logo") ?: intent.data?.getQueryParameter("logo") ?: ""
        if (!url.isNullOrBlank()) {
            tvViewModel.playChannel(Channel(name = name, logo = logo, url = url))
        }
    }

    override fun onStop() {
        super.onStop()
        tvViewModel.playChannel(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        tvLanServer.stop()
        tvNsdBroadcaster.stop()
        tvViewModel.playChannel(null)
    }

    private fun handleLanRemoteEvent(event: LanRemoteEvent) {
        when (event) {
            is LanRemoteEvent.PlayChannel -> {
                tvViewModel.playChannel(event.channel)
            }
            is LanRemoteEvent.SendKeyEvent -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_HOME -> {
                            // Botón "Inicio": si está reproduciendo, cerrar reproductor y volver al menú principal
                            if (tvViewModel.uiState.value.currentChannel != null) {
                                tvViewModel.playChannel(null)
                            } else {
                                tvViewModel.selectFilter("fav")
                            }
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            // Botón "Atrás": primero intentar cerrar el cajón lateral si está abierto
                            val downTime = android.os.SystemClock.uptimeMillis()
                            val actionDown = KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0)
                            val actionUp = KeyEvent(downTime, downTime + 50, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0)
                            val handled = (playerKeyHandler?.invoke(actionDown) == true) || (playerKeyHandler?.invoke(actionUp) == true)
                            if (!handled) {
                                if (tvViewModel.uiState.value.currentChannel != null) {
                                    tvViewModel.playChannel(null)
                                } else {
                                    onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }
                        KeyEvent.KEYCODE_VOLUME_UP -> {
                            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                            audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                        }
                        KeyEvent.KEYCODE_VOLUME_DOWN -> {
                            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                            audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                        }
                        KeyEvent.KEYCODE_VOLUME_MUTE -> {
                            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI)
                            } else {
                                @Suppress("DEPRECATION")
                                audioManager?.setStreamMute(AudioManager.STREAM_MUSIC, true)
                            }
                        }
                        else -> {
                            val downTime = android.os.SystemClock.uptimeMillis()
                            val actionDown = KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, event.keyCode, 0)
                            dispatchKeyEvent(actionDown)
                            delay(50)
                            val upTime = android.os.SystemClock.uptimeMillis()
                            val actionUp = KeyEvent(downTime, upTime, KeyEvent.ACTION_UP, event.keyCode, 0)
                            dispatchKeyEvent(actionUp)
                        }
                    }
                }
            }
            is LanRemoteEvent.SetSearchQuery -> {
                val query = event.query.trim().lowercase()
                if (Constants.SUPPORTED_COUNTRIES.containsKey(query)) {
                    tvViewModel.selectCountry(query)
                } else {
                    val currentChannels = tvViewModel.uiState.value.channels
                    val match = currentChannels.find { it.name.lowercase().contains(query) }
                    if (match != null) {
                        tvViewModel.playChannel(match)
                    }
                }
            }
            is LanRemoteEvent.SyncFavorites -> {
                tvViewModel.syncFavorites(event.favoriteUrls)
            }
            is LanRemoteEvent.StopPlayback -> {
                tvViewModel.playChannel(null)
            }
        }
    }
}

@Composable
fun ChannelGridScreen(
    selectedFilterId: String,
    selectedCountryCode: String,
    isCountrySubMenuVisible: Boolean,
    rssNews: String,
    gridState: LazyGridState,
    mainRowState: LazyListState,
    countryRowState: LazyListState,
    onFilterSelected: (String, Boolean) -> Unit,
    onCountrySelected: (String, Boolean) -> Unit,
    channels: List<Channel>,
    favorites: Set<String>,
    isLoading: Boolean,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onChannelFocused: (Channel) -> Unit = {}
) {
    // Respeta el orden de prioridad 24/7 calculado en Dispatchers.IO y garantiza URLs únicas como clave estable
    val sortedChannels = remember(channels) { channels.distinctBy { it.url } }
    val isGhibli = selectedFilterId == "ghibli"
    val selectedFilterFocusRequester = remember { FocusRequester() }
    val selectedCountryFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    val isScrolledDown by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    // Navegación con tecla ATRÁS del control remoto:
    // 1. Si está abajo en la cuadrícula, sube inmediatamente al inicio y enfoca la pestaña
    // 2. Si ya está arriba y en otra categoría, regresa a Favoritos
    BackHandler(enabled = isScrolledDown || selectedFilterId != "fav") {
        if (isScrolledDown) {
            coroutineScope.launch {
                gridState.scrollToItem(0)
                delay(30)
                try {
                    if (selectedFilterId == "countries") {
                        selectedCountryFocusRequester.requestFocus()
                    } else {
                        selectedFilterFocusRequester.requestFocus()
                    }
                } catch (_: Exception) {}
            }
        } else if (selectedFilterId != "fav") {
            onFilterSelected("fav", false)
            coroutineScope.launch {
                delay(30)
                try {
                    selectedFilterFocusRequester.requestFocus()
                } catch (_: Exception) {}
            }
        }
    }

    var itemToRefresh by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var showSupportDialog by remember { mutableStateOf(false) }

    val mainNavItems = remember {
        listOf(
            MainNavItem("important", "Importante", "", isSpecial = true),
            MainNavItem("fav", "Favoritos", "⭐"),
            MainNavItem("countries", "Países", "🌎"),
            MainNavItem("sports", "Deportes", "⚽"),
            MainNavItem("news", "Noticias", "📰"),
            MainNavItem("movies", "Películas", "🎬"),
            MainNavItem("series", "Series", "📺"),
            MainNavItem("animation", "Infantil", "👶"),
            MainNavItem("music", "Música", "🎵"),
            MainNavItem("ghibli", "Studio Ghibli", "")
        )
    }

    val countryKeys = remember { Constants.SUPPORTED_COUNTRIES.keys.filter { it != "fav" } }

    LaunchedEffect(selectedFilterId, selectedCountryCode) {
        val index = mainNavItems.indexOfFirst { it.id == selectedFilterId }
        if (index >= 0) {
            mainRowState.animateScrollToItem(index)
        }

        if (selectedFilterId == "countries") {
            val countryIndex = countryKeys.indexOf(selectedCountryCode)
            if (countryIndex >= 0) {
                countryRowState.animateScrollToItem(countryIndex)
            }
            delay(120)
            try {
                selectedCountryFocusRequester.requestFocus()
            } catch (_: Exception) {}
        } else {
            delay(100)
            try {
                selectedFilterFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    val currentFilterTitle = if (selectedFilterId == "countries") {
        Constants.SUPPORTED_COUNTRIES[selectedCountryCode] ?: "Países"
    } else {
        Constants.getFilterDisplayName(selectedFilterId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // 1. Cabecera Premium (Brand, Reloj en vivo, Pestañas D-Pad, Marquesina RSS)
        // CERO avatares de usuario, CERO métricas falsas de red
        TvTopHeader(
            mainNavItems = mainNavItems,
            selectedFilterId = selectedFilterId,
            navRowState = mainRowState,
            selectedFilterFocusRequester = selectedFilterFocusRequester,
            onFilterSelected = { id ->
                if (id == "countries") {
                    if (selectedFilterId != "countries" || channels.isEmpty()) {
                        onFilterSelected("countries", channels.isEmpty())
                    }
                } else {
                    if (selectedFilterId != id || channels.isEmpty()) {
                        onFilterSelected(id, channels.isEmpty())
                    }
                }
            },
            onImportantClicked = { showSupportDialog = true },
            rssNews = rssNews
        )

        // 2. Sub-selector de Países Dinámico (visible cuando la pestaña activa es "Países")
        AnimatedVisibility(
            visible = isCountrySubMenuVisible || selectedFilterId == "countries",
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CountrySelectorRow(
                countryKeys = countryKeys,
                selectedCountryCode = selectedCountryCode,
                countryRowState = countryRowState,
                selectedCountryFocusRequester = selectedCountryFocusRequester,
                onCountrySelected = { code ->
                    onCountrySelected(code, channels.isEmpty())
                }
            )
        }

        // 3. Barra de Acciones y Telemetría Real (Conteo de canales, Botón Re-escanear, Subir)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val hasChannels = sortedChannels.isNotEmpty()
            if (hasChannels) {
                Text(
                    text = "${sortedChannels.size} ${if (selectedFilterId == "ghibli") "películas disponibles" else "canales activos"} en $currentFilterTitle",
                    color = TvOutline,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    color = TvPrimaryFocused,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp)
                )
            }

            // Botón Escanear para refrescar fuentes en vivo (excluido en Favoritos y Ghibli)
            if (selectedFilterId != "fav" && selectedFilterId != "ghibli") {
                Surface(
                    onClick = {
                        if (selectedFilterId == "countries") {
                            itemToRefresh = Pair(selectedCountryCode, true)
                        } else {
                            itemToRefresh = Pair(selectedFilterId, false)
                        }
                    },
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(BorderStroke(1.dp, Color(0x33FFFFFF))),
                        focusedBorder = Border(BorderStroke(2.dp, Color.White))
                    ),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = TvSurface,
                        focusedContainerColor = TvPrimaryFocused
                    ),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        androidx.compose.material3.Text("🔄", fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Escanear",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Atajo visual rápido para volver a las pestañas al desplazarse hacia abajo
            AnimatedVisibility(
                visible = isScrolledDown,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    onClick = {
                        coroutineScope.launch {
                            gridState.scrollToItem(0)
                            delay(30)
                            try {
                                if (selectedFilterId == "countries") {
                                    selectedCountryFocusRequester.requestFocus()
                                } else {
                                    selectedFilterFocusRequester.requestFocus()
                                }
                            } catch (_: Exception) {}
                        }
                    },
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(BorderStroke(1.dp, Color(0x33FFFFFF))),
                        focusedBorder = Border(BorderStroke(2.dp, Color.White))
                    ),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = TvSurface,
                        focusedContainerColor = TvPrimaryFocused
                    ),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        androidx.compose.material3.Text("⬆", fontSize = 11.sp, color = TvSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Subir a Pestañas [Atrás]",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 4. Área de Contenido Principal (Grilla 2 columnas para Studio Ghibli con sinopsis, 4 columnas para TV)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val hasChannels = sortedChannels.isNotEmpty()

            if (isLoading && !hasChannels) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TvPrimaryFocused)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.searching_channels, currentFilterTitle),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (sortedChannels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        if (selectedFilterId == "fav") {
                            androidx.compose.material3.Text("⭐", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aún no tienes canales favoritos",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Explora las categorías o países y pulsa la estrella ⭐ en cualquier canal para agregarlo aquí.",
                                color = TvOnSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            androidx.compose.material3.Text("📡", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No se encontraron canales",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = if (isGhibli) GridCells.Fixed(2) else GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    itemsIndexed(
                        items = sortedChannels,
                        key = { _, channel -> channel.url }
                    ) { _, channel ->
                        val isFav = favorites.contains(channel.url)
                        if (isGhibli) {
                            GhibliMovieCardView(
                                channel = channel,
                                onClick = { onChannelClick(channel) },
                                onFocused = { onChannelFocused(channel) }
                            )
                        } else {
                            ChannelCardView(
                                channel = channel,
                                isFavorite = isFav,
                                onClick = { onChannelClick(channel) },
                                onLongClick = { onToggleFavorite(channel) },
                                onToggleFavorite = { onToggleFavorite(channel) },
                                onFocused = { onChannelFocused(channel) }
                            )
                        }
                    }
                }
            }
        }

        // 5. Pie de Pantalla Fijo (Guía D-Pad y VERSIÓN DE LA APP EN LA PARTE INFERIOR DERECHA)
        TvFooterBar(
            channelCountText = if (sortedChannels.isNotEmpty()) "$currentFilterTitle (${sortedChannels.size})" else "",
            showFavoriteHint = !isGhibli,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    // Diálogo de Confirmación para Volver a Escanear
    if (itemToRefresh != null) {
        val (refreshId, isCountry) = itemToRefresh!!
        val itemName = if (isCountry) {
            Constants.SUPPORTED_COUNTRIES[refreshId] ?: refreshId
        } else {
            Constants.getFilterDisplayName(refreshId)
        }

        val cancelFocusRequester = remember { FocusRequester() }
        val scanFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(80)
            try {
                scanFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                val wasCountry = isCountry
                itemToRefresh = null
                coroutineScope.launch {
                    delay(60)
                    try {
                        if (wasCountry) {
                            selectedCountryFocusRequester.requestFocus()
                        } else {
                            selectedFilterFocusRequester.requestFocus()
                        }
                    } catch (_: Exception) {}
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 420.dp, max = 500.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TvBackground)
                    .border(BorderStroke(1.5.dp, Color(0x44FFFFFF)), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.scan_channels_title),
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.scan_channels_desc, itemName),
                        color = TvOnSurfaceVariant,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            onClick = {
                                val wasCountry = isCountry
                                itemToRefresh = null
                                coroutineScope.launch {
                                    delay(60)
                                    try {
                                        if (wasCountry) {
                                            selectedCountryFocusRequester.requestFocus()
                                        } else {
                                            selectedFilterFocusRequester.requestFocus()
                                        }
                                    } catch (_: Exception) {}
                                }
                            },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                            border = ClickableSurfaceDefaults.border(
                                border = Border(BorderStroke(1.5.dp, Color(0x44FFFFFF))),
                                focusedBorder = Border(BorderStroke(3.dp, Color.White))
                            ),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = TvSurfaceContainerHigh,
                                focusedContainerColor = TvLiveRed
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(cancelFocusRequester)
                                .focusProperties { right = scanFocusRequester }
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                val id = refreshId
                                val wasCountry = isCountry
                                itemToRefresh = null
                                if (wasCountry) {
                                    onCountrySelected(id, true)
                                } else {
                                    onFilterSelected(id, true)
                                }
                                coroutineScope.launch {
                                    delay(60)
                                    try {
                                        if (wasCountry) {
                                            selectedCountryFocusRequester.requestFocus()
                                        } else {
                                            selectedFilterFocusRequester.requestFocus()
                                        }
                                    } catch (_: Exception) {}
                                }
                            },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                            border = ClickableSurfaceDefaults.border(
                                border = Border(BorderStroke(1.5.dp, Color(0x443B82F6))),
                                focusedBorder = Border(BorderStroke(3.dp, Color.White))
                            ),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = TvPrimary,
                                focusedContainerColor = TvPrimaryFocused
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(scanFocusRequester)
                                .focusProperties { left = cancelFocusRequester }
                        ) {
                            Text(
                                text = stringResource(R.string.scan),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Panorámico "Importante" (PayPal, Yape, Web Oficial)
    if (showSupportDialog) {
        ImportantModalDialog(onDismiss = { showSupportDialog = false })
    }
}
