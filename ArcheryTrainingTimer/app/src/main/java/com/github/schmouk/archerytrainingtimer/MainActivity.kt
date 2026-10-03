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

package com.github.schmouk.archerytrainingtimer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import kotlinx.coroutines.launch

import com.github.schmouk.archerytrainingtimer.commons.NotImplementedSessionActivity
import com.github.schmouk.archerytrainingtimer.commons.SessionType
import com.github.schmouk.archerytrainingtimer.commons.UserPreferencesRepository
import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowsTrainingTimerActivity
import com.github.schmouk.archerytrainingtimer.sessions.SessionChoiceViewModel
import com.github.schmouk.archerytrainingtimer.ui.theme.*
import com.github.schmouk.archerytrainingtimer.ui.utils.EFoldedPosture
import com.github.schmouk.archerytrainingtimer.ui.utils.considerDevicePortraitPositioned
import com.github.schmouk.archerytrainingtimer.ui.utils.detectDeviceFoldedPosture


// --- MainActivity class definition ---
class MainActivity : ComponentActivity() {

    // The user preference repository associated with the app
    private val userPreferencesRepository by lazy {
        UserPreferencesRepository(applicationContext)
    }

    // the View Model associated with this Main Activity
    private val mainActivityViewModel: SessionChoiceViewModel by viewModels {
        SessionChoiceViewModelFactory(userPreferencesRepository)
    }

    // onCreate() is called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disables edge-to-edge display to NOT draw behind the system bars
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Sets the content of the main screen for the app
        setContent {
            ArcheryTrainingTimerTheme {
                MainAppScreen(viewModel = mainActivityViewModel)
            }
        }
    }

    // onDestroy() is called when the activity is about to be destroyed.
    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            userPreferencesRepository.saveSessionType(null)
        }
    }

}


