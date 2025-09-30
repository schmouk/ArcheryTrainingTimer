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

package com.github.schmouk.archerytrainingtimer.ui.commons

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

import com.github.schmouk.archerytrainingtimer.DEBUG_MODE
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.DimmedProgressBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.DimmedTimerBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.ProgressBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerPreparationColor
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerRestColor

import kotlin.math.max
import kotlin.math.min


//=====   SERIES COUNTDOWN STUFF   ============================
/**
 * The graphical entity for series count-down
 *
 * @param initialDurationSeconds: Int?, the currently selected durations
 * @param currentDurationSecondsLeft: Int?, the actual seconds left before end of current repetition
 * @param numberOfRepetitions: Int?, the currently selected number of repetitions in series
 * @param currentRepetitionsLeft: Int?, the actual left number of repetitions before end
 * @param numberOfSeries: Int?, the total number of series selected for the session
 * @param currentSeriesLeft: Int?, the actual left number of series before end
 * @param isPreparationMode: Boolean, true if timer is currently in preparation mode, or false otherwise
 * @param isTimerRunning: Boolean, true if timer is currently running, or false otherwise
 * @param isTimerStopped: Boolean, true if timer is currently stopped, or false otherwise
 * @param isDimmedDisplay: Boolean, true if display should be dimmed, or false otherwise
 * @param timerBorderColor: Color = TimerBorderColor, the color of the text displaying the series count
 * @param dimmedTimerBorderColor: Color = DimmedTimerBorderColor, the color of the text displaying the series count when dimmed
 * @param restingTimerColor: Color = TimerRestColor,
 * @param progressBorderColor: Color = ProgressBorderColor,
 * @param dimmedProgressBorderColor: Color = DimmedProgressBorderColor,
 * @param seriesCircleRadius: Float, the radius of the series circle
 * @param seriesStrokeWidthPx: Float, the width of the series circle border, in pixels
 * @param localPaddingPx: Float, the local padding to be applied around the series
 */
@Composable
fun SeriesCountdown(
    initialDurationSeconds: Int?,
    currentDurationSecondsLeft: Int?,
    numberOfRepetitions: Int?,
    currentRepetitionsLeft: Int?,
    numberOfSeries: Int?,
    currentSeriesLeft: Int?,
    isPreparationMode: Boolean,
    isTimerRunning: Boolean,
    isTimerStopped: Boolean,
    isDimmedDisplay: Boolean,
    timerBorderColor: Color = TimerBorderColor,
    dimmedTimerBorderColor: Color = DimmedTimerBorderColor,
    restingTimerColor: Color = TimerRestColor,
    progressBorderColor: Color = ProgressBorderColor,
    dimmedProgressBorderColor: Color = DimmedProgressBorderColor,
    seriesCircleRadius: Float,
    seriesStrokeWidthPx: Float,
    localPaddingPx: Float
) {

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (DEBUG_MODE) {
            val debugTextSizePx = 36f
            val restTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AppTextColor.toArgb()
                textSize = debugTextSizePx
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface =
                    Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
            }

            drawContext.canvas.nativeCanvas.drawText(
                ">>> DEBUG mode",
                30f,
                38f,
                restTextPaint
            )
        }

        val circleCenterX = size.width / 2
        val circleCenterY = size.height - seriesCircleRadius - localPaddingPx
        val mainColor = if (isDimmedDisplay || isPreparationMode) DimmedTimerBorderColor else TimerBorderColor

        // Draw circle border
        drawCircle(
            color = mainColor,
            radius = seriesCircleRadius - seriesStrokeWidthPx / 2,
            style = Stroke(width = seriesStrokeWidthPx),
            center = Offset(circleCenterX, circleCenterY)
        )

        // Draw the progress arc
        if (!isPreparationMode) {
            // Progress arc is not drawn in preparation mode
            val totalRepetitions =
                (numberOfRepetitions ?: 0) * (numberOfSeries ?: 0)
            val sweepAngle =
                if (numberOfSeries != null && numberOfSeries > 0 &&
                    currentSeriesLeft != null &&
                    initialDurationSeconds != null &&
                    currentDurationSecondsLeft != null)
                {
                    if (currentSeriesLeft > 1)
                        ((numberOfSeries - currentSeriesLeft) / numberOfSeries.toFloat()) * 360f
                    else if (currentRepetitionsLeft!! > 1)
                        (totalRepetitions - currentRepetitionsLeft) / totalRepetitions.toFloat() * 360f
                    else
                        ((totalRepetitions * initialDurationSeconds - currentDurationSecondsLeft + 1) /
                                (totalRepetitions * initialDurationSeconds).toFloat()) * 360f
                } else {
                    0f
                }

            if ((isTimerRunning || isTimerStopped) && sweepAngle > 0f) {
                // Notice, reminder:
                //  (isTimerRunning || isTimerStopped) avoids red-ghost display
                //  in big timer border when selecting number of repetitions
                val arcDiameter =
                    (seriesCircleRadius - seriesStrokeWidthPx / 2f) * 2f
                val arcTopLeftX = circleCenterX - arcDiameter / 2f
                val arcTopLeftY = circleCenterY - arcDiameter / 2f

                val progressStrokeWidthPx = 0.5f * seriesStrokeWidthPx

                drawArc(
                    color = if (isDimmedDisplay) DimmedProgressBorderColor else ProgressBorderColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = progressStrokeWidthPx),
                    topLeft = Offset(arcTopLeftX, arcTopLeftY),
                    size = androidx.compose.ui.geometry.Size(
                        arcDiameter,
                        arcDiameter
                    )
                )
            }
        }

        // Series display will show 0 when dimmed
        val seriesToDisplayValue = currentSeriesLeft
        val seriesToDisplayString =
            seriesToDisplayValue?.toString()
                ?: currentSeriesLeft?.toString() ?: ""

        if (seriesToDisplayString.isNotEmpty()) {
            val targetTextHeightPx = seriesCircleRadius * 0.9f

            val countdownTextPaint =
                TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = mainColor.toArgb()
                    textSize = targetTextHeightPx
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }

            val countdownBounds = Rect()
            countdownTextPaint.getTextBounds(
                "0",  // one of the tallest digits to ensure proper centering
                0,
                1,
                countdownBounds
            )

            drawContext.canvas.nativeCanvas.drawText(
                seriesToDisplayString,
                circleCenterX,
                circleCenterY - countdownBounds.exactCenterY(), // Draw at the calculated baseline
                countdownTextPaint
            )
        }
    }
}


