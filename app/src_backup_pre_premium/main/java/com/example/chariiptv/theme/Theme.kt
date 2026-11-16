package com.example.chariiptv.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TvColorScheme = darkColorScheme(
    primary = TvPrimary,
    background = TvBackground,
    surface = TvSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.LightGray
)

@Composable
fun TVDROIDTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = TvColorScheme, typography = Typography, content = content)
}
