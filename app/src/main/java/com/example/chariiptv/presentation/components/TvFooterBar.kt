package com.example.chariiptv.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.example.chariiptv.theme.*

@Composable
fun TvFooterBar(
    channelCountText: String,
    showFavoriteHint: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appVersion = remember(context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName ?: "1.2"}"
        } catch (_: Exception) {
            "v1.2"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(TvSurfaceContainerLowest.copy(alpha = 0.92f))
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Lado Izquierdo: Guía de Navegación D-Pad para Control Remoto
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tecla OK
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TvPrimary)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Reproducir",
                    color = TvOnSurfaceVariant,
                    fontSize = 11.5.sp
                )
            }

            // Tecla Mantener OK (Oculto en catálogo Ghibli)
            if (showFavoriteHint) {
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
                        Text(
                            text = "MANTENER OK",
                            color = TvSecondary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Favorito",
                        color = TvOnSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
            }

            // Tecla Atrás
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
                    Text(
                        text = "ATRÁS",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Subir a Pestañas",
                    color = TvOnSurfaceVariant,
                    fontSize = 11.5.sp
                )
            }

            if (channelCountText.isNotBlank()) {
                Text(
                    text = "•",
                    color = TvOutlineVariant,
                    fontSize = 12.sp
                )
                Text(
                    text = channelCountText,
                    color = TvOutline,
                    fontSize = 11.5.sp
                )
            }
        }

        // Lado Derecho: VERSIÓN DE LA APP (Estrictamente ubicada en la parte inferior derecha)
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
                Text(
                    text = "Chari-IPTV",
                    color = TvOnSurfaceVariant,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = appVersion,
                    color = TvPrimaryLight,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
