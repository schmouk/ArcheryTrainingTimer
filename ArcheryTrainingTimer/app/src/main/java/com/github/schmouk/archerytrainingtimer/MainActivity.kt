package com.github.schmouk.archerytrainingtimer

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.archerytrainingtimer.ui.theme.*

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import kotlin.math.min
import kotlin.math.roundToInt

// import android.util.Log

// MainActivity class definition (no change from before)
class MainActivity : ComponentActivity() {
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    // Call this function when you want to allow the screen to turn off normally again
    // e.g., when your timer stops or the user navigates away from the critical section.
    private fun allowScreenTimeout() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // Call this function when you want to force the screen to stay on
    // e.g., when your timer starts.
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


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SimpleScreen(
    userPreferencesRepository: UserPreferencesRepository,
    onCloseApp: () -> Unit
) {
    // adapt items size to screen width
    val configuration = LocalConfiguration.current
    val currentScreenWidthDp = configuration.screenWidthDp.dp
    val currentScreenHeightDp = configuration.screenHeightDp.dp
    val refScreenWidthDp = 411.dp // Your baseline for good proportions
    val refScreenHeightDp = 914.dp // Your baseline for good proportions
    // Calculate scale factor, ensure it's not Dp / Dp if you need a raw float
    val textHorizontalScaleFactor = currentScreenWidthDp.value / refScreenWidthDp.value
    val horizontalScaleFactor = textHorizontalScaleFactor.coerceIn(0.60f, 1.0f)
    val verticalScaleFactor = (currentScreenHeightDp.value / refScreenHeightDp.value).coerceIn(0.40f, 1.5f)
    val scaleFactor = min(horizontalScaleFactor, verticalScaleFactor)

    // Playing sound
    val context = LocalContext.current
    var playBeepEvent by remember { mutableStateOf(false) }
    var playEndBeepEvent by remember { mutableStateOf(false) }
    var playRestBeepEvent by remember { mutableStateOf(false) }

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
    var soundPoolLoaded by remember { mutableStateOf(false) }

    // Load sound and release SoundPool
    DisposableEffect(Unit) {
        beepSoundId = soundPool.load(context, R.raw.beep, 1)
        endBeepSoundId = soundPool.load(context, R.raw.beep_end, 1)
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
            //soundPool.play(beepSoundId!!, 1f, 1f, 1, 0, 1f)
            delay(340L)
            soundPool.play(endBeepSoundId!!, 1f, 1f, 1, 0, 1f)
            //soundPool.play(beepSoundId!!, 1f, 1f, 1, 0, 1f)
            delay(340L)
            soundPool.play(endBeepSoundId!!, 1f, 1f, 1, 0, 1f)
            //soundPool.play(beepSoundId!!, 1f, 1f, 1, 0, 1f)
            playEndBeepEvent = false // Reset trigger
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
    fun deviceScaling(dim: Int) : Float {
        return scaleFactor * dim
    }

    // scales horizontal dimension (width) according to the running device horizontalScaleFactor factor
    fun horizontalDeviceScaling(dim: Int) : Float {
        return horizontalScaleFactor * dim
    }

    // scales big text dimension (width or height) according to the running device horizontalScaleFactor factor
    fun bigTextHorizontalDeviceScaling(dim: Int) : Float {
        return textHorizontalScaleFactor * dim
    }


    // --- Dynamic Sizes & SPs ---
    val mainTimerStrokeWidth = deviceScaling(14).dp
    val adaptiveInitialMainFontSize = bigTextHorizontalDeviceScaling(76).sp
    val adaptiveInitialRestFontSize = bigTextHorizontalDeviceScaling(17).sp
    val adaptiveInitialSeriesFontSize = bigTextHorizontalDeviceScaling(34).sp
    val repetitionBoxSize = deviceScaling(48).dp
    val majorSpacerHeight = deviceScaling(8).dp
    val generalPadding = deviceScaling(12).dp  // 16

    var selectedDurationString by rememberSaveable { mutableStateOf<String?>(null) }
    var numberOfRepetitions by remember { mutableStateOf<Int?>(null) }
    var numberOfSeries by remember { mutableStateOf<Int?>(null) }
    var saveSelectionChecked by remember { mutableStateOf(false) }
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

    val durationOptions = listOf("10 s", "15 s", "20 s", "30 s")
    val durationsScaling = 4f / durationOptions.size
    val durationButtonWidth = (currentScreenWidthDp.value / durationOptions.size - horizontalDeviceScaling(8)).dp
    val minRepetitions = 3
    val maxRepetitions = 15
    val repetitionRange = (minRepetitions..maxRepetitions).toList()
    val seriesOptions = listOf(1, 10, 15, 20, 25, 30)

    val customInteractiveTextStyle = TextStyle(fontSize = deviceScaling(18).sp)
    val smallerTextStyle = TextStyle(fontSize = deviceScaling(16).sp)
    val repetitionsLazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // This flag will determine if the timers should be dimmed
    // It's true when a full cycle of repetitions is complete and timer is stopped
    val showDimmedTimers = rememberSaveable(currentRepetitionsLeft, isTimerRunning) {
        currentRepetitionsLeft == 0 && !isTimerRunning  && !isRestMode
    }

    LaunchedEffect(key1 = Unit) {
        userPreferencesRepository.userPreferencesFlow.collect { loadedPrefs ->
            selectedDurationString = loadedPrefs.selectedDuration
            numberOfRepetitions = loadedPrefs.numberOfRepetitions
            numberOfSeries = loadedPrefs.numberOfSeries
            saveSelectionChecked = loadedPrefs.saveSelection

            if (!isTimerRunning && !isRestMode) {
                val durationValue = loadedPrefs.selectedDuration?.split(" ")?.firstOrNull()?.toIntOrNull()
                initialDurationSeconds = durationValue
                if (currentRepetitionsLeft != 0) { // Only reset if not in a "completed and dimmed" state
                    currentDurationSecondsLeft = durationValue
                }
                currentRepetitionsLeft = numberOfRepetitions  //loadedPrefs.numberOfRepetitions
                currentSeriesLeft = numberOfSeries  //loadedPrefs.numberOfSeries
            }
        }
    }

    // Determine if all selections are made
    val allSelectionsMade = selectedDurationString != null &&
            numberOfRepetitions != null &&
            numberOfSeries != null

    // Update initial/current countdown values when selections change AND timer is NOT running
    LaunchedEffect(selectedDurationString, numberOfRepetitions, numberOfSeries, isTimerRunning) {
        if (!isTimerRunning && !isTimerStopped) {
            val durationValue = selectedDurationString?.split(" ")?.firstOrNull()?.toIntOrNull()
            initialDurationSeconds = durationValue
            // Only update currentDurationSecondsLeft if not in the "dimmed" state from a previous cycle
            if (currentRepetitionsLeft != 0 || currentDurationSecondsLeft != 0 ) {
                currentDurationSecondsLeft = durationValue
                currentRepetitionsLeft = numberOfRepetitions
                currentSeriesLeft = numberOfSeries
            }
        }
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

                while (isTimerRunning && !isRestMode && isActive) {
                    if (currentDurationSecondsLeft != null && currentDurationSecondsLeft == initialDurationSeconds) {
                        playBeepEvent = true
                    }
                    if (currentDurationSecondsLeft != null && currentDurationSecondsLeft!! > 0) {
                        // current repetition timer tick
                        delay(1000L)
                        if (!isTimerRunning || isRestMode)
                            break
                        currentDurationSecondsLeft = currentDurationSecondsLeft!! - 1
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

                                        val seriesDuration = (initialDurationSeconds ?: 1) * (numberOfRepetitions ?: 1) // Avoid 0 if null
                                        initialRestTime = (seriesDuration * restingRatio).roundToInt().coerceAtLeast(endOfRestBeepTime + 2) // Ensure rest is at least 5s for the beep logic
                                        currentRestTimeLeft = initialRestTime

                                        // reset duration for next repetition in the same series
                                        //currentDurationSecondsLeft = initialDurationSeconds
                                        //currentRepetitionsLeft = numberOfRepetitions
                                    }
                                } else {
                                    // (currentSeriesLeft != null && currentSeriesLeft!! > 0)
                                    // this is the end of the training session
                                    // notice: dead code? Let's check...

                                    // If no more series left, stop the timer and show dimmed state
                                    isTimerRunning = false
                                    isTimerStopped = false
                                    isDimmedState = true
                                    isRestMode = false
                                    playRestBeepEvent = false
                                    currentDurationSecondsLeft = 0
                                    currentRepetitionsLeft = 0
                                    currentSeriesLeft = 0
                                    playEndBeepEvent = true
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

            // --- Rest Mode Countdown ---
            if (isRestMode) { // Check isRestMode again, as it could have been set in the block above
                while (isTimerRunning && isActive) {
                    if (currentRestTimeLeft != null && currentRestTimeLeft!! > 0) {
                        // Check for 5 seconds left to play beeps
                        if (currentRestTimeLeft == endOfRestBeepTime) {
                            playRestBeepEvent = true
                        }
                        delay(1000L)
                        if (!isTimerRunning || !isRestMode) break
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

    val processCloseAppActions = {
        coroutineScope.launch {
            if (saveSelectionChecked) {
                userPreferencesRepository.saveAllPreferences(
                    duration = selectedDurationString,
                    repetitions = numberOfRepetitions,
                    series = numberOfSeries,
                    saveSelectionFlag = true
                )
            } else {
                userPreferencesRepository.clearAllPreferencesIfSaveIsUnchecked()
            }
            onCloseApp()
        }
    }

    Box( // PARENT BOX - This is crucial for Modifier.align(Alignment.BottomStart) on the Image
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(deviceScaling(16).dp)
            )
        {
            Text( // Title
                text = stringResource(id = R.string.series_view_title),
                //text = "Series Training Timer",
                style = MaterialTheme.typography.titleLarge,
                color = AppTitleColor,
                modifier = Modifier
                    .padding(bottom = generalPadding)
                    .align(Alignment.CenterHorizontally)
                    .scale(scaleFactor)
            )

            Row( // Timer Row
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(AppTimerRowBackgroundColor)
                    .padding(vertical = deviceScaling(4).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Cell (Square Timer Display)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(0.6f)
                        .aspectRatio(1f)
                        .padding(deviceScaling(4/*8*/).dp),  //8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val circleRadius = min(constraints.maxWidth, constraints.maxHeight) / 2f * 0.9f
                    val strokeWidthPx =
                        with(LocalDensity.current) { mainTimerStrokeWidth.toPx() } // Ensure mainTimerStrokeWidth is defined

                    val sweepAngle =
                        if (numberOfRepetitions != null && numberOfRepetitions!! > 0 && currentRepetitionsLeft != null) {
                            if (currentRepetitionsLeft!! > 1)
                                ((numberOfRepetitions!! - currentRepetitionsLeft!!) / numberOfRepetitions!!.toFloat()) * 360f
                            else
                                ((numberOfRepetitions!! * initialDurationSeconds!! - currentDurationSecondsLeft!! + 1) / (numberOfRepetitions!! * initialDurationSeconds!!).toFloat()) * 360f
                        } else {
                            0f
                        }

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
                        //if (!isRestMode && sweepAngle > 0f) {
                        if (!isRestMode && (isTimerRunning || isTimerStopped) && sweepAngle > 0f) {
                            val arcDiameter = (circleRadius - strokeWidthPx / 2f) * 2f
                            val arcTopLeftX = canvasCenterX - arcDiameter / 2f
                            val arcTopLeftY = canvasCenterY - arcDiameter / 2f

                            val progressStrokeWidth =
                                (0.72 * mainTimerStrokeWidth.value).dp.toPx() //10.dp.toPx() //

                            drawArc(
                                color = if (isDimmedState) DimmedProgressBorderColor else ProgressBorderColor,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = progressStrokeWidth),
                                topLeft = Offset(arcTopLeftX, arcTopLeftY),
                                size = androidx.compose.ui.geometry.Size(arcDiameter, arcDiameter)
                            )
                        }
                    }

                    // --- Column to hold Countdown numbers and "Rest..." text ---
                    Column(
                        modifier = Modifier.fillMaxSize(), // Allow Column to fill the Box to help with alignment
                        horizontalAlignment = Alignment.CenterHorizontally,
                        //verticalArrangement = Arrangement.Center // To center items if they don't fill the space
                    ) {
                        val topWeight = if (textHorizontalScaleFactor <= 1.5f) 1f
                                        else (1f - 0.35f * (textHorizontalScaleFactor - 1.0f))
                        Spacer(modifier = Modifier.weight(topWeight))  //1f)) // Pushes content downwards

                        // AdaptiveText for the main duration
                        val durationToDisplayValue =
                            if (showDimmedTimers) 0 else if (isRestMode) currentRestTimeLeft else currentDurationSecondsLeft
                        val durationToDisplayString = durationToDisplayValue?.toString() ?:
                        initialDurationSeconds?.toString() ?: selectedDurationString?.split(" ")
                            ?.firstOrNull() ?: ""

                        if (durationToDisplayString.isNotEmpty()) {
                            AdaptiveText(
                                text = durationToDisplayString,
                                modifier = Modifier.padding(
                                    generalPadding,
                                    generalPadding,
                                    generalPadding,
                                    deviceScaling(4).dp
                                ),
                                color = if (isRestMode) WABlueColor else if (isDimmedState) DimmedTimerBorderColor else TimerBorderColor,
                                fontWeight = FontWeight.Bold,
                                targetWidth = Dp(circleRadius * 1.2f),
                                initialFontSize = adaptiveInitialMainFontSize
                            )
                        }

                        // "Rest..." Text, displayed only during rest mode
                        AdaptiveText(
                            text = stringResource(id = if (isRestMode) R.string.rest_indicator else R.string.empty_string),
                            //text = if (isRestMode) "Rest..." else "",
                            modifier = Modifier.padding(top = 0.dp),
                            color = WABlueColor,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            targetWidth = Dp(circleRadius * 1.2f),
                            initialFontSize = adaptiveInitialRestFontSize
                        )

                        Spacer(modifier = Modifier.weight(0.65f)) // Less weight below, so numbers are slightly above true center
                        // to make space for "Rest..." text to appear "below center".
                        // Adjust these weights (e.g., 1f and 1f for true center of the block)
                    }
                }

                // Right Control Cell
                val localPadding = deviceScaling(8).dp // 8.dp
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .padding(start = localPadding, end = localPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button( // Start/Stop Button
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
                                        currentDurationSecondsLeft = initialDurationSeconds
                                    } else {  // CAUTION: is this dead code? Let's check...
                                        // Handle cases where selections might have been cleared or timer never run
                                        if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                                            currentDurationSecondsLeft = initialDurationSeconds
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
                            .fillMaxWidth(0.92f)
                            .scale(horizontalScaleFactor)
                    ) {
                        Text(
                            text = stringResource(id = if (isTimerRunning) R.string.stop_button else R.string.start_button),
                            //text = if (isTimerRunning) "Stop" else "Start",
                            style = customInteractiveTextStyle.copy(
                                color = if (allSelectionsMade && !isRestMode) AppButtonTextColor else AppButtonTextColor.copy(
                                    alpha = 0.5f
                                )
                            )
                        )
                    }

                    BoxWithConstraints( // Series Countdown Circle
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f)
                            .padding(deviceScaling(4).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val seriesCircleRadius =
                            min(constraints.maxWidth, constraints.maxHeight) / 2f * 0.85f
                        val seriesStrokeWidthPx =
                            with(LocalDensity.current) { deviceScaling(7).dp.toPx() }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = if (isDimmedState) DimmedTimerBorderColor else TimerBorderColor,
                                radius = seriesCircleRadius - seriesStrokeWidthPx / 2,
                                style = Stroke(width = seriesStrokeWidthPx),
                                center = Offset(size.width / 2, size.height / 2)
                            )
                        }

                        // Series display will show 0 when dimmed
                        val seriesToDisplayValue = currentSeriesLeft
                        val seriesToDisplayString =
                            seriesToDisplayValue?.toString() ?: currentSeriesLeft?.toString() ?: ""

                        if (seriesToDisplayString.isNotEmpty()) {
                            AdaptiveText(
                                text = seriesToDisplayString,
                                modifier = Modifier.padding(localPadding),
                                color = if (isDimmedState) DimmedTimerBorderColor else TimerBorderColor,
                                fontWeight = FontWeight.Normal,
                                targetWidth = Dp(seriesCircleRadius * 1.1f),
                                initialFontSize = adaptiveInitialSeriesFontSize
                            )
                        }
                    }
                }
            }

            // --- Settings Sections (Repetitions duration, Number of repetitions, etc.) ---

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = deviceScaling(16).dp, bottom = deviceScaling(4).dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text( // Select Session parameters Row
                    text = stringResource(id = R.string.please_select),
                    style = smallerTextStyle,
                    fontStyle = FontStyle.Italic,
                    color = AppTitleColor.copy(alpha = if (allSelectionsMade) 0f else 1f)

                )
            }

            Text( // Repetitions duration title
                text = stringResource(id = R.string.repetitions_duration_label),
                style = customInteractiveTextStyle,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(top = deviceScaling(8).dp)
                    .align(Alignment.CenterHorizontally)
            )
            Row( // Duration Buttons Row
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val borderStrokeWidth = deviceScaling(5).dp
                Row(horizontalArrangement = Arrangement.spacedBy(deviceScaling(0).dp)) {
                    durationOptions.forEach { duration ->
                        val isSelected = selectedDurationString == duration
                        Button(
                            onClick = {
                                selectedDurationString = if (isSelected) null else duration
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) SelectedButtonBackgroundColor
                                else if (isTimerRunning || isTimerStopped) AppButtonColor.copy(alpha = 0.8f)
                                else AppButtonDarkerColor,
                                contentColor = AppButtonTextColor
                            ),
                            border = if (isSelected) BorderStroke(
                                borderStrokeWidth,
                                SelectedButtonBorderColor
                            ) else BorderStroke(  //null
                                borderStrokeWidth,
                                AppBackgroundColor
                            ),
                            enabled = isSelected || !(isTimerRunning || isTimerStopped),
                            modifier = Modifier
                                .width(durationButtonWidth)
                            //.scale(horizontalScaleFactor)
                            /*.padding(
                                    horizontal = if (isSelected) borderStrokeWidth else 0.dp,
                                    vertical = 0.dp  //if (isSelected) 0.dp else borderStrokeWidth
                                )*/
                        ) {
                            Text(
                                text = duration,
                                style = TextStyle(
                                    fontSize = (13f * durationsScaling).toInt().sp,
                                    color = if (isSelected) AppButtonTextColor else if (isTimerRunning || isTimerStopped) AppDimmedTextColor else AppTextColor
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(majorSpacerHeight))

            Text( // Number of repetitions title
                text = stringResource(id = R.string.repetitions_number_label),
                //text = "Number of repetitions per series",
                style = customInteractiveTextStyle,
                color = AppTextColor,
                modifier = Modifier
                    .padding(top = generalPadding)
                    .align(Alignment.CenterHorizontally)
            )
            LazyRow( // Repetitions LazyRow
                state = repetitionsLazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = deviceScaling(12).dp, bottom = deviceScaling(8).dp),
                horizontalArrangement = Arrangement.spacedBy(deviceScaling(10).dp),
                contentPadding = PaddingValues(horizontal = generalPadding)
            ) {
                items(repetitionRange) { number ->
                    val isNumberSelected = number == numberOfRepetitions
                    val isClickable = !(isTimerRunning || isTimerStopped)
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
                                    numberOfRepetitions = if (isNumberSelected) null else number
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$number",
                            style = customInteractiveTextStyle.copy(
                                color = if (isNumberSelected) AppButtonTextColor
                                else if (isTimerRunning || isTimerStopped) AppDimmedTextColor
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
                //text = "Number of series",
                style = customInteractiveTextStyle,
                color = AppTextColor,
                modifier = Modifier
                    .padding(top = generalPadding)
                    .align(Alignment.CenterHorizontally)
            )
            Row( // Series Selector Row
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = deviceScaling(12).dp, bottom = deviceScaling(8).dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(deviceScaling(10).dp)) {
                    seriesOptions.forEach { seriesCount ->
                        val isSeriesSelected = seriesCount == numberOfSeries
                        val isClickable = !(isTimerRunning || isTimerStopped)
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
                                        numberOfSeries = if (isSeriesSelected) null else seriesCount
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$seriesCount",
                                style = customInteractiveTextStyle.copy(
                                    color = if (isSeriesSelected) AppButtonTextColor
                                    else if (isTimerRunning || isTimerStopped) AppDimmedTextColor
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
                    .padding(top = deviceScaling(4).dp, bottom = 0.dp)
                    .toggleable(
                        value = saveSelectionChecked,
                        role = Role.Checkbox,
                        enabled = allSelectionsMade,
                        onValueChange = { saveSelectionChecked = !saveSelectionChecked }
                    )
                    .align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveSelectionChecked,
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
                    text = stringResource(id = R.string.save_selection_label),
                    //text = "Save current selection",
                    style = smallerTextStyle,
                    color = AppTextColor.copy(alpha = if (allSelectionsMade) 1f else 0.38f)
                )
            }

            //Spacer(modifier = Modifier.height(majorSpacerHeight))

            Button( // Quit Button
                onClick = { processCloseAppActions() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppButtonColor,
                    contentColor = AppButtonTextColor
                ),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = deviceScaling(8).dp)
                    .scale(horizontalScaleFactor)
            ) {
                Text(
                    stringResource(id = R.string.close_button),
                    style = customInteractiveTextStyle
                )
            }
        }

        // --- Add Your Logo Here, Aligned to Bottom-Left ---
        Image(
            painter = painterResource(id = R.drawable.ps_logo),
            contentDescription = null, // Important for accessibility - provide a meaningful description (e.g. "Editor Logo")or null if purely decorative
            modifier = Modifier
                .align(Alignment.BottomStart) // Aligns this Image to the bottom start (left) of the parent Box
                .padding(
                    start = 16.dp,
                    bottom = 16.dp
                ) // Add some padding from the screen edges
                .size(34.dp) // Set the size of the image on screen
        )

    }
}