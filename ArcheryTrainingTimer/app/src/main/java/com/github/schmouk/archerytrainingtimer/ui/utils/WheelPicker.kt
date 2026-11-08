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

package com.github.schmouk.archerytrainingtimer.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import kotlin.math.abs

@Composable
fun WheelPicker(
    items: List<Int>,
    units: String = "",
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    selectedColor: Color = Color(0xFF_FFFFFF),
    selectedBackground: Color = Color(0x00_000000),
    unselectedColor: Color = Color(0xFF_A3A3A3),
    unselectedBackground: Color = Color(0x00_000000),
    itemHeight: Dp = 48.dp,
    dividerThicknessScaling: Float = 0.07f,
    dividerColor: Color = Color(0xFF_A3A3A3),
    rotationAngle: Float = 55f,
    infiniteRotation: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Selected index = index of the item that is the nearest from center
    val centeredIndex by remember {
        derivedStateOf {
            val idx = listState.firstVisibleItemIndex +
                    if (listState.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0
            idx.coerceIn(items.indices)
        }
    }

    LaunchedEffect(centeredIndex) { onSelectedIndexChange(centeredIndex) }

    Box(
        modifier = modifier.height(itemHeight * 3),  // i.e. 1 centered value + 1 above + 1 below
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            // Permet au 1er et au dernier élément d'atteindre le centre, mais pas d'aller plus loin
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(items) { index, value ->
                // Distance au centre, en nombre d'éléments (0 = centré, ±1 = voisin)
                val distance by remember {
                    derivedStateOf {
                        val info = listState.layoutInfo.visibleItemsInfo
                            .firstOrNull { it.index == index }
                        if (info == null) 2f else (info.offset / itemHeightPx).coerceIn(-2f, 2f)
                    }
                }
                val isSelected = index == centeredIndex

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .background(if (isSelected) selectedBackground else unselectedBackground)
                        .graphicsLayer {
                            // Effet "cylindre" : rotation autour de l'axe horizontal
                            rotationX = -distance * rotationAngle  // inverser le signe si le sens est opposé
                            cameraDistance = 12f * density.density
                            val s = 1f - 0.15f * abs(distance).coerceAtMost(1f)
                            scaleX = s; scaleY = s
                            alpha = 1f  //- 0.55f * abs(distance).coerceAtMost(1f)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val textScale = 1f  //if (isSelected) 0.92f else 0.64f
                    val textHeight = itemHeight * textScale
                    val textFontSize = with(density) { (textHeight * 0.72f).toSp() }
                    val textLineHeight = with(density) { textHeight.toSp() }
                    Text(
                        text = value.toString(),
                        style = LocalTextStyle.current.copy(
                            fontSize = textFontSize,
                            lineHeight = textLineHeight,
                        ),
                        color = if (isSelected) selectedColor  //MaterialTheme.colorScheme.onSurface
                                else unselectedColor  //MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Guide lines, drawn above the item rows so they stay visible.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val dividerThickness = itemHeight * dividerThicknessScaling
            HorizontalDivider(color = dividerColor, thickness = dividerThickness)
            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = dividerColor, thickness = dividerThickness)
        }
    }
}


@Preview
@Composable
fun WheelPickerPreview() {
    val numbers = remember { (0..59).toList() }
    var selected1 by remember { mutableIntStateOf(10) }
    var selected2 by remember { mutableIntStateOf(10) }
    var selected3 by remember { mutableIntStateOf(10) }

    Row(
        Modifier.background(Color(0xFF_2F3133)).padding(top=12.dp, start=8.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start=8.dp)
        ) {
            WheelPicker(
                items = numbers,
                selectedIndex = selected1,
                onSelectedIndexChange = { selected1 = it },
                modifier = Modifier.width(80.dp),
                itemHeight = 24.dp,
                rotationAngle = 35f
            )
            Text("Value : ${numbers[selected1]}")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start=8.dp)
        ) {
            WheelPicker(
                items = numbers,
                selectedIndex = selected2,
                onSelectedIndexChange = { selected2 = it },
                modifier = Modifier.width(80.dp),
                itemHeight = 32.dp,
                rotationAngle = 20f
            )
            Text("Value : ${numbers[selected2]}")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start=8.dp)
        ) {
            WheelPicker(
                items = numbers,
                selectedIndex = selected3,
                onSelectedIndexChange = { selected3 = it },
                modifier = Modifier.width(80.dp),
                itemHeight = 44.dp,
                rotationAngle = 55f
            )
            Text("Value : ${numbers[selected3]}")
        }
    }
}
