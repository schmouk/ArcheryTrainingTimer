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

package com.github.schmouk.archerytrainingtimer.commons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.schmouk.archerytrainingtimer.R

import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WAYellowColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WARedColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WABlueColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WAWhiteColor
import com.github.schmouk.archerytrainingtimer.ui.theme.DimmedWAYellowColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonDarkerColor

import kotlin.math.roundToInt

/**
 * The common definition of Steps Sliders for the Archery Training Timer app
 *
 * Defines a column with a title label and the slider  container.
 * The slider container is a row with the selected value at start
 * side of the row and the slider filling the row until its  end.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ATTStepsSlider(
    labelTitle: String = stringResource(id = R.string.approx_total_count),
    labelFontSize: Float = 18f,
    valueRange: ClosedFloatingPointRange<Float> = 40f..250f,
    steps: Int = 20,
    enabled: Boolean = true,
    colors: SliderColors = SliderColors(
        thumbColor = WARedColor,
        activeTrackColor = WAYellowColor,
        activeTickColor = DimmedWAYellowColor,
        inactiveTrackColor = AppButtonDarkerColor,
        inactiveTickColor = WAWhiteColor,
        disabledThumbColor = Color.Gray,
        disabledActiveTrackColor = Color.Gray,
        disabledActiveTickColor = Color.Gray,
        disabledInactiveTrackColor = Color.LightGray,
        disabledInactiveTickColor = Color.DarkGray,
    ),
    onValueChangeFinished : (() -> Unit)? = null,
) {
    val sliderValue = remember { mutableFloatStateOf(60f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // --- The label title for this steps slider ---
        Text(
            text = labelTitle,
            fontSize = labelFontSize.sp,
            color = if (enabled) WABlueColor else colors.disabledThumbColor,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // --- The slider container ---
        Row(
            verticalAlignment = CenterVertically,
            modifier = Modifier.padding(8.dp).fillMaxWidth()
        ) {
            Text(
                //text = "%.0f".format(sliderValue.floatValue),
                text = "%3d".format(sliderValue.floatValue.roundToInt()),
                fontSize = (1.33 * labelFontSize).sp,
                color = if (enabled) AppTextColor else colors.disabledThumbColor,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 8.dp)
            )
            Slider(
                value = sliderValue.floatValue,
                colors = colors,
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                onValueChange = { sliderValue.floatValue = it },
                onValueChangeFinished = onValueChangeFinished,
                modifier = Modifier
                    .height(44.dp)
                    .weight(1f, fill = true)
                    .fillMaxWidth()
            )
        }
    }
}


/**
 * The preview function to test displays in Android Studio
 */
/*@Preview
@Composable
fun ATTStepsSliderPreview(
    labelTitle: String = stringResource(id = R.string.approx_total_count),
    labelFontSize: Float = 18f,
    valueRange: ClosedFloatingPointRange<Float> = 40f..250f,
    steps: Int = 20,
    enabled: Boolean = true,
    colors: SliderColors = SliderColors(
        thumbColor = WARedColor,
        activeTrackColor = WAYellowColor,
        activeTickColor = DimmedWAYellowColor,
        inactiveTrackColor = AppButtonDarkerColor,
        inactiveTickColor = WAWhiteColor,
        disabledThumbColor = Color.Gray,
        disabledActiveTrackColor = Color.Gray,
        disabledActiveTickColor = Color.Gray,
        disabledInactiveTrackColor = Color.LightGray,
        disabledInactiveTickColor = Color.DarkGray,
    ),
    onValueChangeFinished : (() -> Unit)? = null,
) {
    ATTStepsSlider(
        labelTitle,
        labelFontSize,
        valueRange,
        steps,
        enabled,
        colors,
        onValueChangeFinished
    )
}*/
