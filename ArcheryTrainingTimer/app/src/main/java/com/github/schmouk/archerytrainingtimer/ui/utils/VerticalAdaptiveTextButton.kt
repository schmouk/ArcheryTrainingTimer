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
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp


/**
 * A Modifier extension to adapt the "visual middle" of a text to the actual middle
 * of its bounding box. This may be useful when the text contains characters that
 * have parts extending below the baseline, such as 'g', 'j', 'p', 'q', 'y', etc.
 */
fun Modifier.verticallyCenterOnBaseline(
    fontSizePx: Float
) = this.layout { measurable, constraints ->
    // Measure the text with the given constraints.
    val placeable = measurable.measure(constraints)

    // Get both the first and last baseline positions.
    val firstBaseline = placeable[FirstBaseline]
    val lastBaseline = placeable[LastBaseline]

    if (firstBaseline != null && lastBaseline != null) {
        // The visual center of the text is the midpoint between its top-most
        // baseline and its bottom-most baseline.
        val textVisualCenter = (firstBaseline + lastBaseline) / 2

        // The center of the available space (the button's height).
        val containerCenter = constraints.maxHeight / 2

        // Calculate the final 'y' position for the text's top edge.
        // To align the centers, we place the text's top edge at:
        // (containerCenter) - (the offset of the text's visual center from its top).
        val yPosition = containerCenter - textVisualCenter
        val yFinalOffset = (fontSizePx / 2).toInt() -2

        // Set the layout size and place the text at the calculated position.
        layout(placeable.width, constraints.maxHeight) {
            placeable.placeRelative(x = 0, y = yPosition + yFinalOffset)
        }
    } else {
        // Fallback for composables without a baseline or single-line text where
        // lastBaseline might be the same as firstBaseline.
        // The default centering is a reasonable fallback.
        layout(placeable.width, constraints.maxHeight) {
            val yPosition = (constraints.maxHeight - placeable.height) / 2
            placeable.placeRelative(x = 0, y = yPosition)
        }
    }
}


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
    maxLines: Int = 1,  // Explicitly sets the maximum lines to 1
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
        val fontSizeSp = (forcedButtonHeightDp * textProportionalHeight).value.sp
        val fontSizePx = with(LocalDensity.current) { fontSizeSp.toPx() }

        Text(
            text = text,
            modifier = textModifier
                //.fillMaxHeight() // Takes full height of button
                .verticallyCenterOnBaseline(fontSizePx)
                //.align(Alignment.Center)
            //.wrapContentHeight(Alignment.CenterVertically) // Centers vertically
            ,
            color = textColor,
            fontSize = fontSizeSp,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            textAlign = textAlign,
            softWrap = softWrap,
            maxLines = maxLines,
            style = textStyle
        )
    }
}
