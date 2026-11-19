package com.example.chariiptv.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.size.Precision
import com.example.chariiptv.R
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.theme.*

@Composable
fun ChannelCardView(
    channel: Channel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logoRequest = remember(channel.logo, context) {
        if (channel.logo.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(channel.logo)
                .size(220, 150)
                .precision(Precision.INEXACT)
                .crossfade(false)
                .error(R.drawable.default_tv)
                .fallback(R.drawable.default_tv)
                .build()
        } else null
    }

    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, Color(0x33FFFFFF))),
            focusedBorder = Border(BorderStroke(3.5.dp, Color.White))
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = TvSurface,
            focusedContainerColor = TvPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 11f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo con degradado sutil hacia abajo para destacar los textos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Fila Superior de la Tarjeta: Badge EN VIVO + País (si existe)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Badge En Vivo
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TvLiveRedContainer)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
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
                                text = "EN VIVO",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Botón Estrella Favorito
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isFavorite) Color(0x44F59E0B) else Color(0x33000000))
                            .clickable { onToggleFavorite() }
                            .padding(4.dp)
                    ) {
                        androidx.compose.material3.Text(
                            text = if (isFavorite) "⭐" else "☆",
                            fontSize = 12.sp,
                            color = if (isFavorite) TvSecondary else Color.LightGray
                        )
                    }
                }

                // Centro: Logotipo del canal con contenedor estilizado
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoRequest != null) {
                        AsyncImage(
                            model = logoRequest,
                            contentDescription = channel.name,
                            modifier = Modifier
                                .fillMaxHeight(0.85f)
                                .fillMaxWidth(0.85f),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_tv),
                            contentDescription = channel.name,
                            modifier = Modifier
                                .fillMaxHeight(0.7f)
                                .fillMaxWidth(0.7f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Parte Inferior: Nombre del canal centrado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TvSurfaceContainerLowest.copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
