package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.github.schmouk.archerytrainingtimer.ui.theme.AppTitleColor

/**
 * The header title to be displayed at top of views
 *
 * @param viewTitleText: String, the text of this title
 * @param scaleFactor: Float, the scaling factor to be applied to the text size - depends on the device resolution
 */
@Composable
fun ViewHeader(
    viewTitleText : String,
    modifier : Modifier
) {
    Text(
        text = viewTitleText,
        style = MaterialTheme.typography.titleLarge,
        color = AppTitleColor,
        modifier = modifier
    )
}