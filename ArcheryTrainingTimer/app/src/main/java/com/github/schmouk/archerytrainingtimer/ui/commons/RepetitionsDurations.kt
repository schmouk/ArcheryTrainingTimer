package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.AppBackgroundColor
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
                    if (isSelected) SelectedButtonBorderColor else AppBackgroundColor
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