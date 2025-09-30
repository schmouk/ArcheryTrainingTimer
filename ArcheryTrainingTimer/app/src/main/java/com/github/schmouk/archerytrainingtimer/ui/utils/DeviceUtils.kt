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

package com.github.schmouk.archerytrainingtimer.ui.utils

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo


/**
 * Returns true when the current orientation of the device should
 * lead to a PORTRAIT display UI organization, or false when this
 * orientation should lead to a LANDSCAPE display UI organization.
 * Notice: this is based on the current Window Sizes Classes, i.e.
 * COMPACT, MEDIUM or EXPANDED for width and for height sizes.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun considerDevicePortraitPositioned() : Boolean {
    val windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
    val windowSizeClass = windowAdaptiveInfo.windowSizeClass
    val widthSizeClass = windowSizeClass.windowWidthSizeClass
    val heightSizeClass = windowSizeClass.windowHeightSizeClass

    // portrait if we have a height class greater or equal to width class
    // Notice: EXPANDED > MEDIUM > COMPACT (values associated with classes
    // are directly accessed via method .hashCode())
    return heightSizeClass.hashCode() >= widthSizeClass.hashCode()
}


/**
 * The list of recognized postures for a folding device.
 * These values are based on the current Android Jetpack WindowManager
 */
enum class EFoldedPosture {
    POSTURE_NOT_FOLDED,     // Device is not folded
    POSTURE_FLAT,           // Device is fully open flat (180 degrees)
    POSTURE_BOOK_LIKE,      // Device is half-open (90 degrees, vertical)
    POSTURE_LAPTOP_LIKE,    // Device is half-open (90 degrees, horizontal)
    //POSTURE_CLOSED,         // Device is fully closed (0 degrees)
    POSTURE_UNKNOWN         // Device posture is unknown
}

/**
 * Returns then current posture of the device when it is folded,
 * or POSTURE_NOT_FOLDED when the device is not folded.
 */
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun detectDeviceFoldedPosture(/*windowLayoutInfo: WindowLayoutInfo*/): EFoldedPosture {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    // Collect window layout info
    val windowLayoutInfo by WindowInfoTracker.getOrCreate(context)
        .windowLayoutInfo(activity)
        .collectAsStateWithLifecycle(initialValue = WindowLayoutInfo(emptyList()))

    val foldingFeatures = windowLayoutInfo.displayFeatures.filterIsInstance<FoldingFeature>()

    if (foldingFeatures.isEmpty())
        return EFoldedPosture.POSTURE_NOT_FOLDED

    val feature = foldingFeatures.first()

    return when (feature.state) {
        FoldingFeature.State.FLAT ->
            EFoldedPosture.POSTURE_FLAT

        FoldingFeature.State.HALF_OPENED ->
            when (feature.orientation) {
                FoldingFeature.Orientation.VERTICAL -> EFoldedPosture.POSTURE_BOOK_LIKE
                FoldingFeature.Orientation.HORIZONTAL -> EFoldedPosture.POSTURE_LAPTOP_LIKE
                else -> EFoldedPosture.POSTURE_UNKNOWN
            }

        else -> EFoldedPosture.POSTURE_UNKNOWN
    }
}
