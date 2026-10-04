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

import androidx.compose.runtime.MutableState
import com.github.schmouk.archerytrainingtimer.commons.ESignal
import com.github.schmouk.archerytrainingtimer.commons.SoundPlayer
import com.github.schmouk.archerytrainingtimer.ui.commons.DurationSessionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Keeps the runtime No-Arrows timer actions out of the screen composable.
 */
class NoArrowSessionController(
    private val noArrowsViewModel: NoArrowsTimerViewModel,
    private val soundPlayer: SoundPlayer,
    private val scope: CoroutineScope,
    private val sessionDurationManager: DurationSessionController = DurationSessionController(),
    private val beepScheduler: NoArrowBeepScheduler = NoArrowBeepScheduler(soundPlayer, scope),
) {
    fun pauseCountdowns(tickBaseTimeState: MutableState<Long>) {
        tickBaseTimeState.value = System.currentTimeMillis()
        noArrowsViewModel.action(ESignal.SIG_STOP)
    }

    fun resumeCountdowns(tickBaseTimeState: MutableState<Long>) {
        tickBaseTimeState.value = System.currentTimeMillis()
        noArrowsViewModel.action(ESignal.SIG_START)
    }

    fun setRestMode() = noArrowsViewModel.action(ESignal.SIG_REST_ON)
    fun setEndOfRestMode() = noArrowsViewModel.action(ESignal.SIG_REST_OFF)
    fun setFutureRestMode() = noArrowsViewModel.action(ESignal.SIG_WILL_REST)

    fun beginSession() = sessionDurationManager.beginSession()
    fun endSession() = sessionDurationManager.endSession()

    fun sessionHasCompleted() {
        noArrowsViewModel.action(ESignal.SIG_COMPLETED)
        beepScheduler.playEndBeep()
        sessionDurationManager.endSession()
    }

    suspend fun runTimerLoop(
        isTimerRunningProvider: () -> Boolean,
        isRestModeProvider: () -> Boolean,
        isPreparationModeProvider: () -> Boolean,
        isTimerStoppedProvider: () -> Boolean,
        tickBaseTimeState: MutableState<Long>,
        countDownDelay: Long,
        getInitialDurationSeconds: () -> Int?,
        getCurrentDurationSecondsLeft: () -> Int?,
        getCurrentRepetitionsLeft: () -> Int?,
        getCurrentSeriesLeft: () -> Int?,
        getCurrentRestTimeLeft: () -> Int?,
        getCurrentPreparationSecondsLeft: () -> Int?,
        getNumberOfRepetitions: () -> Int?,
        getNumberOfSeries: () -> Int?,
        getIntermediateBeepsChecked: () -> Boolean?,
        getEndOfRestBeepTime: () -> Int,
        getIntermediateBeepsDuration: () -> Int,
        setCurrentDurationSecondsLeft: (Int?) -> Unit,
        setCurrentRepetitionsLeft: (Int?) -> Unit,
        setCurrentSeriesLeft: (Int?) -> Unit,
        setCurrentRestTimeLeft: (Int?) -> Unit,
        setCurrentPreparationSecondsLeft: (Int?) -> Unit,
        startCountdowns: () -> Unit,
        setRestMode: () -> Unit,
        setEndOfRestMode: () -> Unit,
        sessionHasCompletedCallback: () -> Unit,
        evaluateRestTime: () -> Int,
        playBeep: () -> Unit,
        playIntermediateBeep: () -> Unit,
        playRestBeep: () -> Unit,
        keepScreenOn: (Boolean) -> Unit,
    ) {
        while (scope.coroutineContext.isActive) {
            val timerRunning = isTimerRunningProvider()
            val restMode = isRestModeProvider()
            val preparationMode = isPreparationModeProvider()
            val timerStopped = isTimerStoppedProvider()

            if (timerRunning || restMode || preparationMode) {
                keepScreenOn(true)
            } else {
                keepScreenOn(false)
            }

            if (!(timerRunning || restMode || timerStopped || preparationMode)) {
                break
            }

            tickBaseTimeState.value = System.currentTimeMillis()

            if (!restMode) {
                if (getCurrentRepetitionsLeft() == 0) {
                    setCurrentRepetitionsLeft(getNumberOfRepetitions())
                    if (getCurrentDurationSecondsLeft() == 0) {
                        setCurrentDurationSecondsLeft(getInitialDurationSeconds())
                    }
                    setCurrentSeriesLeft(getCurrentSeriesLeft()!! - 1)
                } else {
                    if (getCurrentDurationSecondsLeft() == null || getCurrentDurationSecondsLeft() == 0) {
                        setCurrentDurationSecondsLeft(getInitialDurationSeconds())
                    }
                    if (getCurrentRepetitionsLeft() == null) {
                        setCurrentRepetitionsLeft(getNumberOfRepetitions())
                    }
                    if (getCurrentSeriesLeft() == null) {
                        setCurrentSeriesLeft(getNumberOfSeries())
                    }
                }

                if (preparationMode) {
                    adjustedDelay(countDownDelay, tickBaseTimeState)
                    setCurrentPreparationSecondsLeft((getCurrentPreparationSecondsLeft() ?: 0) - 1)
                    if (getCurrentPreparationSecondsLeft() == 0) {
                        startCountdowns()
                    }
                    continue
                }

                while (scope.coroutineContext.isActive && getCurrentSeriesLeft()!! > 0) {
                    val currentDuration = getCurrentDurationSecondsLeft()
                    val initialDuration = getInitialDurationSeconds()
                    if (currentDuration != null && initialDuration != null && currentDuration == initialDuration) {
                        playBeep()
                    }

                    if (currentDuration != null && currentDuration > 0) {
                        if (timerRunning) {
                            adjustedDelay(countDownDelay, tickBaseTimeState)
                        }
                        if (!timerStopped) {
                            setCurrentDurationSecondsLeft(currentDuration - 1)
                        }
                        if (getIntermediateBeepsChecked() != null &&
                            getIntermediateBeepsChecked() == true &&
                            getCurrentDurationSecondsLeft() != null &&
                            getCurrentDurationSecondsLeft()!! > 0 &&
                            (initialDuration!! - getCurrentDurationSecondsLeft()!!) % getIntermediateBeepsDuration() == 0
                        ) {
                            playIntermediateBeep()
                        }
                    } else if (currentDuration == 0) {
                        if (getCurrentRepetitionsLeft() != null && getCurrentRepetitionsLeft()!! > 0) {
                            setCurrentRepetitionsLeft(getCurrentRepetitionsLeft()!! - 1)

                            if (getCurrentRepetitionsLeft() == 0) {
                                if (getCurrentSeriesLeft() != null && getCurrentSeriesLeft()!! > 0) {
                                    if (getCurrentSeriesLeft() == 1) {
                                        sessionHasCompletedCallback()
                                        return
                                    } else {
                                        setRestMode()
                                        setCurrentRestTimeLeft(evaluateRestTime())
                                    }
                                } else {
                                    sessionHasCompletedCallback()
                                    return
                                }
                            } else {
                                setCurrentDurationSecondsLeft(initialDuration)
                            }
                        } else {
                            setRestMode()
                            break
                        }
                    }

                    if (isTimerStoppedProvider()) {
                        return
                    }
                }
            }

            val latestRestMode = isRestModeProvider()
            if ((getCurrentSeriesLeft() ?: 0) <= 0) {
                sessionHasCompletedCallback()
                return
            } else if (latestRestMode) {
                if ((getCurrentRestTimeLeft() ?: 0) == evaluateRestTime()) {
                    playRestBeep()
                }

                while (scope.coroutineContext.isActive) {
                    val restTimeLeft = getCurrentRestTimeLeft()
                    if (restTimeLeft != null && restTimeLeft > 0) {
                        if (restTimeLeft == getEndOfRestBeepTime()) {
                            playRestBeep()
                        }
                        adjustedDelay(countDownDelay, tickBaseTimeState)
                        setCurrentRestTimeLeft(restTimeLeft - 1)
                    } else {
                        setEndOfRestMode()
                        setCurrentRepetitionsLeft(getNumberOfRepetitions())
                        setCurrentSeriesLeft((getCurrentSeriesLeft() ?: 0) - 1)
                        break
                    }
                }
            }

            if (isTimerStoppedProvider()) {
                return
            }
        }
    }

    private suspend fun adjustedDelay(countDownDelay: Long, tickBaseTimeState: MutableState<Long>) {
        val t2 = System.currentTimeMillis()
        val d = countDownDelay - (t2 - tickBaseTimeState.value)
        tickBaseTimeState.value += countDownDelay
        delay(if (d >= 0L) d else 0L)
    }
}
