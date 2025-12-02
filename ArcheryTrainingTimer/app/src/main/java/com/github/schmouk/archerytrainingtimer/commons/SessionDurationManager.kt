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

package com.github.schmouk.archerytrainingtimer.commons

import kotlinx.coroutines.delay

import com.github.schmouk.archerytrainingtimer.DEBUG_MODE
import com.github.schmouk.archerytrainingtimer.MINUTE_DURATION_MS



//=====   The Session Duration Manager   ======================
/**
 * This class manages the duration of a session.
 */
class SessionDurationManager {
    private var currentDurationHr: Int = 0
    private var currentDurationMn: Int = 0
    private var isSessionActive: Boolean = false

    suspend fun start() {
        currentDurationMn = 0
        currentDurationHr = 0
        isSessionActive = true
        updateClockOnMinuteChange()
    }

    fun stop() {
        isSessionActive = false
    }

    fun isSessionActive(): Boolean {
        return isSessionActive
    }

    fun isSessionInactive(): Boolean {
        return !isSessionActive
    }

    fun getDurationText(): String {
        return String.format("%02d:%02d", currentDurationHr, currentDurationMn)
    }

    private suspend fun updateClockOnMinuteChange() {
        while (isSessionActive) {
            // Wait until the next minute
            delay(MINUTE_DURATION_MS - 1L)
            if (++currentDurationMn >= 60) {
                currentDurationMn = 0
                currentDurationHr++
            }
        }
    }
}
