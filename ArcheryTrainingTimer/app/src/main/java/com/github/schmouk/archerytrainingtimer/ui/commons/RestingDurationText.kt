package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerRestColor


/**
 * A small text displayed for showing the resting time evaluated
 * from repetitions durations and number of repetitions per series.
 * Only shown when number of series is greater than 1
 *
 * @param restingTime: Int, the evaluated resting time to show
 * @param restingRatio: Float, the evaluated ratio of resting time to show
 * @param numberOfSeries: Int?, the number of series selected for the session
 * @param textStyle: TextStyle, the style to apply to the text
 * @param modifier: Modifier = Modifier, optional modifier for the Text composable
 */
@Composable
fun RestingDurationText(
    restingTime: Int,
    restingRatio: Float,
    numberOfSeries: Int?,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Text( // Select Session parameters Row
        text = String.format("%s %d s (%d%%)",
            stringResource(id = R.string.rest_time),
            restingTime,
            (restingRatio * 100f).toInt()
            ),
        style = textStyle,
        fontStyle = FontStyle.Italic,
        color = TimerRestColor.copy(alpha = if ((numberOfSeries ?: 1) == 1) 0f else 1f),
        modifier = modifier
    )
}