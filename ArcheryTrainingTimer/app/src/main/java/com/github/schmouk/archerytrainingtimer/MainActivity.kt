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

import android.os.Bundle
import android.content.Intent

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowsTrainingTimerActivity
import com.github.schmouk.archerytrainingtimer.ui.theme.*


// --- MainActivity class definition ---
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disables edge-to-edge display to NOT draw behind the system bars
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Sets the content of the main screen for the app
        setContent {
            ArcheryTrainingTimerTheme {
                MainAppScreen()
            }
        }
    }
}


@Composable
fun MainAppScreen() {
    val context = LocalContext.current

    // Only one choice actually available
    context.startActivity(
        Intent(context, NoArrowsTrainingTimerActivity::class.java)
    )

    /* as soon as many choices will be available:
    Scaffold(
    // We can have a top bar for MainActivity if needed
    // topBar = { TopAppBar(title = { Text("Archery Timer App") }) }
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


@Preview(showBackground = true)
@Composable
fun DefaultPreviewMainActivity() {  // Notice: currently unused
    ArcheryTrainingTimerTheme {
        MainAppScreen()
    }
}
