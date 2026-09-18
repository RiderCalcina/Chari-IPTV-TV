package com.example.chariiptv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.presentation.TvViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

val SUPPORTED_COUNTRIES = mapOf(
    "fav" to "Favoritos",
    "ar" to "Argentina", "bo" to "Bolivia", "cl" to "Chile", "co" to "Colombia",
    "cr" to "Costa Rica", "cu" to "Cuba", "do" to "Rep. Dominicana", "ec" to "Ecuador",
    "es" to "España", "gt" to "Guatemala", "hn" to "Honduras", "mx" to "México",
    "ni" to "Nicaragua", "pa" to "Panamá", "pe" to "Perú", "pr" to "Puerto Rico",
    "py" to "Paraguay", "sv" to "El Salvador", "uy" to "Uruguay", "ve" to "Venezuela"
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            val viewModel: TvViewModel = hiltViewModel()
            val channels by viewModel.channels.collectAsState()
            val favorites by viewModel.favorites.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val currentChannel by viewModel.currentChannel.collectAsState()
            val selectedCountryCode by viewModel.selectedCountryCode.collectAsState()
            val rssNews by viewModel.rssNews.collectAsState()

            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
                ChannelGrid(
                    selectedCountryCode = selectedCountryCode,
                    rssNews = rssNews,
                    onCountrySelected = { code, forceRefresh -> viewModel.selectCountry(code, forceRefresh) },
                    channels = channels,
                    favorites = favorites,
                    isLoading = isLoading,
                    onChannelClick = { viewModel.playChannel(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )

                if (currentChannel != null) {
                    BackHandler {
                        viewModel.playChannel(null)
                    }
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        ExoPlayerView(
                            channel = currentChannel!!,
                            isFavorite = favorites.contains(currentChannel!!.url),
                            onToggleFavorite = { viewModel.toggleFavorite(currentChannel!!) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelGrid(
    selectedCountryCode: String,
    rssNews: String,
    onCountrySelected: (String, Boolean) -> Unit,
    channels: List<Channel>,
    favorites: List<String>,
    isLoading: Boolean,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit
) {
    val sortedChannels = channels.sortedBy { it.name }
    val gridState = rememberLazyGridState()
    val countryRowState = rememberLazyListState()
    val firstItemFocusRequester = remember { FocusRequester() }
    val selectedCountryFocusRequester = remember { FocusRequester() }
    val countryKeys = SUPPORTED_COUNTRIES.keys.toList()
    var countryToRefresh by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedCountryCode) {
        val index = countryKeys.indexOf(selectedCountryCode)
        if (index >= 0) {
            countryRowState.animateScrollToItem(index)
        }
        
        if (sortedChannels.isNotEmpty()) {
            gridState.scrollToItem(0)
            delay(100)
            try {
                firstItemFocusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        
        LazyRow(
            state = countryRowState,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).focusRestorer(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(countryKeys) { code ->
                val name = SUPPORTED_COUNTRIES[code]!!
                val isSelected = code == selectedCountryCode
                Surface(
                    onClick = { 
                        if (code == selectedCountryCode && code != "fav") {
                            countryToRefresh = code
                        } else {
                            onCountrySelected(code, false) 
                        }
                    },
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSelected) Color(0xFF3B82F6) else Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF60A5FA)
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        .then(if (isSelected) Modifier.focusRequester(selectedCountryFocusRequester) else Modifier)
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Buscando canales de ${SUPPORTED_COUNTRIES[selectedCountryCode]}...",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${channels.size} canales activos en ${SUPPORTED_COUNTRIES[selectedCountryCode]}", 
                    color = Color.Gray, 
                    fontSize = 11.sp
                )
                if (rssNews.isNotEmpty()) {
                    androidx.compose.material3.Text(
                        text = rssNews,
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 16.dp).weight(1f).basicMarquee(iterations = Int.MAX_VALUE, velocity = 30.dp),
                        maxLines = 1
                    )
                }
            }
            
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(items = sortedChannels, key = { _, channel -> channel.url }) { index, channel ->
                    val isFav = favorites.contains(channel.url)
                    
                    Surface(
                        onClick = { onChannelClick(channel) },
                        onLongClick = { onToggleFavorite(channel) },
                        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(BorderStroke(4.dp, Color.White))
                        ),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f/11f)
                            .then(if (index == 0) Modifier.focusRequester(firstItemFocusRequester) else Modifier)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (channel.logo.isNotEmpty()) {
                                    SubcomposeAsyncImage(
                                        model = channel.logo,
                                        contentDescription = channel.name,
                                        modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 8.dp),
                                        contentScale = ContentScale.Fit
                                    ) {
                                        val state = painter.state
                                        if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                androidx.compose.material3.Text("📺", fontSize = 32.sp)
                                            }
                                        } else {
                                            SubcomposeAsyncImageContent()
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        androidx.compose.material3.Text("📺", fontSize = 32.sp)
                                    }
                                }
                                Text(
                                    text = channel.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x88000000))
                                    .padding(6.dp)
                            ) {
                                androidx.compose.material3.Text(
                                    text = if (isFav) "❤️" else "🤍",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (countryToRefresh != null) {
            val countryName = SUPPORTED_COUNTRIES[countryToRefresh] ?: ""
            AlertDialog(
                onDismissRequest = { countryToRefresh = null },
                properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = false),
                title = { androidx.compose.material3.Text("Escanear Canales", color = Color.White) },
                text = { androidx.compose.material3.Text("¿Deseas volver a escanear los canales de $countryName? Esto actualizará la lista desde el servidor.", color = Color.LightGray) },
                containerColor = Color(0xFF1E293B),
                confirmButton = {
                    Button(
                        onClick = {
                            onCountrySelected(countryToRefresh!!, true)
                            countryToRefresh = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        androidx.compose.material3.Text("Escanear", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { countryToRefresh = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                    ) {
                        androidx.compose.material3.Text("Cancelar", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun ExoPlayerView(
    channel: Channel,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    var exoPlayer: ExoPlayer? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    exoPlayer?.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer?.play()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer?.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    player = ExoPlayer.Builder(context).build().also {
                        exoPlayer = it
                        it.setMediaItem(MediaItem.fromUri(channel.url))
                        it.prepare()
                        it.playWhenReady = true
                        it.addListener(object : Player.Listener {
                            override fun onPlayerError(error: PlaybackException) {
                                super.onPlayerError(error)
                                android.widget.Toast.makeText(
                                    context,
                                    "Error al reproducir el canal (Puede estar caído o geobloqueado)",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                    }
                    useController = true 
                }
            },
            update = { view ->
                view.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && event.action == android.view.KeyEvent.ACTION_UP) {
                        onToggleFavorite()
                        val msg = if (isFavorite) "Eliminado de Favoritos" else "Añadido a Favoritos"
                        android.widget.Toast.makeText(view.context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        true
                    } else {
                        false
                    }
                }
            },
            onRelease = {
                exoPlayer?.release()
            }
        )
    }
}
