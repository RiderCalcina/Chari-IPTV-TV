package com.example.chariiptv.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.chariiptv.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Immutable
data class MainNavItem(
    val id: String,
    val label: String,
    val icon: String,
    val isSpecial: Boolean = false
)

@Composable
private fun HeaderLiveClock() {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var currentTime by remember { mutableStateOf(formatter.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = formatter.format(Date())
            delay(10_000)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TvSurfaceContainerHigh.copy(alpha = 0.8f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "🕒",
            fontSize = 11.sp
        )
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TvTopHeader(
    mainNavItems: List<MainNavItem>,
    selectedFilterId: String,
    navRowState: LazyListState,
    selectedFilterFocusRequester: FocusRequester,
    onFilterSelected: (String) -> Unit,
    onImportantClicked: () -> Unit,
    rssNews: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(TvSurfaceContainerLowest.copy(alpha = 0.95f))
            .padding(top = 10.dp, bottom = 6.dp, start = 20.dp, end = 20.dp)
    ) {
        // Fila 1: Logo de Marca y Reloj en Vivo (Sin avatares de usuario ni telemetría falsa)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logotipo: ChariIPTV + Badge En Vivo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Chari",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "IPTV",
                        color = TvPrimaryLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Badge En Vivo
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TvSurfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TvLiveRed)
                        )
                        Text(
                            text = "EN VIVO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Lado Derecho: Reloj aislado (no recompone las pestañas cada 10 segundos)
            HeaderLiveClock()
        }

        // Fila 2: Pestañas de Navegación (Pills D-Pad)
        LazyRow(
            state = navRowState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .focusRestorer(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(mainNavItems, key = { it.id }) { item ->
                val isSelected = item.id == selectedFilterId
                val isSpecial = item.isSpecial

                Surface(
                    onClick = {
                        if (isSpecial) {
                            onImportantClicked()
                        } else {
                            onFilterSelected(item.id)
                        }
                    },
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(
                            BorderStroke(
                                1.dp,
                                if (isSpecial) Color(0x66F59E0B)
                                else if (isSelected) Color(0x662563EB)
                                else Color(0x22FFFFFF)
                            )
                        ),
                        focusedBorder = Border(
                            BorderStroke(
                                2.5.dp,
                                if (isSpecial) TvSecondary else Color.White
                            )
                        )
                    ),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSpecial) TvSecondaryContainer.copy(alpha = 0.85f)
                        else if (isSelected) TvPrimary
                        else TvSurfaceContainerHigh,
                        focusedContainerColor = if (isSpecial) TvSecondary
                        else if (isSelected) TvPrimaryFocused
                        else TvSurfaceFocused
                    ),
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .then(if (isSelected) Modifier.focusRequester(selectedFilterFocusRequester) else Modifier)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (item.icon.isNotBlank()) {
                            androidx.compose.material3.Text(
                                text = item.icon,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = item.label,
                            color = if (isSpecial) Color(0xFF3B1E00) else Color.White,
                            fontWeight = if (isSelected || isSpecial) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }

        // Fila 3: Cinta de Noticias RSS (Marquee)
        if (rssNews.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TvSurfaceContainerLowest)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TvSecondaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NOTICIAS",
                        color = Color(0xFF3B1E00),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Text(
                    text = rssNews,
                    color = TvOnSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(iterations = Int.MAX_VALUE, velocity = 28.dp),
                    maxLines = 1
                )
            }
        }
    }
}
