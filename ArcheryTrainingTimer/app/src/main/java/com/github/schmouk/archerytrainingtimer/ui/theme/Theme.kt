/*
MIT License

Copyright (c) 2025 Philippe Schmouker, ph (dot) schmouker (at) gmail (dot) com

This file is part of Android application ArcheryTrainingTimer.

Permission is hereby granted,  free of charge,  to any person obtaining a copy
of this software and associated documentation files (the "Software"),  to deal
in the Software without restriction,  including without limitation the  rights
to use,  copy,  modify,  merge,  publish,  distribute, sublicense, and/or sell
copies of the Software,  and  to  permit  persons  to  whom  the  Software  is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS",  WITHOUT WARRANTY OF ANY  KIND,  EXPRESS  OR
IMPLIED,  INCLUDING  BUT  NOT  LIMITED  TO  THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT  SHALL  THE
AUTHORS  OR  COPYRIGHT  HOLDERS  BE  LIABLE  FOR  ANY CLAIM,  DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM,
OUT  OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.github.schmouk.archerytrainingtimer.ui.theme // Ensure this package is correct

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define our single, fixed ColorScheme using the colors from Color.kt
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
