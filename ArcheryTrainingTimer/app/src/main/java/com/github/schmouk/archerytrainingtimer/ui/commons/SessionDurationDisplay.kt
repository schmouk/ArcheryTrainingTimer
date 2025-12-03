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

package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

import com.github.schmouk.archerytrainingtimer.SECOND_DURATION_MS

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


class DurationSessionController {
    private var isSessionActive = mutableStateOf(false)
    //private var sessionStartTime = 0L
    //private var accumulatedDuration = 0L
    private var currentDurationMn: Int = 0
    private var currentDurationHr: Int = 0

    fun beginSession() {
        //sessionStartTime = System.currentTimeMillis()
        //accumulatedDuration = 0L
        currentDurationMn = 0
        currentDurationHr = 0
        isSessionActive.value = true
    }

    fun endSession() {
        if (isSessionActive.value) {
            //accumulatedDuration = System.currentTimeMillis() - sessionStartTime
            isSessionActive.value = false
        }
    }

    fun isActive(): Boolean = isSessionActive.value

    fun updateDuration() {
        if (isSessionActive.value) {
            currentDurationMn++
            if (currentDurationMn >= 60) {
                currentDurationMn = 0
                currentDurationHr++
            }
        }
    }

    fun getCurrentDuration(): String {
        /*return if (isSessionActive.value) {
            System.currentTimeMillis() - sessionStartTime
        } else {
            accumulatedDuration
        }*/
        return String.format("%02d:%02d", currentDurationHr, currentDurationMn)
    }
}

@Composable
fun SessionDurationDisplay(
    controller: DurationSessionController,
    fontSize: Float = 24f,
    modifier: Modifier = Modifier
) {
    var displayTime by remember { mutableStateOf("00:00") }

    LaunchedEffect(controller.isActive()) {
        while (coroutineContext.isActive) {
            if (controller.isActive()) {
                //displayTime = formatDuration(controller.getCurrentDuration())
                displayTime = controller.getCurrentDuration()
                delay(60L * SECOND_DURATION_MS - 1L) // i.e. 1 minute
                controller.updateDuration()
            } else {
                // Update one last time when session ends
                //displayTime = formatDuration(controller.getCurrentDuration())
                displayTime = controller.getCurrentDuration()
                break
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = displayTime,
            fontSize = fontSize.sp
        )
    }
}

/*private fun formatDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 1000 / 60).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return String.format("%02d:%02d", hours, minutes)
}*/

// Example usage:
// val sessionController = remember { TrainingSessionController() }
// TrainingDurationDisplay(controller = sessionController, fontSize = 32.sp)
//
// Button(onClick = { sessionController.beginSession() }) { Text("Start") }
// Button(onClick = { sessionController.endSession() }) { Text("Stop") }