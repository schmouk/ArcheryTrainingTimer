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

package com.github.schmouk.archerytrainingtimer.ui.noarrowsession

import android.annotation.SuppressLint
import android.view.WindowManager

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope

import com.github.schmouk.archerytrainingtimer.DEBUG_MODE
import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.commons.ESignal
import com.github.schmouk.archerytrainingtimer.commons.UserPreferencesRepository
import com.github.schmouk.archerytrainingtimer.commons.SoundPlayer
import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowsTimerViewModel
import com.github.schmouk.archerytrainingtimer.services.AudioService
import com.github.schmouk.archerytrainingtimer.ui.commons.IntermediateBeepsCheckedRow
import com.github.schmouk.archerytrainingtimer.ui.commons.LogoImage
import com.github.schmouk.archerytrainingtimer.ui.commons.PleaseSelectText
import com.github.schmouk.archerytrainingtimer.ui.commons.RepetitionsDurationButtons
import com.github.schmouk.archerytrainingtimer.ui.commons.RepetitionsDurationTitle
import com.github.schmouk.archerytrainingtimer.ui.commons.RepetitionsNumberTitle
import com.github.schmouk.archerytrainingtimer.ui.commons.RepetitionsSelectorWithScrollIndicators
import com.github.schmouk.archerytrainingtimer.ui.commons.RestingDurationText
import com.github.schmouk.archerytrainingtimer.ui.commons.SeriesCountdownConstrainedBox
import com.github.schmouk.archerytrainingtimer.ui.commons.SeriesNumbersButtons
import com.github.schmouk.archerytrainingtimer.ui.commons.SeriesNumberTitle
import com.github.schmouk.archerytrainingtimer.ui.commons.SessionPreparationText
import com.github.schmouk.archerytrainingtimer.ui.commons.StartButtonRow
import com.github.schmouk.archerytrainingtimer.ui.commons.TimerCountdownConstrainedBox
import com.github.schmouk.archerytrainingtimer.ui.commons.ViewHeader
import com.github.schmouk.archerytrainingtimer.ui.theme.*
import com.github.schmouk.archerytrainingtimer.ui.theme.ProgressBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerBorderColor
import com.github.schmouk.archerytrainingtimer.ui.utils.considerDevicePortraitPositioned
import com.github.schmouk.archerytrainingtimer.ui.utils.detectDeviceFoldedPosture
import com.github.schmouk.archerytrainingtimer.ui.utils.EFoldedPosture

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


