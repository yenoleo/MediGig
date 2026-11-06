package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
  primary = MediTealPrimary,
  onPrimary = MediSurfaceWhite,
  primaryContainer = MediTealLight,
  onPrimaryContainer = MediTealOnContainer,
  secondary = MediCyanAccent,
  onSecondary = MediSurfaceWhite,
  secondaryContainer = MediCyanLight,
  onSecondaryContainer = MediTextPrimary,
  tertiary = MediMint,
  onTertiary = MediSurfaceWhite,
  background = MediBgLight,
  onBackground = MediTextPrimary,
  surface = MediSurfaceWhite,
  onSurface = MediTextPrimary,
  surfaceVariant = MediBgLight,
  onSurfaceVariant = MediTextSecondary,
  outline = MediCardBorder,
  error = MediDangerRed,
  onError = MediSurfaceWhite,
  errorContainer = MediDangerLight,
  onErrorContainer = MediDangerRed
)

private val DarkColorScheme = darkColorScheme(
  primary = MediTealPrimaryDark,
  onPrimary = MediBgDark,
  primaryContainer = MediTealDark,
  onPrimaryContainer = MediTealLight,
  secondary = MediCyanAccent,
  onSecondary = MediBgDark,
  tertiary = MediMint,
  background = MediBgDark,
  onBackground = MediTextPrimaryDark,
  surface = MediSurfaceDark,
  onSurface = MediTextPrimaryDark,
  surfaceVariant = MediCardBorderDark,
  onSurfaceVariant = MediTextSecondaryDark,
  outline = MediCardBorderDark,
  error = MediDangerRed,
  onError = MediSurfaceWhite
)

@Composable
fun MediGigTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