/**
 * A BoxWithConstraints composable that contains the SeriesCountdown composable,
 * ensuring that the countdown fits within the available space while maintaining its aspect ratio.
 *
 * @param initialDurationSeconds: Int?, the currently selected durations
 * @param currentDurationSecondsLeft: Int?, the actual seconds left before end of current repetition
 * @param numberOfRepetitions: Int?, the currently selected number of repetitions in series
 * @param currentRepetitionsLeft: Int?, the actual left number of repetitions before end
 * @param numberOfSeries: Int?, the total number of series selected for the session
 * @param currentSeriesLeft: Int?, the actual left number of series before end
 * @param isPreparationMode: Boolean, true if timer is currently in preparation mode, or false otherwise
 * @param isTimerRunning: Boolean, true if timer is currently running, or false otherwise
 * @param isTimerStopped: Boolean, true if timer is currently stopped, or false otherwise
 * @param isDimmedDisplay: Boolean, true if display should be dimmed, or false otherwise
 * @param textColor: Color = TimerBorderColor, the color of the text displaying the series count
 * @param dimmedTextColor: Color = DimmedTimerBorderColor, the color of the text displaying the series count when dimmed
 * @param restingTimerColor: Color = TimerRestColor,
 * @param progressBorderColor: Color = ProgressBorderColor,
 * @param dimmedProgressBorderColor: Color = DimmedProgressBorderColor,
 * @param seriesStrokeWidthPx: Float, the width of the series circle border, in pixels
 * @param localPaddingPx: Float, the local padding to be applied around the series
 * @param modifier: Modifier, the modifier to be applied to the BoxWithConstraints composable
 * @param boxContentAlignment: Alignment = Alignment.Center, the alignment of the content within the
 */
