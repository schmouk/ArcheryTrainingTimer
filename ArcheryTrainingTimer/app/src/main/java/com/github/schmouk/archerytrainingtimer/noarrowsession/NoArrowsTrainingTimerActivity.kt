/*
MIT License

Copyright (c) 2025 Philippe Schmouker

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

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat

import com.github.schmouk.archerytrainingtimer.ui.noarrowsession.noArrowsTimerScreen
import com.github.schmouk.archerytrainingtimer.ui.theme.*


class NoArrowsTrainingTimerActivity : ComponentActivity() {

    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private var initialRingtoneVolume: Int = 0
    private lateinit var audioManager: AudioManager

    // Get the ViewModel instance, scoped to this Activity.
    // The Android system will create it if it doesn't exist, or re-provide
    // the existing one if, for example, the Activity is recreated due to rotation.
    // This requires NoArrowsTimerViewModel to have a default constructor OR
    // for us to provide a ViewModelProvider.Factory if it has constructor parameters.
    private val noArrowsTimerViewModel: NoArrowsTimerViewModel by viewModels()

    // --- Lifecycle Methods ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false) // Edge-to-edge

        userPreferencesRepository = UserPreferencesRepository(applicationContext) // Initialize or inject

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initialRingtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)

        keepScreenOn()

        setContent {
            ArcheryTrainingTimerTheme {
                noArrowsTimerScreen(
                    noArrowsTimerViewModel,
                    userPreferencesRepository
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // It's good practice to clear the flag when the activity is destroyed
        // to ensure it doesn't leak or affect other parts of the system if not
        // cleared explicitly elsewhere.
        allowScreenTimeout()

        // Restore initial ringtone volume
        val context = this
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_RING, initialRingtoneVolume, 0)
    }

    override fun onPause() {
        super.onPause()
        // Allow screen to turn off when the activity is no longer in the foreground
        allowScreenTimeout()
    }

    override fun onResume() {
        super.onResume()
        // Keep screen on when the activity is active and in the foreground
        keepScreenOn()
    }

    override fun onStop() {
        super.onStop()

        // Restore initial ringtone volume
        val context = this
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_RING, initialRingtoneVolume, 0)
    }

    // --- Helper Functions (if any were specific to Activity context before) ---

    // Call this function when we want to allow the screen to turn off normally again
    // e.g., when the timer stops or the user navigates away from the critical section.
    private fun allowScreenTimeout() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // Call this function when you want to force the screen to stay on
    // e.g., when the timer starts.
    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

}