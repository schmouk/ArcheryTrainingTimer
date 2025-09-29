package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle

import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTitleColor


/**
 * A small text displayed for asking to select parameters for a session,
 * when not all selections have been made yet. Not shown when all selections
 * have been made.
 *
 * @param modifier: Modifier = Modifier, optional modifier for the Text composable
 * @param allSelectionsMade: Boolean, true if all selections for the session have been made
 * @param textStyle: TextStyle, the style to apply to the text
 */
@Composable
fun PleaseSelectText(
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Text( // Select Session parameters Row
        text = stringResource(id = R.string.please_select),
        style = textStyle,
        fontStyle = FontStyle.Italic,
        color = AppTitleColor,
        modifier = modifier
    )
}