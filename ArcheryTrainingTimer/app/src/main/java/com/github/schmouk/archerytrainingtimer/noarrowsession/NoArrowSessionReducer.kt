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

package com.github.schmouk.archerytrainingtimer.noarrowsession

import kotlin.math.roundToInt

/**
 * Pure reducer helpers for the No-Arrows timer logic.
 * These functions keep the countdown rules out of the Compose screen.
 */
object NoArrowSessionReducer {

    fun evaluateRestingRatio(
        repetitionsDuration: Int,
        repetitionsNumberPerSeries: Int?
    ): Float {
        val ratio: Float = if (repetitionsNumberPerSeries == null) {
            0.5f
        } else if (repetitionsDuration <= 20) {
            1.1f - repetitionsDuration / 25f
        } else {
            (0.3f - (repetitionsDuration - 20) / 50f).coerceAtLeast(0.0f)
        }

        return (100f * ratio).roundToInt().toFloat() / 100f
    }

    fun evaluateRestTime(
        lastDurationSeconds: Int,
        numberOfRepetitions: Int?
    ): Int {
        val ratio = evaluateRestingRatio(lastDurationSeconds, numberOfRepetitions)
        return ((numberOfRepetitions ?: 0) * lastDurationSeconds * ratio).roundToInt()
    }

    fun startPreparationMode(
        state: NoArrowSessionState,
        preparationTime: Int
    ): NoArrowSessionState = state.copy(
        currentPreparationSecondsLeft = preparationTime,
        isPreparationMode = true,
        isRestMode = false,
        isTimerRunning = false,
        isTimerStopped = false,
    )

    fun startCountdowns(
        state: NoArrowSessionState
    ): NoArrowSessionState = state.copy(
        currentDurationSecondsLeft = state.initialDurationSeconds,
        isPreparationMode = false,
        isTimerRunning = true,
        isTimerStopped = false,
    )

    fun sessionCompleted(
        state: NoArrowSessionState
    ): NoArrowSessionState = state.copy(
        currentDurationSecondsLeft = 0,
        currentRepetitionsLeft = 0,
        currentSeriesLeft = 0,
        currentRestTimeLeft = 0,
        isSessionCompleted = true,
        isTimerRunning = false,
        isTimerStopped = false,
        isPreparationMode = false,
        isRestMode = false,
    )
}
