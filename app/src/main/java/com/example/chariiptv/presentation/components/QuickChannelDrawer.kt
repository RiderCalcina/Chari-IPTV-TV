package com.example.chariiptv.presentation.components

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.chariiptv.R
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.theme.*
import kotlinx.coroutines.delay

@Composable
fun QuickChannelDrawer(
    channels: List<Channel>,
    currentChannelUrl: String,
    categoryTitle: String,
    favorites: Set<String>,
    onChannelSelected: (Channel) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex = remember(channels, currentChannelUrl) {
        val idx = channels.indexOfFirst { it.url == currentChannelUrl }
        if (idx >= 0) idx else 0
    }

    val listState = rememberLazyListState()

    val targetFocusRequester = remember { FocusRequester() }
    val firstItemFocusRequester = remember { FocusRequester() }

    LaunchedEffect(channels, currentChannelUrl) {
        if (currentIndex > 0) {
            try {
                listState.scrollToItem(maxOf(0, currentIndex - 1))
            } catch (_: Exception) {}
        }
        for (delayMs in listOf(40L, 120L, 250L, 400L)) {
            delay(delayMs)
            try {
                targetFocusRequester.requestFocus()
                break
            } catch (_: Exception) {
                try {
                    firstItemFocusRequester.requestFocus()
                    break
                } catch (_: Exception) {}
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(340.dp)
            .shadow(elevation = 24.dp, spotColor = Color.Black)
            .background(TvBackground.copy(alpha = 0.94f))
            .border(
                BorderStroke(1.dp, Color(0x33FFFFFF)),
                RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            )
            .padding(vertical = 14.dp, horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cabecera del Drawer Lateral
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TvSurfaceContainerHigh.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = categoryTitle.ifBlank { "Canales" },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TvPrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${channels.size} ch",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Guía de atajos para el control remoto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "[OK] Cambiar",
                        color = TvSecondaryLight,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "•", color = TvOutlineVariant, fontSize = 10.sp)
                    Text(
                        text = "[▶ / ATRÁS] Cerrar",
                        color = TvOnSurfaceVariant,
                        fontSize = 10.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lista Vertical de Canales
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRestorer(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(
                    items = channels,
                    key = { index, channel -> "${channel.url}_drawer_$index" }
                ) { index, channel ->
                    val isPlaying = channel.url == currentChannelUrl
                    val isFav = favorites.contains(channel.url)
                    val isTarget = index == currentIndex

                    val focusMod = when {
                        isTarget -> Modifier.focusRequester(targetFocusRequester)
                        index == 0 -> Modifier.focusRequester(firstItemFocusRequester)
                        else -> Modifier
                    }

                    Surface(
                        onClick = {
                            if (isPlaying) {
                                onClose()
                            } else {
                                onChannelSelected(channel)
                            }
                        },
                        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
                        border = ClickableSurfaceDefaults.border(
                            border = Border(
                                BorderStroke(
                                    1.dp,
                                    if (isPlaying) Color(0x662563EB) else Color(0x1AFFFFFF)
                                )
                            ),
                            focusedBorder = Border(BorderStroke(2.5.dp, Color.White))
                        ),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (isPlaying) TvSurfaceContainerHighest.copy(alpha = 0.9f)
                            else TvSurfaceContainerLow.copy(alpha = 0.8f),
                            focusedContainerColor = TvPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(focusMod)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Miniatura del Logotipo
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(TvSurfaceContainerLowest)
                                        .padding(3.dp),
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
                                            modifier = Modifier.fillMaxSize(0.7f),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                // Textos: Nombre y Estado
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = channel.name,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )

                                    if (isPlaying) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 1.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(TvLiveRed)
                                            )
                                            Text(
                                                text = "EN REPRODUCCIÓN",
                                                color = TvPrimaryLight,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Señal En Vivo",
                                            color = TvOutline,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }

                            // Estrella de Favorito
                            androidx.compose.material3.Text(
                                text = if (isFav) "⭐" else "☆",
                                fontSize = 12.sp,
                                color = if (isFav) TvSecondary else Color(0x66FFFFFF),
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