@Composable
fun SeriesCountdownConstrainedBox(
    initialDurationSeconds: Int?,
    currentDurationSecondsLeft: Int?,
    numberOfRepetitions: Int?,
    currentRepetitionsLeft: Int?,
    numberOfSeries: Int?,
    currentSeriesLeft: Int?,
    isPreparationMode: Boolean,
    isTimerRunning: Boolean,
    isTimerStopped: Boolean,
    isDimmedDisplay: Boolean,
    textColor: Color = TimerBorderColor,
    dimmedTextColor: Color = DimmedTimerBorderColor,
    restingTimerColor: Color = TimerRestColor,
    progressBorderColor: Color = ProgressBorderColor,
    dimmedProgressBorderColor: Color = DimmedProgressBorderColor,
    seriesStrokeWidthPx: Float,
    localPaddingPx: Float,
    modifier: Modifier,
    boxContentAlignment: Alignment = Alignment.Center
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = boxContentAlignment
    ) {
        val seriesCircleRadius =
            min(
                constraints.maxWidth,
                constraints.maxHeight
            ) / 2f * 0.85f

        SeriesCountdown(
            initialDurationSeconds,
            currentDurationSecondsLeft,
            numberOfRepetitions,
            currentRepetitionsLeft,
            numberOfSeries,
            currentSeriesLeft,
            isPreparationMode,
            isTimerRunning,
            isTimerStopped,
            isDimmedDisplay,
            textColor,
            dimmedTextColor,
            restingTimerColor,
            progressBorderColor,
            dimmedProgressBorderColor,
            seriesCircleRadius,
            seriesStrokeWidthPx,
            localPaddingPx
        )
    }
}


//=====   TIMER COUNTDOWN STUFF   =============================
/**
 * The graphical entity for timer count-down
 *
 * @param selectedDurationString: String?, the currently selected duration string value
 * @param initialDurationSeconds: Int?, the currently selected durations
 * @param currentDurationSecondsLeft: Int?, the actual seconds left before end of current repetition
 * @param numberOfRepetitions: Int?, the currently selected number of repetitions in series
 * @param currentRepetitionsLeft: Int?, the actual left number of repetitions before end of current series
 * @param currentRestTimeLeft: Int?, the actual number of seconds left before enf of resting time
 * @param isPreparationMode: Boolean, true if timer is currently in preparation mode, or false otherwise
 * @param isTimerRunning: Boolean, true if timer is currently running, or false otherwise
 * @param isTimerStopped: Boolean, true if timer is currently stopped, or false otherwise
 * @param isDimmedDisplay: Boolean, true if display should be dimmed, or false otherwise
 * @param isRestMode: Boolean, true if timer is currently counting down resting time, or false otherwise
 * @param restModeText: String, the text related to the "Rest..." caption
 * @param prepModeText: String, the text related to the "Prep..." caption
 * @param circleRadius: Float, the radius of the timer border
 * @param strokeWidthPx: Float, the width of the timer border, in pixels
 * @param timerBorderColor: Color = TimerBorderColor,
 * @param dimmedTimerBorderColor: Color = DimmedTimerBorderColor,
 * @param restingTimerColor: Color = TimerRestColor,
 * @param progressBorderColor: Color = ProgressBorderColor,
 * @param dimmedProgressBorderColor: Color = DimmedProgressBorderColor,
 * @param preparationTimeColor: Color = TimerPreparationColor,
 * @param modifier: Modifier, the modifier to be applied to the internal canvas that contains the timer countdown
 */
