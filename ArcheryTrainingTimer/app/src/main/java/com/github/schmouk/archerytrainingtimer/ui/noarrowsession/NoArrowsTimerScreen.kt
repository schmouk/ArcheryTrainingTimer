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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.MutableState
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

import com.github.schmouk.archerytrainingtimer.SECOND_DURATION_MS
import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.commons.ESignal
import com.github.schmouk.archerytrainingtimer.commons.EState
import com.github.schmouk.archerytrainingtimer.commons.SoundPlayer
import com.github.schmouk.archerytrainingtimer.commons.TimerInternalRunningState
import com.github.schmouk.archerytrainingtimer.commons.UserPreferencesRepository
import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowSessionController
import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowSessionReducer
import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowSessionState
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
import com.github.schmouk.archerytrainingtimer.ui.commons.SessionDurationDisplay
import com.github.schmouk.archerytrainingtimer.ui.commons.SessionPreparationText
import com.github.schmouk.archerytrainingtimer.ui.commons.StartButtonRow
import com.github.schmouk.archerytrainingtimer.ui.commons.TimerCountdownConstrainedBox
import com.github.schmouk.archerytrainingtimer.ui.commons.DurationSessionController
import com.github.schmouk.archerytrainingtimer.ui.commons.ViewHeader
import com.github.schmouk.archerytrainingtimer.ui.theme.*
import com.github.schmouk.archerytrainingtimer.ui.theme.ProgressBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerBorderColor
import com.github.schmouk.archerytrainingtimer.ui.utils.considerDevicePortraitPositioned
import com.github.schmouk.archerytrainingtimer.ui.utils.detectDeviceFoldedPosture
import com.github.schmouk.archerytrainingtimer.ui.utils.EFoldedPosture

import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min


// --- Session Duration Management ---
private val sessionDurationManager = DurationSessionController()


// --- Delay Adjustment ---
suspend fun adjustedDelay(countDownDelay: Long, tickBaseTimeState: MutableState<Long>) {
    val t2 = System.currentTimeMillis()
    val d = countDownDelay - (t2 - tickBaseTimeState.value)
    tickBaseTimeState.value += countDownDelay
    delay(if (d >= 0L) d else 0L)
}

