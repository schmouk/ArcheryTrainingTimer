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

package com.github.schmouk.archerytrainingtimer.commons

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.services.AudioService


/**
 * Manages loading and playing sounds for a specific UI component (e.g., a timer screen).
 * This is NOT a singleton. It's designed to be created and used by a ViewModel.
 *
 * @param context The context needed to load sound resources.
 * @param audioService The application-wide singleton for checking system audio status.
 */
class SoundPlayer(
    private val context: Context,
    private val audioService: AudioService
) {
    private val soundPool: SoundPool
    private var soundPoolLoaded = false

    private var beepSoundId: Int = 0
    private var endBeepSoundId: Int = 0
    private var intermediateBeepSoundId: Int = 0

    /**
     * Initializes the SoundPool and loads the sound resources.
     */
    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            //.setMaxStreams(2) // Allow for slight overlaps if needed
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                soundPoolLoaded = true
                //Log.i("SoundPlayer", "All sounds loaded successfully.")
            } else {
                //Log.e("SoundPlayer", "Failed to load a sound, status: $status")
            }
        }

        // Load the sounds
        beepSoundId = soundPool.load(context, R.raw.beep_short, 1)
        endBeepSoundId = soundPool.load(context, R.raw.beep_end, 1)
        intermediateBeepSoundId = soundPool.load(context, R.raw.beep_intermediate_short, 1)
    }


    /**
     * Returns true if all sounds are loaded and ready to be played,or false otherwise.
     */
    fun isLoaded(): Boolean = soundPoolLoaded

    /**
     * Plays the beep sound for the start of a repetition.
     */
    fun playBeep() = playSound(beepSoundId)

    /**
     * Plays the end beep sound for the end of the session.
     * The sound is played 3 times with a delay of 380 milliseconds between each play.
     */
    fun playEndBeep() = playSound(endBeepSoundId, 3, 380L)

    /**
     * Plays the intermediate beep sound.
     */
    fun playIntermediateBeep() = playSound(intermediateBeepSoundId)

    /**
     * Plays the rest beep sound for the start of a rest period.
     * The sound is played 2 times with a delay of 240 milliseconds between each play.
     */
    fun playRestBeep() = playSound(beepSoundId, 2, 240L)

    /**
     * Plays the specified sound if audio is not muted.
     *
     * @param soundId The ID of the sound to play.
     * @param repeatsCount The number of times to repeat the sound.
     * @param delayMillis The delay in milliseconds between repeats.
     */
    private fun playSound(
        soundId: Int,
        repeatsCount: Int = 1,
        delayMillis: Long = 0L
    ) {
        // Use the shared AudioService to check if we are allowed to play a sound
        if (soundId != 0  && repeatsCount > 0 && audioService.isAudioNotMuted()) {
            val audioVolume = audioService.getAudioVolumeLevel()
            var repeats = repeatsCount

            while (repeats > 0) {
                soundPool.play(soundId, audioVolume, audioVolume, 1, 0, 1f)
                repeats--

                if (repeats > 0 && delayMillis > 0) {
                    try {
                        Thread.sleep(delayMillis)
                    } catch (e: InterruptedException) {
                        Log.e("SoundPlayer", "Sleep interrupted: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Releases the SoundPool resources. Should be called when the owner (ViewModel) is cleared.
     */
    fun release() {
        soundPool.release()
    }
}