// Props for NoArrowsTimerScreen
// - userPreferencesRepository: UserPreferencesRepository
// - any other callbacks or data needed
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun NoArrowsTimerScreen(
    noArrowsViewModel: NoArrowsTimerViewModel,
    userPreferencesRepository: UserPreferencesRepository
) {
    // related finite state machine control values
    val isRestMode by noArrowsViewModel.isRestMode
    val isSessionCompleted by noArrowsViewModel.isSessionCompleted
    val isTimerRunning by noArrowsViewModel.isTimerRunning
    val isTimerStopped by noArrowsViewModel.isTimerStopped
    val isPreparationMode by noArrowsViewModel.isPreparationMode

    // The screen content
    Scaffold /*(
        // topBar is no more useful since we call
        // WindowCompat.setDecorFitsSystemWindows(window, true)
        // in the related/embedding Activity --> we don't draw behind system bars
        topBar = {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
            )
        }
        // We don't use Scaffold's bottomBar for this either
        // as we will pad the content area directly.
    )*/ { innerPaddingFromScaffold -> // This innerPadding from Scaffold handles the TOP spacer
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPaddingFromScaffold)
                //.padding(WindowInsets.navigationBars.asPaddingValues())  // <-----
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- Local Context ---
            val currentLocalContext = LocalContext.current

            // --- Scaling factor ---
            // Available size for the content
            val availableHeightForContentDp = this.maxHeight
            val availableWidthForContentDp = this.maxWidth

            val refScreenWidthDp = 411.dp // Our baseline for good proportions
            val refScreenHeightDp = 914.dp // Our baseline for good proportions

            // Calculate scale factor
            val textHorizontalScaleFactor =
                availableWidthForContentDp.value / refScreenWidthDp.value
            val horizontalScaleFactor = textHorizontalScaleFactor.coerceIn(0.60f, 1.0f)
            val verticalScaleFactor = (
                    availableHeightForContentDp.value / refScreenHeightDp.value
                    ).coerceIn(0.40f, 1.5f)
            val scaleFactor = min(horizontalScaleFactor, verticalScaleFactor)

            // scales a dimension (width or height) according to the deviceScaling factor of the running device
            fun deviceScaling(dim: Int): Float {
                return scaleFactor * dim
            }

            val heightScalingFactor =
                this.maxHeight.value / availableHeightForContentDp.value  //currentScreenHeightDp.value
            val widthScalingFactor =
                this.maxWidth.value / availableWidthForContentDp.value  //currentScreenWidthDp.value


            // --- Text styles ---
            val selectionTextFontSize = deviceScaling(18)  // Notice; to be used with .sp for specifying font size
            val customInteractiveTextStyle = TextStyle(fontSize = selectionTextFontSize.sp)
            val smallerTextStyle = TextStyle(fontSize = deviceScaling(16).sp)


            // --- Repetitions selector state ---
            val repetitionsLazyListState = rememberLazyListState()


            // --- Playing sound ---
            val audioManager = AudioService(currentLocalContext)
            val soundPlayer = SoundPlayer(
                currentLocalContext,
                audioManager
            )


            // --- Debug / Testing ---
            val countDownDelay = if (DEBUG_MODE) 600L else 1000L


            // --- Dynamic Sizes & SPs ---
            val mainTimerStrokeWidthDp = deviceScaling(14).dp
            val selectionItemsBaseSizeDp = deviceScaling(48).dp
            val majorSpacerHeight = deviceScaling(8).dp
            val generalPadding = deviceScaling(8).dp
            val mainHorizontalSpacingDp = deviceScaling(10).dp

            var selectedDurationString by rememberSaveable { mutableStateOf<String?>(null) }
            var numberOfRepetitions by remember { mutableStateOf<Int?>(null) }
            var numberOfSeries by remember { mutableStateOf<Int?>(null) }
            var intermediateBeepsChecked by remember { mutableStateOf<Boolean?>(null) }

            var lastDurationSeconds by rememberSaveable { mutableIntStateOf(0) }
            var lastNumberOfRepetitions by rememberSaveable { mutableIntStateOf(0) }
            var lastNumberOfSeries by rememberSaveable { mutableIntStateOf(0) }
            var lastIntermediateBeepsChecked by rememberSaveable { mutableStateOf(false) }

            var initialDurationSeconds by rememberSaveable { mutableStateOf<Int?>(null) }
            var currentDurationSecondsLeft by rememberSaveable { mutableStateOf<Int?>(null) }
            var currentRepetitionsLeft by rememberSaveable { mutableStateOf<Int?>(null) }
            var currentSeriesLeft by rememberSaveable { mutableStateOf<Int?>(null) }

            val minRepetitions = 3
            val maxRepetitions = 15
            val repetitionRange = (minRepetitions..maxRepetitions).toList()

            // --- Rest Mode & Series Tracking ---
            var currentRestTimeLeft by rememberSaveable { mutableStateOf<Int?>(null) }
            val endOfRestBeepTime = 7 // seconds before end of rest to play beep

            val durationOptions = listOf("10 s", "15 s", "20 s", "30 s")
            val seriesOptions = mutableListOf(1, 2, 3, 5, 10, 15, 20, 25, 30)
            val intermediateBeepsDuration = 5 // seconds for intermediate beeps

            val restModeText = stringResource(R.string.rest_indicator)

            val preparationTime = 7 // seconds for preparation time before start
            val resPreparationText = stringResource(R.string.preparation)
            var currentPreparationSecondsLeft by rememberSaveable { mutableStateOf<Int?>(null) }


            /**
             * Checks if all selections have been made
             */
            fun allSelectionsMade(): Boolean {
                return selectedDurationString != null &&
                        numberOfRepetitions != null &&
                        numberOfSeries != null
            }

            /**
             * Evaluates the dimmed status of displays.
             */
            fun isDimmedDisplay(): Boolean {
                return isTimerStopped || isSessionCompleted
            }

            /**
             * Actions associated to the completion of a session
             */
            fun sessionHasCompleted() {
                noArrowsViewModel.action(ESignal.SIG_COMPLETED)
                soundPlayer.playEndBeep(noArrowsViewModel.viewModelScope)
                currentDurationSecondsLeft = 0
                currentRepetitionsLeft = 0
                currentSeriesLeft = 0
            }

            /**
             * Evaluates the resting time ratio
             */
            fun evaluateRestingRatio(
                repetitionsDuration: Int,
                repetitionsNumberPerSeries: Int?
            ) : Float {
                val ratio: Float = if (repetitionsNumberPerSeries == null) {
                    0.5f
                }
                else if (repetitionsDuration <= 20){
                    1.1f - repetitionsDuration / 25f
                }
                else {
                    (0.3f - (repetitionsDuration - 20) / 50f).coerceAtLeast(0.0f)
                }

                return (100f * ratio).roundToInt().toFloat() / 100f
            }

            /**
             * Evaluates the resting time
             */
            fun evaluateRestTime(): Int {
                val ratio = evaluateRestingRatio(
                    lastDurationSeconds,
                    numberOfRepetitions
                )
                return ((numberOfRepetitions ?: 0) * lastDurationSeconds * ratio)  //restingRatio)
                    .roundToInt()
                    //.coerceAtLeast(endOfRestBeepTime + 2) // Ensure rest gets a minimum value for the beep logic
            }


            /**
             * Starts the preparation mode before starting countdowns
             */
            fun startPreparationMode() {
                noArrowsViewModel.action(ESignal.SIG_PREPARE)
                currentPreparationSecondsLeft = preparationTime
            }

            /**
             * Starts or Restarts a new session
             */
            fun startNewSession() {
                // Trying to Start (or Restart after session completion)
                if (allSelectionsMade()) {
                    // If currentRepetitionsLeft is 0, it means a cycle just finished (dimmed state).
                    // Reset both countdowns for a new cycle.
                    if (currentSeriesLeft == null || currentSeriesLeft == 0) {
                        currentSeriesLeft = numberOfSeries
                        currentRepetitionsLeft = numberOfRepetitions
                        currentDurationSecondsLeft = initialDurationSeconds
                        currentPreparationSecondsLeft = preparationTime
                    } else {
                        // Handle cases where selections might have been cleared or timer never run
                        if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                            currentDurationSecondsLeft = initialDurationSeconds
                        }
                        if (currentRepetitionsLeft == null) { // This should ideally not happen if allSelectionsMade is true
                            currentRepetitionsLeft = numberOfRepetitions
                        }
                    }
                    startPreparationMode()
                }
            }

            /**
             * Starts countdown
             */
            fun startCountdowns() {
                noArrowsViewModel.action(ESignal.SIG_START)
                currentDurationSecondsLeft = initialDurationSeconds
                // currentRepetitionsLeft = numberOfRepetitions
                // currentSeriesLeft = numberOfSeries
            }

            /**
             * Pauses countdown
             */
            fun pauseCountdowns() {
                noArrowsViewModel.action(ESignal.SIG_STOP)
            }

            /**
             * Resumes countdown
             */
            fun resumeCountdowns() {
                noArrowsViewModel.action(ESignal.SIG_START)
            }

            /**
             * Sets resting mode
             */
            fun setRestMode() {
                noArrowsViewModel.action(ESignal.SIG_REST_ON)
            }

            /**
             * Quits resting mode
             */
            fun setEndOfRestMode() {
                noArrowsViewModel.action(ESignal.SIG_REST_OFF)
            }

            /**
             * Sets future resting mode
             */
            fun setFutureRestMode() {
                noArrowsViewModel.action(ESignal.SIG_WILL_REST)
            }

            /**
             * Evaluates the new or next resting mode
             */
            fun evaluateRestingMode() {
                currentDurationSecondsLeft = 0
                currentRestTimeLeft = evaluateRestTime()

                if (currentSeriesLeft!! <= 1) {
                    // To not have rest time launched if this was the last series
                    currentSeriesLeft = 0
                } else if (isTimerStopped) {
                    setFutureRestMode()
                } else {
                    setRestMode()
                }
            }

            /**
             * Loads user preferences on first composition
             */
            LaunchedEffect(key1 = Unit) {
                userPreferencesRepository.userPreferencesFlow.collect { loadedPrefs ->
                    selectedDurationString = loadedPrefs.selectedDuration
                    numberOfRepetitions = loadedPrefs.numberOfRepetitions
                    numberOfSeries = loadedPrefs.numberOfSeries
                    intermediateBeepsChecked = loadedPrefs.intermediateBeeps

                    if (!isTimerRunning && !isRestMode) {
                        val durationValue =
                            loadedPrefs.selectedDuration?.split(" ")?.firstOrNull()
                                ?.toIntOrNull()
                        initialDurationSeconds = durationValue
                        if (currentRepetitionsLeft != 0 && !isTimerStopped) { // Only reset if not in a "completed or dimmed" state
                            currentDurationSecondsLeft = durationValue
                        }
                        if (!isTimerStopped) {
                            currentRepetitionsLeft = numberOfRepetitions
                            currentSeriesLeft = numberOfSeries
                        }

                        lastDurationSeconds = durationValue ?: 0
                        lastNumberOfRepetitions = numberOfRepetitions ?: 0
                        lastNumberOfSeries = numberOfSeries ?: 0
                    }
                }
            }

            /**
             * Updates initial/current countdown values when selections change
             */
            LaunchedEffect(
                selectedDurationString,
                numberOfRepetitions,
                numberOfSeries,
                intermediateBeepsChecked
            ) {
                if (selectedDurationString != null) {
                    val durationValue =
                        selectedDurationString?.split(" ")?.firstOrNull()?.toIntOrNull()
                    if (durationValue != null && durationValue != lastDurationSeconds) {
                        initialDurationSeconds = durationValue
                        currentDurationSecondsLeft = if (isRestMode) {
                            initialDurationSeconds
                        } else {
                            min(
                                max(
                                    1,
                                    (currentDurationSecondsLeft
                                        ?: 0) + durationValue - lastDurationSeconds
                                ),
                                durationValue
                            )
                        }
                        lastDurationSeconds = durationValue

                        if (currentDurationSecondsLeft!! <= 1 && currentRepetitionsLeft!! <= 1) {
                            evaluateRestingMode()
                        }

                        userPreferencesRepository.saveDurationPreference(selectedDurationString)
                    }
                }

                if (numberOfRepetitions != null && numberOfRepetitions != lastNumberOfRepetitions) {
                    if (!isRestMode) {
                        currentRepetitionsLeft = min(
                            max(
                                0,
                                (currentRepetitionsLeft
                                    ?: 0) + numberOfRepetitions!! - lastNumberOfRepetitions
                            ),
                            numberOfRepetitions!!
                        )
                        if (currentRepetitionsLeft == 0) {
                            evaluateRestingMode()
                        }
                    } else {
                        currentDurationSecondsLeft = 0
                    }
                    lastNumberOfRepetitions = numberOfRepetitions!!
                    userPreferencesRepository.saveRepetitionsPreference(numberOfRepetitions)
                }

                if (numberOfSeries != null && numberOfSeries != lastNumberOfSeries) {
                    currentSeriesLeft =
                        max(0, (currentSeriesLeft ?: 0) + numberOfSeries!! - lastNumberOfSeries)
                    lastNumberOfSeries = numberOfSeries!!
                    userPreferencesRepository.saveSeriesPreference(numberOfSeries)
                    //if (currentSeriesLeft == 0 || (sessionAutomaton.isRestMode() && currentSeriesLeft == 1)) {
                    if (currentSeriesLeft == 0 || (isRestMode && currentSeriesLeft!! <= 1)) {
                        currentRestTimeLeft =
                            0  // To not get rest time if this was the last series
                        currentDurationSecondsLeft = 0
                        currentRepetitionsLeft = 0
                        currentSeriesLeft = 0
                        if (isRestMode) {
                            sessionHasCompleted()
                            //noArrowsViewModel.action(ESignal.SIG_COMPLETED)
                            //playEndBeepEvent = true
                        } else if (isTimerStopped) {
                            resumeCountdowns()
                        }
                    }
                }

                if (intermediateBeepsChecked != null && intermediateBeepsChecked != lastIntermediateBeepsChecked) {
                    userPreferencesRepository.saveIntermediateBeepsPreference(
                        intermediateBeepsChecked ?: false
                    )
                    lastIntermediateBeepsChecked = intermediateBeepsChecked!!
                }
            }


            /**
             * Manages the timer countdown in a coroutine
             *
             * This is the core of the timer logic, managing countdowns, repetitions, series,
             * rest periods, and state transitions.
             * It reacts to changes in isTimerRunning and isRestMode states.
             */
            LaunchedEffect(isTimerRunning, isRestMode, isPreparationMode) {
                if (isTimerRunning || isRestMode || isPreparationMode) {
                    (this as? ComponentActivity)?.window?.addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                } else {
                    (this as? ComponentActivity)?.window?.clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                }

                while (isActive && (isTimerRunning || isRestMode || isTimerStopped || isPreparationMode)) {
                    // --- Normal Repetition Countdown ---
                    if (!isRestMode) {
                        // Ensure values are sane before starting countdown loop
                        // If starting from a dimmed state (reps=0, duration=0), reset them.
                        if (currentRepetitionsLeft == 0) { // Indicates a previous cycle was completed
                            currentRepetitionsLeft = numberOfRepetitions  // Reset for new cycle
                            if (currentDurationSecondsLeft == 0)
                                currentDurationSecondsLeft =
                                    initialDurationSeconds  // Reset for new cycle
                            currentSeriesLeft = currentSeriesLeft!! - 1
                        } else { // Normal start or resume
                            if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                                currentDurationSecondsLeft = initialDurationSeconds
                            }
                            if (currentRepetitionsLeft == null) {
                                currentRepetitionsLeft = numberOfRepetitions
                            }
                            if (currentSeriesLeft == null) {
                                currentSeriesLeft = numberOfSeries  //numberOfSeries
                            }
                        }

                        if (isPreparationMode) {
                            delay(countDownDelay)
                            currentPreparationSecondsLeft = currentPreparationSecondsLeft!! - 1
                            if (currentPreparationSecondsLeft == 0)
                                startCountdowns()
                        }
                        else {
                            while (isActive && currentSeriesLeft!! > 0) {
                                if (currentDurationSecondsLeft!! == initialDurationSeconds!!) {
                                    soundPlayer.playBeep(noArrowsViewModel.viewModelScope)
                                }

                                if (currentDurationSecondsLeft!! > 0) {
                                    // current repetition timer tick
                                    if (isTimerRunning)
                                        delay(countDownDelay)
                                    if (!isTimerStopped)
                                        currentDurationSecondsLeft =
                                            currentDurationSecondsLeft!! - 1
                                    // intermediate beep logic
                                    if (intermediateBeepsChecked != null &&
                                        intermediateBeepsChecked == true &&
                                        currentDurationSecondsLeft != null &&
                                        currentDurationSecondsLeft!! > 0 &&
                                        (initialDurationSeconds!! - currentDurationSecondsLeft!!) % intermediateBeepsDuration == 0
                                    ) {
                                        //playIntermediateBeep = true
                                        soundPlayer.playIntermediateBeep(noArrowsViewModel.viewModelScope)
                                    }
                                } else if (currentDurationSecondsLeft!! == 0) {
                                    // end of current repetition duration
                                    if (currentRepetitionsLeft != null && currentRepetitionsLeft!! > 0) {
                                        // go to next repetition in current series
                                        currentRepetitionsLeft = currentRepetitionsLeft!! - 1

                                        if (currentRepetitionsLeft == 0) {
                                            // this was the last repetition in current series
                                            if (currentSeriesLeft != null && currentSeriesLeft!! > 0) {
                                                // then, count down series number
                                                if (currentSeriesLeft == 1) {
                                                    // If no more series left, stop the timer and show dimmed state
                                                    sessionHasCompleted()
                                                    break
                                                } else {
                                                    // enters the rest mode
                                                    setRestMode()
                                                    currentRestTimeLeft = evaluateRestTime()
                                                }
                                            } else {
                                                // --> this is the end of the training session
                                                // If no more series left, stop the timer and show dimmed state
                                                sessionHasCompleted()
                                                break
                                            }
                                        } else {
                                            // let's start a new repetition into current series
                                            currentDurationSecondsLeft = initialDurationSeconds
                                        }
                                    } else {
                                        // end of current series
                                        // Notice: if end of session also, will be checked in the outer loop
                                        setRestMode()
                                        break
                                    }
                                }

                                if (isTimerStopped) {
                                    break
                                }
                            }
                        }
                    }

                    // Have to check this since it may have been set in the block above
                    if (currentSeriesLeft!! <= 0) {
                        // If no more series left, stop the timer and show dimmed state
                        sessionHasCompleted()
                    } else if (isRestMode) {
                        // --- Rest Mode Countdown ---
                        if ((currentRestTimeLeft ?: 0) == evaluateRestTime()) {
                            //playRestBeepEvent = true
                            soundPlayer.playRestBeep(noArrowsViewModel.viewModelScope)
                        }

                        // Check isRestMode again, as it could have been modified in the block above
                        while (isActive) {  // Notice: isRestMode is always true here
                            if (currentRestTimeLeft != null && currentRestTimeLeft!! > 0) {
                                // Check for some seconds left --> to play rest-beeps
                                if (currentRestTimeLeft == endOfRestBeepTime) {
                                    soundPlayer.playRestBeep(noArrowsViewModel.viewModelScope)
                                }
                                delay(countDownDelay)
                                currentRestTimeLeft = currentRestTimeLeft!! - 1
                            } else {
                                // Rest time ended (currentRestTimeLeft is 0 or null)
                                setEndOfRestMode()
                                currentRepetitionsLeft = numberOfRepetitions // Reset for new cycle
                                currentSeriesLeft = currentSeriesLeft!! - 1
                                break // Exit rest loop
                            }
                        }
                    }

                    if (isTimerStopped)
                        break
                }
            }


            // -------------------------------------------------
            // --- Different functions for responsive layout ---
            // -------------------------------------------------

            //-- The Start button stuff --
            val buttonScaling = 1f / 17.8f
            val buttonHeight =
                (availableHeightForContentDp.value * buttonScaling).coerceAtLeast(32f)

            /**
             * The Start button on-click lambda
             */
            val onStartButtonClick = {
                if (isTimerRunning) {
                    pauseCountdowns()
                } else if (isTimerStopped) {
                    resumeCountdowns()
                } else {
                    // Trying to Start (or Restart after session completion)
                    startNewSession()
                }
            }


            // ------------------------------------------------------
            // --- The different blocks of UI Items (DRY concept) ---
            // ------------------------------------------------------

            /**
             * The screen title block
             */
            @Composable
            fun ViewTitleBlock() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ViewHeader(
                        stringResource(id = R.string.series_view_title),
                        Modifier
                            .padding(bottom = generalPadding)
                            .scale(scaleFactor)
                    )
                }
            }

            /**
             * The Countdowns block
             */
            @Composable
            fun CountdownsBlock(
                countdownsRowModifier: Modifier
            ) {
                // Shows the start button row
                StartButtonRow(
                    allSelectionsMade(),
                    isPreparationMode,
                    isTimerRunning,
                    isRestMode,
                    customInteractiveTextStyle,
                    buttonHeight,
                    onStartButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = deviceScaling(8).dp)
                        .height(buttonHeight.dp),
                    rowHorizontalArrangement = Arrangement.Center
                )


                // --- 3. Second Row: Timer and Countdowns ---
                var rowSize by remember { mutableStateOf(IntSize.Zero) }

                Row(
                    modifier = countdownsRowModifier
                        .fillMaxWidth()
                        .background(AppTimerRowBackgroundColor)
                        .padding(vertical = deviceScaling(4).dp)
                        .onSizeChanged { newSize -> rowSize = newSize }
                        .let {
                            // Conditionally apply the clickable modifier
                            if (allSelectionsMade() && !isRestMode) {
                                // Notice: still clickable while in preparation mode,
                                // this will restart the preparation countdown and is
                                // only available when clicking this timer and count-
                                // down row
                                it.clickable(
                                    interactionSource = remember { MutableInteractionSource() }, // To disable ripple if desired
                                    indication = null, // Set to 'LocalIndication.current' for default ripple or custom
                                    onClick = onStartButtonClick
                                )
                            } else {
                                it // Not clickable if conditions aren't met
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    val currentRowWidth = rowSize.width
                    val currentRowHeight = rowSize.height

                    if (currentRowHeight >= 1.05f * currentRowWidth) {
                        //-- This a a higher than wide row, let's split it into a two-cells column
                        val upperCellHeightRatio = 0.7f

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 0.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //-- Upper Cell (Big Timer Display) --
                            TimerCountdownConstrainedBox(
                                selectedDurationString,
                                initialDurationSeconds,
                                currentDurationSecondsLeft,
                                numberOfRepetitions,
                                currentRepetitionsLeft,
                                currentRestTimeLeft,
                                currentPreparationSecondsLeft,
                                isPreparationMode,
                                isTimerRunning,
                                isTimerStopped,
                                isDimmedDisplay(),
                                isRestMode,
                                restModeText,
                                resPreparationText,
                                mainTimerStrokeWidthDp,
                                TimerBorderColor,
                                DimmedTimerBorderColor,
                                TimerRestColor,
                                ProgressBorderColor,
                                DimmedProgressBorderColor,
                                TimerPreparationColor,
                                heightScalingFactor,
                                widthScalingFactor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(upperCellHeightRatio),
                                boxContentAlignment = Alignment.Center
                            )

                            //-- Lower Control Cell (Small Series Countdown Display) --
                            val seriesStrokeWidthPx = with(LocalDensity.current) {
                                deviceScaling(7).dp.toPx()
                            }

                            val localPaddingPx = with(LocalDensity.current) {
                                deviceScaling(8).dp.toPx()
                            }

                            SeriesCountdownConstrainedBox(
                                initialDurationSeconds,
                                currentDurationSecondsLeft,
                                numberOfRepetitions,
                                currentRepetitionsLeft,
                                numberOfSeries,
                                currentSeriesLeft,
                                isPreparationMode,
                                isTimerRunning,
                                isTimerStopped,
                                isDimmedDisplay(),
                                TimerBorderColor,
                                DimmedTimerBorderColor,
                                TimerRestColor,
                                ProgressBorderColor,
                                DimmedProgressBorderColor,
                                seriesStrokeWidthPx,
                                localPaddingPx,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f - upperCellHeightRatio)
                            )
                        }
                    }
                    else {
                        //-- This is a not-that-high row, let's split it into two horizontally arranged cells
                        val leftCellHeightRatio = 0.7f

                        //-- Left Cell (Big Timer Display) --
                        TimerCountdownConstrainedBox(
                            selectedDurationString,
                            initialDurationSeconds,
                            currentDurationSecondsLeft,
                            numberOfRepetitions,
                            currentRepetitionsLeft,
                            currentRestTimeLeft,
                            currentPreparationSecondsLeft,
                            isPreparationMode,
                            isTimerRunning,
                            isTimerStopped,
                            isDimmedDisplay(),
                            isRestMode,
                            restModeText,
                            resPreparationText,
                            mainTimerStrokeWidthDp,
                            TimerBorderColor,
                            DimmedTimerBorderColor,
                            TimerRestColor,
                            ProgressBorderColor,
                            DimmedProgressBorderColor,
                            TimerPreparationColor,
                            heightScalingFactor,
                            widthScalingFactor,
                            modifier = Modifier
                                .weight(leftCellHeightRatio)
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            boxContentAlignment = Alignment.Center
                        )

                        //-- Right Control Cell (Small Series Countdown Display) --
                        val seriesStrokeWidthPx = with(LocalDensity.current) {
                            deviceScaling(7).dp.toPx()
                        }

                        val localPaddingPx = with(LocalDensity.current) {
                            deviceScaling(8).dp.toPx()
                        }

                        SeriesCountdownConstrainedBox(
                            initialDurationSeconds,
                            currentDurationSecondsLeft,
                            numberOfRepetitions,
                            currentRepetitionsLeft,
                            numberOfSeries,
                            currentSeriesLeft,
                            isPreparationMode,
                            isTimerRunning,
                            isTimerStopped,
                            isDimmedDisplay(),
                            TimerBorderColor,
                            DimmedTimerBorderColor,
                            TimerRestColor,
                            ProgressBorderColor,
                            DimmedProgressBorderColor,
                            seriesStrokeWidthPx,
                            localPaddingPx,
                            modifier = Modifier.weight(1f - leftCellHeightRatio),
                            boxContentAlignment = Alignment.Center
                        )
                    }
                }
            }

            /**
             * The Selection Items block
             */
            @Composable
            fun SelectionItemsBlock() {
                // --- 4. SECTION FOR SELECTABLE ITEMS & RELATED TEXTS ---
                // (Repetitions duration, Number of repetitions, etc.)
                // This section appears *under* the countdowns and has its own height.
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // Take full width
                        .wrapContentHeight() // Take only necessary vertical space for its content
                        .padding(top = deviceScaling(16).dp, bottom = deviceScaling(4).dp),
                ) {
                    if (isPreparationMode) {
                        // Shows the "Get Ready" text only if in preparation mode
                        SessionPreparationText(
                            preparationTime,
                            smallerTextStyle,
                            Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                    else if (allSelectionsMade()) {
                        // Shows the resting duration
                        RestingDurationText(
                            evaluateRestTime(),
                            evaluateRestingRatio(
                                lastDurationSeconds,
                                numberOfRepetitions
                            ),
                            numberOfSeries,
                            smallerTextStyle,
                            Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                    else {
                        // Shows the "Please select ..." text only if not all selections have been made
                        PleaseSelectText(
                            smallerTextStyle,
                            Modifier.align(Alignment.CenterHorizontally),
                        )
                    }

                    //-- Shows the block for the selection of durations of repetitions --
                    Spacer(modifier = Modifier.height(majorSpacerHeight))

                    // Title first
                    RepetitionsDurationTitle(
                        customInteractiveTextStyle,
                        Modifier
                            .padding(bottom = generalPadding)
                            .wrapContentHeight()
                            .align(Alignment.CenterHorizontally),
                    )

                    // Then row of duration buttons
                    RepetitionsDurationButtons(
                        selectedDurationString = selectedDurationString,
                        onDurationSelected = { newDuration ->
                            selectedDurationString = newDuration
                        },
                        durationOptions = durationOptions,
                        borderStrokeWidth = deviceScaling(5).dp,
                        durationButtonHeight = selectionItemsBaseSizeDp,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )


                    //-- Shows the block for the selection of number of repetitions --
                    Spacer(modifier = Modifier.height(majorSpacerHeight))

                    // The block title for repetitions numbers
                    RepetitionsNumberTitle(
                        customInteractiveTextStyle,
                        Modifier
                            .padding(bottom = generalPadding)
                            .wrapContentHeight()
                            .align(Alignment.CenterHorizontally)
                    )

                    // Then the actual selector
                    RepetitionsSelectorWithScrollIndicators(
                        // Repetition lazy row with arrows
                        selectedNumberOfRepetitions = numberOfRepetitions, //The state variable for the current selection
                        onRepetitionSelected = { selected -> numberOfRepetitions = selected },
                        repetitionsListState = repetitionsLazyListState, // Pass the state
                        repetitionsRange = repetitionRange,
                        numbersTextStyle = customInteractiveTextStyle,
                        arrowButtonSizeDp = deviceScaling(24).dp,
                        horizontalSpaceArrangement = deviceScaling(8).dp,
                        repetitionBoxSize = selectionItemsBaseSizeDp, //deviceScaling(48).dp,
                        borderStrokeWidth = deviceScaling(4).dp,
                    )


                    //-- Shows the block for the selection of number of series --
                    Spacer(modifier = Modifier.height(majorSpacerHeight * 1.8f))

                    // The block title
                    SeriesNumberTitle(
                        customInteractiveTextStyle,
                        Modifier
                            .padding(bottom = generalPadding)
                            .wrapContentHeight()
                            .align(Alignment.CenterHorizontally)
                    )

                    // Then the actual selector
                    // Checks first the available display width against the series-options list width
                    with(LocalDensity.current) {
                        val horizontalSpaceArrangementPx = deviceScaling(8).dp.toPx()
                        val seriesBoxSizePx = selectionItemsBaseSizeDp.toPx()
                        val visibleWidth =
                            availableWidthForContentDp.toPx() - 2 * mainHorizontalSpacingDp.toPx()
                        var removedIndex = 1

                        while (seriesOptions.size > removedIndex &&
                               seriesOptions.size * (seriesBoxSizePx + horizontalSpaceArrangementPx) -
                                   horizontalSpaceArrangementPx > visibleWidth
                        ) {
                            // Removes one of the Series number, let's says the one in second position (index 1)
                            // but only if this is NOT the currently selected number of series
                            if (seriesOptions[removedIndex] == numberOfSeries)
                                removedIndex++
                            else
                                seriesOptions.removeAt(removedIndex)
                        }
                    }

                    // Then displays the selector row
                    SeriesNumbersButtons(
                        numberOfSeries = numberOfSeries,
                        onNumberSelected = { seriesCount: Int -> numberOfSeries = seriesCount },
                        seriesOptions = seriesOptions,
                        borderStrokeWidth = deviceScaling(4).dp,
                        seriesBoxSize = selectionItemsBaseSizeDp,  //seriesBoxSize,
                        textStyle = customInteractiveTextStyle,
                        horizontalSpacing = deviceScaling(10).dp,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )


                    //-- Checkbox for intermediate beeps --
                    Spacer(modifier = Modifier.height(majorSpacerHeight * 0.6f))

                    // Shows the whole row, which is toggleable - not just the checkbox
                    IntermediateBeepsCheckedRow(
                        intermediateBeepsChecked = intermediateBeepsChecked,
                        allSelectionsMade = allSelectionsMade(),
                        scaleFactor = scaleFactor,
                        horizontalSpacer = deviceScaling(6).dp,
                        textStyle = smallerTextStyle,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(top = deviceScaling(4).dp, bottom = 0.dp)
                            .toggleable(
                                value = intermediateBeepsChecked ?: false,
                                role = Role.Checkbox,
                                enabled = allSelectionsMade(),
                                onValueChange = {
                                    intermediateBeepsChecked = !intermediateBeepsChecked!!
                                }
                            )
                            .align(Alignment.CenterHorizontally),
                    )
                }
            }

            /**
             * The bottom-right logo image
             */
            @Composable
            fun BottomEndLogoImage() {
                LogoImage(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = deviceScaling(16).dp)
                        .size(deviceScaling(34).dp)
                )
            }

            // ---------------------------------------------------------------------
            // --- A Main Column for the entire screen content (portrait layout) ---
            // ---------------------------------------------------------------------
            @Composable
            fun OneColumn() {
                Column(
                    modifier = Modifier
                        .padding(horizontal = mainHorizontalSpacingDp)
                        .fillMaxSize(), // Fill the BoxWithConstraints
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Add verticalArrangement as needed, e.g., Arrangement.SpaceAround
                ) {
                    // Title of Series View
                    ViewTitleBlock()

                    // The block of countdowns
                    CountdownsBlock(Modifier.weight(1f))

                    // The block of selection items & related texts
                    // This block takes the remaining height
                    SelectionItemsBlock()
                }

                // Finally, add Logo Here - Aligned to Bottom-Right
                BottomEndLogoImage()
            }


            // ----------------------------------------------------------------------------
            // --- Two Columns for the entire screen content (landscape or book layout) ---
            // ----------------------------------------------------------------------------
            @Composable
            fun TwoColumns(equallySized: Boolean = false) {
                Column(modifier = Modifier
                    .padding(horizontal = mainHorizontalSpacingDp)
                    .fillMaxSize()
                ) {
                    // Title of Series View
                    ViewTitleBlock()

                    // The two main columns in next row
                    Row(
                        modifier = Modifier
                            .padding(mainHorizontalSpacingDp)
                            .fillMaxSize()
                    ) {
                        val leftWeight = if (equallySized) 0.5f else 0.6f
                        val rightWeight = 1f - leftWeight

                        // Left column with start button and timer countdowns
                        Column(
                            modifier = Modifier
                                .weight(leftWeight)
                                .padding(end = mainHorizontalSpacingDp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CountdownsBlock(Modifier.weight(1f))
                        }

                        // Right column with the selection items
                        Column(
                            modifier = Modifier
                                .weight(rightWeight)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            SelectionItemsBlock()
                        }
                    }
                }

                // Finally, add Logo here - Aligned to Bottom-Right
                BottomEndLogoImage()
            }


            // --- Two rows for the entire screen content (laptop layout) ---
            @Composable
            fun TwoRows() {
                Column(
                    modifier = Modifier
                        .padding(horizontal = mainHorizontalSpacingDp)
                        .fillMaxSize(), // Fill the BoxWithConstraints
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Add verticalArrangement as needed, e.g., Arrangement.SpaceAround
                ) {
                    // Title of Series View
                    ViewTitleBlock()

                    // Upper row with Start button and countdowns
                    val upperNearlyHalfWeight = 0.47f
                    Row(
                        modifier = Modifier
                            .padding(mainHorizontalSpacingDp)
                            .fillMaxWidth()
                            .weight(upperNearlyHalfWeight), // nearly half of the available height
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(), // Fill the BoxWithConstraints
                            horizontalAlignment = Alignment.CenterHorizontally,
                            // Add verticalArrangement as needed, e.g., Arrangement.SpaceAround
                        ) {
                            CountdownsBlock(Modifier.weight(1f))
                        }
                    }

                    // Lower row with selection items
                    Row(
                        modifier = Modifier
                            .padding(mainHorizontalSpacingDp)
                            .fillMaxWidth()
                            .weight(1f - upperNearlyHalfWeight), // the other nearly half of the available height
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SelectionItemsBlock()
                    }
                }

                // Finally, add Logo Here - Aligned to Bottom-Right
                BottomEndLogoImage()
            }


            // --- UI Layout ---
            val isPortraitPosition : Boolean = considerDevicePortraitPositioned()

            when (detectDeviceFoldedPosture()) {
                EFoldedPosture.POSTURE_NOT_FOLDED -> {
                    // Device is not folded
                    if (isPortraitPosition)
                        OneColumn()
                    else
                        TwoColumns()
                }

                EFoldedPosture.POSTURE_FLAT -> {
                    // Device is fully open flat (180 degrees)
                    if (isPortraitPosition)
                        OneColumn()
                    else
                        TwoColumns(true)
                }

                EFoldedPosture.POSTURE_BOOK_LIKE -> {
                    // Device is half-open (90 degrees, vertical)
                    TwoColumns(true)
                }

                EFoldedPosture.POSTURE_LAPTOP_LIKE -> {
                    // Device is half-open (90 degrees, horizontal)
                    TwoRows()
                }

                else -> {
                    // Unknown folded posture, should act as being not folded
                    if (isPortraitPosition)
                        OneColumn()
                    else
                        TwoColumns(true)
                }
            }
        }
    }
}
