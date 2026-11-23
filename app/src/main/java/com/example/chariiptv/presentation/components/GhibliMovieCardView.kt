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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.chariiptv.domain.GhibliData
import com.example.chariiptv.theme.*

@Composable
fun GhibliMovieCardView(
    channel: Channel,
    onClick: () -> Unit,
    onFocused: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val movieInfo = remember(channel.name) {
        GhibliData.getMovieInfo(channel.name)
    }

    val posterRequest = remember(channel.logo, context) {
        if (channel.logo.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(channel.logo)
                .size(320, 480)
                .precision(Precision.INEXACT)
                .crossfade(true)
                .error(R.drawable.default_tv)
                .fallback(R.drawable.default_tv)
                .build()
        } else null
    }

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(14.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
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
            .height(180.dp)
            .onFocusChanged { if (it.isFocused) onFocused?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Columna Izquierda: Póster de Cine Vertical en Alta Resolución
            Box(
                modifier = Modifier
                    .width(115.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (posterRequest != null) {
                    AsyncImage(
                        model = posterRequest,
                        contentDescription = movieInfo.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_tv),
                        contentDescription = movieInfo.title,
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Columna Derecha: Metadatos, Título y Sinopsis
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Fila Superior: Badges (Sin opción de favoritos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TvPrimaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🎬 PELÍCULA",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${movieInfo.year} • ${movieInfo.duration}",
                        color = TvSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Título de la Película
                Text(
                    text = movieInfo.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Director
                Text(
                    text = "Studio Ghibli • Dir. ${movieInfo.director}",
                    color = TvOnSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                // Sinopsis en Español
                Text(
                    text = movieInfo.synopsis,
                    color = TvOnSurfaceVariant.copy(alpha = 0.9f),
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // Indicador de Reproducción
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    androidx.compose.material3.Text("▶", fontSize = 9.sp, color = TvPrimaryLight)
                    Text(
                        text = "Audio Latino • Presiona OK para reproducir",
                        color = TvOutline,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
