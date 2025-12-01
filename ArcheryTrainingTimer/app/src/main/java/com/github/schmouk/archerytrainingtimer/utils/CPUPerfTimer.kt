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

package com.github.schmouk.archerytrainingtimer.utils

/**
 * Time performance measurement utility.
 */
class CPUPerfTimer {

    private var startTime: Long = 0
    private var endTime: Long = 0
    private var running: Boolean = false

    /**
     * Starts the timer.
     */
    fun start() {
        startTime = System.nanoTime()
        running = true
    }

    /**
     * Stops the timer. Only effective if timer is running.
     */
    fun stop() {
        if (running) {
            endTime = System.nanoTime()
            running = false
        }
    }

    /**
     * Gets the elapsed time in nanoseconds.
     * If the timer is still running, it calculates the time until now.
     *
     * @return Elapsed time in nanoseconds.
     */
    fun getElapsedTime(): Long {
        val elapsedTime = if (running) {
            System.nanoTime() - startTime
        } else {
            endTime - startTime
        }
        return elapsedTime // in nanoseconds
    }

    /**
     * Gets the elapsed time in milliseconds.
     * If the timer is still running, it calculates the time until now.
     *
     * @return Elapsed time in milliseconds.
     */
    fun getElapsedTimeMillis(): Long {
        return getElapsedTime() / 1_000_000L // Converts nanoseconds to milliseconds
    }

}