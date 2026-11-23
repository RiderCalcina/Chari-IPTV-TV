package com.example.chariiptv.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    val dialogFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(120)
        try {
            dialogFocusRequester.requestFocus()
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .focusRequester(dialogFocusRequester)
            .focusable(),
        shape = RoundedCornerShape(20.dp),
        containerColor = TvBackground,
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TvSurfaceContainerLow)
                    .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "¡Muchas gracias por sus donaciones! Esta app es 100% gratuita y de libre acceso.",
                    color = TvOnSurface,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Columna 1: Donaciones PayPal
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TvSurfaceContainerLow)
                            .border(BorderStroke(1.dp, Color(0x28FFFFFF)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Text(
                            text = "💳 PayPal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        androidx.compose.material3.Text(
                            text = "Internacional",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.qr_paypal),
                                contentDescription = "PayPal QR",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.Text(
                            text = "Donación voluntaria con tarjeta o saldo PayPal.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 9.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        androidx.compose.material3.Text(
                            text = "paypal.com",
                            color = Color(0xFF60A5FA),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Columna 2: Donaciones Yape
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TvSurfaceContainerLow)
                            .border(BorderStroke(1.dp, Color(0x28FFFFFF)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Text(
                            text = "📱 Yape",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        androidx.compose.material3.Text(
                            text = "Perú / BCP",
                            color = Color(0xFFC084FC),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.qr_yape),
                                contentDescription = "Yape QR",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.Text(
                            text = "Escanea directamente desde la app Yape.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 9.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        androidx.compose.material3.Text(
                            text = "Yape (Perú)",
                            color = Color(0xFFC084FC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Columna 3: Web del Desarrollador
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TvSurfaceContainerLow)
                            .border(BorderStroke(1.dp, Color(0x28FFFFFF)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Text(
                            text = "🌐 Web Oficial",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        androidx.compose.material3.Text(
                            text = "Desarrollador",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.qr_web),
                                contentDescription = "Web QR",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.Text(
                            text = "Novedades, reportes y contacto directo.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 9.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        androidx.compose.material3.Text(
                            text = "www.qwertyaserty.com",
                            color = Color(0xFF34D399),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TvSurfaceContainerLowest)
                        .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "⚖️ Aviso Legal, Propiedad Intelectual y Exención de Responsabilidad:",
                            color = TvSecondary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Naturaleza del Software: Chari-IPTV es un reproductor multimedia cliente de libre acceso. No almacena, gestiona, transmite ni retransmite señales o contenidos en servidores propios.\n" +
                                   "• Señales en Vivo (IPTV): Los enlaces provienen íntegramente de repositorios e índices públicos abiertos en internet (iptv-org, TDTChannels). El desarrollador no controla la estabilidad ni emisión de las señales.\n" +
                                   "• Catálogo Ghibli: El apartado VOD indexa enlaces de preservación cultural pública alojados externamente en Internet Archive (archive.org). Ningún archivo de video reside en la app.\n" +
                                   "• Derechos de Autor y Marcas: Todas las marcas, logotipos, nombres y producciones pertenecen a Studio Ghibli Inc. (Hayao Miyazaki, Isao Takahata) y a sus respectivas cadenas televisivas.\n" +
                                   "• Disponibilidad: El autor no se responsabiliza por restricciones geográficas, servidores fuera de línea o cambios en enlaces de terceros.",
                            color = Color(0xFF94A3B8),
                            fontSize = 8.5.sp,
                            lineHeight = 11.5.sp,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TvSurfaceContainerHigh)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ATRÁS",
                            color = TvSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Presiona Atrás en el control remoto para cerrar",
                        color = TvOnSurfaceVariant,
                        fontSize = 10.5.sp
                    )
                }
            }
        },
        confirmButton = {}
    )
}
