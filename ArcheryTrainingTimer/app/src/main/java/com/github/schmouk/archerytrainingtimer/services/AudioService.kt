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

package com.github.schmouk.archerytrainingtimer.services

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.github.schmouk.archerytrainingtimer.R

/**
 * A centralized service to manage all audio-related tasks for the application.
 *
 * This class encapsulates the logic for interacting with the Android AudioManager
 *
 * @param context The application context, used for accessing system services and resources.
 */
class AudioService(context: Context) {

    /**
     * Checks if the device's ringer mode is not set to silent or vibrate.
     * @return true if sounds can be played, false otherwise.
     * */
    fun isAudioNotMuted(): Boolean {
        return systemAudioManager.ringerMode != AudioManager.RINGER_MODE_SILENT &&
                systemAudioManager.ringerMode != AudioManager.RINGER_MODE_VIBRATE
    }

    /**
     * Calculates the current device volume as a Float between 0.0 and 1.0.
     * This is the function you selected, now in its correct place.
     */
    fun getAudioVolumeLevel(): Float {
        // We use STREAM_MUSIC as our AudioAttributes are USAGE_MEDIA
        val currentVolume = systemAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)  // or STREAM_RING
        val maxVolume = systemAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        // Ensure maxVolume is not zero to avoid division by zero
        return if (maxVolume > 0) currentVolume.toFloat() / maxVolume else 0f
    }

    private val systemAudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

}
