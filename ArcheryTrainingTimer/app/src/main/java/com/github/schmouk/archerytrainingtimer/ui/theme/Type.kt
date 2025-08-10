package com.example.archerytrainingtimer.ui.theme // Ensure this package is correct

//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Title text is often bold
        fontSize = 36.sp,             // Match the size you used in SimpleScreen
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    )

    // Add other styles if you need them, e.g., for button text if different

)
