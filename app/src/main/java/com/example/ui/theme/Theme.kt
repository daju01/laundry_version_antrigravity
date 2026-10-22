package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Unified Clean & Colorful Daylight Theme across all Tycoon 3D and Business ERP screens
private val UnifiedColorfulScheme = lightColorScheme(
    primary = OceanBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SkyTintBg,
    onPrimaryContainer = OceanBlueDeep,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = TealTintBg,
    onSecondaryContainer = Color(0xFF0F766E),
    tertiary = BerryLavender,
    onTertiary = Color.White,
    tertiaryContainer = LavenderTintBg,
    onTertiaryContainer = BerryLavenderDark,
    background = AppCanvasBg,
    surface = CardSurfaceWhite,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = CardBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = UnifiedColorfulScheme,
        typography = Typography,
        content = content
    )
}
