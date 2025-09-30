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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonDarkerColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTitleColor


/**
 * A row with a checkbox and a text for the "Intermediate Beeps" option.
 *
 * @param intermediateBeepsChecked: Boolean?, true if the intermediate beeps option is checked, null if not applicable
 * @param allSelectionsMade: Boolean, true if all selections for the session have been made
 * @param scaleFactor: Float, the scale factor to apply to the checkbox
 * @param horizontalSpacer: Dp, the horizontal space between the checkbox and the text
 * @param textStyle: TextStyle, the style to apply to the text
 * @param verticalAlignment: Alignment.Vertical, the vertical alignment of the row content
 * @param rowModifier: Modifier, the modifier to be applied to the Row composable
 */
@Composable
fun IntermediateBeepsCheckedRow(
    intermediateBeepsChecked : Boolean?,
    allSelectionsMade : Boolean,
    scaleFactor: Float,
    horizontalSpacer : Dp,
    textStyle : TextStyle,
    verticalAlignment : Alignment.Vertical,
    modifier : Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment
    ) {
        Checkbox(
            checked = intermediateBeepsChecked ?: false,
            onCheckedChange = null,
            enabled = allSelectionsMade,
            colors = CheckboxDefaults.colors(
                checkedColor = AppTitleColor,
                uncheckedColor = AppTextColor.copy(alpha = if (allSelectionsMade) 1f else 0.5f),
                checkmarkColor = AppButtonTextColor,
                disabledCheckedColor = AppTitleColor.copy(alpha = 0.5f),
                disabledUncheckedColor = AppButtonDarkerColor
            ),
            modifier = Modifier.scale(scaleFactor)
        )
        Spacer(modifier = Modifier.width(horizontalSpacer))
        Text(
            text = stringResource(id = R.string.intermediate_beeps),
            style = textStyle,
            color = AppTextColor.copy(alpha = if (allSelectionsMade) 1f else 0.38f)
        )
    }
}
