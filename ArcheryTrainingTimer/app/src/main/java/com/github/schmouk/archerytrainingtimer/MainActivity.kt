package com.github.schmouk.archerytrainingtimer

import android.os.Bundle
import android.content.Intent

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

import com.github.schmouk.archerytrainingtimer.noarrowsession.NoArrowsTrainingTimerActivity
import com.github.schmouk.archerytrainingtimer.ui.theme.*

// --- Global DEBUG MODE flag ---
//val DEBUG_MODE = true
val DEBUG_MODE = false  // i.e. RELEASE MODE


// --- MainActivity class definition ---
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disable edge-to-edge display to NOT draw behind the system bars
        WindowCompat.setDecorFitsSystemWindows(window, true)

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
fun DefaultPreviewMainActivity() {
    ArcheryTrainingTimerTheme {
        MainAppScreen()
    }
}


/*
// The AppTheme Composable
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        // our theme colors, typography, etc.
    ) {
        content()
    }
}
*/
