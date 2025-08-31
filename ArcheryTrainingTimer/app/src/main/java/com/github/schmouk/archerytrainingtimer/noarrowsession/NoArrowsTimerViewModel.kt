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

package com.github.schmouk.archerytrainingtimer.noarrowsession

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel

import com.github.schmouk.archerytrainingtimer.noarrowsession.SessionStateAutomaton


/**
 * ViewModel for managing the state of the No Arrows Training Timer.
 * This ViewModel uses a SessionStateAutomaton to handle state transitions
 * based on user actions and timer events.
 */
class NoArrowsTimerViewModel : ViewModel() {

    // Internal state automaton to manage timer states
    private val stateAutomaton = SessionStateAutomaton()

    // actuates the finite state machine according to the received signal
    // and updates the mutable states accordingly
    @Suppress("MemberVisibilityCanBePrivate")
    fun action(signal: ESignal) {
        stateAutomaton.action(signal)

        _isIdleMode.value = stateAutomaton.isIdleMode()
        _isRestMode.value = stateAutomaton.isRestMode()
        _isSessionCompleted.value = stateAutomaton.isSessionCompleted()
        _isTimerRunning.value = stateAutomaton.isTimerRunning()
        _isTimerStopped.value = stateAutomaton.isTimerStopped()
    }

    // Mutable checking of the internal state - idle mode
    private val _isIdleMode = mutableStateOf(stateAutomaton.isIdleMode())
    val isIdleMode: State<Boolean> = _isIdleMode

    // Mutable checking of the internal state - rest mode
    private val _isRestMode = mutableStateOf(stateAutomaton.isRestMode())
    val isRestMode: State<Boolean> = _isRestMode

    // Mutable checking of the internal state - completed session
    private val _isSessionCompleted = mutableStateOf(stateAutomaton.isSessionCompleted())
    val isSessionCompleted: State<Boolean> = _isSessionCompleted

    // Mutable checking of the internal state - running timer
    private val _isTimerRunning = mutableStateOf(stateAutomaton.isTimerRunning())
    val isTimerRunning: State<Boolean> = _isTimerRunning

    // Mutable checking of the internal state - stopped timer
    private val _isTimerStopped = mutableStateOf(stateAutomaton.isTimerStopped())
    val isTimerStopped: State<Boolean> = _isTimerStopped
}