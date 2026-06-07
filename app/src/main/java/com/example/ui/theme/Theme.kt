package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkAccentColor,
    secondary = DarkBgSecondary,
    tertiary = DarkInkSoft,
    background = DarkBgMain,
    surface = Color(0xFF222433),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = DarkInkPrimary,
    onSurface = DarkInkPrimary,
    surfaceVariant = Color(0xFF2E3147),
    onSurfaceVariant = DarkInkSoft,
    outline = DarkBorderColor
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AccentColor,                // --accent
    secondary = BgSecondary,              // --bg-secondary
    tertiary = InkPrimary,                // --ink
    background = BgMain,                  // --bg-main
    surface = Color.White,                // --white
    onPrimary = Color.White,
    onSecondary = InkPrimary,
    onBackground = InkPrimary,
    onSurface = InkPrimary,
    surfaceVariant = BgSecondary,
    onSurfaceVariant = InkSoft,           // --ink-soft
    outline = BorderColor                 // --border
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Custom brand colors prioritized
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