// Props for NoArrowsTimerScreen
// - userPreferencesRepository: UserPreferencesRepository
// - any other callbacks or data needed
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun NoArrowsTimerScreen(
    noArrowsViewModel: NoArrowsTimerViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    formerAutomatonState: EState? = null,
    formerInternalRunningValues: TimerInternalRunningState = TimerInternalRunningState()
) {
    // restore the former automaton state if specified
    noArrowsViewModel.setStateAutomaton(formerAutomatonState)

    // related finite state machine control values
    val isRestMode by noArrowsViewModel.isRestMode
    val isSessionCompleted by noArrowsViewModel.isSessionCompleted
    val isTimerRunning by noArrowsViewModel.isTimerRunning
    val isTimerStopped by noArrowsViewModel.isTimerStopped
    val isPreparationMode by noArrowsViewModel.isPreparationMode

    // The screen content
    Scaffold {
        innerPaddingFromScaffold -> // Notice: This innerPadding from Scaffold handles the TOP spacer
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPaddingFromScaffold)
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
                this.maxHeight.value / availableHeightForContentDp.value
            val widthScalingFactor =
                this.maxWidth.value / availableWidthForContentDp.value


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
            val countDownDelay = SECOND_DURATION_MS

            val sessionController = remember {
                NoArrowSessionController(
                    noArrowsViewModel = noArrowsViewModel,
                    soundPlayer = soundPlayer,
                    scope = noArrowsViewModel.viewModelScope,
                    sessionDurationManager = sessionDurationManager
                )
            }

            // --- To accurately evaluate delays in countdowns ---
            val tickBaseTimeState = remember { mutableLongStateOf(System.currentTimeMillis()) }

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
                sessionController.sessionHasCompleted()
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
            ): Float = NoArrowSessionReducer.evaluateRestingRatio(
               repetitionsDuration,
               repetitionsNumberPerSeries
            )

            /**
             * Evaluates the resting time
             */
            fun evaluateRestTime(): Int = NoArrowSessionReducer.evaluateRestTime(
               lastDurationSeconds,
               numberOfRepetitions
            )


            /**
             * Starts the preparation mode before starting countdowns
             */
            fun startPreparationMode() {
                val updatedState = NoArrowSessionReducer.startPreparationMode(
                    NoArrowSessionState(
                        currentPreparationSecondsLeft = currentPreparationSecondsLeft,
                        isPreparationMode = isPreparationMode,
                        isRestMode = isRestMode,
                        isTimerRunning = isTimerRunning,
                        isTimerStopped = isTimerStopped,
                    ),
                    preparationTime
                )
                currentPreparationSecondsLeft = updatedState.currentPreparationSecondsLeft
                noArrowsViewModel.action(ESignal.SIG_PREPARE)
            }

            /**
             * Starts or Restarts a new session
             */
            fun startNewSession() {
               if (allSelectionsMade()) {
                   if (currentSeriesLeft == null || currentSeriesLeft == 0) {
                       currentSeriesLeft = numberOfSeries
                       currentRepetitionsLeft = numberOfRepetitions
                       currentDurationSecondsLeft = initialDurationSeconds
                       currentPreparationSecondsLeft = preparationTime
                   } else {
                       if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                           currentDurationSecondsLeft = initialDurationSeconds
                       }
                       if (currentRepetitionsLeft == null) {
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
               val nextState = NoArrowSessionReducer.startCountdowns(
                   NoArrowSessionState(
                       initialDurationSeconds = initialDurationSeconds,
                       currentDurationSecondsLeft = currentDurationSecondsLeft,
                       isPreparationMode = isPreparationMode,
                       isTimerRunning = isTimerRunning,
                       isTimerStopped = isTimerStopped,
                   )
               )
               currentDurationSecondsLeft = nextState.currentDurationSecondsLeft
               noArrowsViewModel.action(ESignal.SIG_START)
            }

            /**
             * Pauses countdown
             */
            fun pauseCountdowns() = sessionController.pauseCountdowns(tickBaseTimeState)

            /**
             * Resumes countdown
             */
            fun resumeCountdowns() = sessionController.resumeCountdowns(tickBaseTimeState)

            /**
             * Sets resting mode
             */
            fun setRestMode() = sessionController.setRestMode()

            /**
             * Quits resting mode
             */
            fun setEndOfRestMode() = sessionController.setEndOfRestMode()

            /**
             * Sets future resting mode
             */
            fun setFutureRestMode() = sessionController.setFutureRestMode()

            /**
             * Evaluates the new or next resting mode
             */
            fun evaluateRestingMode() {
               currentDurationSecondsLeft = 0
               currentRestTimeLeft = NoArrowSessionReducer.evaluateRestTime(
                   lastDurationSeconds,
                   numberOfRepetitions
               )

               if (currentSeriesLeft!! <= 1) {
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

               if (formerAutomatonState != null) {
                   noArrowsViewModel.setStateAutomaton(formerAutomatonState)
                   currentDurationSecondsLeft = formerInternalRunningValues.currentDurationSecondsLeft
                   currentRepetitionsLeft = formerInternalRunningValues.currentRepetitionsLeft
                   currentSeriesLeft = formerInternalRunningValues.currentSeriesLeft
                   currentRestTimeLeft = formerInternalRunningValues.currentRestTimeLeft
               }

               userPreferencesRepository.saveSessionType(null)
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
                                   (currentDurationSecondsLeft ?: 0) + durationValue - lastDurationSeconds
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
                               (currentRepetitionsLeft ?: 0) + numberOfRepetitions!! - lastNumberOfRepetitions
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
                   if (currentSeriesLeft == 0 || (isRestMode && currentSeriesLeft!! <= 1)) {
                       currentRestTimeLeft = 0
                       currentDurationSecondsLeft = 0
                       currentRepetitionsLeft = 0
                       currentSeriesLeft = 0
                       if (isRestMode) {
                           sessionHasCompleted()
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
             * Manages the session duration manager in a coroutine
             */
            LaunchedEffect(isPreparationMode) {
               if (isPreparationMode) {
                   sessionDurationManager.beginSession()
               }
            }

            /**
             * Manages the timer countdown in a coroutine
             */
            LaunchedEffect(isTimerRunning, isRestMode, isPreparationMode) {
               sessionController.runTimerLoop(
                   isTimerRunningProvider = { isTimerRunning },
                   isRestModeProvider = { isRestMode },
                   isPreparationModeProvider = { isPreparationMode },
                   isTimerStoppedProvider = { isTimerStopped },
                   tickBaseTimeState = tickBaseTimeState,
                   countDownDelay = countDownDelay,
                   getInitialDurationSeconds = { initialDurationSeconds },
                   getCurrentDurationSecondsLeft = { currentDurationSecondsLeft },
                   getCurrentRepetitionsLeft = { currentRepetitionsLeft },
                   getCurrentSeriesLeft = { currentSeriesLeft },
                   getCurrentRestTimeLeft = { currentRestTimeLeft },
                   getCurrentPreparationSecondsLeft = { currentPreparationSecondsLeft },
                   getNumberOfRepetitions = { numberOfRepetitions },
                   getNumberOfSeries = { numberOfSeries },
                   getIntermediateBeepsChecked = { intermediateBeepsChecked },
                   getEndOfRestBeepTime = { endOfRestBeepTime },
                   getIntermediateBeepsDuration = { intermediateBeepsDuration },
                   setCurrentDurationSecondsLeft = { value -> currentDurationSecondsLeft = value },
                   setCurrentRepetitionsLeft = { value -> currentRepetitionsLeft = value },
                   setCurrentSeriesLeft = { value -> currentSeriesLeft = value },
                   setCurrentRestTimeLeft = { value -> currentRestTimeLeft = value },
                   setCurrentPreparationSecondsLeft = { value -> currentPreparationSecondsLeft = value },
                   startCountdowns = { startCountdowns() },
                   setRestMode = { setRestMode() },
                   setEndOfRestMode = { setEndOfRestMode() },
                   sessionHasCompletedCallback = { sessionHasCompleted() },
                   evaluateRestTime = { evaluateRestTime() },
                   playBeep = { soundPlayer.playBeep(noArrowsViewModel.viewModelScope) },
                   playIntermediateBeep = { soundPlayer.playIntermediateBeep(noArrowsViewModel.viewModelScope) },
                   playRestBeep = { soundPlayer.playRestBeep(noArrowsViewModel.viewModelScope) },
                   keepScreenOn = { enabled ->
                       if (enabled) {
                           (this as? ComponentActivity)?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                       } else {
                           (this as? ComponentActivity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                       }
                   }
               )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // Take full width
                        .wrapContentHeight() // Take only necessary vertical space for its content
                        .padding(deviceScaling(4).dp),
                ) {
                    // --- First Row: Start Button ---
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


                    // --- Second Row: Timer and Countdowns ---
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
                        } else {
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

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp)
                            .weight(1f)
                    ) {
                        // The block of countdowns
                        CountdownsBlock(Modifier)

                        // And the session duration value
                        val heightOfTheBox = this.maxHeight
                        SessionDurationDisplay(
                            sessionDurationManager,
                            heightOfTheBox.value * 0.08f,
                            Modifier.align(Alignment.BottomStart)
                        )
                        /*ClockDisplay(
                            deviceScaling(clockFontSize),
                            Modifier.align(Alignment.BottomStart)
                        )*/
                    }

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
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(0.dp)
                                    .weight(1f)
                            ) {
                                // The block of countdowns
                                CountdownsBlock(Modifier)

                                // And the session duration value
                                val heightOfTheBox = this.maxHeight
                                SessionDurationDisplay(
                                    sessionDurationManager,
                                    heightOfTheBox.value * 0.08f,
                                    Modifier.align(Alignment.BottomStart)
                                )
                                /*ClockDisplay(
                                    deviceScaling(clockFontSize),
                                    Modifier.align(Alignment.BottomStart)
                                )*/
                            }

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

            // --------------------------------------------------------------
            // --- Two rows for the entire screen content (laptop layout) ---
            // --------------------------------------------------------------
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
                            BoxWithConstraints(
                                modifier = Modifier
                                    //.fillMaxWidth()
                                    .padding(0.dp)
                                    .weight(1f)
                            ) {
                                // The block of countdowns
                                CountdownsBlock(Modifier)

                                // And the session duration value
                                val heightOfTheBox = this.maxHeight
                                SessionDurationDisplay(
                                    sessionDurationManager,
                                    heightOfTheBox.value * 0.08f,
                                    Modifier.align(Alignment.BottomStart)
                                )
                                /*ClockDisplay(
                                    deviceScaling(clockFontSize),
                                    Modifier.align(Alignment.BottomStart)
                                )*/
                            }
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


            // -----------------
            // --- UI Layout ---
            // -----------------
            when (detectDeviceFoldedPosture()) {
                EFoldedPosture.POSTURE_NOT_FOLDED -> {
                    // Device is not folded
                    if (considerDevicePortraitPositioned())
                        OneColumn()
                    else
                        TwoColumns()
                }

                EFoldedPosture.POSTURE_FLAT -> {
                    // Device is fully open flat (180 degrees)
                    if (considerDevicePortraitPositioned())
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
                    if (considerDevicePortraitPositioned())
                        OneColumn()
                    else
                        TwoColumns(true)
                }
            }
        }
    }
}
