package com.example.chariiptv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.player.ExoPlayerView
import com.example.chariiptv.presentation.TvViewModel
import com.example.chariiptv.theme.TVDROIDTheme
import com.example.chariiptv.theme.TvPrimary
import com.example.chariiptv.theme.TvPrimaryFocused
import com.example.chariiptv.theme.TvSurface
import com.example.chariiptv.theme.TvSurfaceFocused
import com.example.chariiptv.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val tvViewModel: TvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            TVDROIDTheme {
                val viewModel: TvViewModel = tvViewModel
                val uiState by viewModel.uiState.collectAsState()
                
                var notificationMessage by remember { mutableStateOf<String?>(null) }
                
                LaunchedEffect(notificationMessage) {
                    if (notificationMessage != null) {
                        delay(2500)
                        notificationMessage = null
                    }
                }
                
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    ChannelGrid(
                        selectedFilterId = uiState.selectedFilterId,
                        selectedCountryCode = uiState.selectedCountryCode,
                        isCountrySubMenuVisible = uiState.isCountrySubMenuVisible,
                        rssNews = uiState.rssNews,
                        onFilterSelected = { filterId, forceRefresh -> viewModel.selectFilter(filterId, forceRefresh) },
                        onCountrySelected = { code, forceRefresh -> viewModel.selectCountry(code, forceRefresh) },
                        channels = uiState.channels,
                        favorites = uiState.favorites,
                        isLoading = uiState.isLoading,
                        onChannelClick = { viewModel.playChannel(it) },
                        onToggleFavorite = { 
                            viewModel.toggleFavorite(it) 
                            notificationMessage = if (uiState.favorites.contains(it.url)) {
                                "🤍 Eliminado de favoritos"
                            } else {
                                "❤️ Añadido a favoritos"
                            }
                        }
                    )

                    if (uiState.currentChannel != null) {
                        BackHandler {
                            viewModel.playChannel(null)
                        }
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                            ExoPlayerView(
                                channel = uiState.currentChannel!!,
                                channelsList = uiState.channels,
                                isFavorite = uiState.favorites.contains(uiState.currentChannel!!.url),
                                onToggleFavorite = { 
                                    viewModel.toggleFavorite(uiState.currentChannel!!)
                                    notificationMessage = if (uiState.favorites.contains(uiState.currentChannel!!.url)) {
                                        "🤍 Eliminado de favoritos"
                                    } else {
                                        "❤️ Añadido a favoritos"
                                    }
                                },
                                onClose = {
                                    viewModel.playChannel(null)
                                }
                            )
                        }
                    }
                    
                    // Custom TV Snackbar Notification
                    AnimatedVisibility(
                        visible = notificationMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(TvSurfaceFocused.copy(alpha = 0.9f))
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = notificationMessage ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (uiState.errorMessage != null) {
                        AlertDialog(
                            onDismissRequest = { viewModel.clearError() },
                            title = { androidx.compose.material3.Text("Error", color = Color.White) },
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

    override fun onStop() {
        super.onStop()
        // Detener y silenciar inmediatamente la reproducción al salir al menú del TV o segundo plano
        tvViewModel.playChannel(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        tvViewModel.playChannel(null)
    }
}

data class MainNavItem(
    val id: String,
    val label: String,
    val icon: String,
    val isSpecial: Boolean = false
)

@Composable
fun FilterCardButton(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpecialYellow: Boolean = false,
    focusRequester: FocusRequester? = null
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                BorderStroke(
                    1.dp,
                    if (isSpecialYellow) Color(0x66FFEB3B) else if (isSelected) Color(0x553B82F6) else Color(0x22FFFFFF)
                )
            ),
            focusedBorder = Border(
                BorderStroke(2.dp, if (isSpecialYellow) Color.Yellow else Color.White)
            )
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) TvPrimary else Color(0xFF0F172A),
            focusedContainerColor = if (isSelected) TvPrimaryFocused else Color(0xFF1E293B)
        ),
        modifier = modifier
            .padding(horizontal = 3.dp, vertical = 4.dp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            androidx.compose.material3.Text(
                text = icon,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSpecialYellow) Color.Yellow else if (isSelected) Color.White else Color.LightGray,
                fontWeight = if (isSelected || isSpecialYellow) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun ChannelGrid(
    selectedFilterId: String,
    selectedCountryCode: String,
    isCountrySubMenuVisible: Boolean,
    rssNews: String,
    onFilterSelected: (String, Boolean) -> Unit,
    onCountrySelected: (String, Boolean) -> Unit,
    channels: List<Channel>,
    favorites: List<String>,
    isLoading: Boolean,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit
) {
    val sortedChannels = channels.sortedBy { it.name }
    val gridState = rememberLazyGridState()
    val mainRowState = rememberLazyListState()
    val countryRowState = rememberLazyListState()
    val firstItemFocusRequester = remember { FocusRequester() }
    val selectedFilterFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    val isScrolledDown by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    // Al presionar 'Atrás' en el control remoto cuando se ha explorado hacia abajo,
    // regresa inmediatamente al inicio de la cuadrícula y enfoca las pestañas superiores
    BackHandler(enabled = isScrolledDown) {
        coroutineScope.launch {
            gridState.animateScrollToItem(0)
            delay(50)
            try {
                selectedFilterFocusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }
    
    // Pair(id, isCountry)
    var itemToRefresh by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var showSupportDialog by remember { mutableStateOf(false) }

    val mainNavItems = remember {
        listOf(
            MainNavItem("important", "Importante", "⭐", isSpecial = true),
            MainNavItem("fav", "Favoritos", "❤️"),
            MainNavItem("countries", "Países", "🌎"),
            MainNavItem("sports", "Deportes", "⚽"),
            MainNavItem("news", "Noticias", "📰"),
            MainNavItem("movies", "Películas", "🎬"),
            MainNavItem("series", "Series", "📺"),
            MainNavItem("animation", "Infantil", "👶"),
            MainNavItem("music", "Música", "🎵")
        )
    }

    val countryKeys = remember { Constants.SUPPORTED_COUNTRIES.keys.filter { it != "fav" } }
    val selectedCountryFocusRequester = remember { FocusRequester() }

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
            } catch (e: Exception) {}
        } else if (sortedChannels.isNotEmpty()) {
            gridState.scrollToItem(0)
            delay(100)
            try {
                firstItemFocusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    val currentFilterTitle = if (selectedFilterId == "countries") {
        Constants.SUPPORTED_COUNTRIES[selectedCountryCode] ?: "Países"
    } else {
        Constants.getFilterDisplayName(selectedFilterId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        
        // Fila 1: Botones Principales (Importante primero, luego categorías y países)
        LazyRow(
            state = mainRowState,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).focusRestorer(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(mainNavItems) { item ->
                val isSelected = item.id == selectedFilterId
                FilterCardButton(
                    icon = item.icon,
                    label = item.label,
                    isSelected = isSelected,
                    isSpecialYellow = item.isSpecial,
                    focusRequester = if (isSelected) selectedFilterFocusRequester else null,
                    onClick = {
                        when (item.id) {
                            "important" -> {
                                showSupportDialog = true
                            }
                            "countries" -> {
                                if (selectedFilterId == "countries") {
                                    itemToRefresh = Pair(selectedCountryCode, true)
                                } else {
                                    onFilterSelected("countries", false)
                                }
                            }
                            else -> {
                                if (isSelected && item.id != "fav") {
                                    itemToRefresh = Pair(item.id, false)
                                } else {
                                    onFilterSelected(item.id, false)
                                }
                            }
                        }
                    }
                )
            }
        }

        // Fila 2: Sub-selector de Países (visible cuando se selecciona "Países")
        AnimatedVisibility(
            visible = isCountrySubMenuVisible || selectedFilterId == "countries",
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val countryKeys = Constants.SUPPORTED_COUNTRIES.keys.filter { it != "fav" }
            LazyRow(
                state = countryRowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .focusRestorer(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(countryKeys) { code ->
                    val name = Constants.SUPPORTED_COUNTRIES[code] ?: code
                    val flag = Constants.COUNTRY_FLAGS[code] ?: "🌐"
                    val isCountryActive = code == selectedCountryCode
                    
                    Surface(
                        onClick = {
                            if (isCountryActive) {
                                itemToRefresh = Pair(code, true)
                            } else {
                                onCountrySelected(code, false)
                            }
                        },
                        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
                        border = ClickableSurfaceDefaults.border(
                            border = Border(
                                BorderStroke(
                                    1.dp,
                                    if (isCountryActive) Color(0x663B82F6) else Color(0x1FFFFFFF)
                                )
                            ),
                            focusedBorder = Border(BorderStroke(2.dp, Color.White))
                        ),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (isCountryActive) TvPrimary else Color(0xFF0F172A),
                            focusedContainerColor = TvPrimaryFocused
                        ),
                        modifier = Modifier
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                            .then(if (isCountryActive) Modifier.focusRequester(selectedCountryFocusRequester) else Modifier)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            androidx.compose.material3.Text(text = flag, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = name,
                                color = if (isCountryActive) Color.White else Color.LightGray,
                                fontWeight = if (isCountryActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }
        }
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.searching_channels, currentFilterTitle),
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.active_channels, channels.size, currentFilterTitle), 
                    color = Color.Gray, 
                    fontSize = 11.sp
                )
                
                // Botón interactivo y atajo para volver arriba a las pestañas rápidamente
                AnimatedVisibility(
                    visible = isScrolledDown,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        onClick = {
                            coroutineScope.launch {
                                gridState.animateScrollToItem(0)
                                delay(50)
                                try {
                                    selectedFilterFocusRequester.requestFocus()
                                } catch (e: Exception) {}
                            }
                        },
                        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                        border = ClickableSurfaceDefaults.border(
                            border = Border(BorderStroke(1.dp, Color(0x33FFFFFF))),
                            focusedBorder = Border(BorderStroke(2.dp, Color.White))
                        ),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color(0xFF1E293B),
                            focusedContainerColor = TvPrimaryFocused
                        ),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            androidx.compose.material3.Text("⬆", fontSize = 11.sp, color = Color.Yellow)
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
            
            if (sortedChannels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        if (selectedFilterId == "fav") {
                            androidx.compose.material3.Text("❤️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aún no tienes canales favoritos",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Explora las categorías o países y pulsa el corazón ❤️ en cualquier canal para agregarlo aquí.",
                                color = Color.LightGray,
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
                            containerColor = TvSurface,
                            focusedContainerColor = TvSurfaceFocused
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
                                if (channel.logo.isNotBlank()) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(channel.logo)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = channel.name,
                                        modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 8.dp),
                                        contentScale = ContentScale.Fit
                                    ) {
                                        val state = painter.state
                                        when (state) {
                                            is AsyncImagePainter.State.Loading -> {
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = Color.White.copy(alpha = 0.4f),
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                            }
                                            is AsyncImagePainter.State.Error -> {
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    androidx.compose.material3.Text("📺", fontSize = 32.sp)
                                                }
                                            }
                                            else -> {
                                                SubcomposeAsyncImageContent()
                                            }
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
                                    .clickable { onToggleFavorite(channel) }
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
    }
        
        if (itemToRefresh != null) {
            val (refreshId, isCountry) = itemToRefresh!!
            val itemName = if (isCountry) {
                Constants.SUPPORTED_COUNTRIES[refreshId] ?: refreshId
            } else {
                Constants.getFilterDisplayName(refreshId)
            }
            
            AlertDialog(
                onDismissRequest = { itemToRefresh = null },
                properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = false),
                title = { androidx.compose.material3.Text(stringResource(R.string.scan_channels_title), color = Color.White) },
                text = { androidx.compose.material3.Text(stringResource(R.string.scan_channels_desc, itemName), color = Color.LightGray) },
                containerColor = Color(0xFF0F172A),
                confirmButton = {
                    Button(
                        onClick = {
                            itemToRefresh = null
                            if (isCountry) {
                                onCountrySelected(refreshId, true)
                            } else {
                                onFilterSelected(refreshId, true)
                            }
                        },
                        colors = ButtonDefaults.colors(containerColor = TvPrimary)
                    ) {
                        Text(stringResource(R.string.scan), color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { itemToRefresh = null },
                        colors = ButtonDefaults.colors(containerColor = Color(0xFF1E293B))
                    ) {
                        Text(stringResource(R.string.cancel), color = Color.White)
                    }
                }
            )
        }

        if (showSupportDialog) {
            val closeFocusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                delay(100)
                try {
                    closeFocusRequester.requestFocus()
                } catch (e: Exception) {}
            }

            AlertDialog(
                onDismissRequest = { showSupportDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false
                ),
                modifier = Modifier
                    .widthIn(min = 820.dp, max = 960.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFF090D16),
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Text(
                            text = "⭐ Información & Apoyo al Proyecto",
                            color = Color.Yellow,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF111827))
                                .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) {
                            androidx.compose.material3.Text(
                                text = "❤️ ¡Muchas gracias por sus donaciones! Esta app es 100% gratuita y de libre acceso.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Columna 1: Donaciones PayPal
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF111827))
                                    .border(BorderStroke(1.dp, Color(0x28FFFFFF)), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                androidx.compose.material3.Text(
                                    text = "💳 PayPal",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                androidx.compose.material3.Text(
                                    text = "Internacional",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(118.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.qr_paypal),
                                        contentDescription = "PayPal QR",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                androidx.compose.material3.Text(
                                    text = "Donación voluntaria con tarjeta o saldo PayPal.",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.5.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                androidx.compose.material3.Text(
                                    text = "paypal.com",
                                    color = Color(0xFF60A5FA),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Columna 2: Donaciones Yape
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF111827))
                                    .border(BorderStroke(1.dp, Color(0x28FFFFFF)), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                androidx.compose.material3.Text(
                                    text = "📱 Yape",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                androidx.compose.material3.Text(
                                    text = "Perú / BCP",
                                    color = Color(0xFFC084FC),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(118.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.qr_yape),
                                        contentDescription = "Yape QR",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                androidx.compose.material3.Text(
                                    text = "Escanea directamente desde la app Yape.",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.5.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                androidx.compose.material3.Text(
                                    text = "Yape (Perú)",
                                    color = Color(0xFFC084FC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Columna 3: Visita la Web del Desarrollador
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF111827))
                                    .border(BorderStroke(1.dp, Color(0x28FFFFFF)), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                androidx.compose.material3.Text(
                                    text = "🌐 Web Oficial",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                androidx.compose.material3.Text(
                                    text = "Desarrollador",
                                    color = Color(0xFF34D399),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(118.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.qr_web),
                                        contentDescription = "Web QR",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                androidx.compose.material3.Text(
                                    text = "Novedades, reportes y contacto directo.",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.5.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                androidx.compose.material3.Text(
                                    text = "www.qwertyaserty.com",
                                    color = Color(0xFF34D399),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ℹ️ Aviso legal: Esta aplicación es un reproductor multimedia y no almacena, retransmite ni controla el contenido emitido por cada canal. Todos los enlaces provienen de fuentes públicas y abiertas en internet. El desarrollador no se responsabiliza por la programación, disponibilidad, derechos de autor o contenidos provistos por terceros.",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.5.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            onClick = { showSupportDialog = false },
                            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(20.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                            border = ClickableSurfaceDefaults.border(
                                border = Border(BorderStroke(1.dp, Color(0x44FFFFFF))),
                                focusedBorder = Border(BorderStroke(2.dp, Color.White))
                            ),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color(0xFF1E293B),
                                focusedContainerColor = TvPrimaryFocused
                            ),
                            modifier = Modifier
                                .focusRequester(closeFocusRequester)
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "Cerrar",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 30.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}


