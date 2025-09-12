package com.github.schmouk.archerytrainingtimer.ui.utils

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass


/**
 * Returns true when the device current orientation should lead
 * to a PORTRAIT display UI organization, or false when this
 * orientation should lead to a LANDSCAPE display UI organization.
 * Notice: this is based on the current Window Size Classes.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun isPortraitPositioned( windowAdaptiveInfo: WindowAdaptiveInfo) : Boolean {
    val windowSizeClass = windowAdaptiveInfo.windowSizeClass
    val widthSizeClass = windowSizeClass.windowWidthSizeClass
    val heightSizeClass = windowSizeClass.windowHeightSizeClass

    // portrait if we have a height class greater or equal to width class
    // Notice: EXPANDED > MEDIUM > COMPACT - values are directly accessed via method .hashCode()
    return heightSizeClass.hashCode() >= widthSizeClass.hashCode()
}
