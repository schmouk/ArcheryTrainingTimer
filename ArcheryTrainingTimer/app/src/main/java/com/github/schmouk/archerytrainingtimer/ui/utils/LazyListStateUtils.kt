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

package com.github.schmouk.archerytrainingtimer.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.LazyListState

/**
 * Calculates and remembers the indices of all items that are fully visible in a LazyList.
 *
 * @param listState The LazyListState of the list to observe.
 * @return A State object holding a List of integers, where each integer is the index of a fully visible item.
 */
@Composable
fun rememberFullyVisibleItemIndices(listState: LazyListState): State<List<Int>> {
    return remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                emptyList()
            } else {
                visibleItemsInfo
                    .filter { item ->
                        // The viewport's start offset is always 0 for a LazyRow.
                        val viewportStartOffset = 0
                        // The viewport's end offset is the size of the container.
                        val viewportEndOffset = layoutInfo.viewportSize.width

                        // An item is fully visible if its start is after the viewport start,
                        // and its end is before the viewport end.
                        item.offset >= viewportStartOffset && (item.offset + item.size) <= viewportEndOffset
                    }
                    .map { it.index }
            }
        }
    }
}