@Composable
fun TimerCountdown(
    selectedDurationString: String?,
    initialDurationSeconds: Int?,
    currentDurationSecondsLeft: Int?,
    numberOfRepetitions: Int?,
    currentRepetitionsLeft: Int?,
    currentRestTimeLeft: Int?,
    isPreparationMode: Boolean,
    isTimerRunning: Boolean,
    isTimerStopped: Boolean,
    isDimmedDisplay: Boolean,
    isRestMode: Boolean,
    restModeText: String,
    prepModeText: String,
    circleRadius: Float,
    strokeWidthPx: Float,
    timerBorderColor: Color = TimerBorderColor,
    dimmedTimerBorderColor: Color = DimmedTimerBorderColor,
    restingTimerColor: Color = TimerRestColor,
    progressBorderColor: Color = ProgressBorderColor,
    dimmedProgressBorderColor: Color = DimmedProgressBorderColor,
    preparationTimeColor: Color = TimerPreparationColor,
    modifier: Modifier
) {
    Canvas(modifier = modifier) {
        val canvasCenterX = size.width / 2f
        val canvasCenterY = size.height / 2f
        val mainColor = if (isRestMode) restingTimerColor
                        else if (isDimmedDisplay) dimmedTimerBorderColor
                        else if (isPreparationMode) preparationTimeColor
                        else timerBorderColor
        
        // 1. Draw the main circle border
        drawCircle(
            color = mainColor,
            radius = circleRadius - strokeWidthPx / 2f, // Radius to the center of the stroke
            style = Stroke(width = strokeWidthPx),
            center = Offset(canvasCenterX, canvasCenterY)
        )

        // 2. Draw the progress arc
        if (!isPreparationMode ) {
            // Progress arc is not drawn in preparation mode
            val sweepAngle =
                if (numberOfRepetitions != null && numberOfRepetitions > 0 && currentRepetitionsLeft != null) {
                    if (currentRepetitionsLeft > 1)
                        ((numberOfRepetitions - currentRepetitionsLeft) / numberOfRepetitions.toFloat()) * 360f
                    else
                        ((numberOfRepetitions * initialDurationSeconds!! - currentDurationSecondsLeft!! + 1) /
                                (numberOfRepetitions * initialDurationSeconds).toFloat()) * 360f
                } else {
                    0f
                }
    
            if (!isRestMode && (isTimerRunning || isTimerStopped) && sweepAngle > 0f) {
                // Notice, reminder:
                //  (isTimerRunning || isTimerStopped) avoids red-ghost display
                //  in big timer border when selecting number of repetitions
                val arcDiameter =
                    (circleRadius - strokeWidthPx / 2f) * 2f
                val arcTopLeftX = canvasCenterX - arcDiameter / 2f
                val arcTopLeftY = canvasCenterY - arcDiameter / 2f
    
                val progressStrokeWidth = (0.72f * strokeWidthPx)  //* mainTimerStrokeWidth.value).dp.toPx()
    
                drawArc(
                    color = if (isDimmedDisplay) dimmedProgressBorderColor else progressBorderColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = progressStrokeWidth),
                    topLeft = Offset(arcTopLeftX, arcTopLeftY),
                    size = androidx.compose.ui.geometry.Size(
                        arcDiameter,
                        arcDiameter
                    )
                )
            }
        }


        // --- Column to hold Time Countdown numbers and "Rest..." text ---
        // Text for the main duration
        val showDimmedTimers = currentRepetitionsLeft == 0 &&
                !(isTimerRunning || isTimerStopped || isRestMode)

        val durationToDisplayValue =
            if (showDimmedTimers) 0
            else if (isRestMode) currentRestTimeLeft
            else currentDurationSecondsLeft

        val durationToDisplayString = durationToDisplayValue?.toString()
            ?: initialDurationSeconds?.toString()
            ?: selectedDurationString?.split(" ")
                ?.firstOrNull() ?: ""

        if (durationToDisplayString.isNotEmpty()) {
            val targetTextHeightPx = circleRadius * 0.9f

            val countdownTextPaint =
                TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = mainColor.toArgb()
                    textSize = targetTextHeightPx
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }

            val countdownBounds = Rect()
            countdownTextPaint.getTextBounds(
                "0",  // one of the tallest digits to ensure proper vertical centering
                0,
                1,
                countdownBounds
            )

            val yBaseLine = canvasCenterY - countdownBounds.exactCenterY()

            drawContext.canvas.nativeCanvas.drawText(
                durationToDisplayString,
                canvasCenterX,
                yBaseLine, // Draw at the calculated baseline
                countdownTextPaint
            )

            // "Rest..." / "Prep..." Text, displayed only during rest or preparation mode
            if (isRestMode || isPreparationMode) {
                val restTextSizePx = targetTextHeightPx * 0.22f
                val restTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = mainColor.toArgb()
                    textSize = max(16f, restTextSizePx)
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(
                        Typeface.DEFAULT_BOLD,
                        Typeface.ITALIC
                    )
                }

                drawContext.canvas.nativeCanvas.drawText(
                    if (isRestMode) restModeText else prepModeText,
                    canvasCenterX,
                    yBaseLine + 5 * restTextSizePx / 3,
                    restTextPaint
                )
            }
        }
    }
}

