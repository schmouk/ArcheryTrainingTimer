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
import androidx.compose.ui.unit.dp

import com.github.schmouk.archerytrainingtimer.ui.theme.DimmedProgressBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.DimmedTimerBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.ProgressBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.WABlueColor

import kotlin.math.max
import kotlin.math.min


/**
 * The graphical entity for timers count-down
 *
 * @param selectedDurationString : String?, the currently selected duration string value
 * @param initialDurationSeconds : Int?, the currently selected durations
 * @param currentDurationSecondsLeft : Int?, the actual seconds left before end of current repetition
 * @param numberOfRepetitions : Int?, the currently selected number of repetitions in series
 * @param currentRepetitionsLeft : Int?, the actual left number of repetitions before end of current series
 * @param currentRestTimeLeft : Int?, the actual number of seconds left before enf of resting time
 * @param isTimerRunning : Boolean, true if timer is currently running, or false otherwise
 * @param isTimerStopped : Boolean, true if timer is currently stopped, or false otherwise
 * @param isDimmedDisplay : Boolean, true if display should be dimmed, or false otherwise
 * @param isRestMode : Boolean, true if timer is currently counting down resting time, or false otherwise
 * @param restModeText : String, the text related to the "Rest..." caption
 * @param circleRadius : Float, the radius of the timer border
 * @param strokeWidthPx : Float, the width of the timer border, in pixels
 * @param timerBorderColor : Color = TimerBorderColor,
 * @param dimmedTimerBorderColor : Color = DimmedTimerBorderColor,
 * @param restingTimerColor : Color = WABlueColor,
 * @param progressBorderColor : Color = ProgressBorderColor,
 * @param dimmedProgressBorderColor : Color = DimmedProgressBorderColor,
 * @param canvasModifier : Modifier
 */
@Composable
fun TimerCountdown(
    selectedDurationString : String?,
    initialDurationSeconds : Int?,
    currentDurationSecondsLeft : Int?,
    numberOfRepetitions : Int?,
    currentRepetitionsLeft : Int?,
    currentRestTimeLeft : Int?,
    isTimerRunning : Boolean,
    isTimerStopped : Boolean,
    isDimmedDisplay : Boolean,
    isRestMode : Boolean,
    restModeText : String,
    circleRadius : Float,
    strokeWidthPx : Float,
    timerBorderColor : Color = TimerBorderColor,
    dimmedTimerBorderColor : Color = DimmedTimerBorderColor,
    restingTimerColor : Color = WABlueColor,
    progressBorderColor : Color = ProgressBorderColor,
    dimmedProgressBorderColor : Color = DimmedProgressBorderColor,
    canvasModifier : Modifier
) {
    Canvas(modifier = canvasModifier) {
        val canvasCenterX = size.width / 2f
        val canvasCenterY = size.height / 2f

        // 1. Draw the main circle border
        drawCircle(
            color = if (isRestMode) restingTimerColor
                    else if (isDimmedDisplay) dimmedTimerBorderColor
                    else timerBorderColor,
            radius = circleRadius - strokeWidthPx / 2f, // Radius to the center of the stroke
            style = Stroke(width = strokeWidthPx),
            center = Offset(canvasCenterX, canvasCenterY)
        )

        // 2. Draw the progress arc
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
                    color = (
                        if (isRestMode) restingTimerColor
                        else if (isDimmedDisplay) dimmedTimerBorderColor
                        else timerBorderColor
                    ).toArgb()
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

            // "Rest..." Text, displayed only during rest mode
            if (isRestMode) {
                val restTextSizePx = targetTextHeightPx * 0.22f
                val restTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = restingTimerColor.toArgb()
                    textSize = max(16f, restTextSizePx)
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(
                        Typeface.DEFAULT_BOLD,
                        Typeface.ITALIC
                    )
                }

                drawContext.canvas.nativeCanvas.drawText(
                    restModeText,
                    canvasCenterX,
                    yBaseLine + 5 * restTextSizePx / 3,
                    restTextPaint
                )
            }
        }
    }
}


@Composable
fun TimerCountdownConstrainedBox(
    selectedDurationString : String?,
    initialDurationSeconds : Int?,
    currentDurationSecondsLeft : Int?,
    numberOfRepetitions : Int?,
    currentRepetitionsLeft : Int?,
    currentRestTimeLeft : Int?,
    isTimerRunning : Boolean,
    isTimerStopped : Boolean,
    isDimmedDisplay : Boolean,
    isRestMode : Boolean,
    restModeText : String,
    timerStrokeWidthDp : Dp,
    timerBorderColor : Color = TimerBorderColor,
    dimmedTimerBorderColor : Color = DimmedTimerBorderColor,
    restingTimerColor : Color = WABlueColor,
    progressBorderColor : Color = ProgressBorderColor,
    dimmedProgressBorderColor : Color = DimmedProgressBorderColor,
    heightScalingFactor : Float,
    widthScalingFactor : Float,
    boxModifier : Modifier,
    boxContentAlignment : Alignment = Alignment.Center
) {
    BoxWithConstraints(
        modifier = boxModifier,
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
            isTimerRunning,
            isTimerStopped,
            isDimmedDisplay,
            isRestMode,
            restModeText,
            circleRadius,
            timerStrokeWidthPx,
            timerBorderColor,
            dimmedTimerBorderColor,
            restingTimerColor,
            progressBorderColor,
            dimmedProgressBorderColor,
            canvasModifier = Modifier.fillMaxSize()
        )
    }
}