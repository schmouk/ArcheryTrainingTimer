package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonTextColor


/**
 * A big Start/Stop button
 *
 * @param allSelectionsMade: Boolean, true when all session parameters have been selected
 * @param isTimerRunning: Boolean, true when the timer countdown is currently active, false otherwise
 * @param isRestMode: Boolean, true when resting mode is active, false otherwise
 * @param buttonTextStyle: TextStyle, the style to be applied to the text of the button
 * @param buttonHeight: Float, the height of the button
 * @param widthFilling: Float = 0.75f, the fraction of the available width to be occupied by the button
 * @param onButtonClick: Unit, a lambda to be called when button is clicked
 */
@Composable
fun BigStartButton(
    allSelectionsMade : Boolean,
    isTimerRunning : Boolean,
    isRestMode : Boolean,
    buttonTextStyle : TextStyle,
    buttonHeight : Float,
    widthFilling : Float = 0.75f,
    onButtonClick : () -> Unit,
) {
    Button(
        onClick = onButtonClick,
        enabled = allSelectionsMade && !isRestMode,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppButtonColor,
            contentColor = AppButtonTextColor,
            disabledContainerColor = AppButtonColor.copy(alpha = 0.5f),
            disabledContentColor = AppButtonTextColor.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth(widthFilling)
            .height(buttonHeight.dp)
            //.fillMaxHeight(heightFilling)
        ) {
        Text(
            text = stringResource(
                id = if (isTimerRunning || isRestMode) R.string.stop_button
                else R.string.start_button
            ),
            style = buttonTextStyle.copy(
                color = if (allSelectionsMade && !isRestMode) AppButtonTextColor
                else AppButtonTextColor.copy(alpha = 0.5f)
            ),
            fontSize = (18f * buttonHeight / 35f).sp
        )
    }
}


/**
 * A row containing the big Start/Stop button, centered by default.
 * This composable can be extended to include other elements in the row if needed.
 *
 * @param allSelectionsMade: Boolean, true when all session parameters have been selected
 * @param isTimerRunning: Boolean, true when the timer countdown is currently active, false otherwise
 * @param isRestMode: Boolean, true when resting mode is active, false otherwise
 * @param buttonTextStyle: TextStyle, the style to be applied to the text of the button
 * @param buttonHeight: Float, the height of the button
 * @param onButtonClick: Unit, a lambda to be called when button is clicked
 * @param modifier: Modifier, the modifier to be applied to the Row composable
 * @param rowHorizontalArrangement: Arrangement.Horizontal, the horizontal arrangement of the Row content
 */
@Composable
fun StartButtonRow(
    allSelectionsMade : Boolean,
    isTimerRunning : Boolean,
    isRestMode : Boolean,
    buttonTextStyle : TextStyle,
    buttonHeight : Float,
    onButtonClick : () -> Unit,
    modifier : Modifier,
    rowHorizontalArrangement : Arrangement.Horizontal
) {
    Row(
        modifier = modifier,
        horizontalArrangement = rowHorizontalArrangement,
    ) {
        //-- START Button --
        // Notice: default width and height filling values are used here
        BigStartButton(
            allSelectionsMade,
            isTimerRunning,
            isRestMode,
            buttonTextStyle,
            buttonHeight,
            onButtonClick = onButtonClick
        )

        // We can add other elements to this Row if needed,
        // for example, a small status icon or text next to the button.
        // If so, let's adjust Arrangement.Center or use Spacers.
    }
}