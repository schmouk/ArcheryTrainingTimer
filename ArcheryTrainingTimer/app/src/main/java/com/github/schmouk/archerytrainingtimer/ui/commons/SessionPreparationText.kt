package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerPreparationColor


/**
 * A small text displayed to inform that session will start in n seconds
 *
 * @param textStyle: TextStyle, the style to apply to the text
 * @param modifier: Modifier = Modifier, optional modifier for the Text composable
 */
@Composable
fun SessionPreparationText(
    preparationTime: Int,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = String.format("%s %d s",
            stringResource(id = R.string.preparation_label),
            preparationTime
        ),
        style = textStyle,
        fontStyle = FontStyle.Italic,
        color = TimerPreparationColor,
        modifier = modifier
    )
}
