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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor


@Composable
fun ClockDisplay(fontSize: Float,
                 modifier: Modifier = Modifier
) {
    var currentTime : String by remember { mutableStateOf(getCurrentTime()) }

    LaunchedEffect(Unit) {
        updateTimeOnMinuteChange { newTime : String ->
            currentTime = newTime
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 6.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = currentTime,
            modifier = Modifier.background(Color.Transparent),
            color = AppTextColor,
            fontSize = fontSize.sp
        )
    }
}

private fun getCurrentTime(): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date())
}

private suspend fun updateTimeOnMinuteChange(onTimeUpdate: (String) -> Unit) {
    // Update immediately on first call
    onTimeUpdate(getCurrentTime())

    while (true) {
        val now = Calendar.getInstance()
        val currentSecond = now.get(Calendar.SECOND)
        val currentMillis = now.get(Calendar.MILLISECOND)

        // Calculate delay until next minute
        val delayMillis = ((60 - currentSecond) * 1000L) - currentMillis

        // Wait until the next minute
        delay(delayMillis)

        // Update time when minute changes
        onTimeUpdate(getCurrentTime())
    }
}