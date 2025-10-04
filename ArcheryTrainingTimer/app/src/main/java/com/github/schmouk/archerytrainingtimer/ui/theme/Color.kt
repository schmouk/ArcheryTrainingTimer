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

package com.github.schmouk.archerytrainingtimer.ui.theme

import androidx.compose.ui.graphics.Color

val WAYellowColor = Color(0xFFFFE552) // Yellow color used in the app
val WARedColor = Color(0xFFF65058) // Red color used in the app
val WABlueColor = Color(0xFF00B4E4) // Blue color used in the app
//val WABlackColor = Color(0xFF000000)  // Notice: actually not used
val WAWhiteColor = Color(0xFFFFFFFF)

val AppBackgroundColor = Color(0xFF2F3133)
val AppTimerRowBackgroundColor = Color(0xFF111213)
val AppTextColor = WAWhiteColor
val AppDimmedTextColor = Color(0xFF313233) // Dimmed text color for less emphasis
val AppTitleColor = WAYellowColor
val AppButtonColor = Color(0xFF00B4E4)
val AppButtonDarkerColor = Color(0xFF17728B)  //AppButtonColor.copy(alpha = 0.38f)
//val AppDimmedButtonColor = Color(0xFF484A4C)  // Notice: actually not used
val AppButtonTextColor = Color(0xFF33373F)

val SelectedButtonBackgroundColor = AppTitleColor // Background for selected button
val SelectedButtonBorderColor = WARedColor

val TimerBorderColor = WAYellowColor
val TimerPreparationColor = WARedColor
val TimerRestColor = WABlueColor
val ProgressBorderColor = Color(0xFF92302C) // i.e 60% of WARedColor
val DimmedTimerBorderColor = Color(0xFF4C4418) // i.e 30% of WAYellowColor
val DimmedProgressBorderColor = Color(0xFF4A1816) // i.e 30% of WARedColor

val AppSurfaceContainerLow = Color(0xFF08090A)
val AppSurfaceContainerHigh = Color(0xFF212325)
