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

package com.github.schmouk.archerytrainingtimer.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp


/**
 * A Button that embeds an adaptive text. The font size of the
 * embedded text adapts itself to the height of the button. The
 * width of the button finally adapts itself to the width of the
 * text.
 *
 * @param text The text to display.
 * @param modifier The modifier to be applied to the layout.
 * @param style The text style to be applied to the text.
 */
@Composable
fun VerticalAdaptiveTextButton(
    onClick: () -> Unit,
    buttonModifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    buttonContainerColor: Color,
    buttonDisabledContainerColor: Color = Color.Unspecified,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    forcedButtonHeightDp: Dp,
    text: String,
    textColor: Color,
    textDisabledColor: Color = Color.Unspecified,
    textStyle: TextStyle = LocalTextStyle.current,
    fontStyle: FontStyle = FontStyle.Normal,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign = TextAlign.Center,
    textProportionalHeight: Float = 0.33f,
    softWrap: Boolean = false, // Prevents the text from wrapping to the next line
    maxLines: Int = 1,     // Explicitly sets the maximum lines to 1
    textModifier: Modifier = Modifier
) {
    textProportionalHeight.coerceIn(0.15f, 0.90f)
    maxLines.coerceAtLeast(1)

    Button(
        onClick = onClick,
        modifier = buttonModifier.height(forcedButtonHeightDp),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            buttonContainerColor,
            textColor,
            buttonDisabledContainerColor,
            textDisabledColor
        ),
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            modifier = textModifier
                .fillMaxHeight() // Takes full height of button
                .wrapContentHeight(Alignment.CenterVertically) // Centers vertically
            ,
            color = textColor,
            fontSize = (forcedButtonHeightDp * textProportionalHeight).value.sp,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            textAlign = textAlign,
            softWrap = softWrap,
            maxLines = maxLines,
            style = textStyle
        )
    }
}
