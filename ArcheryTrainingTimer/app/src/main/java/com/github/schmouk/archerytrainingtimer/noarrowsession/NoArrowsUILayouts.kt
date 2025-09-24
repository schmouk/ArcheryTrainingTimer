package com.github.schmouk.archerytrainingtimer.noarrowsession

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.commons.IntermediateBeepsCheckedRow
import com.github.schmouk.archerytrainingtimer.ui.commons.LogoImage
import com.github.schmouk.archerytrainingtimer.ui.commons.PleaseSelectText
import com.github.schmouk.archerytrainingtimer.ui.commons.RepetitionsDurationButtons
import com.github.schmouk.archerytrainingtimer.ui.commons.RepetitionsDurationTitle
import com.github.schmouk.archerytrainingtimer.ui.commons.RepetitionsNumberTitle
import com.github.schmouk.archerytrainingtimer.ui.commons.RepetitionsSelectorWithScrollIndicators
import com.github.schmouk.archerytrainingtimer.ui.commons.SeriesCountdownConstrainedBox
import com.github.schmouk.archerytrainingtimer.ui.commons.SeriesNumberTitle
import com.github.schmouk.archerytrainingtimer.ui.commons.SeriesNumbersButtons
import com.github.schmouk.archerytrainingtimer.ui.commons.StartButtonRow
import com.github.schmouk.archerytrainingtimer.ui.commons.TimerCountdownConstrainedBox
import com.github.schmouk.archerytrainingtimer.ui.commons.ViewHeader
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTimerRowBackgroundColor
import com.github.schmouk.archerytrainingtimer.ui.theme.DimmedProgressBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.DimmedTimerBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.ProgressBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerBorderColor
import com.github.schmouk.archerytrainingtimer.ui.theme.TimerRestColor

