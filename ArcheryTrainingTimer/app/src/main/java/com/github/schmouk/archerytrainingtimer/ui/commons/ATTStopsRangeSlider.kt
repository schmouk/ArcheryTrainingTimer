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

package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonDarkerColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.DimmedWAYellowColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WARedColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WAYellowColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WAWhiteColor

import kotlin.math.roundToInt

private fun normalizeRangeValues(
    lowValue: Float,
    highValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    stepsGap: Float
): Pair<Float, Float> {
    var normalizedLow = lowValue
    var normalizedHigh = highValue

    if (normalizedLow > normalizedHigh) {
        val tmp = normalizedLow
        normalizedLow = normalizedHigh
        normalizedHigh = tmp
    }

    if (normalizedHigh - normalizedLow < stepsGap) {
        normalizedHigh = normalizedLow + stepsGap
    }

    normalizedLow = normalizedLow.coerceIn(valueRange)
    normalizedHigh = normalizedHigh.coerceIn(valueRange)

    if (normalizedHigh - normalizedLow < stepsGap) {
        normalizedLow = normalizedHigh - stepsGap
    }

    return normalizedLow to normalizedHigh
}

/**
 * The common definition of Range Sliders for the Archery Training Timer app.
 *
 * Defines a column with a title label and the slider container.
 * The slider container is a row with the selected low and high values
 * at the start side of the range slider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ATTStopsRangeSlider(
    modifier: Modifier = Modifier,
    labelTitle: String? = null,
    labelFontSize: Float = 18f,
    labelColor: Color = AppTextColor,
    valueRange: ClosedFloatingPointRange<Float> = 50f..250f,
    valueUnits: String = "",
    sliderLowValue: Float = 60f,
    sliderHighValue: Float = 100f,
    stepsGap: Float = 10f,
    sliderHeight: Dp = 40.dp,
    enabled: Boolean = true,
    showStopsValues: Boolean = true,
    showStopsLabelsEveryNMultiple: Int = 50,
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
    onValueChange: ((ClosedFloatingPointRange<Float>) -> Unit)? = null,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val initialRange = normalizeRangeValues(sliderLowValue, sliderHighValue, valueRange, stepsGap)

    val sliderLowValueState = remember(sliderLowValue, sliderHighValue) {
        mutableFloatStateOf(initialRange.first)
    }
    val sliderHighValueState = remember(sliderLowValue, sliderHighValue) {
        mutableFloatStateOf(initialRange.second)
    }

    LaunchedEffect(sliderLowValue, sliderHighValue) {
        val safeRange = normalizeRangeValues(sliderLowValue, sliderHighValue, valueRange, stepsGap)
        sliderLowValueState.floatValue = safeRange.first
        sliderHighValueState.floatValue = safeRange.second
    }

    val actualValueRange = valueRange.start - stepsGap..valueRange.endInclusive + stepsGap
    val steps: Int = ((actualValueRange.endInclusive - actualValueRange.start) / stepsGap - 1f)
        .roundToInt()
        .coerceAtLeast(0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        if (labelTitle != null) {
            Text(
                text = labelTitle,
                fontSize = labelFontSize.sp,
                color = if (enabled) labelColor else colors.disabledThumbColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth()
        ) {
            val textFontSize = 1.1f * labelFontSize
            Text(
                text = "%3d%s / %3d%s".format(
                    sliderLowValueState.floatValue.roundToInt(),
                    valueUnits,
                    sliderHighValueState.floatValue.roundToInt(),
                    valueUnits
                ),
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

            Box(modifier = Modifier.fillMaxWidth()) {
                RangeSlider(
                    value = sliderLowValueState.floatValue..sliderHighValueState.floatValue,
                    colors = colors,
                    enabled = enabled,
                    valueRange = actualValueRange,
                    steps = steps,
                    onValueChange = { range ->
                        val safeRange = normalizeRangeValues(
                            range.start.coerceIn(actualValueRange.start + stepsGap..actualValueRange.endInclusive - stepsGap),
                            range.endInclusive.coerceIn(actualValueRange.start + stepsGap..actualValueRange.endInclusive - stepsGap),
                            valueRange,
                            stepsGap
                        )

                        sliderLowValueState.floatValue = safeRange.first
                        sliderHighValueState.floatValue = safeRange.second

                        onValueChange?.invoke(safeRange.first..safeRange.second)
                    },
                    onValueChangeFinished = onValueChangeFinished,
                    modifier = Modifier
                        .height(sliderHeight.coerceIn(24.dp, 44.dp))
                        .fillMaxWidth()
                )

                if (showStopsValues) {
                    val stopsLabels = mutableListOf<String>()
                    var currentValue = actualValueRange.start + stepsGap
                    var currentIndex = 0
                    val highIndex: Int = ((actualValueRange.endInclusive - actualValueRange.start) / stepsGap).roundToInt() - 2

                    stopsLabels += ""
                    while (currentValue < actualValueRange.endInclusive) {
                        val stopValue: Int = currentValue.roundToInt()
                        if (stopValue % showStopsLabelsEveryNMultiple == 0 ||
                            currentIndex == 0 ||
                            currentIndex == highIndex
                        ) {
                            stopsLabels.add("%d".format(stopValue))
                        } else {
                            stopsLabels += ""
                        }
                        currentIndex += 1
                        currentValue = actualValueRange.start + (currentIndex + 1) * stepsGap
                    }
                    stopsLabels += ""

                    DisplaySpotsLabels(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = sliderHeight.coerceIn(24.dp, 44.dp) / 2f + 6.dp),
                        labels = stopsLabels
                    )
                }
            }
        }
    }
}

/**
 * Backward-compatible wrapper keeping the old ATTRangeSlider API.
 */
/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ATTRangeSlider(
    modifier: Modifier = Modifier,
    labelTitle: String? = null,
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
    onValueChange: ((ClosedFloatingPointRange<Float>) -> Unit)? = null,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    ATTStopsRangeSlider(
        modifier = modifier,
        labelTitle = labelTitle,
        labelFontSize = labelFontSize,
        labelColor = labelColor,
        valueRange = valueRange,
        sliderLowValue = lowValue,
        sliderHighValue = highValue,
        stepsGap = (valueRange.endInclusive - valueRange.start) / steps.toFloat(),
        sliderHeight = 40.dp,
        enabled = enabled,
        colors = colors,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished
    )
}*/

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
        val placeable = measurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val thumbPx = 24.dp.roundToPx()

        val trackWidth = (constraints.maxWidth - thumbPx).coerceAtLeast(0)
        val last = (placeable.size - 1).coerceAtLeast(1)

        layout(constraints.maxWidth, placeable.maxOfOrNull { it.height } ?: 0) {
            placeable.forEachIndexed { i, p ->
                val centerX = thumbPx / 2f + trackWidth * i / last
                p.placeRelative(
                    x = (centerX - p.width / 2f).roundToInt(),
                    y = 0
                )
            }
        }
    }
}

/*@Composable
private fun DisplaySoptsLabels(
    modifier: Modifier = Modifier,
    labels: List<String>,
    labelFontSize: Float = 9f
) {
    DisplaySpotsLabels(modifier = modifier, labels = labels, labelFontSize = labelFontSize)
}*/

/**
 * Preview function for Android Studio
 */
@Preview
@Composable
fun ATTStopsRangeSliderPreview() {
    ATTStopsRangeSlider()
}