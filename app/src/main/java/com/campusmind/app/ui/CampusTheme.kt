package com.campusmind.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusmind.app.R

// Palette resolved from the Google Stitch "CampusMind" design system (Material 3, warm academic).
private val CampusColors = lightColorScheme(
  primary = Color(0xFF00473E),
  onPrimary = Color.White,
  primaryContainer = Color(0xFF1E5F55),
  onPrimaryContainer = Color(0xFF98D6CA),
  secondary = Color(0xFF8D4D2F),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFDAA85),
  onSecondaryContainer = Color(0xFF783D20),
  tertiary = Color(0xFF263D6B),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF3E5484),
  onTertiaryContainer = Color(0xFFB4C9FF),
  background = Color(0xFFF6F2EA),
  onBackground = Color(0xFF1D1C16),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF1D1C16),
  surfaceVariant = Color(0xFFE7E2D9),
  onSurfaceVariant = Color(0xFF3F4946),
  outline = Color(0xFF6F7976),
  outlineVariant = Color(0xFFBFC9C5),
  error = Color(0xFFBA1A1A),
  onError = Color.White,
  errorContainer = Color(0xFFFFDAD6),
  onErrorContainer = Color(0xFF93000A),
)

// Extra Stitch tokens that have no direct slot in Material 3 ColorScheme.
object CampusPalette {
  val surfaceContainer = Color(0xFFF2EDE5)
  val surfaceContainerHigh = Color(0xFFEDE8DF)
  val secondaryFixed = Color(0xFFFFDBCD)
  val onSecondaryFixed = Color(0xFF350F00)
  val onSecondaryFixedVariant = Color(0xFF70371A)
}

private val CampusShapes = Shapes(
  extraSmall = RoundedCornerShape(6.dp),
  small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(8.dp),
  large = RoundedCornerShape(12.dp),
  extraLarge = RoundedCornerShape(12.dp),
)

private val Poppins = FontFamily(
  Font(R.font.poppins_regular, FontWeight.Normal),
  Font(R.font.poppins_medium, FontWeight.Medium),
  Font(R.font.poppins_semibold, FontWeight.SemiBold),
  Font(R.font.poppins_bold, FontWeight.Bold),
)

// Apply Poppins uniformly across every text style so all screens share one font.
private val CampusTypography = Typography().run {
  copy(
    displayLarge = displayLarge.copy(fontFamily = Poppins),
    displayMedium = displayMedium.copy(fontFamily = Poppins),
    displaySmall = displaySmall.copy(fontFamily = Poppins),
    headlineLarge = headlineLarge.copy(fontFamily = Poppins),
    headlineMedium = headlineMedium.copy(fontFamily = Poppins),
    headlineSmall = headlineSmall.copy(fontFamily = Poppins),
    titleLarge = titleLarge.copy(fontFamily = Poppins),
    titleMedium = titleMedium.copy(fontFamily = Poppins),
    titleSmall = titleSmall.copy(fontFamily = Poppins),
    bodyLarge = bodyLarge.copy(fontFamily = Poppins),
    bodyMedium = bodyMedium.copy(fontFamily = Poppins),
    bodySmall = bodySmall.copy(fontFamily = Poppins),
    labelLarge = labelLarge.copy(fontFamily = Poppins),
    labelMedium = labelMedium.copy(fontFamily = Poppins),
    labelSmall = labelSmall.copy(fontFamily = Poppins),
  )
}

@Composable
fun CampusTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = CampusColors,
    typography = CampusTypography,
    shapes = CampusShapes,
    content = content,
  )
}
