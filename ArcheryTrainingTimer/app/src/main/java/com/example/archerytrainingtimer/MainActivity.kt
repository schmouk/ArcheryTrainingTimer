package com.example.archerytrainingtimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.min

// import android.util.Log
// MainActivity class definition (no change from before)
class MainActivity : ComponentActivity() {
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferencesRepository = UserPreferencesRepository(applicationContext)
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
}

// AdaptiveText composable (no change from before)
@Composable
fun AdaptiveText(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    fontWeight: FontWeight? = null,
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


@Composable
fun SimpleScreen(
    userPreferencesRepository: UserPreferencesRepository,
    onCloseApp: () -> Unit
) {
    var selectedDurationString by rememberSaveable { mutableStateOf<String?>(null) }
    var numberOfRepetitions by remember { mutableStateOf<Int?>(null) }
    var numberOfSeries by remember { mutableStateOf<Int?>(null) }
    var saveSelectionChecked by remember { mutableStateOf(false) }
    var isTimerRunning by remember { mutableStateOf(false) }

    var initialDurationSeconds by rememberSaveable { mutableStateOf<Int?>(null) }
    var currentDurationSecondsLeft by rememberSaveable { mutableStateOf<Int?>(null) }
    var currentRepetitionsLeft by rememberSaveable { mutableStateOf<Int?>(null) }

    val durationOptions = listOf("5 s", "10 s", "15 s", "20 s", "30 s")
    val minRepetitions = 2
    val maxRepetitions = 15
    val repetitionRange = (minRepetitions..maxRepetitions).toList()
    val seriesOptions = listOf(2, 10, 15, 20, 25, 30)

    val customInteractiveTextStyle = TextStyle(fontSize = 18.sp)
    val smallerTextStyle = TextStyle(fontSize = 14.sp)
    val repetitionsLazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val activeTimerColor = AppTitleColor // Your active color
    val dimmedTimerColor = Color(0xFF999999) // Color for "dimmed" countdown display

    // This flag will determine if the timers should be dimmed
    // It's true when a full cycle of repetitions is complete and timer is stopped
    val showDimmedTimers = rememberSaveable(currentRepetitionsLeft, isTimerRunning) {
        currentRepetitionsLeft == 0 && !isTimerRunning
    }

    LaunchedEffect(key1 = Unit) {
        userPreferencesRepository.userPreferencesFlow.collect { loadedPrefs ->
            selectedDurationString = loadedPrefs.selectedDuration
            numberOfRepetitions = loadedPrefs.numberOfRepetitions
            numberOfSeries = loadedPrefs.numberOfSeries
            saveSelectionChecked = loadedPrefs.saveSelection

            if (!isTimerRunning) {
                val durationValue = loadedPrefs.selectedDuration?.split(" ")?.firstOrNull()?.toIntOrNull()
                initialDurationSeconds = durationValue
                if (currentRepetitionsLeft != 0) { // Only reset if not in a "completed and dimmed" state
                    currentDurationSecondsLeft = durationValue
                }
                currentRepetitionsLeft = loadedPrefs.numberOfRepetitions
            }
        }
    }

    // Determine if all selections are made
    val isSaveEnabled = selectedDurationString != null &&
            numberOfRepetitions != null &&
            numberOfSeries != null

    val allSelectionsMade = selectedDurationString != null &&
            numberOfRepetitions != null &&
            numberOfSeries != null

    // Update initial/current countdown values when selections change AND timer is NOT running
    LaunchedEffect(selectedDurationString, numberOfRepetitions, numberOfSeries, isTimerRunning) {
        if (!isTimerRunning) {
            val durationValue = selectedDurationString?.split(" ")?.firstOrNull()?.toIntOrNull()
            initialDurationSeconds = durationValue
            // Only update currentDurationSecondsLeft if not in the "dimmed" state from a previous cycle
            if (currentRepetitionsLeft != 0 || currentDurationSecondsLeft != 0 ) {
                currentDurationSecondsLeft = durationValue
            }
            // Always update currentRepetitionsLeft from selection if timer is not running
            currentRepetitionsLeft = numberOfRepetitions
        }
    }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            // Ensure values are sane before starting countdown loop
            // If starting from a dimmed state (reps=0, duration=0), reset them.
            if (currentRepetitionsLeft == 0) { // Indicates a previous cycle was completed
                currentRepetitionsLeft = numberOfRepetitions // Reset for new cycle
                currentDurationSecondsLeft = initialDurationSeconds // Reset for new cycle
            } else { // Normal start or resume
                if (currentDurationSecondsLeft == null || currentDurationSecondsLeft == 0) {
                    currentDurationSecondsLeft = initialDurationSeconds
                }
                if (currentRepetitionsLeft == null) {
                    currentRepetitionsLeft = numberOfRepetitions
                }
            }

            while (isTimerRunning && isActive) {
                if (currentDurationSecondsLeft != null && currentDurationSecondsLeft!! > 0) {
                    delay(1000L)
                    if (!isTimerRunning) break
                    currentDurationSecondsLeft = currentDurationSecondsLeft!! - 1
                } else if (currentDurationSecondsLeft == 0) {
                    if (currentRepetitionsLeft != null && currentRepetitionsLeft!! > 0) {
                        currentRepetitionsLeft = currentRepetitionsLeft!! - 1
                        if (currentRepetitionsLeft == 0) {
                            isTimerRunning = false // Stop timer, will trigger dimmed state via showDimmedTimers
                            // currentDurationSecondsLeft is already 0, currentRepetitionsLeft is now 0.
                            break
                        } else {
                            currentDurationSecondsLeft = initialDurationSeconds
                        }
                    } else {
                        isTimerRunning = false
                        break
                    }
                } else {
                    isTimerRunning = false
                    break
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text( // Title
            text = "Series Training Timer",
            style = MaterialTheme.typography.titleLarge,
            color = AppTitleColor,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        Row( // Timer Row
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTimerRowBackgroundColor)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Cell (Square Timer Display)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(0.6f)
                    .aspectRatio(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val circleRadius = min(constraints.maxWidth, constraints.maxHeight) / 2f * 0.9f
                val strokeWidthPx = with(LocalDensity.current) { 14.dp.toPx() }

                val mainTimerDisplayColor = if (showDimmedTimers) dimmedTimerColor else activeTimerColor

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = mainTimerDisplayColor, // Use conditional color
                        radius = circleRadius - strokeWidthPx / 2,
                        style = Stroke(width = strokeWidthPx),
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }

                // When dimmed, main duration should also show 0
                val durationToDisplayValue = if (showDimmedTimers) 0 else currentDurationSecondsLeft
                val durationToDisplayString = durationToDisplayValue?.toString() ?:
                initialDurationSeconds?.toString() ?:
                selectedDurationString?.split(" ")?.firstOrNull() ?: ""


                if (durationToDisplayString.isNotEmpty()) {
                    AdaptiveText(
                        text = durationToDisplayString,
                        modifier = Modifier.padding(16.dp),
                        color = mainTimerDisplayColor, // Use conditional color
                        fontWeight = FontWeight.Bold,
                        targetWidth = Dp(circleRadius * 1.2f),
                        initialFontSize = 76.sp
                    )
                }
            }

            // Right Control Cell
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .padding(start = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Button( // Start/Stop Button
                    onClick = {
                        if (isTimerRunning) {
                            isTimerRunning = false // Just pause
                        } else { // Trying to Start
                            if (allSelectionsMade) {
                                // If currentRepetitionsLeft is 0, it means a cycle just finished (dimmed state).
                                // Reset both countdowns for a new cycle.
                                if (currentRepetitionsLeft == 0) {
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
                                isTimerRunning = true // Start or resume
                            }
                        }
                    },
                    enabled = isTimerRunning || allSelectionsMade,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppButtonColor,
                        contentColor = AppButtonTextColor,
                        disabledContainerColor = AppButtonColor.copy(alpha = 0.5f),
                        disabledContentColor = AppButtonTextColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(
                        text = if (isTimerRunning) "Stop" else "Start",
                        style = customInteractiveTextStyle.copy(color = if (isTimerRunning || allSelectionsMade) AppButtonTextColor else AppButtonTextColor.copy(alpha = 0.5f))
                    )
                }
                BoxWithConstraints( // Series Countdown Circle
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val seriesCircleRadius = min(constraints.maxWidth, constraints.maxHeight) / 2f * 0.85f
                    val seriesStrokeWidthPx = with(LocalDensity.current) { 7.dp.toPx() }

                    val seriesDisplayColor = if (showDimmedTimers) dimmedTimerColor else activeTimerColor

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = seriesDisplayColor, // Use conditional color
                            radius = seriesCircleRadius - seriesStrokeWidthPx / 2,
                            style = Stroke(width = seriesStrokeWidthPx),
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }

                    // Series display will show 0 when dimmed
                    val seriesToDisplayValue = currentRepetitionsLeft
                    val seriesToDisplayString = seriesToDisplayValue?.toString() ?:
                    numberOfRepetitions?.toString() ?: ""

                    if (seriesToDisplayString.isNotEmpty()) {
                        AdaptiveText(
                            text = seriesToDisplayString,
                            modifier = Modifier.padding(8.dp),
                            color = seriesDisplayColor, // Use conditional color
                            fontWeight = FontWeight.Normal,
                            targetWidth = Dp(seriesCircleRadius * 1.1f),
                            initialFontSize = 34.sp
                        )
                    }
                }
            }
        }

        // --- Settings Sections (Repetitions duration, Number of repetitions, etc.) ---
        // ... (No changes to the settings sections themselves) ...

        Text( // Repetitions duration title
            text = "Repetitions duration",
            style = customInteractiveTextStyle,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(top = 24.dp, bottom = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
        Row( // Duration Buttons Row
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                durationOptions.forEach { duration ->
                    val isSelected = selectedDurationString == duration
                    Button(
                        onClick = { selectedDurationString = if (isSelected) null else duration },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) SelectedButtonBackgroundColor else AppButtonColor,
                            contentColor = AppButtonTextColor
                        ),
                        border = if (isSelected) BorderStroke(
                            5.dp,
                            SelectedButtonBorderColor
                        ) else null,
                        modifier = Modifier.defaultMinSize(minWidth = 72.dp)
                    ) { Text(text = duration, style = customInteractiveTextStyle) }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text( // Number of repetitions title
            text = "Number of repetitions per series",
            style = customInteractiveTextStyle,
            color = AppTextColor,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
        LazyRow( // Repetitions LazyRow
            state = repetitionsLazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(repetitionRange) { number ->
                val isNumberSelected = number == numberOfRepetitions
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .then(
                            if (isNumberSelected) Modifier.border(
                                BorderStroke(
                                    4.dp,
                                    SelectedButtonBorderColor
                                ), shape = CircleShape
                            ) else Modifier
                        )
                        .padding(if (isNumberSelected) 4.dp else 0.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isNumberSelected) AppTitleColor else AppButtonColor.copy(
                                alpha = 0.3f
                            )
                        )
                        .clickable { numberOfRepetitions = if (isNumberSelected) null else number },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$number",
                        style = customInteractiveTextStyle.copy(color = if (isNumberSelected) AppButtonTextColor else AppTextColor),
                        fontWeight = if (isNumberSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text( // Number of series title
            text = "Number of series",
            style = customInteractiveTextStyle,
            color = AppTextColor,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
        Row( // Series Selector Row
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                seriesOptions.forEach { seriesCount ->
                    val isSeriesSelected = seriesCount == numberOfSeries
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .then(
                                if (isSeriesSelected) Modifier.border(
                                    BorderStroke(
                                        4.dp,
                                        SelectedButtonBorderColor
                                    ), shape = CircleShape
                                ) else Modifier
                            )
                            .padding(if (isSeriesSelected) 4.dp else 0.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isSeriesSelected) AppTitleColor else AppButtonColor.copy(
                                    alpha = 0.3f
                                )
                            )
                            .clickable {
                                numberOfSeries = if (isSeriesSelected) null else seriesCount
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$seriesCount",
                            style = customInteractiveTextStyle.copy(color = if (isSeriesSelected) AppButtonTextColor else AppTextColor),
                            fontWeight = if (isSeriesSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row( // Checkbox Row
            modifier = Modifier
                .padding(top = 4.dp, bottom = 16.dp)
                .toggleable(
                    value = saveSelectionChecked,
                    role = Role.Checkbox,
                    enabled = isSaveEnabled,
                    onValueChange = { saveSelectionChecked = !saveSelectionChecked }
                )
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = saveSelectionChecked,
                onCheckedChange = null,
                enabled = isSaveEnabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = AppTitleColor,
                    uncheckedColor = AppTextColor.copy(alpha = if (isSaveEnabled) 1f else 0.5f),
                    checkmarkColor = AppButtonTextColor,
                    disabledCheckedColor = AppTitleColor.copy(alpha = 0.5f),
                    disabledUncheckedColor = AppTextColor.copy(alpha = 0.38f)
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Save current selection",
                style = smallerTextStyle,
                color = AppTextColor.copy(alpha = if (isSaveEnabled) 1f else 0.38f)
            )
        }

        Spacer(Modifier.weight(0.05f))

        Button( // Close Button
            onClick = { processCloseAppActions() },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppButtonColor,
                contentColor = AppButtonTextColor
            ),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text("Close", style = customInteractiveTextStyle)
        }
    }
}