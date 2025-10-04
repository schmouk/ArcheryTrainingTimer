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
