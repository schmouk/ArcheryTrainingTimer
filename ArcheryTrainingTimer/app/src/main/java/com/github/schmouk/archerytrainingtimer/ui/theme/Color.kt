package com.github.schmouk.archerytrainingtimer.ui.theme

import androidx.compose.ui.graphics.Color

val WAYellowColor = Color(0xFFFFE552) // Yellow color used in the app
val WARedColor = Color(0xFFF65058) // Red color used in the app
val WABlueColor = Color(0xFF00B4E4) // Blue color used in the app
val WABlackColor = Color(0xFF000000)
val WAWhiteColor = Color(0xFFFFFFFF)

val AppBackgroundColor = Color(0xFF2F3133)
val AppTimerRowBackgroundColor = Color(0xFF111213)
val AppTextColor = WAWhiteColor
val AppDimmedTextColor = Color(0xFF313233) // Dimmed text color for less emphasis
val AppTitleColor = WAYellowColor
val AppButtonColor = Color(0xFF00B4E4)
val AppButtonDarkerColor = AppButtonColor.copy(alpha = 0.38f)
val AppDimmedButtonColor = Color(0xFF484A4C)
val AppButtonTextColor = Color(0xFF33373F)

val SelectedButtonBackgroundColor = AppTitleColor // Background for selected button
val SelectedButtonBorderColor = WARedColor

val TimerBorderColor = WAYellowColor
val TimerRestColor = WABlueColor
val ProgressBorderColor = Color(0xFF923035) // i.e 60% of WARedColor
val DimmedTimerBorderColor = Color(0xFF4B4A19) // i.e 30% of WAYellowColor
val DimmedProgressBorderColor = Color(0xFF4A1813) // i.e 30% of WARedColor

val AppSurfaceContainerLow = Color(0xFF08090A)
val AppSurfaceContainerHigh = Color(0xFF212325)
