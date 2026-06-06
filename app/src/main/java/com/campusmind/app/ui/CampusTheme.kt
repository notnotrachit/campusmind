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

private val CampusColors = lightColorScheme(
  primary = Color(0xFF1E5F55),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFCFE9DE),
  onPrimaryContainer = Color(0xFF0D332E),
  secondary = Color(0xFF8A4B2D),
  secondaryContainer = Color(0xFFFFDAC8),
  tertiary = Color(0xFF465C8C),
  background = Color(0xFFF6F2EA),
  surface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xFFECE5D8),
  outline = Color(0xFF77746D),
  error = Color(0xFFB3261E),
)

private val CampusShapes = Shapes(
  extraSmall = RoundedCornerShape(6.dp),
  small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(8.dp),
  large = RoundedCornerShape(8.dp),
  extraLarge = RoundedCornerShape(8.dp),
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
