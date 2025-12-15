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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.github.schmouk.archerytrainingtimer.commons.SessionType
import com.github.schmouk.archerytrainingtimer.commons.UserPreferencesRepository
import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowsTrainingTimerActivity
import com.github.schmouk.archerytrainingtimer.sessionchoice.SessionChoiceViewModel
import com.github.schmouk.archerytrainingtimer.ui.commons.ViewHeader
import com.github.schmouk.archerytrainingtimer.ui.theme.*




// --- MainActivity class definition ---
class MainActivity : ComponentActivity() {

    // the View Model associated with this Main Activity
    private val mainActivityViewModel: SessionChoiceViewModel by viewModels {
        SessionChoiceViewModelFactory(UserPreferencesRepository(applicationContext))
    }

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
                // TODO: Add intents for other activities once they are created
                SessionType.NO_ARROWS_PYRAMIDAL -> null // Intent(context, NoArrowsPyramidalActivity::class.java)
                SessionType.ARROWS_UNIFORM -> null // Intent(context, ArrowsTrainingTimerActivity::class.java)
                SessionType.ARROWS_PYRAMIDAL -> null // Intent(context, ArrowsPyramidalActivity::class.java)
                else -> null
            }
            intent?.let { context.startActivity(it) }
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBackgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ViewHeader(viewTitleText = "Archery Training Timer")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // First Row: No-Arrows Sessions
                SessionRow(
                    mainImageRes = R.drawable.no_arrows_session_400,
                    onOption1Click = { viewModel.selectSessionType(SessionType.NO_ARROWS_UNIFORM) },
                    onOption2Click = { viewModel.selectSessionType(SessionType.NO_ARROWS_PYRAMIDAL) },
                    selected = selectedSession,
                    option1Type = SessionType.NO_ARROWS_UNIFORM,
                    option2Type = SessionType.NO_ARROWS_PYRAMIDAL
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Second Row: Arrows Sessions
                SessionRow(
                    mainImageRes = R.drawable.arrows_session_400,
                    onOption1Click = { viewModel.selectSessionType(SessionType.ARROWS_UNIFORM) },
                    onOption2Click = { viewModel.selectSessionType(SessionType.ARROWS_PYRAMIDAL) },
                    selected = selectedSession,
                    option1Type = SessionType.ARROWS_UNIFORM,
                    option2Type = SessionType.ARROWS_PYRAMIDAL
                )
            }
        }
    }


    /* // Only one choice actually available
    context.startActivity(
        Intent(context, NoArrowsTrainingTimerActivity::class.java)
    )
    */

    /* as soon as many choices will be available:
    Scaffold(
    // We can have a top bar for MainActivity if needed
    // topBar = { TopAppBar(title = { Text("Archery Training Timer") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp), // General padding for the content
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Archery Training Timer",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                context.startActivity(Intent(context, NoArrowsTrainingTimerActivity::class.java))
            }) {
                Text("Start No-Arrows Timer")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add buttons for other activities here when you create them
            // Button(onClick = { /* Launch AnotherActivity */ }) {
            // Text("Other Feature")
            // }
        }
    }
    */
}


// --- Choice Button Composable for the selection of the type of training session ---
@Composable
fun SessionRow(
    mainImageRes: Int,
    onOption1Click: () -> Unit,
    onOption2Click: () -> Unit,
    selected: SessionType?,
    option1Type: SessionType,
    option2Type: SessionType
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Main Image
        Image(
            painter = painterResource(id = mainImageRes),
            contentDescription = null, // Descriptions should be more specific if needed
            modifier = Modifier
                .weight(1f)
                .heightIn(max = 256.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Right Side: Two choice buttons
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChoiceButton(
                imageRes = R.drawable.uniform_series,
                isSelected = selected == option1Type,
                onClick = onOption1Click
            )
            Spacer(modifier = Modifier.height(16.dp))
            ChoiceButton(
                imageRes = R.drawable.pyramidal_series,
                isSelected = selected == option2Type,
                onClick = onOption2Click
            )
        }
    }
}


// --- Individual Choice Button Composable ---
@Composable
fun ChoiceButton(imageRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    else ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)

    Button(
        onClick = onClick,
        elevation = elevation,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.7f) // Adjust aspect ratio to make buttons look good
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null // Decorative
        )
    }
}


/*
@Preview(showBackground = true)
@Composable
fun DefaultPreviewMainActivity() {  // Notice: currently unused
    ArcheryTrainingTimerTheme {
        MainAppScreen()
    }
}
*/
