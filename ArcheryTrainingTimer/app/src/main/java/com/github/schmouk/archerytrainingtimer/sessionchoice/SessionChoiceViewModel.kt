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

package com.github.schmouk.archerytrainingtimer.sessionchoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.schmouk.archerytrainingtimer.commons.SessionType
import com.github.schmouk.archerytrainingtimer.commons.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionChoiceViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Expose the selected session type as a StateFlow for the UI to observe.
    // Default to null initially.
    val selectedSessionType: StateFlow<Int?> =  //StateFlow<SessionType?> =
        userPreferencesRepository.sessionType.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3_500),
            initialValue = null
        )

    // Function for the UI to call when a session type is selected.
    fun selectSessionType(sessionTypeId: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveSessionType(sessionTypeId)
        }
    }
}