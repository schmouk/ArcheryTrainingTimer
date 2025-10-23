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

import android.app.Application
import com.github.schmouk.archerytrainingtimer.services.AudioService

/**
 * Custom Application class for ArcheryTrainingTimer.
 *
 * This class is the first component to be instantiated when the application starts.
 * It serves as a centralized place to initialize and hold application-wide singletons,
 * such as the AudioService.
 */
class ArcheryTrainingTimerApp : Application() {

    // A single, lazily-initialized instance of AudioService for the entire app.
    // "lazy" means the AudioService will only be created the very first time it's accessed.
    val audioService: AudioService by lazy {
        AudioService(applicationContext)
    }

    /*
    override fun onCreate() {
        super.onCreate()
        // We can perform other one-time initializations here if needed.
        // For example, priming the audio cache can be done here.
        audioService.primeCache() // Let's add a priming method to AudioService
    }
    */
}
