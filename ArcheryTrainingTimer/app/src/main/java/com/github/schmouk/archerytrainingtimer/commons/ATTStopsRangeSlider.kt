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

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WAYellowColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WARedColor
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
fun ATTStopsRangeSlider(
    modifier: Modifier = Modifier,
    labelTitle: String? = null,
    labelFontSize: Float = 18f,
    labelColor: Color = AppTextColor,
    valueRange: ClosedFloatingPointRange<Float> = 40f..250f,
    valueUnits: String = "",
    sliderValue: Float = 60f,
    stepsGap: Float = 10f,
    sliderHeight: Dp = 40.dp,
    enabled: Boolean = true,
    showStopsValues: Boolean = true,
    oneOverNStopsValues: Int = 1,
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
    onValueChange: ((Float) -> Unit)? = null,
    onValueChangeFinished : (() -> Unit)? = null,
) {
    //val sliderValueState = remember { mutableFloatStateOf(sliderValue) }
    val sliderValueState = remember(sliderValue) { mutableFloatStateOf(sliderValue) }
    val sliderHeightDp = sliderHeight.coerceIn(24.dp, 44.dp)

    LaunchedEffect(sliderValue) {
        sliderValueState.floatValue = sliderValue.coerceIn(valueRange)
    }

    val steps: Int = ((valueRange.endInclusive - valueRange.start) / stepsGap - 1f)
                        .roundToInt()
                        .coerceAtLeast(0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // --- The label title for this steps slider ---
        if (labelTitle != null) {
            Text(
                text = labelTitle,
                fontSize = labelFontSize.sp,
                color = if (enabled) labelColor else colors.disabledThumbColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // --- The slider container ---
        Row(
            //verticalAlignment = Alignment.CenterVertically, //Alignment.Bottom,
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth()
        ) {
            // Align the selected value to the bottom of the slider track instead of the row center.
            val textFontSize = 1.1f * labelFontSize
            Text(
                text = "%3d%s".format(sliderValueState.floatValue.roundToInt(), valueUnits),
                fontSize = textFontSize.sp,
                color = if (enabled) labelColor else colors.disabledThumbColor,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(
                        end = 8.dp,
                        top = (sliderHeight - textFontSize.dp - 3.dp) / 2f
                    )
            )

            // The slider then, with maybe its stops labels
            Box(modifier = modifier.fillMaxWidth()) {
                // The slider first
                Slider(
                    value = sliderValueState.floatValue,
                    colors = colors,
                    enabled = enabled,
                    valueRange = valueRange,
                    steps = steps,
                    onValueChange = {
                        val newSliderValue: Float = it.coerceIn(
                            valueRange.start+stepsGap..valueRange.endInclusive-stepsGap
                        )
                        sliderValueState.floatValue = newSliderValue
                        onValueChange?.invoke(newSliderValue)
                    },
                    onValueChangeFinished = onValueChangeFinished,
                    modifier = Modifier
                        .height(sliderHeightDp)
                        .fillMaxWidth()
                )

                // Then the maybe spots labels
                if (showStopsValues) {
                    val stopsLabels = mutableListOf<String>()
                    val stepSize = stepsGap * oneOverNStopsValues.toFloat()
                    var currentValue = valueRange.start
                    while (currentValue <= valueRange.endInclusive) {
                        if (currentValue == valueRange.start ||
                            currentValue == valueRange.endInclusive)
                            stopsLabels += ""
                        else
                            stopsLabels.add("%3d".format(currentValue.roundToInt()))

                        currentValue += stepSize
                    }

                    DisplaySpotsLabels(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = sliderHeightDp / 2f + 6.dp),
                        labels = stopsLabels
                    )
                }
            }
        }
    }
}


/**
 * The stops labels displayed in the slider
 */
@Composable
private fun DisplaySpotsLabels(
    modifier: Modifier = Modifier,
    labels: List<String>,
    labelFontSize: Float = 9f
) {
    Layout(
        modifier = modifier,
        content = {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = labelFontSize.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val thumbPx = 24.dp.roundToPx()

        // The track spans from thumbPx to (width - thumbPx)
        val trackWidth = (constraints.maxWidth - thumbPx).coerceAtLeast(0)
        val last = (placeables.size - 1).coerceAtLeast(1)

        layout(constraints.maxWidth, placeables.maxOfOrNull { it.height } ?: 0) {
            placeables.forEachIndexed { i, p ->
                val centerX = thumbPx / 2f + trackWidth * i / last
                p.placeRelative(
                    x = (centerX - p.width / 2f).roundToInt(),
                    y = 0
                )
            }
        }
    }
}


/**
 * The preview function to test displays in Android Studio
 */
@Preview
@Composable
fun ATTStopsRangeSliderPreview() {
    ATTStopsRangeSlider()
}
