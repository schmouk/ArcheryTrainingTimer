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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonDarkerColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.SelectedButtonBackgroundColor
import com.github.schmouk.archerytrainingtimer.ui.theme.SelectedButtonBorderColor
import com.github.schmouk.archerytrainingtimer.ui.utils.VerticalAdaptiveTextButton


/**
 * A small text displayed as title for the Repetitions Duration selection.
 *
 * @param textStyle: TextStyle, the style to apply to the text
 * @param modifier: Modifier = Modifier, optional modifier for the Text composable
 */
@Composable
fun RepetitionsDurationTitle(
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Text( // Repetitions duration title
        text = stringResource(id = R.string.repetitions_duration_label),
        style = textStyle,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}


/**
 * The row of buttons for selecting the duration of each repetition.
 */
@Composable
fun RepetitionsDurationButtons(
    selectedDurationString : String?,
    onDurationSelected: (String) -> Unit,
    durationOptions : List<String>,
    borderStrokeWidth : Dp,
    durationButtonHeight : Dp,
    durationsTextStyle : TextStyle,
    horizontalArrangement : Arrangement.Horizontal,
    modifier : Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
    ) {
        var selectedDurationString = selectedDurationString

        durationOptions.forEach { durationString ->
            val isSelected = selectedDurationString == durationString

            VerticalAdaptiveTextButton(
                onClick = {
                    if (!isSelected)
                        onDurationSelected(durationString)
                },
                buttonModifier = Modifier.padding(borderStrokeWidth),
                enabled = true,
                buttonContainerColor =
                    if (isSelected) SelectedButtonBackgroundColor
                    else AppButtonDarkerColor,
                border = BorderStroke(
                    borderStrokeWidth,
                    if (isSelected) SelectedButtonBorderColor else AppButtonDarkerColor //AppBackgroundColor
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                forcedButtonHeightDp = durationButtonHeight,
                text = durationString,
                textColor = if (isSelected) AppButtonTextColor else AppTextColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textProportionalHeight = 0.33f,
                softWrap = false, // Prevents the text from wrapping to the next line
                maxLines = 1,     // Explicitly sets the maximum lines to 1
                textModifier = Modifier.wrapContentWidth() // Adapts width to text content
            )
        }
    }
}
