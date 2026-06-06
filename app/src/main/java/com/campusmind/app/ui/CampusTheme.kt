package com.campusmind.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CampusColors = lightColorScheme(
  primary = Color(0xFF24584F),
  onPrimary = Color.White,
  secondary = Color(0xFF7A5132),
  tertiary = Color(0xFF4D5F88),
  background = Color(0xFFF7F7F2),
  surface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xFFE7EBE3),
  outline = Color(0xFF7B8278),
)

@Composable
fun CampusTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = CampusColors,
    content = content,
  )
}
