package com.github.schmouk.archerytrainingtimer

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.text.TextPaint
//import android.util.Log
import android.view.WindowManager

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display to draw behind the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        userPreferencesRepository = UserPreferencesRepository(applicationContext)
        keepScreenOn()
        setContent {
            ArcheryTrainingTimerTheme {
                SimpleScreen(
                    userPreferencesRepository = userPreferencesRepository,
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
    onCloseApp: () -> Unit
) {
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

            val heightScalingFactor = this.maxHeight.value / currentScreenHeightDp.value
            val widthScalingFactor  = this.maxWidth.value  / currentScreenWidthDp.value

            // Playing sound
            val context = LocalContext.current
            var playBeepEvent by remember { mutableStateOf(false) }
            var playEndBeepEvent by remember { mutableStateOf(false) }
            var playRestBeepEvent by remember { mutableStateOf(false) }
            var playIntermediateBeep by remember { mutableStateOf(false) }

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

            // Play sound effect - single beep
            LaunchedEffect(playBeepEvent) {
                if (playBeepEvent && soundPoolLoaded && beepSoundId != null) {
                    soundPool.play(beepSoundId!!, 1f, 1f, 1, 0, 1f)
                    playBeepEvent = false // Reset trigger
                }
            }

            // Play sound effect - end beep
            LaunchedEffect(playEndBeepEvent) {
                if (playEndBeepEvent && soundPoolLoaded && endBeepSoundId != null) {
                    soundPool.play(endBeepSoundId!!, 1f, 1f, 1, 0, 1f)
                    delay(380L)
                    soundPool.play(endBeepSoundId!!, 1f, 1f, 1, 0, 1f)
                    delay(380L)
                    soundPool.play(endBeepSoundId!!, 1f, 1f, 1, 0, 1f)
                    playEndBeepEvent = false // Reset trigger
                }
            }

            // Play sound effect - intermediate beep
            LaunchedEffect(playIntermediateBeep) {
                if (playIntermediateBeep && soundPoolLoaded && intermediateBeepSoundId != null) {
                    soundPool.play(intermediateBeepSoundId!!, 1f, 1f, 1, 0, 1f)
                    playIntermediateBeep = false // Reset trigger
                }

            }

            // Play sound effect - rest beeps
            LaunchedEffect(playRestBeepEvent) {
                if (playRestBeepEvent && soundPoolLoaded && beepSoundId != null) {
                    soundPool.play(beepSoundId!!, 1f, 1f, 1, 0, 1f)
                    delay(240L)
                    soundPool.play(beepSoundId!!, 1f, 1f, 1, 0, 1f)
                    playRestBeepEvent = false // Reset trigger
                }
            }

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


            // --- The Main Column for the entire screen content ---
            Column(
                modifier = Modifier
                    .padding(horizontal = deviceScaling(10).dp)
                    .fillMaxSize(), // Fill the BoxWithConstraints
                horizontalAlignment = Alignment.CenterHorizontally,

                // Add verticalArrangement as needed, e.g., Arrangement.SpaceAround
            ) {
                // --- Dynamic Sizes & SPs ---
                val mainTimerStrokeWidth = deviceScaling(14).dp
                val repetitionBoxSize = deviceScaling(48).dp
                val majorSpacerHeight = deviceScaling(8).dp
                val generalPadding = deviceScaling(12).dp

                var selectedDurationString by rememberSaveable { mutableStateOf<String?>(null) }
                var numberOfRepetitions by remember { mutableStateOf<Int?>(null) }
                var numberOfSeries by remember { mutableStateOf<Int?>(null) }
                var intermediateBeepsChecked by remember { mutableStateOf<Boolean?>(null) }
                //var saveSelectionChecked by remember { mutableStateOf(false) }

                var lastDurationSeconds by rememberSaveable { mutableStateOf<Int>(0) }
                var lastNumberOfRepetitions by rememberSaveable { mutableStateOf<Int>(0) }
                var lastNumberOfSeries by rememberSaveable { mutableStateOf<Int>(0) }

                var isTimerRunning by remember { mutableStateOf(false) }
                var isTimerStopped by remember { mutableStateOf(false) }
                var isDimmedState by remember { mutableStateOf(false) }

                var initialDurationSeconds by rememberSaveable { mutableStateOf<Int?>(null) }
                var currentDurationSecondsLeft by rememberSaveable { mutableStateOf<Int?>(null) }
                var currentRepetitionsLeft by rememberSaveable { mutableStateOf<Int?>(null) }
                var currentSeriesLeft by rememberSaveable { mutableStateOf<Int?>(null) }

                // Rest Mode & Series Tracking
                var isRestMode by rememberSaveable { mutableStateOf(false) }
                var currentRestTimeLeft by rememberSaveable { mutableStateOf<Int?>(null) }
                var initialRestTime by rememberSaveable { mutableStateOf<Int?>(null) } // To store calculated rest time
                val restingRatio = 0.5f
                val endOfRestBeepTime = 7 // seconds before end of rest to play beep

                val durationOptions = listOf("5 s", "10 s", "15 s", "20 s", "30 s")
                val durationsScaling = 4f / durationOptions.size
                val durationButtonWidth = (
                        currentScreenWidthDp.value / durationOptions.size - horizontalDeviceScaling(8)
                    ).dp
                val minRepetitions = 3
                val maxRepetitions = 15
                val repetitionRange = (minRepetitions..maxRepetitions).toList()
                val seriesOptions = listOf(1, 10, 15, 20, 25, 30)
                val intermediateBeepsDuration = 5 // seconds for intermediate beeps

                val customInteractiveTextStyle = TextStyle(fontSize = deviceScaling(18).sp)
                val smallerTextStyle = TextStyle(fontSize = deviceScaling(16).sp)
                val repetitionsLazyListState = rememberLazyListState()
                //val coroutineScope = rememberCoroutineScope()

                val restModeText = stringResource(R.string.rest_indicator).toString()


                // This flag will determine if the timers should be dimmed
                // It's true when a full cycle of repetitions is complete and timer is stopped
                val showDimmedTimers = rememberSaveable(currentRepetitionsLeft, isTimerRunning) {
                    currentRepetitionsLeft == 0 && !isTimerRunning && !isRestMode
                }

                LaunchedEffect(key1 = Unit) {
                    userPreferencesRepository.userPreferencesFlow.collect { loadedPrefs ->
                        selectedDurationString = loadedPrefs.selectedDuration
                        numberOfRepetitions = loadedPrefs.numberOfRepetitions
                        numberOfSeries = loadedPrefs.numberOfSeries
                        //saveSelectionChecked = loadedPrefs.saveSelection
                        intermediateBeepsChecked = loadedPrefs.intermediateBeeps

                        if (!isTimerRunning && !isRestMode) {
                            val durationValue =
                                loadedPrefs.selectedDuration?.split(" ")?.firstOrNull()
                                    ?.toIntOrNull()
                            initialDurationSeconds = durationValue
                            if (currentRepetitionsLeft != 0) { // Only reset if not in a "completed and dimmed" state
                                currentDurationSecondsLeft = durationValue
                            }
                            currentRepetitionsLeft = numberOfRepetitions
                            currentSeriesLeft = numberOfSeries

                            lastDurationSeconds = durationValue ?: 0
                            lastNumberOfRepetitions = numberOfRepetitions ?: 0
                            lastNumberOfSeries = numberOfSeries ?: 0
                        }
                    }
                }

                // Determine if all selections are made
                val allSelectionsMade = selectedDurationString != null &&
                        numberOfRepetitions != null &&
                        numberOfSeries != null

                // Update initial/current countdown values when selections change AND timer is NOT running
                LaunchedEffect(
                    selectedDurationString,
                    numberOfRepetitions,
                    numberOfSeries,
                    isTimerRunning,
                    intermediateBeepsChecked
                ) {
                    /*
                    if (!isTimerRunning && !isTimerStopped) {
                        val durationValue =
                            selectedDurationString?.split(" ")?.firstOrNull()?.toIntOrNull()
                        initialDurationSeconds = durationValue
                        // Only update currentDurationSecondsLeft if not in the "dimmed" state from a previous cycle
                        if (currentRepetitionsLeft != 0 || currentDurationSecondsLeft != 0) {
                            currentDurationSecondsLeft = durationValue
                            currentRepetitionsLeft = numberOfRepetitions
                            currentSeriesLeft = numberOfSeries
                        }
                    }
                    */


                    if (selectedDurationString != null) {
                        val durationValue =
                            selectedDurationString?.split(" ")?.firstOrNull()?.toIntOrNull()
                        if (durationValue != null && durationValue != lastDurationSeconds) {
                            initialDurationSeconds = durationValue
                            currentDurationSecondsLeft = min(
                                max( 1, currentDurationSecondsLeft!! + durationValue - lastDurationSeconds),
                                durationValue!!
                            )
                            lastDurationSeconds = durationValue
                            userPreferencesRepository.saveDurationPreference(selectedDurationString)
                        }
                    }

                    if (numberOfRepetitions != null && numberOfRepetitions != lastNumberOfRepetitions) {
                        currentRepetitionsLeft = min(
                            max(if (isRestMode) 0 else 1, currentRepetitionsLeft!! + numberOfRepetitions!! - lastNumberOfRepetitions),
                            numberOfRepetitions!!
                        )
                        lastNumberOfRepetitions = /*if (isRestMode) numberOfRepetitions!! - 1 else*/ numberOfRepetitions!!
                        userPreferencesRepository.saveRepetitionsPreference(numberOfRepetitions)
                    }

                    if (numberOfSeries != null && numberOfSeries != lastNumberOfSeries) {
                        currentSeriesLeft = max(1, currentSeriesLeft!! + numberOfSeries!! - lastNumberOfSeries)
                        lastNumberOfSeries = numberOfSeries!!
                        userPreferencesRepository.saveSeriesPreference(numberOfSeries)
                    }

                    if (intermediateBeepsChecked != null)
                        userPreferencesRepository.saveIntermediateBeepsPreference(intermediateBeepsChecked ?: false)

                    //userPreferencesRepository.saveSaveSelectionPreference(true)
                }

                LaunchedEffect(isTimerRunning) {
                    if (isTimerRunning)
                        (this as? ComponentActivity)?.window?.addFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        )
                    else
                        (this as? ComponentActivity)?.window?.clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        )

                    while (isTimerRunning) {
                        // --- Normal Repetition Countdown ---
                        if (!isRestMode) {
                            // Ensure values are sane before starting countdown loop
                            // If starting from a dimmed state (reps=0, duration=0), reset them.
                            if (currentRepetitionsLeft == 0) { // Indicates a previous cycle was completed
                                currentRepetitionsLeft = numberOfRepetitions  // Reset for new cycle
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

                            while (isTimerRunning &&
                                    !isRestMode &&
                                    isActive &&
                                    currentSeriesLeft!! > 0
                            ) {
                                if (currentDurationSecondsLeft != null && currentDurationSecondsLeft == initialDurationSeconds) {
                                    playBeepEvent = true
                                }
                                if (currentDurationSecondsLeft != null && currentDurationSecondsLeft!! > 0) {
                                    // current repetition timer tick
                                    delay(1000L)
                                    if (!isTimerRunning || isRestMode)
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
                                } else if (currentDurationSecondsLeft == 0) {
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
                                                    isTimerRunning = false
                                                    isTimerStopped = false
                                                    isDimmedState = true
                                                    isRestMode = false
                                                    playRestBeepEvent = false
                                                    playEndBeepEvent = true
                                                    currentDurationSecondsLeft = 0
                                                    currentRepetitionsLeft = 0
                                                    currentSeriesLeft = 0
                                                    break
                                                } else {
                                                    // enters the rest mode
                                                    playRestBeepEvent = true
                                                    isRestMode = true

                                                    val seriesDuration = (initialDurationSeconds
                                                        ?: 1) * (numberOfRepetitions
                                                        ?: 1) // Avoid 0 if null
                                                    initialRestTime =
                                                        (seriesDuration * restingRatio).roundToInt()
                                                            .coerceAtLeast(endOfRestBeepTime + 2) // Ensure rest is at least 5s for the beep logic
                                                    currentRestTimeLeft = initialRestTime
                                                }
                                            } else {
                                                // !(currentSeriesLeft != null && currentSeriesLeft!! > 0)
                                                // --> this is the end of the training session
                                                // notice: dead code? Let's check...

                                                // If no more series left, stop the timer and show dimmed state
                                                isTimerRunning = false
                                                isTimerStopped = false
                                                isDimmedState = true
                                                isRestMode = false
                                                playRestBeepEvent = false
                                                playEndBeepEvent = true
                                                currentDurationSecondsLeft = 0
                                                currentRepetitionsLeft = 0
                                                currentSeriesLeft = 0
                                                break
                                            }
                                        } else {
                                            // let's start a new repetition into current series
                                            currentDurationSecondsLeft = initialDurationSeconds
                                        }
                                    } else { // currentRepetitionsLeft is null -> should never happen
                                        isTimerRunning = false
                                        break
                                    }
                                } else {  // currentDurationSecondsLeft is null -> should never happen
                                    isTimerRunning = false
                                    break
                                }
                            }
                        }

                        // Have to check this since it may have been set in the block above
                        if (currentSeriesLeft!! == 0) {
                            // If no more series left, stop the timer and show dimmed state
                            isTimerRunning = false
                            isTimerStopped = false
                            isDimmedState = true
                            isRestMode = false
                            playRestBeepEvent = false
                            playEndBeepEvent = true
                            currentDurationSecondsLeft = 0
                            currentRepetitionsLeft = 0
                            currentSeriesLeft = 0
                        }
                        else
                        // --- Rest Mode Countdown ---
                        if (isRestMode) { // Check isRestMode again, as it could have been set in the block above
                            while (isTimerRunning && isActive) {
                                if (currentRestTimeLeft != null && currentRestTimeLeft!! > 0) {
                                    // Check for some seconds left --> to play rest-beeps
                                    if (currentRestTimeLeft == endOfRestBeepTime) {
                                        playRestBeepEvent = true
                                    }
                                    delay(1000L)
                                    if (!isTimerRunning || !isRestMode)
                                        break
                                    currentRestTimeLeft = currentRestTimeLeft!! - 1
                                } else {
                                    // Rest time ended (currentRestTimeLeft is 0 or null)
                                    isRestMode = false
                                    isTimerRunning = true
                                    isDimmedState = false
                                    isTimerStopped = false
                                    currentRepetitionsLeft = numberOfRepetitions // Reset for new cycle
                                    currentSeriesLeft = currentSeriesLeft!! - 1
                                    break // Exit rest loop
                                }
                            }
                        }
                    }
                }

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
                            if (isTimerRunning) {  // Just pause - stop state
                                isTimerRunning = false
                                isTimerStopped = true
                                isDimmedState = true
                            } else if (isTimerStopped) {  // Resume from stop state
                                isTimerStopped = false
                                isTimerRunning = true
                                isDimmedState = false
                            } else { // Trying to Start or Restart after session completion
                                if (allSelectionsMade) {
                                    // If currentRepetitionsLeft is 0, it means a cycle just finished (dimmed state).
                                    // Reset both countdowns for a new cycle.
                                    if (currentSeriesLeft == null || currentSeriesLeft == 0) {
                                        currentSeriesLeft = numberOfSeries
                                        currentRepetitionsLeft = numberOfRepetitions
                                        currentDurationSecondsLeft =
                                            initialDurationSeconds
                                    } else {  // CAUTION: is this dead code? Let's check...
                                        // Handle cases where selections might have been cleared or timer never run
                                        if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                                            currentDurationSecondsLeft =
                                                initialDurationSeconds
                                        }
                                        if (currentRepetitionsLeft == null) { // This should ideally not happen if allSelectionsMade is true
                                            currentRepetitionsLeft = numberOfRepetitions
                                        }
                                    }
                                    isTimerRunning = true  // Start - running state
                                    isDimmedState = false
                                }
                            }
                        },
                        enabled = (isTimerRunning || allSelectionsMade) && !isRestMode,
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
                            text = stringResource(id = if (isTimerRunning) R.string.stop_button else R.string.start_button),
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
                                color = if (isRestMode) WABlueColor else if (isDimmedState) DimmedTimerBorderColor else TimerBorderColor,
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
                                    (circleRadius - strokeWidthPx / 2f) * 2f  //) * 2f  //
                                val arcTopLeftX = canvasCenterX - arcDiameter / 2f
                                val arcTopLeftY = canvasCenterY - arcDiameter / 2f

                                val progressStrokeWidth =
                                    (0.72 * mainTimerStrokeWidth.value).dp.toPx()

                                drawArc(
                                    color = if (isDimmedState) DimmedProgressBorderColor else ProgressBorderColor,
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
                            val durationToDisplayValue =
                                if (showDimmedTimers) 0 else if (isRestMode) currentRestTimeLeft else currentDurationSecondsLeft
                            val durationToDisplayString = durationToDisplayValue?.toString()
                                ?: initialDurationSeconds?.toString()
                                ?: selectedDurationString?.split(" ")
                                    ?.firstOrNull() ?: ""

                            if (durationToDisplayString.isNotEmpty()) {
                                val targetTextHeightPx = circleRadius * 0.9f

                                val countdownTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                    color = (if (isRestMode) WABlueColor
                                             else if (isDimmedState) DimmedTimerBorderColor
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
                        val seriesStrokeWidthPx = with(LocalDensity.current) {
                            deviceScaling(7).dp.toPx()
                        }
                        val localPadding = deviceScaling(8).dp

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val circleCenterX = size.width / 2
                            val circleCenterY = size.height - seriesCircleRadius - localPadding.toPx()
                            drawCircle(
                                color = if (isDimmedState) DimmedTimerBorderColor else TimerBorderColor,
                                radius = seriesCircleRadius - seriesStrokeWidthPx / 2,
                                style = Stroke(width = seriesStrokeWidthPx),
                                center = Offset(circleCenterX, circleCenterY )
                                //center = Offset(size.width / 2, size.height / 2)
                            )

                            // Series display will show 0 when dimmed
                            val seriesToDisplayValue = currentSeriesLeft
                            val seriesToDisplayString =
                                seriesToDisplayValue?.toString()
                                    ?: currentSeriesLeft?.toString() ?: ""

                            if (seriesToDisplayString.isNotEmpty()) {
                                val targetTextHeightPx = seriesCircleRadius * 0.9f

                                val countdownTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                    color = (
                                        if (isDimmedState)
                                            DimmedTimerBorderColor
                                        else
                                            TimerBorderColor
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
                                    enabled = true,  //isSelected || !(isTimerRunning || isTimerStopped),
                                    modifier = Modifier
                                        .width(durationButtonWidth)
                                ) {
                                    Text(
                                        text = durationString,
                                        style = TextStyle(
                                            fontSize = (13f * durationsScaling).toInt().sp,
                                            //color = if (isSelected) AppButtonTextColor else if (isTimerRunning || isTimerStopped) AppDimmedTextColor else AppTextColor
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
                            .padding(top = generalPadding)
                            .wrapContentHeight()
                            .align(Alignment.CenterHorizontally)
                    )
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
                        Row(horizontalArrangement = Arrangement.spacedBy(deviceScaling(10).dp)) {
                            seriesOptions.forEach { seriesCount ->
                                val isSeriesSelected = seriesCount == numberOfSeries
                                val isClickable = true  //!(isTimerRunning || isTimerStopped)
                                Box(
                                    modifier = Modifier
                                        .size(repetitionBoxSize)  //48.dp)
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
                                            //else if (isTimerRunning || isTimerStopped) AppDimmedTextColor
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
                                onValueChange = { intermediateBeepsChecked = !intermediateBeepsChecked!! }
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