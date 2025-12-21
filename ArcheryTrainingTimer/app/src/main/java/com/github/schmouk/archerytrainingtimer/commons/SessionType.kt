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

/**
 * Represents the different types of training sessions the user can choose.
 * The ID is used for storing the selection in DataStore.
 */
class SessionType(val id: Int?) {
    companion object {
        // Without-arrows session Types
        const val NO_ARROWS_UNIFORM: Int = 11
        const val NO_ARROWS_PYRAMIDAL: Int = 12

        // With-arrows session Types
        const val ARROWS_UNIFORM: Int = 21
        const val ARROWS_PYRAMIDAL: Int = 22

        // Kisik Lee Specific Physical Training Types
        const val KSL_SPT_ENDURANCE: Int = 31
        const val KSP_SPT_POWER_STRENGTH: Int = 32
        const val KSL_SPT_FLEXIBILITY: Int = 33
    }
}