@Composable
fun OneColumn() {
/*
    // --- The Main Column for the entire screen content ---
    Column(
        modifier = Modifier
            .padding(horizontal = mainHorizontalSpacingDp)
            .fillMaxSize(), // Fill the BoxWithConstraints
        horizontalAlignment = Alignment.CenterHorizontally,
        // Add verticalArrangement as needed, e.g., Arrangement.SpaceAround
    ) {
        // Determine if all selections are made
        val allSelectionsMade = selectedDurationString != null &&
                numberOfRepetitions != null &&
                numberOfSeries != null


        // --- 1. Title of Series View ---
        ViewHeader(
            stringResource(id = R.string.series_view_title),
            Modifier
                .padding(bottom = generalPadding)
                .align(Alignment.CenterHorizontally)
                .scale(scaleFactor)
        )


        // --- 2. First Row: Start Button and related items ---
        val buttonScaling = 1f / 17.8f
        val buttonHeight = availableHeightForContentDp.value * buttonScaling  //currentScreenHeightDp.value * buttonScaling

        // the start button on-click lambda
        val onStartButtonClick = {
            if (isTimerRunning) {
                pauseCountdowns()
            } else if (isTimerStopped) {
                resumeCountdowns()
            } else {
                // Trying to Start (or Restart after session completion)
                startNewSession(allSelectionsMade)
            }
        }

        // Shows the start button row
        StartButtonRow(
            allSelectionsMade,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTimerRowBackgroundColor)
                .padding(vertical = deviceScaling(4).dp)
                .let {
                    // Conditionally apply the clickable modifier
                    if (allSelectionsMade && !isRestMode) {
                        it.clickable(
                            interactionSource = remember { MutableInteractionSource() }, // To disable ripple if desired
                            indication = null, // Set to 'LocalIndication.current' for default ripple or custom
                            onClick = {
                                // Send a signal to our ViewModel to toggle pause/resume
                                // This signal should be handled by our session state automaton
                                if (isTimerRunning) {
                                    pauseCountdowns()
                                } else {
                                    // Ensure we only resume if there's time left and it's not completed
                                    startNewSession(true)  // Notice: (allSelectionsMade) is always true here
                                }
                            }
                        )
                    } else {
                        it // Not clickable if conditions aren't met
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            //-- Left Cell (Big Timer Display) --
            TimerCountdownConstrainedBox(
                selectedDurationString,
                initialDurationSeconds,
                currentDurationSecondsLeft,
                numberOfRepetitions,
                currentRepetitionsLeft,
                currentRestTimeLeft,
                isTimerRunning,
                isTimerStopped,
                isDimmedDisplay(),
                isRestMode,
                restModeText,
                mainTimerStrokeWidthDp,
                TimerBorderColor,
                DimmedTimerBorderColor,
                TimerRestColor,
                ProgressBorderColor,
                DimmedProgressBorderColor,
                heightScalingFactor,
                widthScalingFactor,
                modifier = Modifier
                    .weight(0.70f)
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
                modifier = Modifier.weight(0.3f), // 30% of this Row's width,
                boxContentAlignment = Alignment.Center
            )
        }

        // --- 4. SECTION FOR SELECTABLE ITEMS & RELATED TEXTS ---
        // (Repetitions duration, Number of repetitions, etc.)
        // This section appears *under* the countdowns and has its own height.
        Column(
            modifier = Modifier
                .fillMaxWidth() // Take full width
                .wrapContentHeight() // Take only necessary vertical space for its content
                .padding(top = deviceScaling(16).dp, bottom = deviceScaling(4).dp),
        ) {
            //-- Shows the "Please select ..." text only if not all selections have been made --
            PleaseSelectText(
                allSelectionsMade,
                smallerTextStyle,
                Modifier.align(Alignment.CenterHorizontally),
            )

            //-- Shows the block for the selection of durations of repetitions --
            Spacer(modifier = Modifier.height(majorSpacerHeight))

            // Title first
            RepetitionsDurationTitle(
                customInteractiveTextStyle,
                Modifier
                    .wrapContentHeight()
                    .align(Alignment.CenterHorizontally),
            )

            // Then row of duration buttons
            RepetitionsDurationButtons(
                selectedDurationString = selectedDurationString,
                onDurationSelected = { newDuration -> selectedDurationString = newDuration },
                durationOptions = durationOptions,
                borderStrokeWidth = deviceScaling(5).dp,
                durationButtonWidth = durationButtonWidth,
                durationsTextScaling = durationsTextScaling,
                horizontalArrangement = Arrangement.Center,
                rowModifier = Modifier
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
            RepetitionsSelectorWithScrollIndicators(  // Repetition lazy row with arrows
                numberOfRepetitions = numberOfRepetitions, //The state variable for the current selection
                onRepetitionSelected = { selected -> numberOfRepetitions = selected },
                repetitionsListState = repetitionsLazyListState, // Pass the state
                repetitionsRange = repetitionRange,
                numbersTextSize = customInteractiveTextStyle,
                arrowButtonSizeDp = deviceScaling(24).dp,
                horizontalSpaceArrangement = deviceScaling(8).dp,
                repetitionBoxSize = deviceScaling(48).dp,
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
                val seriesBoxSizePx = seriesBoxSize.toPx()
                val visibleWidth =
                    availableWidthForContentDp.toPx() - 2 * mainHorizontalSpacingDp.toPx()

                while (seriesOptions.size > 1 &&
                    seriesOptions.size * (seriesBoxSizePx + horizontalSpaceArrangementPx) -
                    horizontalSpaceArrangementPx > visibleWidth
                ) {
                    // Removes one of the Series number, let's says the one in second position (index 1)
                    seriesOptions.removeAt(1)
                }
            }
            // Then displays the selector row
            SeriesNumbersButtons(
                numberOfSeries = numberOfSeries,
                onNumberSelected = { seriesCount : Int -> numberOfSeries = seriesCount },
                seriesOptions = seriesOptions,
                borderStrokeWidth = deviceScaling(4).dp,
                seriesBoxSize = seriesBoxSize,
                textStyle = customInteractiveTextStyle,
                horizontalSpacing = deviceScaling(10).dp,
                horizontalArrangement = Arrangement.Center,
                rowModifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )


            //-- Checkbox for intermediate beeps --
            Spacer(modifier = Modifier.height(majorSpacerHeight))

            // Shows the whole row, which is toggleable - not just the checkbox
            IntermediateBeepsCheckedRow(
                intermediateBeepsChecked = intermediateBeepsChecked,
                allSelectionsMade = allSelectionsMade,
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
                        enabled = allSelectionsMade,
                        onValueChange = {
                            intermediateBeepsChecked = !intermediateBeepsChecked!!
                        }
                    )
                    .align(Alignment.CenterHorizontally),
            )
        }
    }


    // --- Finally, add Logo Here - Aligned to Bottom-Right ---
    LogoImage(
        Modifier
            .align(Alignment.BottomEnd)
            .padding(end = deviceScaling(16).dp)
            .size(deviceScaling(34).dp)
    )
*/
}