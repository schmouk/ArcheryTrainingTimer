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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.schmouk.archerytrainingtimer.SECOND_DURATION_MS
import com.github.schmouk.archerytrainingtimer.commons.ESignal
import com.github.schmouk.archerytrainingtimer.commons.EState
import com.github.schmouk.archerytrainingtimer.commons.SoundPlayer
import com.github.schmouk.archerytrainingtimer.commons.TimerInternalRunningState
import com.github.schmouk.archerytrainingtimer.commons.UserPreferencesRepository
import com.github.schmouk.archerytrainingtimer.ui.commons.DurationSessionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SelectionState {
    var userPreferencesRepository: UserPreferencesRepository? = null
    var controller: NoArrowSessionController? = null

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
    var currentPreparationSecondsLeft by mutableStateOf<Int?>(null)

    var tickBaseTimeState: MutableState<Long> = mutableLongStateOf(System.currentTimeMillis())
    var countDownDelay: Long = SECOND_DURATION_MS
    var endOfRestBeepTime: Int = 7
    var intermediateBeepsDuration: Int = 5

    suspend fun initState(
        userPreferencesRepository: UserPreferencesRepository,
        isTimerRunning: Boolean = false,
        isRestMode: Boolean = false,
        isTimerStopped: Boolean = false,
        isPreparationMode: Boolean = false,
        formerAutomatonState: EState? = null,
        formerInternalRunningValues: TimerInternalRunningState = TimerInternalRunningState(),
        onAutomatonStateRestored: ((EState) -> Unit)? = null,
        onPreferencesLoaded: (() -> Unit)? = null,
    ) {
        this.userPreferencesRepository = userPreferencesRepository

        if (formerAutomatonState != null) {
            onAutomatonStateRestored?.invoke(formerAutomatonState)
            currentDurationSecondsLeft = formerInternalRunningValues.currentDurationSecondsLeft
            currentRepetitionsLeft = formerInternalRunningValues.currentRepetitionsLeft
            currentSeriesLeft = formerInternalRunningValues.currentSeriesLeft
            currentRestTimeLeft = formerInternalRunningValues.currentRestTimeLeft
        }

        userPreferencesRepository.saveSessionType(null)

        userPreferencesRepository.userPreferencesFlow.collect { loadedPrefs ->
            val durationValue = loadedPrefs.selectedDuration?.split(" ")?.firstOrNull()?.toIntOrNull()

            selectedDurationString = loadedPrefs.selectedDuration
            numberOfRepetitions = loadedPrefs.numberOfRepetitions
            numberOfSeries = loadedPrefs.numberOfSeries
            intermediateBeepsChecked = loadedPrefs.intermediateBeeps

            lastDurationSeconds = durationValue ?: 0
            lastNumberOfRepetitions = loadedPrefs.numberOfRepetitions ?: 0
            lastNumberOfSeries = loadedPrefs.numberOfSeries ?: 0
            lastIntermediateBeepsChecked = loadedPrefs.intermediateBeeps ?: false

            initialDurationSeconds = durationValue

            val isIdleOrConfig = !isTimerRunning && !isRestMode && !isTimerStopped && !isPreparationMode
            if (isIdleOrConfig) {
                if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                    currentDurationSecondsLeft = durationValue
                }
                if (currentRepetitionsLeft == null || currentRepetitionsLeft == 0) {
                    currentRepetitionsLeft = loadedPrefs.numberOfRepetitions
                }
                if (currentSeriesLeft == null || currentSeriesLeft == 0) {
                    currentSeriesLeft = loadedPrefs.numberOfSeries
                }
            }

            onPreferencesLoaded?.invoke()
        }
    }

    suspend fun updateSelectionState(
        isRestMode: Boolean,
        isTimerStopped: Boolean,
        isTimerRunning: Boolean = false,
        isPreparationMode: Boolean = false,
    ) {
        val isSessionActive = isTimerRunning || isRestMode || isTimerStopped || isPreparationMode

        val selectedValue = selectedDurationString
        if (selectedValue != null) {
            val durationValue = selectedValue.split(" ").firstOrNull()?.toIntOrNull()
            if (durationValue != null && durationValue != lastDurationSeconds) {
                initialDurationSeconds = durationValue
                currentDurationSecondsLeft = if (!isSessionActive) {
                    durationValue
                } else if (isRestMode) {
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

                if (isSessionActive && (currentDurationSecondsLeft ?: 0) <= 1 && (currentRepetitionsLeft ?: 0) <= 1) {
                    currentDurationSecondsLeft = 0
                    currentRestTimeLeft = controller?.evaluateRestTime(
                        lastDurationSeconds,
                        numberOfRepetitions
                    ) ?: 0
                    if (isTimerStopped) {
                        controller?.setFutureRestMode()
                    } else {
                        controller?.setRestMode()
                    }
                }

                userPreferencesRepository?.saveDurationPreference(selectedValue)
            }
        }

        val repetitions = numberOfRepetitions
        if (repetitions != null && repetitions != lastNumberOfRepetitions) {
            if (!isSessionActive) {
                currentRepetitionsLeft = repetitions
            } else if (!isRestMode) {
                val nextRepetitions = min(
                    max(
                        0,
                        (currentRepetitionsLeft ?: 0) + repetitions - lastNumberOfRepetitions
                    ),
                    repetitions
                )
                currentRepetitionsLeft = nextRepetitions
                if (nextRepetitions == 0) {
                    currentDurationSecondsLeft = 0
                    currentRestTimeLeft = controller?.evaluateRestTime(
                        lastDurationSeconds,
                        numberOfRepetitions
                    ) ?: 0
                    if (isTimerStopped) {
                        controller?.setFutureRestMode()
                    } else {
                        controller?.setRestMode()
                    }
                }
            } else {
                currentDurationSecondsLeft = 0
            }
            lastNumberOfRepetitions = repetitions
            userPreferencesRepository?.saveRepetitionsPreference(repetitions)
        }

        val series = numberOfSeries
        if (series != null && series != lastNumberOfSeries) {
            if (!isSessionActive) {
                currentSeriesLeft = series
            } else {
                val nextSeries = max(0, (currentSeriesLeft ?: 0) + series - lastNumberOfSeries)
                currentSeriesLeft = nextSeries
                if (nextSeries == 0 || (isRestMode && nextSeries <= 1)) {
                    currentRestTimeLeft = 0
                    currentDurationSecondsLeft = 0
                    currentRepetitionsLeft = 0
                    currentSeriesLeft = 0
                    if (isRestMode) {
                        controller?.sessionHasCompleted()
                        currentDurationSecondsLeft = 0
                        currentRepetitionsLeft = 0
                        currentSeriesLeft = 0
                        currentRestTimeLeft = 0
                    } else if (isTimerStopped) {
                        controller?.resumeCountdowns(tickBaseTimeState)
                    }
                }
            }
            lastNumberOfSeries = series
            userPreferencesRepository?.saveSeriesPreference(series)
        }

        val intermediate = intermediateBeepsChecked
        if (intermediate != null && intermediate != lastIntermediateBeepsChecked) {
            userPreferencesRepository?.saveIntermediateBeepsPreference(intermediate)
            lastIntermediateBeepsChecked = intermediate
        }
    }

    private fun evaluateRestingMode(isTimerStopped: Boolean) {
        currentDurationSecondsLeft = 0
        currentRestTimeLeft = controller?.evaluateRestTime(
            lastDurationSeconds,
            numberOfRepetitions
        ) ?: 0
        if (isTimerStopped) {
            controller?.setFutureRestMode()
        } else {
            controller?.setRestMode()
        }
    }

    private fun completeSession() {
        controller?.sessionHasCompleted()
        currentDurationSecondsLeft = 0
        currentRepetitionsLeft = 0
        currentSeriesLeft = 0
        currentRestTimeLeft = 0
    }

    suspend fun runTimerLoop(
        controller: NoArrowSessionController,
        keepScreenOn: (Boolean) -> Unit,
    ) {
        controller.runTimerLoop(
            selectionState = this,
            keepScreenOn = keepScreenOn,
        )
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
    init {
        noArrowsViewModel.selectionState.controller = this
    }
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
        numberOfSeries: Int?,
        numberOfRepetitions: Int?,
        initialDurationSeconds: Int?,
    ): Triple<Int?, Int?, Int?> {
        if (!allSelectionsMade) return Triple(null, null, null)
        return Triple(numberOfSeries, numberOfRepetitions, initialDurationSeconds)
    }

    fun startCountdowns(initialDurationSeconds: Int?): Int? {
        noArrowsViewModel.action(ESignal.SIG_START)
        playBeep()
        return initialDurationSeconds
    }

    suspend fun runTimerLoop(
        selectionState: SelectionState,
        keepScreenOn: (Boolean) -> Unit,
    ) {
        keepScreenOn(true)
        try {
            while (scope.coroutineContext.isActive) {
                // If paused (stopped), suspend until resumed or cancelled
                if (noArrowsViewModel.isTimerStopped.value) {
                    delay(100L)
                    selectionState.tickBaseTimeState.value = System.currentTimeMillis()
                    continue
                }

                // If not in active running/rest/preparation mode, exit loop
                if (!noArrowsViewModel.isTimerRunning.value &&
                    !noArrowsViewModel.isRestMode.value &&
                    !noArrowsViewModel.isPreparationMode.value
                ) {
                    break
                }

                // --- 1. PREPARATION MODE ---
                if (noArrowsViewModel.isPreparationMode.value) {
                    selectionState.tickBaseTimeState.value = System.currentTimeMillis()
                    val prepLeft = selectionState.currentPreparationSecondsLeft ?: 0
                    if (prepLeft > 0) {
                        adjustedDelay(selectionState.countDownDelay, selectionState.tickBaseTimeState)
                        if (noArrowsViewModel.isTimerStopped.value) continue
                        val remainingPrep = prepLeft - 1
                        selectionState.currentPreparationSecondsLeft = remainingPrep
                        if (remainingPrep <= 0) {
                            selectionState.currentDurationSecondsLeft = selectionState.initialDurationSeconds
                            selectionState.currentRepetitionsLeft = selectionState.numberOfRepetitions
                            if (selectionState.currentSeriesLeft == null || selectionState.currentSeriesLeft == 0) {
                                selectionState.currentSeriesLeft = selectionState.numberOfSeries
                            }
                            startCountdowns(selectionState.initialDurationSeconds)
                        }
                    } else {
                        selectionState.currentDurationSecondsLeft = selectionState.initialDurationSeconds
                        selectionState.currentRepetitionsLeft = selectionState.numberOfRepetitions
                        if (selectionState.currentSeriesLeft == null || selectionState.currentSeriesLeft == 0) {
                            selectionState.currentSeriesLeft = selectionState.numberOfSeries
                        }
                        startCountdowns(selectionState.initialDurationSeconds)
                    }
                    continue
                }

                // --- 2. REST MODE ---
                if (noArrowsViewModel.isRestMode.value) {
                    val totalRestTime = evaluateRestTime(selectionState.lastDurationSeconds, selectionState.numberOfRepetitions)
                    val restLeft = selectionState.currentRestTimeLeft ?: totalRestTime
                    if (restLeft > 0) {
                        adjustedDelay(selectionState.countDownDelay, selectionState.tickBaseTimeState)
                        if (noArrowsViewModel.isTimerStopped.value) continue
                        val nextRestTime = restLeft - 1
                        selectionState.currentRestTimeLeft = nextRestTime

                        // Play warning rest beep near end of rest (e.g. at 7 seconds remaining)
                        if (nextRestTime == selectionState.endOfRestBeepTime && nextRestTime > 0) {
                            playRestBeep()
                        }

                        if (nextRestTime == 0) {
                            val currentSeries = selectionState.currentSeriesLeft ?: 1
                            val nextSeries = currentSeries - 1
                            selectionState.currentSeriesLeft = nextSeries
                            if (nextSeries <= 0) {
                                completeSession(selectionState)
                                break
                            } else {
                                setEndOfRestMode()
                                selectionState.currentDurationSecondsLeft = selectionState.initialDurationSeconds
                                selectionState.currentRepetitionsLeft = selectionState.numberOfRepetitions
                                selectionState.currentRestTimeLeft = null
                                playBeep()
                            }
                        }
                    } else {
                        val currentSeries = selectionState.currentSeriesLeft ?: 1
                        val nextSeries = currentSeries - 1
                        selectionState.currentSeriesLeft = nextSeries
                        if (nextSeries <= 0) {
                            completeSession(selectionState)
                            break
                        } else {
                            setEndOfRestMode()
                            selectionState.currentDurationSecondsLeft = selectionState.initialDurationSeconds
                            selectionState.currentRepetitionsLeft = selectionState.numberOfRepetitions
                            selectionState.currentRestTimeLeft = null
                            playBeep()
                        }
                    }
                    continue
                }

                // --- 3. TIMER RUNNING (WORK REPETITION) ---
                if (noArrowsViewModel.isTimerRunning.value) {
                    val initialDuration = selectionState.initialDurationSeconds ?: 10
                    val currentDuration = selectionState.currentDurationSecondsLeft ?: initialDuration
                    val currentRepetitions = selectionState.currentRepetitionsLeft ?: (selectionState.numberOfRepetitions ?: 1)
                    val currentSeries = selectionState.currentSeriesLeft ?: (selectionState.numberOfSeries ?: 1)

                    if (currentDuration > 0) {
                        adjustedDelay(selectionState.countDownDelay, selectionState.tickBaseTimeState)
                        if (noArrowsViewModel.isTimerStopped.value) continue
                        val nextDuration = currentDuration - 1
                        selectionState.currentDurationSecondsLeft = nextDuration

                        // Play intermediate beep if enabled
                        if (selectionState.intermediateBeepsChecked == true && nextDuration > 0) {
                            val elapsed = initialDuration - nextDuration
                            if (elapsed > 0 && elapsed % selectionState.intermediateBeepsDuration == 0) {
                                playIntermediateBeep()
                            }
                        }

                        if (nextDuration == 0) {
                            val nextReps = currentRepetitions - 1
                            selectionState.currentRepetitionsLeft = nextReps

                            if (nextReps <= 0) {
                                // Series completed!
                                if (currentSeries <= 1) {
                                    completeSession(selectionState)
                                    break
                                } else {
                                    // Enter rest period
                                    setRestMode()
                                    val restTime = evaluateRestTime(selectionState.lastDurationSeconds, selectionState.numberOfRepetitions)
                                    selectionState.currentRestTimeLeft = restTime
                                    playRestBeep()
                                }
                            } else {
                                // Next repetition in current series
                                selectionState.currentDurationSecondsLeft = initialDuration
                                playBeep()
                            }
                        }
                    } else {
                        val nextReps = currentRepetitions - 1
                        selectionState.currentRepetitionsLeft = nextReps
                        if (nextReps <= 0) {
                            if (currentSeries <= 1) {
                                completeSession(selectionState)
                                break
                            } else {
                                setRestMode()
                                val restTime = evaluateRestTime(selectionState.lastDurationSeconds, selectionState.numberOfRepetitions)
                                selectionState.currentRestTimeLeft = restTime
                                playRestBeep()
                            }
                        } else {
                            selectionState.currentDurationSecondsLeft = initialDuration
                            playBeep()
                        }
                    }
                    continue
                }
            }
        } finally {
            keepScreenOn(false)
        }
    }

    private fun completeSession(selectionState: SelectionState) {
        sessionHasCompleted()
        selectionState.currentDurationSecondsLeft = 0
        selectionState.currentRepetitionsLeft = 0
        selectionState.currentSeriesLeft = 0
        selectionState.currentRestTimeLeft = 0
    }

    private suspend fun adjustedDelay(countDownDelay: Long, tickBaseTimeState: MutableState<Long>) {
        val t2 = System.currentTimeMillis()
        val d = countDownDelay - (t2 - tickBaseTimeState.value)
        if (d >= 0L) delay(d)
        tickBaseTimeState.value += countDownDelay
    }
}
