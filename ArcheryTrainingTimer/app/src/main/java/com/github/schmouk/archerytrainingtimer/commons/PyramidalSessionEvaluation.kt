/*
MIT License

Copyright (c) 2026 Philippe Schmouker, ph (dot) schmouker (at) gmail (dot) com

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
 * Evaluates the sum of a range ran through with a specified step.
 */
private fun rangeSum(start: Int, end: Int, step: Int = 1) : Int {
    var s: Int = 0
    for (n: Int in start..end step step) {
        s += n
    }
    return s
}


/**
 * Lazily yields the ramp-up variants described in step (A.), in the order they must be evaluated:
 *  1. the full ramp-up (nLow .. nHigh-1)
 *  2. for a group size k = 1, 2, 3, ...: groups of k successive series are removed,
 *     the first group starting at the 2nd series, the next ones every (k + 1) series
 *     (k=1 -> 2nd, 4th, 6th...; k=2 -> 2nd+3rd, 5th+6th...), progressively:
 *     first one group, then two groups, etc. until no more group can be removed.
 *  3. finally the minimal ramp-up: only nLow remains (session = nLow ... nHigh ... nLow).
 */
private fun rampUpVariants(full: List<Int>): Sequence<List<Int>> = sequence {
    yield(full)
    val size = full.size
    for (k in 1 until size) {
        val groupCount = (size - 1 + 1) / (k + 1) + 1          // upper bound, bounded below
        var g = 0
        while (g < groupCount) {
            val firstIdx = 1 + g * (k + 1)
            if (firstIdx >= size) break
            g++
            val removed = HashSet<Int>()
            for (j in 0 until g) {
                val start = 1 + j * (k + 1)
                for (i in start until minOf(start + k, size)) removed.add(i)
            }
            val variant = full.filterIndexed { idx, _ -> idx !in removed }
            if (variant.size < size) yield(variant)
        }
    }
    yield(listOf(full.first()))
}


/**
 * Evaluates the list of repetitions/arrows in successive series/ends for a
 * pyramidal session.
 */
fun evaluatePyramidalSession(
    approxTotalReps: Int,
    repsLowCount: Int,
    repsHighCount: Int  // Notice: repsHighCount is always > repsLowCount
) : MutableList<Int> {

    var evaluatedSession = mutableListOf<Int>()

    val halfTotal: Int = ((approxTotalReps - repsHighCount) / 2).coerceAtLeast(0)

    val fullRamp: List<Int> = (repsLowCount until repsHighCount).toList()

    // --- assemble + score a session from a ramp-up ---
    fun buildSession(ramp: List<Int>, duplicates: Int, duplicateMiddle: Boolean): MutableList<Int> {
        val up = ramp.flatMap { v -> List(duplicates) { v } }
        val middle = if (duplicateMiddle) listOf(repsHighCount, repsHighCount) else listOf(repsHighCount)
        return (up + middle + up.reversed()).toMutableList()
    }

    var best: MutableList<Int>? = null
    var bestDiff = Int.MAX_VALUE

    fun consider(session: MutableList<Int>) {
        val diff = abs(session.sum() - approxTotalReps)
        if (diff < bestDiff) {          // strict: on ties the earlier (less reduced) candidate wins
            bestDiff = diff
            best = session
        }
    }

    // Evaluates a ramp-up with every admissible duplication factor (C.) and with / without
    // a duplicated middle series.
    fun evaluateRamp(ramp: List<Int>, maxDuplicates: Int) {
        for (d in maxDuplicates downTo 1) {
            for (dupMiddle in listOf(false, true)) {
                consider(buildSession(ramp, d, dupMiddle))
            }
        }
    }

    val fullSum = rangeSum(repsLowCount, repsHighCount - 1, 1)
    val d0 = halfTotal / fullSum

    if (d0 == 0) {
        // (A.) shrink the ramp-up until the integer division is no longer zero
        var constructed: List<Int> = listOf(repsLowCount)
        var dRamp = 1
        for (variant in rampUpVariants(fullRamp)) {
            val s = variant.sum()
            val d = halfTotal / s
            if (d >= 1) {
                constructed = variant
                dRamp = d
                break
            }
        }
        evaluateRamp(constructed, dRamp)
    } else {
        // (B.) d0 == 1  /  (C.) d0 > 1 : explore every ramp-up variant and keep the closest
        for (variant in rampUpVariants(fullRamp)) {
            val d = (halfTotal / variant.sum()).coerceAtLeast(1)
            evaluateRamp(variant, d)
        }
    }

    evaluatedSession = best ?: buildSession(listOf(repsLowCount), 1, false)
    return evaluatedSession
}
