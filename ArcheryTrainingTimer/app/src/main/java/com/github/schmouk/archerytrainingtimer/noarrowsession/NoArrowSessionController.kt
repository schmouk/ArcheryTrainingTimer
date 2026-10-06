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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.schmouk.archerytrainingtimer.commons.ESignal
import com.github.schmouk.archerytrainingtimer.commons.SoundPlayer
import com.github.schmouk.archerytrainingtimer.ui.commons.DurationSessionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SelectionState {
    var selectedDurationString by mutableStateOf<String?>(null)
    var numberOfRepetitions by mutableStateOf<Int?>(null)
    var numberOfSeries by mutableStateOf<Int?>(null)
    var intermediateBeepsChecked by mutableStateOf<Boolean?>(null)

    var lastDurationSeconds by mutableIntStateOf(0)
    var lastNumberOfRepetitions by mutableIntStateOf(0)
    var lastNumberOfSeries by mutableIntStateOf(0)
    var lastIntermediateBeepsChecked by mutableStateOf(false)

    var initialDurationSeconds by mutableStateOf<Int?>(null)
    var currentDurationSecondsLeft by mutableStateOf<Int?>(null)
    var currentRepetitionsLeft by mutableStateOf<Int?>(null)
    var currentSeriesLeft by mutableStateOf<Int?>(null)
    var currentRestTimeLeft by mutableStateOf<Int?>(null)

    fun hydrateFromPreferences(
        selectedDurationString: String?,
        numberOfRepetitions: Int?,
        numberOfSeries: Int?,
        intermediateBeepsChecked: Boolean?,
        lastDurationSeconds: Int = 0,
        lastNumberOfRepetitions: Int = 0,
        lastNumberOfSeries: Int = 0,
        lastIntermediateBeepsChecked: Boolean = false,
        initialDurationSeconds: Int? = null,
        currentDurationSecondsLeft: Int? = null,
        currentRepetitionsLeft: Int? = null,
        currentSeriesLeft: Int? = null,
        currentRestTimeLeft: Int? = null,
    ) {
        this.selectedDurationString = selectedDurationString
        this.numberOfRepetitions = numberOfRepetitions
        this.numberOfSeries = numberOfSeries
        this.intermediateBeepsChecked = intermediateBeepsChecked
        this.lastDurationSeconds = lastDurationSeconds
        this.lastNumberOfRepetitions = lastNumberOfRepetitions
        this.lastNumberOfSeries = lastNumberOfSeries
        this.lastIntermediateBeepsChecked = lastIntermediateBeepsChecked
        this.initialDurationSeconds = initialDurationSeconds
        this.currentDurationSecondsLeft = currentDurationSecondsLeft
        this.currentRepetitionsLeft = currentRepetitionsLeft
        this.currentSeriesLeft = currentSeriesLeft
        this.currentRestTimeLeft = currentRestTimeLeft
    }

    suspend fun updateSelectionState(
        isRestMode: Boolean,
        isTimerStopped: Boolean,
        saveDurationPreference: suspend (String?) -> Unit,
        saveRepetitionsPreference: suspend (Int?) -> Unit,
        saveSeriesPreference: suspend (Int?) -> Unit,
        saveIntermediateBeepsPreference: suspend (Boolean) -> Unit,
        evaluateRestingMode: () -> Unit,
        sessionHasCompletedCallback: () -> Unit,
        resumeCountdowns: () -> Unit,
    ) {
        val selectedValue = selectedDurationString
        if (selectedValue != null) {
            val durationValue = selectedValue.split(" ").firstOrNull()?.toIntOrNull()
            if (durationValue != null && durationValue != lastDurationSeconds) {
                initialDurationSeconds = durationValue
                currentDurationSecondsLeft = if (isRestMode) {
                    durationValue
                } else {
                    min(
                        max(
                            1,
                            (currentDurationSecondsLeft ?: 0) + durationValue - lastDurationSeconds
                        ),
                        durationValue
                    )
                }
                lastDurationSeconds = durationValue

                if ((currentDurationSecondsLeft ?: 0) <= 1 && (currentRepetitionsLeft ?: 0) <= 1) {
                    evaluateRestingMode()
                }

                saveDurationPreference(selectedValue)
            }
        }

        val repetitions = numberOfRepetitions
        if (repetitions != null && repetitions != lastNumberOfRepetitions) {
            if (!isRestMode) {
                val nextRepetitions = min(
                    max(
                        0,
                        (currentRepetitionsLeft ?: 0) + repetitions - lastNumberOfRepetitions
                    ),
                    repetitions
                )
                currentRepetitionsLeft = nextRepetitions
                if (nextRepetitions == 0) {
                    evaluateRestingMode()
                }
            } else {
                currentDurationSecondsLeft = 0
            }
            lastNumberOfRepetitions = repetitions
            saveRepetitionsPreference(repetitions)
        }

        val series = numberOfSeries
        if (series != null && series != lastNumberOfSeries) {
            val nextSeries = max(0, (currentSeriesLeft ?: 0) + series - lastNumberOfSeries)
            currentSeriesLeft = nextSeries
            lastNumberOfSeries = series
            saveSeriesPreference(series)
            if (nextSeries == 0 || (isRestMode && nextSeries <= 1)) {
                currentRestTimeLeft = 0
                currentDurationSecondsLeft = 0
                currentRepetitionsLeft = 0
                currentSeriesLeft = 0
                if (isRestMode) {
                    sessionHasCompletedCallback()
                } else if (isTimerStopped) {
                    resumeCountdowns()
                }
            }
        }

        val intermediate = intermediateBeepsChecked
        if (intermediate != null && intermediate != lastIntermediateBeepsChecked) {
            saveIntermediateBeepsPreference(intermediate)
            lastIntermediateBeepsChecked = intermediate
        }
    }
}

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
    private var lastRepetitionStartKey: String? = null
    private var lastRestStartKey: String? = null
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
    fun playBeep() = beepScheduler.playStartBeep()
    fun playIntermediateBeep() = beepScheduler.playIntermediateBeep()
    fun playRestBeep() = beepScheduler.playRestBeep()

    fun sessionHasCompleted() {
        noArrowsViewModel.action(ESignal.SIG_COMPLETED)
        beepScheduler.playEndBeep()
        sessionDurationManager.endSession()
    }

    fun calculateRestingRatio(
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

    fun calculateRestTime(
        lastDurationSeconds: Int,
        numberOfRepetitions: Int?
    ): Int {
        val ratio = calculateRestingRatio(lastDurationSeconds, numberOfRepetitions)
        return ((numberOfRepetitions ?: 0) * lastDurationSeconds * ratio).roundToInt()
    }

    fun evaluateRestingRatio(
        repetitionsDuration: Int,
        repetitionsNumberPerSeries: Int?
    ): Float = calculateRestingRatio(repetitionsDuration, repetitionsNumberPerSeries)

    fun evaluateRestTime(
        lastDurationSeconds: Int,
        numberOfRepetitions: Int?
    ): Int = calculateRestTime(lastDurationSeconds, numberOfRepetitions)

    fun prepareSessionState(
        state: NoArrowSessionState,
        preparationTime: Int
    ): NoArrowSessionState = state.copy(
        currentPreparationSecondsLeft = preparationTime,
        isPreparationMode = true,
        isRestMode = false,
        isTimerRunning = false,
        isTimerStopped = false,
    )

    fun startCountdownState(
        state: NoArrowSessionState
    ): NoArrowSessionState = state.copy(
        currentDurationSecondsLeft = state.initialDurationSeconds,
        isPreparationMode = false,
        isTimerRunning = true,
        isTimerStopped = false,
    )

    fun completeSessionState(
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

    fun startPreparationMode(initialSeconds: Int?, preparationTime: Int): Int {
        noArrowsViewModel.action(ESignal.SIG_PREPARE)
        return preparationTime
    }

    fun startNewSession(
        allSelectionsMade: Boolean,
        currentSeriesLeft: Int?,
        numberOfSeries: Int?,
        numberOfRepetitions: Int?,
        initialDurationSeconds: Int?,
        currentDurationSecondsLeft: Int?,
        currentRepetitionsLeft: Int?,
        preparationTime: Int
    ): Triple<Int?, Int?, Int?> {
        if (!allSelectionsMade) return Triple(currentSeriesLeft, currentRepetitionsLeft, currentDurationSecondsLeft)
        val nextSeries = if (currentSeriesLeft == null || currentSeriesLeft == 0) numberOfSeries else currentSeriesLeft
        val nextRepetitions = if (currentSeriesLeft == null || currentSeriesLeft == 0) numberOfRepetitions else currentRepetitionsLeft ?: numberOfRepetitions
        val nextDuration = if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) initialDurationSeconds else currentDurationSecondsLeft
        return Triple(nextSeries, nextRepetitions, nextDuration)
    }

    fun startCountdowns(initialDurationSeconds: Int?): Int? {
        noArrowsViewModel.action(ESignal.SIG_START)
        return initialDurationSeconds
    }

    //fun sessionCompleted(state: NoArrowSessionState): NoArrowSessionState = completeSessionState(state)

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
                    val repetitionStartKey = listOf(
                        getCurrentSeriesLeft(),
                        getCurrentRepetitionsLeft(),
                        initialDuration,
                        currentDuration
                    ).joinToString(":")
                    if (
                        currentDuration != null &&
                        initialDuration != null &&
                        currentDuration == initialDuration &&
                        repetitionStartKey != lastRepetitionStartKey
                    ) {
                        lastRepetitionStartKey = repetitionStartKey
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
                val restStartKey = listOf(
                    getCurrentSeriesLeft(),
                    getCurrentRepetitionsLeft(),
                    getCurrentRestTimeLeft(),
                    evaluateRestTime()
                ).joinToString(":")
                if ((getCurrentRestTimeLeft() ?: 0) == evaluateRestTime() && restStartKey != lastRestStartKey) {
                    lastRestStartKey = restStartKey
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
        if (d >= 0L) delay(d)
        tickBaseTimeState.value += countDownDelay
    }
}
