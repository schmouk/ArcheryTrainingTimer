package com.github.schmouk.archerytrainingtimer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioManager
//import android.media.RingtoneManager
import android.media.SoundPool
import android.os.Bundle
import android.text.TextPaint
//import android.util.Log
import android.view.WindowManager

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // For the 'by viewModels()' delegate
// import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
//import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material.icons.automirrored.filled.ArrowRight
//import androidx.compose.material.icons.filled.KeyboardArrowLeft
//import androidx.compose.material.icons.filled.KeyboardArrowRight
//import androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowLeft
//import androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
//import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.text.drawText
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import androidx.compose.ui.unit.TextUnit
import androidx.core.view.WindowCompat

import com.github.schmouk.archerytrainingtimer.ui.theme.*
import com.github.schmouk.archerytrainingtimer.noarrowsession.ESignal
//import com.github.schmouk.archerytrainingtimer.noarrowsession.SessionStateAutomaton as NASessionStateAutomaton
import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowsTimerViewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
//import kotlin.times

val DEBUG_MODE = true
//val DEBUG_MODE = false


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCustomTopAppBar() {
    // Apply padding ONLY to the top of the TopAppBar to account for the status bar
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge // Or some other preferred style
            )
        },
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        windowInsets = WindowInsets.statusBars // Keep this, it's good practice
    )
}
*/

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    Scaffold(
        topBar = {
            MyCustomTopAppBar()
        }
    ) { innerPadding -> // This innerPadding from Scaffold already handles some insets
        Column(
            modifier = Modifier
                .padding(innerPadding) // Apply Scaffold's inner padding
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // So content is visible
        ) {
            // The main screen content
            Text(
                "Hello, this is the main content!",
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
*/

/*
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        MainScreen()
    }
}
*/

// The AppTheme Composable
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        // our theme colors, typography, etc.
    ) {
        content()
    }
}


// MainActivity class definition
class MainActivity : ComponentActivity() {
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private var initialRingtoneVolume: Int = 0

    //private val sessionAutomaton = NASessionStateAutomaton()

    // Get the ViewModel instance, scoped to this Activity.
    // The Android system will create it if it doesn't exist, or re-provide
    // the existing one if, for example, the Activity is recreated due to rotation.
    // This requires NoArrowsTimerViewModel to have a default constructor OR
    // for us to provide a ViewModelProvider.Factory if it has constructor parameters.
    private val timerViewModel: NoArrowsTimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display to draw behind the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        userPreferencesRepository = UserPreferencesRepository(applicationContext)

        val context = this
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initialRingtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)

        keepScreenOn()

        setContent {
            ArcheryTrainingTimerTheme {
                SimpleScreen(
                    userPreferencesRepository = userPreferencesRepository,
                    //sessionAutomaton = sessionAutomaton,
                    timerViewModel = timerViewModel,
                    onCloseApp = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Keep screen on when the activity is active and in the foreground
        keepScreenOn()
    }

    override fun onPause() {
        super.onPause()
        // Allow screen to turn off when the activity is no longer in the foreground
        allowScreenTimeout()
    }

    // Call this function when we want to allow the screen to turn off normally again
    // e.g., when the timer stops or the user navigates away from the critical section.
    private fun allowScreenTimeout() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // Call this function when you want to force the screen to stay on
    // e.g., when the timer starts.
    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        super.onDestroy()

        // It's good practice to clear the flag when the activity is destroyed
        // to ensure it doesn't leak or affect other parts of the system if not
        // cleared explicitly elsewhere.
        allowScreenTimeout()

        // Restore initial ringtone volume
        val context = this
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_RING, initialRingtoneVolume, 0)
    }

    override fun onStop() {
        super.onStop()

        // Restore initial ringtone volume
        val context = this
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_RING, initialRingtoneVolume, 0)
    }

}

