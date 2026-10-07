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
import androidx.compose.runtime.mutableLongStateOf
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
import com.github.schmouk.archerytrainingtimer.commons.EState
import com.github.schmouk.archerytrainingtimer.commons.SoundPlayer
import com.github.schmouk.archerytrainingtimer.commons.TimerInternalRunningState
import com.github.schmouk.archerytrainingtimer.commons.UserPreferencesRepository
import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowSessionController
import com.github.schmouk.archerytrainingtimer.noarrowsession.SelectionState
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

import kotlin.math.min


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
    if (formerAutomatonState != null) {
        noArrowsViewModel.setStateAutomaton(formerAutomatonState)
    }

    // related finite state machine control values
    val isIdleMode by noArrowsViewModel.isIdleMode
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

            // --- State retained in ViewModel ---
            val selectionState = noArrowsViewModel.selectionState
            val sessionDurationManager = noArrowsViewModel.sessionDurationManager

            // --- Session Controller ---
            val sessionController = remember(noArrowsViewModel, soundPlayer) {
                NoArrowSessionController(
                    noArrowsViewModel = noArrowsViewModel,
                    soundPlayer = soundPlayer,
                    scope = noArrowsViewModel.viewModelScope,
                    sessionDurationManager = sessionDurationManager
                )
            }

            // --- To accurately evaluate delays in countdowns ---
            selectionState.countDownDelay = SECOND_DURATION_MS
            selectionState.endOfRestBeepTime = 7
            selectionState.intermediateBeepsDuration = 5

            // --- Dynamic Sizes & SPs ---
            val mainTimerStrokeWidthDp = deviceScaling(14).dp
            val selectionItemsBaseSizeDp = deviceScaling(48).dp
            val majorSpacerHeight = deviceScaling(8).dp
            val generalPadding = deviceScaling(8).dp
            val mainHorizontalSpacingDp = deviceScaling(10).dp

            var selectedDurationString by rememberSaveable { mutableStateOf(selectionState.selectedDurationString) }
            var numberOfRepetitions by rememberSaveable { mutableStateOf(selectionState.numberOfRepetitions) }
            var numberOfSeries by rememberSaveable { mutableStateOf(selectionState.numberOfSeries) }
            var intermediateBeepsChecked by rememberSaveable { mutableStateOf(selectionState.intermediateBeepsChecked) }

            var lastDurationSeconds by rememberSaveable { mutableStateOf(selectionState.lastDurationSeconds) }

            fun syncUiFromSelectionState() {
                selectedDurationString = selectionState.selectedDurationString
                numberOfRepetitions = selectionState.numberOfRepetitions
                numberOfSeries = selectionState.numberOfSeries
                intermediateBeepsChecked = selectionState.intermediateBeepsChecked
                lastDurationSeconds = selectionState.lastDurationSeconds
            }

            val minRepetitions = 3
            val maxRepetitions = 15
            val repetitionRange = (minRepetitions..maxRepetitions).toList()

            // --- Rest Mode & Series Tracking ---
            val durationOptions = listOf("10 s", "15 s", "20 s", "30 s")
            val seriesOptions = mutableListOf(1, 2, 3, 5, 10, 15, 20, 25, 30)

            val restModeText = stringResource(R.string.rest_indicator)

            val preparationTime = 7 // seconds for preparation time before start
            val resPreparationText = stringResource(R.string.preparation)


            // Sync on each composition
            syncUiFromSelectionState()

            /**
             * Checks if all selections have been made
             */
            fun allSelectionsMade(): Boolean =
               selectionState.selectedDurationString != null &&
                       selectionState.numberOfRepetitions != null &&
                       selectionState.numberOfSeries != null

            /**
             * Evaluates the dimmed status of displays.
             */
            fun isDimmedDisplay(): Boolean = isTimerStopped || isSessionCompleted

            /**
             * No runtime countdown helpers remain here.
             * Timer lifecycle logic is delegated to NoArrowSessionController in noarrowsession/.
             */

            /**
             * Loads user preferences on first composition
             */
            LaunchedEffect(key1 = Unit) {
               selectionState.initState(
                   userPreferencesRepository = userPreferencesRepository,
                   isTimerRunning = isTimerRunning,
                   isRestMode = isRestMode,
                   isTimerStopped = isTimerStopped,
                   isPreparationMode = isPreparationMode,
                   formerAutomatonState = formerAutomatonState,
                   formerInternalRunningValues = formerInternalRunningValues,
                   onAutomatonStateRestored = { state -> noArrowsViewModel.setStateAutomaton(state) },
                   onPreferencesLoaded = { syncUiFromSelectionState() }
               )
            }

            /**
             * Updates initial/current countdown values when selections change
             */
            LaunchedEffect(
               selectionState.selectedDurationString,
               selectionState.numberOfRepetitions,
               selectionState.numberOfSeries,
               selectionState.intermediateBeepsChecked
            ) {
               selectionState.updateSelectionState(
                   isRestMode = isRestMode,
                   isTimerStopped = isTimerStopped,
                   isTimerRunning = isTimerRunning,
                   isPreparationMode = isPreparationMode,
               )
               syncUiFromSelectionState()
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
            LaunchedEffect(isIdleMode, isSessionCompleted) {
               if (!isIdleMode && !isSessionCompleted) {
                   selectionState.runTimerLoop(
                       controller = sessionController,
                       keepScreenOn = { enabled ->
                           (currentLocalContext as? ComponentActivity)?.window?.let { window ->
                               if (enabled) {
                                   window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                               } else {
                                   window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                               }
                           }
                       }
                   )
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
                    sessionController.pauseCountdowns(selectionState.tickBaseTimeState)
                } else if (isTimerStopped) {
                    sessionController.resumeCountdowns(selectionState.tickBaseTimeState)
                } else {
                    val prepared = sessionController.startNewSession(
                        allSelectionsMade = allSelectionsMade(),
                        numberOfSeries = numberOfSeries,
                        numberOfRepetitions = numberOfRepetitions,
                        initialDurationSeconds = selectionState.initialDurationSeconds,
                    )
                    selectionState.currentSeriesLeft = prepared.first
                    selectionState.currentRepetitionsLeft = prepared.second
                    selectionState.currentDurationSecondsLeft = prepared.third
                    if (allSelectionsMade()) {
                        selectionState.currentPreparationSecondsLeft = preparationTime
                        sessionController.startPreparationMode(
                            selectionState.initialDurationSeconds,
                            preparationTime
                        )
                    }
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
                            //-- This is a higher than wide row, let's split it into a two-cells column
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
                                    selectionState.initialDurationSeconds,
                                    selectionState.currentDurationSecondsLeft,
                                    numberOfRepetitions,
                                    selectionState.currentRepetitionsLeft,
                                    selectionState.currentRestTimeLeft,
                                    selectionState.currentPreparationSecondsLeft,
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
                                    selectionState.initialDurationSeconds,
                                    selectionState.currentDurationSecondsLeft,
                                    numberOfRepetitions,
                                    selectionState.currentRepetitionsLeft,
                                    numberOfSeries,
                                    selectionState.currentSeriesLeft,
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
                                selectionState.initialDurationSeconds,
                                selectionState.currentDurationSecondsLeft,
                                numberOfRepetitions,
                                selectionState.currentRepetitionsLeft,
                                selectionState.currentRestTimeLeft,
                                selectionState.currentPreparationSecondsLeft,
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
                                selectionState.initialDurationSeconds,
                                selectionState.currentDurationSecondsLeft,
                                numberOfRepetitions,
                                selectionState.currentRepetitionsLeft,
                                numberOfSeries,
                                selectionState.currentSeriesLeft,
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
                            sessionController.evaluateRestTime(lastDurationSeconds, numberOfRepetitions),
                            sessionController.evaluateRestingRatio(
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
                            selectionState.selectedDurationString = newDuration
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
                        selectedNumberOfRepetitions = numberOfRepetitions,
                        onRepetitionSelected = { selected ->
                            numberOfRepetitions = selected
                            selectionState.numberOfRepetitions = selected
                        },
                        repetitionsListState = repetitionsLazyListState,
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
                        onNumberSelected = { seriesCount: Int ->
                            numberOfSeries = seriesCount
                            selectionState.numberOfSeries = seriesCount
                        },
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
                                    val nextValue = !(intermediateBeepsChecked ?: false)
                                    intermediateBeepsChecked = nextValue
                                    selectionState.intermediateBeepsChecked = nextValue
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