// --- ViewModel Factory to provide dependencies ---
class SessionChoiceViewModelFactory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionChoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionChoiceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// --- Composable function for the main app screen ---
@Composable
fun MainAppScreen(viewModel: SessionChoiceViewModel) {
    val context = LocalContext.current
    val selectedSession by viewModel.selectedSessionType.collectAsState()

    // When a session is selected, launch the corresponding activity
    LaunchedEffect(selectedSession) {
        // This check prevents launching an activity on initial composition if nothing is selected
        if (selectedSession != null) {
            val intent = when (selectedSession) {
                SessionType.NO_ARROWS_UNIFORM -> Intent(context, NoArrowsTrainingTimerActivity::class.java)
                SessionType.NO_ARROWS_PYRAMIDAL -> Intent(context, NotImplementedSessionActivity::class.java)
                SessionType.ARROWS_UNIFORM -> Intent(context, NotImplementedSessionActivity::class.java)
                SessionType.ARROWS_PYRAMIDAL -> Intent(context, NotImplementedSessionActivity::class.java)
                else -> null
            }
            intent?.let {
                context.startActivity(it)
                viewModel.clearSelectedSessionType()
            }
        }
    }

    // --- The Sessions Row Composable ---
    @Composable
    fun TypedSessionRow(
        scale: Float,
        sessionImageRes: Int,
        uniformSessionType: Int,
        pyramidalSessionType: Int,
        modifier: Modifier
    ) {
        SessionRow(
            mainImageRes = sessionImageRes,
            onOption1Click = { viewModel.selectSessionType(uniformSessionType) },
            onOption2Click = { viewModel.selectSessionType(pyramidalSessionType) },
            selected = selectedSession,
            option1Type = uniformSessionType,
            option2Type = pyramidalSessionType,
            scale = scale,
            modifier = modifier
        )

    }

    // --- A Main Column for the entire screen content (portrait layout) ---
    @Composable
    fun OneColumn(scale: Float = 1f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TypedSessionRow(scale,
                R.drawable.no_arrows_session_400,
                SessionType.NO_ARROWS_UNIFORM,
                SessionType.NO_ARROWS_PYRAMIDAL,
                Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(minOf(24.dp, (12f * scale).dp)))

            TypedSessionRow(scale,
                R.drawable.arrows_session_400,
                SessionType.ARROWS_UNIFORM,
                SessionType.ARROWS_PYRAMIDAL,
                Modifier.weight(1f)
            )
        }
    }

    // --- Two Columns for the entire screen content (landscape or book layout) ---
    @Composable
    fun TwoColumns(equallySized: Boolean = false, scale: Float = 1f) {
        val leftWeight = if (equallySized) 1f else 1f
        val rightWeight = if (equallySized) 1f else 1f

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy((12f * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(leftWeight)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TypedSessionRow(scale,
                    R.drawable.no_arrows_session_400,
                    SessionType.NO_ARROWS_UNIFORM,
                    SessionType.NO_ARROWS_PYRAMIDAL,
                    Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .weight(rightWeight)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TypedSessionRow(scale,
                    R.drawable.arrows_session_400,
                    SessionType.ARROWS_UNIFORM,
                    SessionType.ARROWS_PYRAMIDAL,
                    Modifier.fillMaxWidth()
                )
            }
        }
    }

    // --- Two rows for the entire screen content (laptop layout) ---
    @Composable
    fun TwoRows(scale: Float = 1f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TypedSessionRow(scale,
                R.drawable.no_arrows_session_400,
                SessionType.NO_ARROWS_UNIFORM,
                SessionType.NO_ARROWS_PYRAMIDAL,
                Modifier.fillMaxWidth().weight(1f)
            )
            /*SessionRow(
                mainImageRes = R.drawable.no_arrows_session_400,
                onOption1Click = { viewModel.selectSessionType(SessionType.NO_ARROWS_UNIFORM) },
                onOption2Click = { viewModel.selectSessionType(SessionType.NO_ARROWS_PYRAMIDAL) },
                selected = selectedSession,
                option1Type = SessionType.NO_ARROWS_UNIFORM,
                option2Type = SessionType.NO_ARROWS_PYRAMIDAL,
                scale = scale,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )*/

            Spacer(modifier = Modifier.height((18f * scale).dp))

            TypedSessionRow(scale,
                R.drawable.arrows_session_400,
                SessionType.ARROWS_UNIFORM,
                SessionType.ARROWS_PYRAMIDAL,
                Modifier.fillMaxWidth().weight(1f)
            )
            /*SessionRow(
                mainImageRes = R.drawable.arrows_session_400,
                onOption1Click = { viewModel.selectSessionType(SessionType.ARROWS_UNIFORM) },
                onOption2Click = { viewModel.selectSessionType(SessionType.ARROWS_PYRAMIDAL) },
                selected = selectedSession,
                option1Type = SessionType.ARROWS_UNIFORM,
                option2Type = SessionType.ARROWS_PYRAMIDAL,
                scale = scale,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )*/
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(title = {
                Text(
                    text = stringResource(id = R.string.app_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = AppTitleColor
                )
            })
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            color = AppBackgroundColor
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val horizontalScale = if (maxWidth > 0.dp) (maxWidth.value / 720f) else 1f
                val verticalScale = if (maxHeight > 0.dp) (maxHeight.value / 1280f) else 1f
                val adaptiveScale = maxOf(horizontalScale, verticalScale, 0.8f).coerceAtMost(1.15f)

                when (detectDeviceFoldedPosture()) {
                    EFoldedPosture.POSTURE_NOT_FOLDED -> {
                        if (considerDevicePortraitPositioned()) OneColumn(adaptiveScale) else TwoColumns(scale = adaptiveScale)
                    }
                    EFoldedPosture.POSTURE_FLAT -> {
                        if (considerDevicePortraitPositioned()) OneColumn(adaptiveScale) else TwoColumns(true, adaptiveScale)
                    }
                    EFoldedPosture.POSTURE_BOOK_LIKE -> {
                        TwoColumns(true, adaptiveScale)
                    }
                    EFoldedPosture.POSTURE_LAPTOP_LIKE -> {
                        TwoRows(adaptiveScale)
                    }
                    else -> {
                        if (considerDevicePortraitPositioned()) OneColumn(adaptiveScale) else TwoColumns(true, adaptiveScale)
                    }
                }
            }
        }
    }
}


// --- Choice Button Composable for the selection of the type of training session ---
@Composable
fun SessionRow(
    mainImageRes: Int,
    onOption1Click: () -> Unit,
    onOption2Click: () -> Unit,
    selected: Int?,
    option1Type: Int,
    option2Type: Int,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val spacing = (12f * scale).dp
        val imageHeight = if (maxHeight > 0.dp) maxHeight * 0.72f else (130f * scale).dp
        val buttonHeight = (imageHeight - spacing) / 2f

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = mainImageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f)
                    .aspectRatio(1f)
                    .padding(end = spacing)
            )

            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(start = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                ChoiceButton(
                    imageRes = R.drawable.uniform_series,
                    isSelected = selected == option1Type,
                    onClick = onOption1Click,
                    scale = scale,
                    modifier = Modifier.fillMaxWidth().height(buttonHeight)
                )
                ChoiceButton(
                    imageRes = R.drawable.pyramidal_series,
                    isSelected = selected == option2Type,
                    onClick = onOption2Click,
                    scale = scale,
                    modifier = Modifier.fillMaxWidth().height(buttonHeight)
                )
            }
        }
    }
}


// --- Individual Choice Button Composable ---
@Composable
fun ChoiceButton(
    imageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val elevation = if (isSelected)
        ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
    else
        ButtonDefaults.buttonElevation(defaultElevation = 12.dp, pressedElevation = 8.dp)

    Button(
        onClick = onClick,
        elevation = elevation,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        )
    }
}