/**
 * A BoxWithConstraints composable that contains the TimerCountdown composable,
 * ensuring that the timer fits within the available space while maintaining its aspect ratio.
 *
 * @param selectedDurationString: String?, the currently selected duration string value
 * @param initialDurationSeconds: Int?, the currently selected durations
 * @param currentDurationSecondsLeft: Int?, the actual seconds left before end of current repetition
 * @param numberOfRepetitions: Int?, the currently selected number of repetitions in series
 * @param currentRepetitionsLeft: Int?, the actual left number of repetitions before end of current series
 * @param currentRestTimeLeft: Int?, the actual number of seconds left before enf of resting time
 * @param isPreparationMode: Boolean, true if timer is currently in preparation mode, or false otherwise
 * @param isTimerRunning: Boolean, true if timer is currently running, or false otherwise
 * @param isTimerStopped: Boolean, true if timer is currently stopped, or false otherwise
 * @param isDimmedDisplay: Boolean, true if display should be dimmed, or false otherwise
 * @param isRestMode: Boolean, true if timer is currently counting down resting time, or false otherwise
 * @param restModeText: String, the text related to the "Rest..." caption
 * @param prepModeText: String, the text related to the "Prep..." caption
 * @param timerStrokeWidthDp: Dp, the width of the timer border, in Dp
 * @param timerBorderColor: Color = TimerBorderColor, the color of the timer border
 * @param dimmedTimerBorderColor: Color = DimmedTimerBorderColor, the color of the timer border when dimmed
 * @param restingTimerColor: Color = WABlueColor, the color of the timer border in resting mode
 * @param progressBorderColor: Color = ProgressBorderColor, the color of the progress arc
 * @param dimmedProgressBorderColor: Color = DimmedProgressBorderColor, the color of the progress arc when dimmed
 * @param heightScalingFactor: Float, the overall height scaling factor associated with the device display
 * @param widthScalingFactor: Float, the overall width scaling factor associated with the device display
 * @param preparationTimeColor: Color = TimerPreparationColor, the color of the timer text in preparation mode
 * @param modifier: Modifier, the modifier to be applied to the BoxWithConstraints composable
 * @param boxContentAlignment: Alignment = Alignment.Center, the alignment of the content within the BoxWithConstraints
 */
@Composable
fun TimerCountdownConstrainedBox(
    selectedDurationString: String?,
    initialDurationSeconds: Int?,
    currentDurationSecondsLeft: Int?,
    numberOfRepetitions: Int?,
    currentRepetitionsLeft: Int?,
    currentRestTimeLeft: Int?,
    isPreparationMode: Boolean,
    isTimerRunning: Boolean,
    isTimerStopped: Boolean,
    isDimmedDisplay: Boolean,
    isRestMode: Boolean,
    restModeText: String,
    prepModeText: String,
    timerStrokeWidthDp: Dp,
    timerBorderColor: Color = TimerBorderColor,
    dimmedTimerBorderColor: Color = DimmedTimerBorderColor,
    restingTimerColor: Color = AppTextColor,
    progressBorderColor: Color = ProgressBorderColor,
    dimmedProgressBorderColor: Color = DimmedProgressBorderColor,
    preparationTimeColor: Color = TimerPreparationColor,
    heightScalingFactor: Float,
    widthScalingFactor: Float,
    modifier: Modifier,
    boxContentAlignment: Alignment = Alignment.Center
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = boxContentAlignment
    ) {
        val circleRadius =
            min(
                widthScalingFactor * constraints.maxWidth,
                heightScalingFactor * constraints.maxHeight
            ) / 2f * 0.9f

        val timerStrokeWidthPx =
            with(LocalDensity.current) { timerStrokeWidthDp.toPx() }

        TimerCountdown(
            selectedDurationString,
            initialDurationSeconds,
            currentDurationSecondsLeft,
            numberOfRepetitions,
            currentRepetitionsLeft,
            currentRestTimeLeft,
            isPreparationMode,
            isTimerRunning,
            isTimerStopped,
            isDimmedDisplay,
            isRestMode,
            restModeText,
            prepModeText,
            circleRadius,
            timerStrokeWidthPx,
            timerBorderColor,
            dimmedTimerBorderColor,
            restingTimerColor,
            progressBorderColor,
            dimmedProgressBorderColor,
            preparationTimeColor,
            Modifier.fillMaxSize()
        )
    }
}
