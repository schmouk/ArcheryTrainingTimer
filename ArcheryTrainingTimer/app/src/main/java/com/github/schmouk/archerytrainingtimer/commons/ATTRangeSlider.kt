/*
MIT License

Copyright (c) 2026 Philippe Schmouker, ph (dot) schmouker (at) gmail (dot) com

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
import androidx.compose.material3.RangeSlider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonDarkerColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.DimmedWAYellowColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WARedColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WAYellowColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WAWhiteColor

import kotlin.math.roundToInt

/**
 * The common definition of Range Sliders for the Archery Training Timer app.
 *
 * Defines a column with a title label and the slider container.
 * The slider container is a row with the selected low and high values
 * at both sides of the range slider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ATTRangeSlider(
    labelTitle: String = stringResource(id = R.string.min_max_reps),
    labelFontSize: Float = 18f,
    labelColor: Color = AppTextColor,
    valueRange: ClosedFloatingPointRange<Float> = 3f..24f,
    lowValue: Float = 6f,
    highValue: Float = 10f,
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
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val sliderLowValue = remember { mutableFloatStateOf(lowValue) }
    val sliderHighValue = remember { mutableFloatStateOf(highValue) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = labelTitle,
            fontSize = labelFontSize.sp,
            color = if (enabled) labelColor else colors.disabledThumbColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Row(
            verticalAlignment = CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth()
        ) {
            Text(
                text = "%d / %d".format(
                    sliderLowValue.floatValue.roundToInt(),
                    sliderHighValue.floatValue.roundToInt()
                ),
                fontSize = (1.33 * labelFontSize).sp,
                color = if (enabled) labelColor else colors.disabledThumbColor,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 8.dp)
            )
            RangeSlider(
                value = sliderLowValue.floatValue..sliderHighValue.floatValue,
                colors = colors,
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                onValueChange = { range ->
                    sliderLowValue.floatValue = range.start
                    sliderHighValue.floatValue = range.endInclusive
                },
                onValueChangeFinished = onValueChangeFinished,
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * The preview function to test displays in Android Studio
 */
@Preview
@Composable
fun ATTRangeSliderPreview() {
    ATTRangeSlider()
}
