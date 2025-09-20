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