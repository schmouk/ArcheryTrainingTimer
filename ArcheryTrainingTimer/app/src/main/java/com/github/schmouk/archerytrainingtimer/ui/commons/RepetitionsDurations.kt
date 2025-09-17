package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.AppBackgroundColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonDarkerColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.SelectedButtonBackgroundColor
import com.github.schmouk.archerytrainingtimer.ui.theme.SelectedButtonBorderColor

/**
 * A small text displayed as title for the Repetitions Duration selection.
 *
 * @param modifier: Modifier = Modifier, optional modifier for the Text composable
 * @param textStyle: TextStyle, the style to apply to the text
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
    durationButtonWidth : Dp,
    durationsTextScaling : Float,
    horizontalArrangement : Arrangement.Horizontal,
    rowModifier : Modifier = Modifier
) {
    Row( // Duration Buttons Row
        horizontalArrangement = horizontalArrangement,
        modifier = rowModifier
    ) {
        var selectedDurationString = selectedDurationString

        Row() {
            durationOptions.forEach { durationString ->
                val isSelected = selectedDurationString == durationString
                Button(
                    onClick = {
                        if (!isSelected)
                            onDurationSelected(durationString)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) SelectedButtonBackgroundColor
                                         else AppButtonDarkerColor,
                        contentColor = AppButtonTextColor
                    ),
                    border = BorderStroke(
                        borderStrokeWidth,
                        if (isSelected) SelectedButtonBorderColor else AppBackgroundColor
                    ),
                    enabled = true,
                    modifier = Modifier
                        .width(durationButtonWidth)
                ) {
                    Text(
                        text = durationString,
                        style = TextStyle(
                            fontSize = (13f * durationsTextScaling).toInt().sp,
                            color = if (isSelected) AppButtonTextColor else AppTextColor
                        )
                    )
                }
            }
        }
    }
}