package com.github.schmouk.archerytrainingtimer.ui.noarrowsession

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.text.TextPaint
import android.view.WindowManager

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.github.schmouk.archerytrainingtimer.DEBUG_MODE
import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.noarrowsession.ESignal
import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowsTimerViewModel
import com.github.schmouk.archerytrainingtimer.noarrowsession.UserPreferencesRepository
import com.github.schmouk.archerytrainingtimer.ui.utils.detectFoldedPosture
import com.github.schmouk.archerytrainingtimer.ui.utils.isPortraitPositioned
import com.github.schmouk.archerytrainingtimer.ui.theme.*

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

    // The screen content
    Scaffold(
        // topBar is no more useful since we call
        // WindowCompat.setDecorFitsSystemWindows(window, true)
        // in the related/embedding Activity --> we don't draw behind system bars
        /*
        topBar = {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
            )
        }
        */
        // We don't use Scaffold's bottomBar for this either,
        // we will pad the content area directly.
    ) { innerPaddingFromScaffold -> // This innerPadding from Scaffold handles the TOP spacer
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPaddingFromScaffold)
                //.padding(WindowInsets.navigationBars.asPaddingValues())  // <-----
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            // Debug Mode - Adaptive Screen Tests  <-----<<<
            //val deviceOrientation : Boolean = isPortraitPositioned(currentWindowAdaptiveInfo())
            val devicePortraitOrientation : Boolean = isPortraitPositioned()
            val deviceFoldedPosture = detectFoldedPosture()
            val DBG = devicePortraitOrientation
            /*
            @OptIn(ExperimentalMaterial3AdaptiveApi::class)
            @Composable
            fun MyAdaptiveScreen() {
                // This usually works if a parent composable (like an adaptive scaffold)
                // is providing this information.
                val windowAdaptiveInfo = currentWindowAdaptiveInfo()
                val windowSizeClass = windowAdaptiveInfo.windowSizeClass

                val widthSizeClass = windowSizeClass.windowWidthSizeClass
                val heightSizeClass = windowSizeClass.windowHeightSizeClass

                when (widthSizeClass) {
                    WindowWidthSizeClass.COMPACT -> {
                        // show vertical layout
                        val DBG = 1
                    }
                    WindowWidthSizeClass.MEDIUM -> {
                        if (heightSizeClass == WindowHeightSizeClass.COMPACT) {
                            // show a 2-pane layout
                            val DBG = 2
                        }
                        else {
                            // show vertical layout
                            val DBG = 3
                        }
                    }
                    WindowWidthSizeClass.EXPANDED -> {
                        if (heightSizeClass == WindowHeightSizeClass.COMPACT) {
                            // show a 2-pane layout
                            val DBG = 4
                        }
                        else {
                            // show vertical layout
                            val DBG = 5
                        }
                    }
                }
            }

            MyAdaptiveScreen()
            */
            // End of Debug Mode - Adaptive Screen Tests  <-----<<<


            val availableHeightForContentDp = this.maxHeight
            val availableWidthForContentDp = this.maxWidth

            // adapt items size to screen width
            val configuration = LocalConfiguration.current
            val currentScreenWidthDp = configuration.screenWidthDp.dp
            val currentScreenHeightDp = configuration.screenHeightDp.dp
            val refScreenWidthDp = 411.dp // Your baseline for good proportions
            val refScreenHeightDp = 914.dp // Your baseline for good proportions

            // Calculate scale factor, ensure it's not Dp / Dp if you need a raw float
            val textHorizontalScaleFactor =
                availableWidthForContentDp.value / refScreenWidthDp.value
            val horizontalScaleFactor = textHorizontalScaleFactor.coerceIn(0.60f, 1.0f)
            val verticalScaleFactor = (
                    availableHeightForContentDp.value / refScreenHeightDp.value
                    ).coerceIn(0.40f, 1.5f)
            val scaleFactor = min(horizontalScaleFactor, verticalScaleFactor)

            // scales a dimension (width or height) according to the running device deviceScaling factor
            fun deviceScaling(dim: Int): Float {
                return scaleFactor * dim
            }

            // scales a dimension (width or height) according to the running device deviceScaling factor
            fun deviceScalingFloat(dim: Float): Float {
                return scaleFactor * dim
            }

            // scales horizontal dimension (width) according to the running device horizontalScaleFactor factor
            fun horizontalDeviceScaling(dim: Int): Float {
                return horizontalScaleFactor * dim
            }

            // scales big text dimension (width or height) according to the running device horizontalScaleFactor factor
            fun bigTextHorizontalDeviceScaling(dim: Int): Float {
                return textHorizontalScaleFactor * dim
            }

            val heightScalingFactor = this.maxHeight.value / currentScreenHeightDp.value
            val widthScalingFactor = this.maxWidth.value / currentScreenWidthDp.value

            val customInteractiveTextStyle = TextStyle(fontSize = deviceScaling(18).sp)
            val smallerTextStyle = TextStyle(fontSize = deviceScaling(16).sp)
            val repetitionsLazyListState = rememberLazyListState()

            // Playing sound
            val context = LocalContext.current

            var playBeepEvent by remember { mutableStateOf(false) }
            var playEndBeepEvent by remember { mutableStateOf(false) }
            var playRestBeepEvent by remember { mutableStateOf(false) }
            var playIntermediateBeep by remember { mutableStateOf(false) }

            val audioManager =
                remember { // Remember to avoid re-creating it on every recomposition
                    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                }

            // SoundPool setup
            val soundPool = remember {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)  //USAGE_ASSISTANCE_SONIFICATION) // Or USAGE_GAME, USAGE_MEDIA
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                SoundPool.Builder()
                    .setMaxStreams(1) // Only need to play one beep at a time
                    .setAudioAttributes(audioAttributes)
                    .build()
            }

            fun audioIsNotMuted(): Boolean {
                return audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT &&
                        audioManager.ringerMode != AudioManager.RINGER_MODE_VIBRATE
            }

            fun audioVolumeLevel(): Float {
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
                return 1f * currentVolume / maxVolume
            }

            // --- Debug / Testing ---
            val countDownDelay = if (DEBUG_MODE) 600L else 1000L

            // --- Dynamic Sizes & SPs ---
            val mainTimerStrokeWidth = deviceScaling(14).dp
            val repetitionBoxSize = deviceScaling(48).dp
            val seriesBoxSize = deviceScaling(48).dp
            val majorSpacerHeight = deviceScaling(8).dp
            val generalPadding = deviceScaling(12).dp
            val mainHorizontalSpacingDp = deviceScaling(10).dp

            var selectedDurationString by rememberSaveable { mutableStateOf<String?>(null) }
            var numberOfRepetitions by remember { mutableStateOf<Int?>(null) }
            var numberOfSeries by remember { mutableStateOf<Int?>(null) }
            var intermediateBeepsChecked by remember { mutableStateOf<Boolean?>(null) }

            var lastDurationSeconds by rememberSaveable { mutableStateOf<Int>(0) }
            var lastNumberOfRepetitions by rememberSaveable { mutableStateOf<Int>(0) }
            var lastNumberOfSeries by rememberSaveable { mutableStateOf<Int>(0) }
            var lastIntermediateBeepsChecked by rememberSaveable { mutableStateOf<Boolean>(false) }

            var initialDurationSeconds by rememberSaveable { mutableStateOf<Int?>(null) }
            var currentDurationSecondsLeft by rememberSaveable { mutableStateOf<Int?>(null) }
            var currentRepetitionsLeft by rememberSaveable { mutableStateOf<Int?>(null) }
            var currentSeriesLeft by rememberSaveable { mutableStateOf<Int?>(null) }

            val minRepetitions = 3
            val maxRepetitions = 15
            val repetitionRange = (minRepetitions..maxRepetitions).toList()

            // Rest Mode & Series Tracking
            var currentRestTimeLeft by rememberSaveable { mutableStateOf<Int?>(null) }
            val restingRatio = 0.5f
            val endOfRestBeepTime = 7 // seconds before end of rest to play beep

            val durationOptions = listOf("10 s", "15 s", "20 s", "30 s")
            val durationsScaling = 4f / durationOptions.size
            val durationButtonWidth = (
                    currentScreenWidthDp.value / durationOptions.size - horizontalDeviceScaling(
                        8
                    )
                    ).dp
            val seriesOptions =
                mutableListOf<Int>(1, 2, 3, 5, 10, 15, 20, 25, 30)  // MutableList
            val intermediateBeepsDuration = 5 // seconds for intermediate beeps

            val restModeText = stringResource(R.string.rest_indicator).toString()

            var beepSoundId by remember { mutableStateOf<Int?>(null) }
            var endBeepSoundId by remember { mutableStateOf<Int?>(null) }
            var intermediateBeepSoundId by remember { mutableStateOf<Int?>(null) }
            var soundPoolLoaded by remember { mutableStateOf(false) }

            /**
             *  Loads sound and releases SoundPool
             */
            DisposableEffect(Unit) {
                beepSoundId = soundPool.load(context, R.raw.beep, 1)
                endBeepSoundId = soundPool.load(context, R.raw.beep_end, 1)
                intermediateBeepSoundId = soundPool.load(context, R.raw.beep_intermediate, 1)

                soundPool.setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) {
                        soundPoolLoaded = true
                    }
                }

                onDispose {
                    soundPool.release()
                }
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
                playBeepEvent = false
                playRestBeepEvent = false
                playEndBeepEvent = true
                currentDurationSecondsLeft = 0
                currentRepetitionsLeft = 0
                currentSeriesLeft = 0
            }

            /**
             * Evaluates the resting time
             */
            fun evaluateRestTime(): Int {
                return ((numberOfRepetitions ?: 0) * (lastDurationSeconds ?: 0) * restingRatio)
                    .roundToInt()
                    .coerceAtLeast(endOfRestBeepTime + 2) // Ensure rest gets a minimum value for the beep logic
            }


            /**
             * Starts or Restarts a new session
             */
            fun startNewSession(allSelectionsMade: Boolean) {
                // Trying to Start (or Restart after session completion)
                if (allSelectionsMade) {
                    // If currentRepetitionsLeft is 0, it means a cycle just finished (dimmed state).
                    // Reset both countdowns for a new cycle.
                    if (currentSeriesLeft == null || currentSeriesLeft == 0) {
                        currentSeriesLeft = numberOfSeries
                        currentRepetitionsLeft = numberOfRepetitions
                        currentDurationSecondsLeft = initialDurationSeconds
                    } else {
                        // Handle cases where selections might have been cleared or timer never run
                        if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                            currentDurationSecondsLeft = initialDurationSeconds
                        }
                        if (currentRepetitionsLeft == null) { // This should ideally not happen if allSelectionsMade is true
                            currentRepetitionsLeft = numberOfRepetitions
                        }
                    }
                    noArrowsViewModel.action(ESignal.SIG_START)
                }
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
             * Play sound effect - single beep
             */
            LaunchedEffect(playBeepEvent) {
                if (playBeepEvent && soundPoolLoaded && beepSoundId != null) {  // && audioIsNotMuted()) {
                    val actualVolume = audioVolumeLevel()
                    soundPool.play(beepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                }
                playBeepEvent = false // Reset trigger
            }

            /**
             * Play sound effect - end beep
             */
            LaunchedEffect(playEndBeepEvent) {
                if (playEndBeepEvent && soundPoolLoaded && endBeepSoundId != null && audioIsNotMuted()) {
                    val actualVolume = audioVolumeLevel()
                    soundPool.play(endBeepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                    delay(380L)
                    soundPool.play(endBeepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                    delay(380L)
                    soundPool.play(endBeepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                }
                playEndBeepEvent = false // Reset trigger
            }

            /**
             * Play sound effect - intermediate beep
             */
            LaunchedEffect(playIntermediateBeep) {
                if (playIntermediateBeep && soundPoolLoaded && intermediateBeepSoundId != null && audioIsNotMuted()) {
                    val actualVolume = audioVolumeLevel()
                    soundPool.play(
                        intermediateBeepSoundId!!,
                        actualVolume,
                        actualVolume,
                        1,
                        0,
                        1f
                    )
                }
                playIntermediateBeep = false // Reset trigger
            }

            /**
             * Play sound effect - rest beeps
             */
            LaunchedEffect(playRestBeepEvent) {
                if (playRestBeepEvent && soundPoolLoaded && beepSoundId != null) {
                    val actualVolume = audioVolumeLevel()
                    soundPool.play(beepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                    delay(240L)
                    soundPool.play(beepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                    playRestBeepEvent = false // Reset trigger
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
             * Updates initial/current countdown values when selections change AND timer is NOT running
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
                        if (isRestMode) {
                            currentDurationSecondsLeft = initialDurationSeconds
                        } else {
                            currentDurationSecondsLeft = min(
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
            LaunchedEffect(isTimerRunning, isRestMode) {
                if (isTimerRunning || isRestMode) {
                    (this as? ComponentActivity)?.window?.addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                } else {
                    (this as? ComponentActivity)?.window?.clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                }

                while (isActive && (isTimerRunning || isRestMode || isTimerStopped)) {
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
                                currentDurationSecondsLeft =
                                    initialDurationSeconds  //initialDurationSeconds
                            }
                            if (currentRepetitionsLeft == null) {
                                currentRepetitionsLeft =
                                    numberOfRepetitions  //numberOfRepetitions
                            }
                            if (currentSeriesLeft == null) {
                                currentSeriesLeft = numberOfSeries  //numberOfSeries
                            }
                        }

                        while (isActive &&
                            currentSeriesLeft!! > 0 //&&
                            //(isTimerRunning || isTimerStopped) &&  // Notice: always true here
                            //!isRestMode                            // Notice: always true here
                        ) {
                            if (currentDurationSecondsLeft!! == initialDurationSeconds!!) {
                                playBeepEvent = true
                            } else if (currentDurationSecondsLeft!! == initialDurationSeconds!! - 1) {
                                // in some dark circumstances, the cancelling of the first beep may be missed
                                playBeepEvent = false
                            }

                            //if (currentDurationSecondsLeft != null && currentDurationSecondsLeft!! > 0) {
                            if (currentDurationSecondsLeft!! > 0) {
                                // current repetition timer tick
                                if (isTimerRunning)
                                    delay(countDownDelay)
                                if (!(isTimerRunning || isTimerStopped) || isRestMode)
                                    break
                                currentDurationSecondsLeft = currentDurationSecondsLeft!! - 1
                                // intermediate beep logic
                                if (intermediateBeepsChecked != null &&
                                    intermediateBeepsChecked == true &&
                                    currentDurationSecondsLeft != null &&
                                    currentDurationSecondsLeft!! > 0 &&
                                    (initialDurationSeconds!! - currentDurationSecondsLeft!!) % intermediateBeepsDuration == 0
                                ) {
                                    playIntermediateBeep = true
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
                                                //playRestBeepEvent = true
                                                //isRestMode = true
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
                                    if (isRestMode)
                                        currentSeriesLeft = currentSeriesLeft!! + 1  // Must be restored to previous value
                                    else
                                        setRestMode()
                                    break
                                }
                            }

                            if (isTimerStopped) {
                                /*
                                if (isRestMode)
                                    currentSeriesLeft =
                                        currentSeriesLeft!! + 1  // Must be restored to previous value
                                */
                                break
                            }
                        }
                    }

                    // Have to check this since it may have been set in the block above
                    if (currentSeriesLeft!! <= 0) {
                        // If no more series left, stop the timer and show dimmed state
                        sessionHasCompleted()
                    } else if (isRestMode) {
                        // --- Rest Mode Countdown ---
                        if ((currentRestTimeLeft?: 0) == evaluateRestTime())
                            playRestBeepEvent = true

                        // Check isRestMode again, as it could have been modified in the block above
                        while (isActive && isRestMode) {
                            if (currentRestTimeLeft != null && currentRestTimeLeft!! > 0) {
                                // Check for some seconds left --> to play rest-beeps
                                if (currentRestTimeLeft == endOfRestBeepTime) {
                                    playRestBeepEvent = true
                                    playBeepEvent = false
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


            /**
             * Repetitions selector with scroll indicators
             */
            @Composable
            fun RepetitionsSelectorWithScrollIndicators(
                selectedRepetition: Int?,
                onRepetitionSelected: (Int) -> Unit,
                repetitionsListState: LazyListState = rememberLazyListState(), // Pass or remember
                // Add scaleFactor or other styling params if needed
                items: List<Int>
            ) {
                val coroutineScope = rememberCoroutineScope()

                // Standard IconButton size (Material guidelines suggest 48.dp touch target)
                val arrowButtonSizeDp = deviceScaling(24).dp

                // Derived states to determine if arrows should be shown
                // canScrollBackward is true if the first item is not fully visible at the start
                val canScrollBackward by remember {
                    derivedStateOf {
                        repetitionsListState.firstVisibleItemIndex > 0 || repetitionsListState.firstVisibleItemScrollOffset > 0
                    }
                }

                // canScrollForward is true if the last item is not fully visible at the end
                // This requires knowing the total item count and the layout info of visible items.
                val canScrollForward by remember {
                    derivedStateOf {
                        // Check if there are items and the LazyListState has layout info
                        if (repetitionsListState.layoutInfo.visibleItemsInfo.isNotEmpty() && repetitionRange.isNotEmpty()) {
                            val lastVisibleItem = repetitionsListState.layoutInfo.visibleItemsInfo.last()
                            // If the last visible item's index is less than the total number of items - 1
                            // OR if the last visible item is not fully occupying the viewport width at its end
                            val viewportWidth = repetitionsListState.layoutInfo.viewportSize.width
                            lastVisibleItem.index < repetitionRange.size - 1 || lastVisibleItem.offset + lastVisibleItem.size > viewportWidth
                        } else {
                            items.isNotEmpty() // True if there are items but no layout info yet (initial state before first scroll/layout)
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth(), // This Box takes the full width
                    contentAlignment = Alignment.Center // Centers its child (the Row) if the child is smaller
                ) {
                    Row(
                        //horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        // The Row itself will only take the width of its content.
                        // If showArrows is false and LazyRow content is small, this Row will be small.
                        // If showArrows is true, it will be wider.
                        modifier = Modifier.wrapContentWidth(unbounded = false, align = Alignment.CenterHorizontally)
                        //modifier = Modifier.fillMaxWidth()
                    ) {
                        // --- Left Arrow ---
                        Box(
                            modifier = Modifier
                                .size(arrowButtonSizeDp), // Occupy space whether visible or not to help layout
                            contentAlignment = Alignment.Center // Center the AnimatedVisibility content within the Box
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = canScrollBackward,
                                enter = fadeIn(animationSpec = tween(durationMillis = 400)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 400))
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            // Scroll to the beginning or by a certain amount
                                            val targetIndex =
                                                (repetitionsListState.firstVisibleItemIndex - 5).coerceAtLeast(
                                                    0
                                                ) // Scroll back 5 items
                                            repetitionsListState.animateScrollToItem(targetIndex)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize() // Fill the Box
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,  //.KeyboardArrowLeft,
                                        contentDescription = "Scroll Left", // For accessibility
                                        tint = AppTextColor  //MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // --- LazyRow ---
                        // LazyRow takes the available space between arrows
                        val horizontalSpaceArrangement = deviceScaling(8).dp

                        LazyRow(
                            state = repetitionsListState,
                            horizontalArrangement = Arrangement.spacedBy(horizontalSpaceArrangement), // Spacing between number buttons
                            modifier = Modifier
                                .weight(1f) // LazyRow takes available space between arrows
                                //.padding(horizontal = 0.dp), // No extra padding here if arrows handle spacing
                                .wrapContentWidth() // Let LazyRow determine its own width
                        ) {
                            /*items(repetitionRange) { repetition ->
                                Button(
                                    onClick = { onRepetitionSelected(repetition) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (repetition == selectedRepetition) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (repetition == selectedRepetition) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.height(40.dp) // Example fixed height
                                    // Add other styling from your original button
                                ) {
                                    Text(text = repetition.toString() /*, style = customInteractiveTextStyle */)
                                }
                            }*/
                            items(
                                count = items.size,
                                key = { index -> items[index] }
                            ) { index ->
                                val repetitionNum = items[index]
                                val isNumberSelected = repetitionNum == numberOfRepetitions
                                val isClickable = true

                                Box(
                                    modifier = Modifier
                                        .size(repetitionBoxSize)
                                        .then(
                                            if (isNumberSelected) Modifier.border(
                                                BorderStroke(
                                                    deviceScaling(4).dp,
                                                    SelectedButtonBorderColor
                                                ), shape = CircleShape
                                            ) else Modifier
                                        )
                                        .padding(if (isNumberSelected) deviceScaling(4).dp else 0.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = if (isNumberSelected) AppTitleColor
                                            else if (isClickable) AppButtonDarkerColor
                                            else AppDimmedButtonColor
                                        )
                                        .clickable {
                                            if (isClickable)
                                                numberOfRepetitions =
                                                    if (isNumberSelected) null else repetitionNum
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$repetitionNum",
                                        style = customInteractiveTextStyle.copy(
                                            color = if (isNumberSelected) AppButtonTextColor else AppTextColor
                                        ),
                                        fontWeight = if (isNumberSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // --- Right Arrow ---
                        Box(
                            modifier = Modifier
                                .size(arrowButtonSizeDp), // Occupy space whether visible or not
                            contentAlignment = Alignment.Center // Center the AnimatedVisibility content
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = canScrollForward,
                                enter = fadeIn(animationSpec = tween(durationMillis = 400)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 400))
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            // Scroll to the end or by a certain amount
                                            val targetIndex =
                                                (repetitionsListState.firstVisibleItemIndex + 5).coerceAtMost(
                                                    repetitionRange.size - 1
                                                ) // Scroll forward 5 items example
                                            repetitionsListState.animateScrollToItem(targetIndex)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,  //.KeyboardArrowRight,
                                        contentDescription = "Scroll Right",
                                        tint = AppTextColor  //MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }


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

                // --- Main Column for the whole layout ---
                // --- 1. Title of Series View ---
                Text(
                    text = stringResource(id = R.string.series_view_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AppTitleColor,
                    modifier = Modifier
                        .padding(bottom = generalPadding)
                        .align(Alignment.CenterHorizontally)
                        .scale(scaleFactor)
                )

                // --- 2. First Row: Start Button and related items ---
                val buttonScaling = 1f / 17.8f
                val buttonHeight =
                    currentScreenHeightDp.value * buttonScaling  //deviceScaling(56)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = deviceScaling(8).dp)
                        .height(buttonHeight.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    //-- START Button --
                    Button(
                        onClick = {
                            if (isTimerRunning) {
                                pauseCountdowns()
                            } else if (isTimerStopped) {
                                resumeCountdowns()
                            } else {
                                // Trying to Start (or Restart after session completion)
                                startNewSession(allSelectionsMade)
                                /*
                                if (allSelectionsMade) {
                                    // If currentRepetitionsLeft is 0, it means a cycle just finished (dimmed state).
                                    // Reset both countdowns for a new cycle.
                                    if (currentSeriesLeft == null || currentSeriesLeft == 0) {
                                        currentSeriesLeft = numberOfSeries
                                        currentRepetitionsLeft = numberOfRepetitions
                                        currentDurationSecondsLeft = initialDurationSeconds
                                    } else {
                                        // Handle cases where selections might have been cleared or timer never run
                                        if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                                            currentDurationSecondsLeft = initialDurationSeconds
                                        }
                                        if (currentRepetitionsLeft == null) { // This should ideally not happen if allSelectionsMade is true
                                            currentRepetitionsLeft = numberOfRepetitions
                                        }
                                    }
                                    resumeCountdowns()
                                }
                                */
                            }
                        },
                        enabled = allSelectionsMade && !isRestMode,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppButtonColor,
                            contentColor = AppButtonTextColor,
                            disabledContainerColor = AppButtonColor.copy(alpha = 0.5f),
                            disabledContentColor = AppButtonTextColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .fillMaxHeight(1f)
                    ) {
                        Text(
                            text = stringResource(
                                id = if (isTimerRunning) R.string.stop_button
                                else R.string.start_button
                            ),
                            style = customInteractiveTextStyle.copy(
                                color = if (allSelectionsMade && !isRestMode) AppButtonTextColor
                                else AppButtonTextColor.copy(alpha = 0.5f)
                            ),
                            fontSize = (18 * buttonHeight / 35f).sp
                        )
                    }

                    // We can add other elements to this Row if needed,
                    // for example, a small status icon or text next to the button.
                    // If so, let's adjust Arrangement.Center or use Spacers.
                }

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
                                        // Send a signal to your ViewModel to toggle pause/resume
                                        // This signal should be handled by your FSM
                                        // e.g., ESignal.TOGGLE_PAUSE_RESUME or separate ESignal.PAUSE / ESignal.RESUME
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
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(0.70f)
                            .fillMaxWidth()
                            .fillMaxHeight(), // Fill the height of THIS Row
                        contentAlignment = Alignment.Center,
                    ) {
                        val circleRadius =
                            min(
                                widthScalingFactor * constraints.maxWidth,
                                heightScalingFactor * constraints.maxHeight
                            ) / 2f * 0.9f

                        val strokeWidthPx =
                            with(LocalDensity.current) { mainTimerStrokeWidth.toPx() }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasCenterX = size.width / 2f
                            val canvasCenterY = size.height / 2f

                            // 1. Draw the main circle border
                            drawCircle(
                                color = if (isRestMode) WABlueColor
                                else if (isDimmedDisplay()) DimmedTimerBorderColor
                                else TimerBorderColor,
                                radius = circleRadius - strokeWidthPx / 2f, // Radius to the center of the stroke
                                style = Stroke(width = strokeWidthPx),
                                center = Offset(canvasCenterX, canvasCenterY)
                            )

                            // 2. Draw the progress arc
                            val sweepAngle =
                                if (numberOfRepetitions != null && numberOfRepetitions!! > 0 && currentRepetitionsLeft != null) {
                                    if (currentRepetitionsLeft!! > 1)
                                        ((numberOfRepetitions!! - currentRepetitionsLeft!!) / numberOfRepetitions!!.toFloat()) * 360f
                                    else
                                        ((numberOfRepetitions!! * initialDurationSeconds!! - currentDurationSecondsLeft!! + 1) / (numberOfRepetitions!! * initialDurationSeconds!!).toFloat()) * 360f
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

                                val progressStrokeWidth =
                                    (0.72f * mainTimerStrokeWidth.value).dp.toPx()

                                drawArc(
                                    color = if (isDimmedDisplay()) DimmedProgressBorderColor
                                    else ProgressBorderColor,
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
                                        color =
                                            (if (isRestMode) WABlueColor
                                            else if (isDimmedDisplay()) DimmedTimerBorderColor
                                            else TimerBorderColor
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
                                        color = WABlueColor.toArgb()
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

                    //-- Right Control Cell (Small Series Countdown Display) --
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(0.3f) // 30% of this Row's width
                        ,
                        contentAlignment = Alignment.BottomCenter // Align small circle to bottom center of this cell
                    ) {
                        val seriesCircleRadius =
                            min(
                                constraints.maxWidth,
                                constraints.maxHeight
                            ) / 2f * 0.85f

                        val seriesStrokeWidthDp = with(LocalDensity.current) {
                            deviceScaling(7).dp
                        }
                        val seriesStrokeWidthPx = with(LocalDensity.current) {
                            seriesStrokeWidthDp.toPx()
                        }

                        val localPadding = deviceScaling(8).dp

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (DEBUG_MODE) {
                                val debugTextSizePx = 36f
                                val restTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                    color = WAWhiteColor.toArgb()
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
                            val circleCenterY =
                                size.height - seriesCircleRadius - localPadding.toPx()

                            // Draw circle border
                            drawCircle(
                                color = if (isDimmedDisplay()) DimmedTimerBorderColor else TimerBorderColor,
                                radius = seriesCircleRadius - seriesStrokeWidthPx / 2,
                                style = Stroke(width = seriesStrokeWidthPx),
                                center = Offset(circleCenterX, circleCenterY)
                            )

                            // Draw the progress arc
                            val totalRepetitions =
                                (numberOfRepetitions ?: 0) * (numberOfSeries ?: 0)
                            val sweepAngle =
                                if (numberOfSeries != null && numberOfSeries!! > 0 && currentSeriesLeft != null) {
                                    if (currentSeriesLeft!! > 1)
                                        ((numberOfSeries!! - currentSeriesLeft!!) / numberOfSeries!!.toFloat()) * 360f
                                    else if (currentRepetitionsLeft!! > 1)
                                        (totalRepetitions - currentRepetitionsLeft!!) / totalRepetitions.toFloat() * 360f
                                    else
                                        ((totalRepetitions * initialDurationSeconds!! - currentDurationSecondsLeft!! + 1) / (totalRepetitions * initialDurationSeconds!!).toFloat()) * 360f
                                } else {
                                    0f
                                }

                            //if (sessionAutomaton.isTimerActivated() && sweepAngle > 0f) {
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
                                    color = if (isDimmedDisplay()) DimmedProgressBorderColor else ProgressBorderColor,
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

                            // Series display will show 0 when dimmed
                            val seriesToDisplayValue = currentSeriesLeft
                            val seriesToDisplayString =
                                seriesToDisplayValue?.toString()
                                    ?: currentSeriesLeft?.toString() ?: ""

                            if (seriesToDisplayString.isNotEmpty()) {
                                val targetTextHeightPx = seriesCircleRadius * 0.9f

                                val countdownTextPaint =
                                    TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                        color = (if (isDimmedDisplay()) DimmedTimerBorderColor
                                        else TimerBorderColor
                                                ).toArgb()
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
                    Text( // Select Session parameters Row
                        text = stringResource(id = R.string.please_select),
                        style = smallerTextStyle,
                        fontStyle = FontStyle.Italic,
                        color = AppTitleColor.copy(alpha = if (allSelectionsMade) 0f else 1f),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )

                    Text( // Repetitions duration title
                        text = stringResource(id = R.string.repetitions_duration_label),
                        style = customInteractiveTextStyle,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(top = deviceScaling(8).dp)
                            .wrapContentHeight()
                            .align(Alignment.CenterHorizontally)
                    )

                    Row( // Duration Buttons Row
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = 0.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val borderStrokeWidth = deviceScaling(5).dp
                        Row(horizontalArrangement = Arrangement.spacedBy(deviceScaling(0).dp)) {
                            durationOptions.forEach { durationString ->
                                val isSelected = selectedDurationString == durationString
                                Button(
                                    onClick = {
                                        if (!isSelected)
                                            selectedDurationString = durationString
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) SelectedButtonBackgroundColor
                                        else AppButtonDarkerColor,
                                        contentColor = AppButtonTextColor
                                    ),
                                    border = if (isSelected) BorderStroke(
                                        borderStrokeWidth,
                                        SelectedButtonBorderColor
                                    ) else BorderStroke(
                                        borderStrokeWidth,
                                        AppBackgroundColor
                                    ),
                                    enabled = true,
                                    modifier = Modifier
                                        .width(durationButtonWidth)
                                ) {
                                    Text(
                                        text = durationString,
                                        style = TextStyle(
                                            fontSize = (13f * durationsScaling).toInt().sp,
                                            color = if (isSelected) AppButtonTextColor else AppTextColor
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(majorSpacerHeight))

                    Text( // Number of repetitions title
                        text = stringResource(id = R.string.repetitions_number_label),
                        style = customInteractiveTextStyle,
                        color = AppTextColor,
                        modifier = Modifier
                            .padding(bottom = generalPadding)
                            .wrapContentHeight()
                            .align(Alignment.CenterHorizontally)
                    )

                    RepetitionsSelectorWithScrollIndicators(  // Repetition lazy row with arrows
                        selectedRepetition = numberOfRepetitions, //The state variable for the current selection
                        onRepetitionSelected = { selected ->
                            numberOfRepetitions = selected
                        },
                        repetitionsListState = repetitionsLazyListState, // Pass the state
                        repetitionRange
                    )

                    Spacer(modifier = Modifier.height(majorSpacerHeight))

                    Text( // Number of series title
                        text = stringResource(id = R.string.series_number_label),
                        style = customInteractiveTextStyle,
                        color = AppTextColor,
                        modifier = Modifier
                            .padding(top = generalPadding)
                            .wrapContentHeight()
                            .align(Alignment.CenterHorizontally)
                    )
                    Row( // Series Selector Row
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = deviceScaling(12).dp, bottom = deviceScaling(8).dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Checks the available display width against the series-options list width
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

                        Row(horizontalArrangement = Arrangement.spacedBy(deviceScaling(10).dp)) {
                            seriesOptions.forEach { seriesCount ->
                                val isSeriesSelected = seriesCount == numberOfSeries
                                val isClickable = true
                                Box(
                                    modifier = Modifier
                                        .size(seriesBoxSize)
                                        .then(
                                            if (isSeriesSelected) Modifier.border(
                                                BorderStroke(
                                                    deviceScaling(4).dp,
                                                    SelectedButtonBorderColor
                                                ), shape = CircleShape
                                            ) else Modifier
                                        )
                                        .padding(if (isSeriesSelected) deviceScaling(4).dp else 0.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = if (isSeriesSelected) AppTitleColor
                                            else if (isClickable) AppButtonDarkerColor
                                            else AppDimmedButtonColor
                                        )
                                        .clickable {
                                            if (isClickable)
                                                numberOfSeries =
                                                    if (isSeriesSelected) null else seriesCount
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$seriesCount",
                                        style = customInteractiveTextStyle.copy(
                                            color = if (isSeriesSelected) AppButtonTextColor
                                            else AppTextColor
                                        ),
                                        fontWeight = if (isSeriesSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(majorSpacerHeight))

                    Row( // Checkbox Row
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = intermediateBeepsChecked ?: false,
                            onCheckedChange = null,
                            enabled = allSelectionsMade,
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppTitleColor,
                                uncheckedColor = AppTextColor.copy(alpha = if (allSelectionsMade) 1f else 0.5f),
                                checkmarkColor = AppButtonTextColor,
                                disabledCheckedColor = AppTitleColor.copy(alpha = 0.5f),
                                disabledUncheckedColor = AppButtonDarkerColor
                            ),
                            modifier = Modifier.scale(scaleFactor)
                        )
                        Spacer(modifier = Modifier.width(deviceScaling(6).dp))
                        Text(
                            text = stringResource(id = R.string.intermediate_beeps),
                            style = smallerTextStyle,
                            color = AppTextColor.copy(alpha = if (allSelectionsMade) 1f else 0.38f)
                        )
                    }
                }
            }

            // --- Add Logo Here, Aligned to Bottom-Right ---
            Image(
                painter = painterResource(id = R.drawable.ps_logo),
                contentDescription = null, // Important for accessibility - provide a meaningful description (e.g. "Editor Logo")or null if purely decorative
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Aligns this Image to the bottom end (right) of the parent Box
                    .padding(
                        end = deviceScaling(16).dp//,
                        //bottom = deviceScaling(8).dp
                    ) // Add some padding from the screen edges
                    .size(34.dp) // Set the size of the image on screen
            )
        }
    }
}


// AdaptiveText composable - remains here will actually not used
/*
@Composable
fun AdaptiveText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    targetWidth: Dp,
    maxLines: Int = 1,
    initialFontSize: TextUnit = 80.sp // Slightly reduced initial size for faster convergence
) {
    var textSize by remember(text, targetWidth) { mutableStateOf(initialFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.then(
            Modifier.graphicsLayer(alpha = if (readyToDraw) 1f else 0f)
        ),
        color = color,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        fontSize = textSize,
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                if (textSize > 8.sp) { // Keep a reasonable minimum
                    textSize *= 0.85f // Shrink a bit more aggressively if needed
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}
*/
