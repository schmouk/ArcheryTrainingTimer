package com.example.archerytrainingtimer.ui.theme // Ensure this package is correct

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define your single, fixed ColorScheme using the colors from Color.kt
private val FixedDarkColorScheme = ColorScheme(
    primary = AppTextColor,
    onPrimary = AppBackgroundColor,
    primaryContainer = AppButtonColor,
    onPrimaryContainer = AppButtonTextColor,

    inversePrimary = AppBackgroundColor, // Kept from original, adjust if needed

    secondary = AppTextColor,
    onSecondary = AppBackgroundColor,
    secondaryContainer = Color.Transparent, // Consider AppButtonColor or a variant if used
    onSecondaryContainer = AppTextColor,

    tertiary = AppTextColor,
    onTertiary = AppBackgroundColor,
    tertiaryContainer = Color.Transparent, // Consider AppButtonColor or a variant if used
    onTertiaryContainer = AppTextColor,

    background = AppBackgroundColor,
    onBackground = AppTextColor,

    surface = AppBackgroundColor, // Existing surface
    onSurface = AppTextColor,     // Existing onSurface

    surfaceVariant = AppButtonColor,
    onSurfaceVariant = AppTextColor,

    surfaceTint = Color.Transparent, // Often same as primary or transparent

    inverseSurface = AppTextColor,
    inverseOnSurface = AppBackgroundColor,

    error = Color.Red,
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC), // Consider deriving from AppErrorColor if defined
    onErrorContainer = Color(0xFF410E0B), // Consider deriving from AppOnErrorColor if defined

    outline = AppTextColor,
    outlineVariant = AppButtonColor, // Kept from original

    scrim = Color.Black,

    // New Material 3 Surface Container roles
    surfaceBright = AppBackgroundColor, // Example: Could be lighter than surface (not always applicable in dark themes)
    surfaceDim = AppBackgroundColor.copy(alpha = 0.8f), // Example: Could be darker/dimmer

    // For a simple theme, these can often map to your existing background/surface
    // or slight variations. If you don't have specific colors for these,
    // using AppBackgroundColor is a safe default for a flat theme.
    surfaceContainerLowest = AppSurfaceContainerLow,  // Lightest or same as background
    surfaceContainerLow = AppSurfaceContainerLow,     // Slightly more emphasis than Lowest
    surfaceContainer = AppBackgroundColor,            // Default container background (maps to old 'surface')
    surfaceContainerHigh = AppSurfaceContainerHigh,   // More emphasis than default
    surfaceContainerHighest = AppSurfaceContainerHigh // Highest emphasis before primary
)

@Composable
fun ArcheryTrainingTimerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FixedDarkColorScheme,
        typography = AppTypography, // From Type.kt
        content = content
    )
}
