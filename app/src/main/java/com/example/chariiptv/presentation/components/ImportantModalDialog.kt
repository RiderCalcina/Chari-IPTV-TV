package com.example.chariiptv.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.chariiptv.R
import com.example.chariiptv.theme.*
import kotlinx.coroutines.delay

@Composable
fun ImportantModalDialog(
    onDismiss: () -> Unit
) {
    val closeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(120)
        try {
            closeFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .widthIn(min = 820.dp, max = 960.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        containerColor = TvBackground,
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = "⭐ Información & Apoyo al Proyecto",
                    color = TvSecondary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TvSurfaceContainerLow)
                        .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "❤️ ¡Muchas gracias por sus donaciones! Esta app es 100% gratuita y de libre acceso.",
                        color = TvOnSurface,
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
                            .background(TvSurfaceContainerLow)
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
                            .background(TvSurfaceContainerLow)
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

                    // Columna 3: Web del Desarrollador
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(TvSurfaceContainerLow)
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
                        .background(TvSurfaceContainerLowest)
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
                    onClick = onDismiss,
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(20.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(BorderStroke(1.dp, Color(0x44FFFFFF))),
                        focusedBorder = Border(BorderStroke(2.dp, Color.White))
                    ),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = TvSurface,
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