// AdaptiveText composable
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


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SimpleScreen(
    userPreferencesRepository: UserPreferencesRepository,
    //sessionAutomaton: NASessionStateAutomaton,
    timerViewModel: NoArrowsTimerViewModel,
    onCloseApp: () -> Unit
) {
    val isSessionCompleted_ by timerViewModel.isSessionCompleted
    val isTimerStopped_ by timerViewModel.isTimerStopped
    val isTimerRunning_ by timerViewModel.isTimerRunning
    val isRestMode_ by timerViewModel.isRestMode

    Scaffold(
        topBar = {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
            )
        }
        // We don't use Scaffold's bottomBar for this,
        // we will pad the content area directly.
    ) { innerPaddingFromScaffold -> // This innerPadding from Scaffold handles the TOP spacer
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPaddingFromScaffold)
                .padding(WindowInsets.navigationBars.asPaddingValues())  // <-----
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val availableHeightForContentDp = this.maxHeight
            val availableWidthForContentDp = this.maxWidth

            // adapt items size to screen width
            val configuration = LocalConfiguration.current
            val currentScreenWidthDp = configuration.screenWidthDp.dp
            val currentScreenHeightDp = configuration.screenHeightDp.dp
            val refScreenWidthDp = 411.dp // Your baseline for good proportions
            val refScreenHeightDp = 914.dp // Your baseline for good proportions

            // Calculate scale factor, ensure it's not Dp / Dp if you need a raw float
            val textHorizontalScaleFactor = availableWidthForContentDp.value / refScreenWidthDp.value
            val horizontalScaleFactor = textHorizontalScaleFactor.coerceIn(0.60f, 1.0f)
            val verticalScaleFactor =(
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
            val widthScalingFactor  = this.maxWidth.value  / currentScreenWidthDp.value

            val customInteractiveTextStyle = TextStyle(fontSize = deviceScaling(18).sp)
            val smallerTextStyle = TextStyle(fontSize = deviceScaling(16).sp)
            val repetitionsLazyListState = rememberLazyListState()

            // Playing sound
            val context = LocalContext.current

            var playBeepEvent by remember { mutableStateOf(false) }
            var playEndBeepEvent by remember { mutableStateOf(false) }
            var playRestBeepEvent by remember { mutableStateOf(false) }
            var playIntermediateBeep by remember { mutableStateOf(false) }

            val audioManager = remember { // Remember to avoid re-creating it on every recomposition
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
            var lastIntermediateBeepsChecked by rememberSaveable  { mutableStateOf<Boolean>(false) }

            //var isDimmedState by remember { mutableStateOf<Boolean>(false) }
            //var isTimerRunning by remember { mutableStateOf<Boolean>(false) }
            //var isRestMode by rememberSaveable { mutableStateOf(false) }
            //var isTimerStopped by remember { mutableStateOf(false) }

            var initialDurationSeconds by rememberSaveable { mutableStateOf<Int?>(null) }
            var currentDurationSecondsLeft by rememberSaveable { mutableStateOf<Int?>(null) }
            var currentRepetitionsLeft by rememberSaveable { mutableStateOf<Int?>(null) }
            var currentSeriesLeft by rememberSaveable { mutableStateOf<Int?>(null) }

            val minRepetitions = 3
            val maxRepetitions = 15
            val repetitionRange = (minRepetitions..maxRepetitions).toList()

            // Rest Mode & Series Tracking
            var currentRestTimeLeft by rememberSaveable { mutableStateOf<Int?>(null) }
            var initialRestTime by rememberSaveable { mutableStateOf<Int?>(null) } // To store calculated rest time
            val restingRatio = 0.5f
            val endOfRestBeepTime = 7 // seconds before end of rest to play beep

            val durationOptions = listOf("10 s", "15 s", "20 s", "30 s")
            val durationsScaling = 4f / durationOptions.size
            val durationButtonWidth = (
                    currentScreenWidthDp.value / durationOptions.size - horizontalDeviceScaling(8)
                    ).dp
            val seriesOptions = mutableListOf<Int>(1, 2, 3, 5, 10, 15, 20, 25, 30)  // MutableList
            val intermediateBeepsDuration = 5 // seconds for intermediate beeps

            val restModeText = stringResource(R.string.rest_indicator).toString()

            var beepSoundId by remember { mutableStateOf<Int?>(null) }
            var endBeepSoundId by remember { mutableStateOf<Int?>(null) }
            var intermediateBeepSoundId by remember { mutableStateOf<Int?>(null) }
            var soundPoolLoaded by remember { mutableStateOf(false) }

            // Load sound and release SoundPool
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
            fun isDimmedDisplay() : Boolean {
                return isTimerStopped_ || isSessionCompleted_
                //return timerViewModel.isTimerStopped.value || timerViewModel.isSessionCompleted.value
                //return isDimmedState
                //return sessionAutomaton.isTimerStopped() || sessionAutomaton.isSessionCompleted()
            }

            /**
             * Actions associated to the completion of a session
             */
            fun sessionHasCompleted() {
                /*isTimerRunning = false
                isDimmedState = true
                isTimerStopped = false
                isRestMode = false*/
                //sessionAutomaton.action(ESignal.SIG_COMPLETED)
                timerViewModel.action(ESignal.SIG_COMPLETED)
                playRestBeepEvent = false
                playEndBeepEvent = true
                currentDurationSecondsLeft = 0
                currentRepetitionsLeft = 0
                currentSeriesLeft = 0
            }

            // Play sound effect - single beep
            LaunchedEffect(playBeepEvent) {
                if (playBeepEvent && soundPoolLoaded && beepSoundId != null) {  // && audioIsNotMuted()) {
                    val actualVolume = audioVolumeLevel()
                    soundPool.play(beepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                }
                playBeepEvent = false // Reset trigger
            }

            // Play sound effect - end beep
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

            // Play sound effect - intermediate beep
            LaunchedEffect(playIntermediateBeep) {
                if (playIntermediateBeep && soundPoolLoaded && intermediateBeepSoundId != null && audioIsNotMuted()) {
                    val actualVolume = audioVolumeLevel()
                    soundPool.play(intermediateBeepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                }
                playIntermediateBeep = false // Reset trigger
            }

            // Play sound effect - rest beeps
            LaunchedEffect(playRestBeepEvent) {
                if (playRestBeepEvent && soundPoolLoaded && beepSoundId != null) {
                    val actualVolume = audioVolumeLevel()
                    soundPool.play(beepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                    delay(240L)
                    soundPool.play(beepSoundId!!, actualVolume, actualVolume, 1, 0, 1f)
                    playRestBeepEvent = false // Reset trigger
                }
            }

            LaunchedEffect(key1 = Unit) {
                userPreferencesRepository.userPreferencesFlow.collect { loadedPrefs ->
                    selectedDurationString = loadedPrefs.selectedDuration
                    numberOfRepetitions = loadedPrefs.numberOfRepetitions
                    numberOfSeries = loadedPrefs.numberOfSeries
                    intermediateBeepsChecked = loadedPrefs.intermediateBeeps

                    if (!isTimerRunning_ && !isRestMode_) {
                        val durationValue =
                            loadedPrefs.selectedDuration?.split(" ")?.firstOrNull()
                                ?.toIntOrNull()
                        initialDurationSeconds = durationValue
                        if (currentRepetitionsLeft != 0 && !isTimerStopped_) { // Only reset if not in a "completed or dimmed" state
                            currentDurationSecondsLeft = durationValue
                        }
                        if (!isTimerStopped_) {
                            currentRepetitionsLeft = numberOfRepetitions
                            currentSeriesLeft = numberOfSeries
                        }

                        lastDurationSeconds = durationValue ?: 0
                        lastNumberOfRepetitions = numberOfRepetitions ?: 0
                        lastNumberOfSeries = numberOfSeries ?: 0
                    }
                }
            }

            // Update initial/current countdown values when selections change AND timer is NOT running
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
                        if (isRestMode_) {
                            currentDurationSecondsLeft = initialDurationSeconds
                        }
                        else {
                            currentDurationSecondsLeft = min(
                                max(1,
                                    (currentDurationSecondsLeft
                                        ?: 0) + durationValue - lastDurationSeconds
                                ),
                                durationValue!!
                            )
                        }
                        lastDurationSeconds = durationValue
                        userPreferencesRepository.saveDurationPreference(selectedDurationString)
                    }
                }

                if (numberOfRepetitions != null && numberOfRepetitions != lastNumberOfRepetitions) {
                    currentRepetitionsLeft = min(
                        max(
                            if (isRestMode_) 1 else 0,  //if (sessionAutomaton.isRestMode()) 1 else 0,
                            (currentRepetitionsLeft?: 0) + numberOfRepetitions!! - lastNumberOfRepetitions
                        ),
                        numberOfRepetitions!!
                    )
                    lastNumberOfRepetitions = numberOfRepetitions!!
                    if (currentRepetitionsLeft == 0) {
                        currentDurationSecondsLeft = 1
                        initialRestTime =
                            (lastNumberOfRepetitions * lastDurationSeconds * restingRatio).roundToInt()
                                .coerceAtLeast(endOfRestBeepTime + 2) // Ensure rest is at least 5s for the beep logic
                        currentRestTimeLeft = initialRestTime
                        currentSeriesLeft = currentSeriesLeft!! - 1
                    }
                    userPreferencesRepository.saveRepetitionsPreference(numberOfRepetitions)
                }

                if (numberOfSeries != null && numberOfSeries != lastNumberOfSeries) {
                    currentSeriesLeft = max(0, (currentSeriesLeft?: 0) + numberOfSeries!! - lastNumberOfSeries)
                    lastNumberOfSeries = numberOfSeries!!
                    userPreferencesRepository.saveSeriesPreference(numberOfSeries)
                    //if (currentSeriesLeft == 0 || (sessionAutomaton.isRestMode() && currentSeriesLeft == 1)) {
                    if (currentSeriesLeft == 0 || (isRestMode_ && currentSeriesLeft == 1)) {
                            currentRestTimeLeft = 0
                            //isRestMode = false
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
             * Manage the timer countdown in a coroutine
             *
             * This is the core of the timer logic, managing countdowns, repetitions, series,
             * rest periods, and state transitions.
             * It reacts to changes in isTimerRunning and isRestMode states.
             */
            //LaunchedEffect(stateHasChanged) {            // <-----<<<
            LaunchedEffect(isTimerRunning_) {            // <-----<<<
                if (isTimerRunning_ || isRestMode_) {
                    (this as? ComponentActivity)?.window?.addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                }
                else {
                    (this as? ComponentActivity)?.window?.clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                }
                //sessionAutomaton.stateChanged = false

                //while (isActive && (sessionAutomaton.isTimerRunning() || sessionAutomaton.isRestMode())) {
                while (isActive && (isTimerRunning_ || isRestMode_ || isTimerStopped_)) {
                    // --- Normal Repetition Countdown ---
                    //isDimmedState = false

                    if (!isRestMode_) {  //!sessionAutomaton.isRestMode()) {  //
                        // Ensure values are sane before starting countdown loop
                        // If starting from a dimmed state (reps=0, duration=0), reset them.
                        if (currentRepetitionsLeft == 0) { // Indicates a previous cycle was completed
                            currentRepetitionsLeft = numberOfRepetitions  // Reset for new cycle
                            currentDurationSecondsLeft = initialDurationSeconds  // Reset for new cycle
                            currentSeriesLeft = currentSeriesLeft!! - 1
                        } else { // Normal start or resume
                            if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                                currentDurationSecondsLeft = initialDurationSeconds  //initialDurationSeconds
                            }
                            if (currentRepetitionsLeft == null) {
                                currentRepetitionsLeft = numberOfRepetitions  //numberOfRepetitions
                            }
                            if (currentSeriesLeft == null) {
                                currentSeriesLeft = numberOfSeries  //numberOfSeries
                            }
                        }

                        while (isActive &&
                            currentSeriesLeft!! > 0 &&
                            (isTimerRunning_ || isTimerStopped_) &&  //sessionAutomaton.isTimerRunning() &&  //
                            !isRestMode_  //!sessionAutomaton.isRestMode()  //
                        ) {
                            //if (currentDurationSecondsLeft != null && currentDurationSecondsLeft == initialDurationSeconds) {
                            if (currentDurationSecondsLeft!! == initialDurationSeconds!!) {
                                playBeepEvent = true
                            }

                            //if (currentDurationSecondsLeft != null && currentDurationSecondsLeft!! > 0) {
                            if (currentDurationSecondsLeft!! > 0) {
                                // current repetition timer tick
                                if (isTimerRunning_)
                                    delay(countDownDelay)  //1000L)
                                if (!(isTimerRunning_ || isTimerStopped_) || isRestMode_)
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
                                                playRestBeepEvent = true
                                                //isRestMode = true
                                                //sessionAutomaton.action(ESignal.SIG_REST_ON)
                                                timerViewModel.action(ESignal.SIG_REST_ON)

                                                val seriesDuration = (initialDurationSeconds
                                                    ?: 1) * (numberOfRepetitions
                                                    ?: 1) // Avoid 0 if null
                                                initialRestTime =
                                                    (seriesDuration * restingRatio).roundToInt()
                                                        .coerceAtLeast(endOfRestBeepTime + 2) // Ensure rest is at least 5s for the beep logic
                                                currentRestTimeLeft = initialRestTime
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
                                    if (isRestMode_)
                                        currentSeriesLeft = currentSeriesLeft!! + 1  // Must be restored to previous value
                                    timerViewModel.action(ESignal.SIG_REST_ON)  // Enter the resting mode
                                    break
                                }
                            } else {  // currentDurationSecondsLeft is null -> should never happen
                                break
                            }

                            if (isTimerStopped_) {
                                if (isRestMode_)
                                    currentSeriesLeft = currentSeriesLeft!! + 1  // Must be restored to previous value
                                break
                            }
                        }
                    }

                    // Have to check this since it may have been set in the block above
                    if (currentSeriesLeft!! == 0) {
                        // If no more series left, stop the timer and show dimmed state
                        sessionHasCompleted()
                    }
                    else if (isRestMode_) {  //sessionAutomaton.isRestMode()) {  //
                        // --- Rest Mode Countdown ---
                        // Check isRestMode again, as it could have been set in the block above
                        while (isActive && isRestMode_) {  //isTimerRunning_) {  //sessionAutomaton.isTimerRunning()) {  //
                            if (currentRestTimeLeft != null && currentRestTimeLeft!! > 0) {
                                // Check for some seconds left --> to play rest-beeps
                                if (currentRestTimeLeft == endOfRestBeepTime) {
                                    playRestBeepEvent = true
                                }
                                delay(countDownDelay)
                                //if (!isTimerRunning_ || !isRestMode_)
                                //    break
                                currentRestTimeLeft = currentRestTimeLeft!! - 1
                            } else {
                                // Rest time ended (currentRestTimeLeft is 0 or null)
                                /*isTimerRunning = true
                                isRestMode = false
                                isTimerStopped = false*/
                                //sessionAutomaton.action(ESignal.SIG_REST_OFF)
                                timerViewModel.action(ESignal.SIG_REST_OFF)
                                currentRepetitionsLeft = numberOfRepetitions // Reset for new cycle
                                currentSeriesLeft = currentSeriesLeft!! - 1
                                break // Exit rest loop
                            }
                        }
                    }

                if (isTimerStopped_)
                    break
                }

                //isDimmedState = sessionAutomaton.isSessionCompleted() || sessionAutomaton.isTimerStopped()
                //isDimmedState = currentSeriesLeft == 0 || isTimerStopped  //sessionAutomaton.isTimerStopped()
            }


            @Composable
            fun RepetitionsSelectorWithScrollIndicators(
                selectedRepetition: Int?,
                onRepetitionSelected: (Int) -> Unit,
                repetitionsListState: LazyListState = rememberLazyListState(), // Pass or remember
                // Add scaleFactor or other styling params if needed
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
                            repetitionRange.isNotEmpty() // True if there are items but no layout info yet (initial state before first scroll/layout)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier
                            .weight(1f) // LazyRow takes available space between arrows
                            .padding(horizontal = 0.dp), // No extra padding here if arrows handle spacing
                        horizontalArrangement = Arrangement.spacedBy(horizontalSpaceArrangement) // Spacing between number buttons
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
                        items(repetitionRange, key = { it }) { number -> // Add a key for better performance
                            val isNumberSelected = number == numberOfRepetitions
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
                                                if (isNumberSelected) null else number
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$number",
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
                val buttonHeight = currentScreenHeightDp.value * buttonScaling  //deviceScaling(56)
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
                            if (isTimerRunning_) {  // sessionAutomaton.isTimerRunning()) {  // // Ask for stop state
                                /*isTimerRunning = false
                                isTimerStopped = true*/
                                //sessionAutomaton.action(ESignal.SIG_STOP)
                                timerViewModel.action(ESignal.SIG_STOP)
                            } else if (isTimerStopped_) {  //sessionAutomaton.isTimerStopped()) {  //  // Resume from stop state
                                /*isTimerRunning = true
                                isTimerStopped = false*/
                                //sessionAutomaton.action(ESignal.SIG_START)
                                timerViewModel.action(ESignal.SIG_START)
                            } else {  // Trying to Start (or Restart after session completion)
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
                                    //isTimerRunning = true  // Start - running state
                                    //sessionAutomaton.action(ESignal.SIG_START)
                                    timerViewModel.action(ESignal.SIG_START)
                                }
                            }
                        },
                        enabled = (isTimerRunning_ || allSelectionsMade) && !isRestMode_,
                        //enabled = allSelectionsMade && !sessionAutomaton.isRestMode(),              // <------<<<
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
                                //id = if (sessionAutomaton.isTimerRunning()) R.string.stop_button
                                id = if (isTimerRunning_) R.string.stop_button
                                     else R.string.start_button
                            ),
                            style = customInteractiveTextStyle.copy(
                                //color = if (allSelectionsMade && !sessionAutomaton.isRestMode()) AppButtonTextColor
                                color = if (allSelectionsMade && !isRestMode_) AppButtonTextColor
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
                        .padding(vertical = deviceScaling(4).dp),
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
                                color = if (isRestMode_) WABlueColor  //sessionAutomaton.isRestMode()) WABlueColor
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

                            //if (!isRestMode && sessionAutomaton.isTimerActivated() && sweepAngle > 0f) {  // <-----<<<
                            if (!isRestMode_ && (isTimerRunning_ || isTimerStopped_) && sweepAngle > 0f) {
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
                            //val showDimmedTimers = currentRepetitionsLeft == 0 && !sessionAutomaton.isTimerActivated()
                            val showDimmedTimers = currentRepetitionsLeft == 0 &&
                                                    !(isTimerRunning_ || isTimerStopped_ || isRestMode_)

                            val durationToDisplayValue =
                                /*if (sessionAutomaton.isSessionCompleted()) 0
                                else if (sessionAutomaton.isRestMode()) currentRestTimeLeft
                                else currentDurationSecondsLeft*/
                                if (showDimmedTimers) 0
                                else if (isRestMode_) currentRestTimeLeft
                                else currentDurationSecondsLeft

                            val durationToDisplayString = durationToDisplayValue?.toString()
                                ?: initialDurationSeconds?.toString()
                                ?: selectedDurationString?.split(" ")
                                    ?.firstOrNull() ?: ""

                            if (durationToDisplayString.isNotEmpty()) {
                                val targetTextHeightPx = circleRadius * 0.9f

                                val countdownTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                    color = (if (isRestMode_) WABlueColor  //sessionAutomaton.isRestMode()) WABlueColor
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
                                //if (sessionAutomaton.isRestMode()) {
                                if (isRestMode_) {
                                    val restTextSizePx = targetTextHeightPx * 0.22f
                                    val restTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                        color = WABlueColor.toArgb()
                                        textSize = max(16f, restTextSizePx)
                                        isAntiAlias = true
                                        textAlign = Paint.Align.CENTER
                                        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
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
                                    typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
                                }

                                drawContext.canvas.nativeCanvas.drawText(
                                    ">>> DEBUG mode",
                                    30f,
                                    38f,
                                    restTextPaint
                                )
                            }

                            val circleCenterX = size.width / 2
                            val circleCenterY = size.height - seriesCircleRadius - localPadding.toPx()

                            // Draw circle border
                            drawCircle(
                                color = if (isDimmedDisplay()) DimmedTimerBorderColor else TimerBorderColor,
                                radius = seriesCircleRadius - seriesStrokeWidthPx / 2,
                                style = Stroke(width = seriesStrokeWidthPx),
                                center = Offset(circleCenterX, circleCenterY )
                                //center = Offset(size.width / 2, size.height / 2)
                            )

                            // Draw the progress arc
                            val totalRepetitions = (numberOfRepetitions?: 0) * (numberOfSeries?: 0)
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
                            if ((isTimerRunning_ || isTimerStopped_) && sweepAngle > 0f) {
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

                                val countdownTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
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
                                        //selectedDurationString =
                                        //    if (isSelected) null else durationString
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

                    RepetitionsSelectorWithScrollIndicators(
                        selectedRepetition = numberOfRepetitions, // Your state variable for the current selection
                        onRepetitionSelected = { selected ->
                            numberOfRepetitions = selected
                            // Call your userPreferencesRepository.saveRepetitionsPreference(selected) here
                        },
                        repetitionsListState = repetitionsLazyListState // Pass the state
                    )

                    /*
                    LazyRow( // Repetitions LazyRow
                        state = repetitionsLazyListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = deviceScaling(12).dp, bottom = deviceScaling(8).dp),
                        horizontalArrangement = Arrangement.spacedBy(deviceScaling(10).dp),
                        contentPadding = PaddingValues(horizontal = generalPadding)
                    ) {
                        items(repetitionRange) { number ->
                            val isNumberSelected = number == numberOfRepetitions
                            val isClickable = true  //!(isTimerRunning || isTimerStopped)
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
                                                if (isNumberSelected) null else number
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$number",
                                    style = customInteractiveTextStyle.copy(
                                        color = if (isNumberSelected) AppButtonTextColor
                                        //else if (isTimerRunning || isTimerStopped) AppDimmedTextColor
                                        else AppTextColor
                                    ),
                                    fontWeight = if (isNumberSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    */

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
                        // Check the available display width against the series-options list width
                        with(LocalDensity.current) {
                            val horizontalSpaceArrangementPx = deviceScaling(8).dp.toPx()
                            val seriesBoxSizePx = seriesBoxSize.toPx()
                            val visibleWidth = availableWidthForContentDp.toPx() - 2 * mainHorizontalSpacingDp.toPx()

                            while(seriesOptions.size > 1 &&
                                seriesOptions.size * (seriesBoxSizePx + horizontalSpaceArrangementPx) -
                                    horizontalSpaceArrangementPx > visibleWidth
                            ) {
                                // Remove one of the Series number, let's says the one in second position (index 1)
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

                    /*
                    Button( // Quit Button
                        onClick = { processCloseAppActions() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppButtonColor,
                            contentColor = AppButtonTextColor
                        ),
                        modifier = Modifier
                            .align(Alignment.End)
                            .wrapContentHeight()
                            .padding(
                                end = deviceScaling(16).dp,
                                top = deviceScaling(8).dp
                            )
                            .scale(horizontalScaleFactor)
                    ) {
                        Text(
                            stringResource(id = R.string.close_button),
                            style = customInteractiveTextStyle
                        )
                    }
                    */
                }
            }

            // --- Add Your Logo Here, Aligned to Bottom-Left ---
            Image(
                painter = painterResource(id = R.drawable.ps_logo),
                contentDescription = null, // Important for accessibility - provide a meaningful description (e.g. "Editor Logo")or null if purely decorative
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Aligns this Image to the bottom start (left) of the parent Box
                    .padding(
                        end = deviceScaling(16).dp//,
                        //bottom = deviceScaling(8).dp
                    ) // Add some padding from the screen edges
                    .size(34.dp) // Set the size of the image on screen
            )
        }
    }

}
