package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldYellow,
    secondary = GoldYellow,
    background = TrueBlack,
    surface = DarkGrey,
    surfaceVariant = CardBackground,
    onPrimary = TrueBlack,
    onSecondary = TrueBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextPrimary,
  )

@Composable
fun TrackVerseTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = {
      androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
      ) {
        content()
      }
    }
  )
}
