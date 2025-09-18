package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonDarkerColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppDimmedButtonColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTitleColor
import com.github.schmouk.archerytrainingtimer.ui.theme.SelectedButtonBorderColor
import kotlinx.coroutines.launch


/**
 * A small text displayed as title for the Repetitions Number selection.
 *
 * @param textStyle: TextStyle, the style to apply to the text
 * @param modifier: Modifier = Modifier, optional modifier for the Text composable
 */
@Composable
fun RepetitionsNumberTitle(
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Text( // Number of repetitions title
        text = stringResource(id = R.string.repetitions_number_label),
        style = textStyle,
        color = AppTextColor,
        modifier = modifier
    )
}


/**
 * Repetitions selector with scroll indicators
 */
@Composable
fun RepetitionsSelectorWithScrollIndicators(
    numberOfRepetitions: Int?,
    onRepetitionSelected: (Int) -> Unit,
    repetitionsListState: LazyListState = rememberLazyListState(),
    repetitionsRange: List<Int>,
    numbersTextSize : TextStyle,
    arrowButtonSizeDp : Dp,
    horizontalSpaceArrangement : Dp,
    repetitionBoxSize : Dp,
    borderStrokeWidth : Dp,
) {
    val coroutineScope = rememberCoroutineScope()

    // Derived states to determine if arrows should be shown
    // canScrollBackward is true if the first item is not fully visible at the start
    val canScrollBackward by remember {
        derivedStateOf {
            repetitionsListState.firstVisibleItemIndex > 0 || repetitionsListState.firstVisibleItemScrollOffset > 0
        }
    }

    // canScrollForward is true if the last item is not fully visible at the end
    // This requires knowing the total item count and the layout info of visible items.
    val canScrollForward by remember {
        derivedStateOf {
            // Check if there are items and the LazyListState has layout info
            if (repetitionsListState.layoutInfo.visibleItemsInfo.isNotEmpty() && repetitionsRange.isNotEmpty()) {
                val lastVisibleItem = repetitionsListState.layoutInfo.visibleItemsInfo.last()
                // If the last visible item's index is less than the total number of items - 1
                // OR if the last visible item is not fully occupying the viewport width at its end
                val viewportWidth = repetitionsListState.layoutInfo.viewportSize.width
                lastVisibleItem.index < repetitionsRange.size - 1 ||
                    lastVisibleItem.offset + lastVisibleItem.size > viewportWidth
            } else {
                repetitionsRange.isNotEmpty() // True if there are items but no layout info yet (initial state before first scroll/layout)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(), // This Box takes the full width
        contentAlignment = Alignment.Center // Centers its child (the Row) if the child is smaller
    ) {
        Row(
            //horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            // The Row itself will only take the width of its content.
            // If showArrows is false and LazyRow content is small, this Row will be small.
            // If showArrows is true, it will be wider.
            modifier = Modifier.wrapContentWidth(unbounded = false, align = Alignment.CenterHorizontally)
        ) {
            // --- Left Arrow ---
            Box(
                modifier = Modifier
                    .size(arrowButtonSizeDp), // Occupy space whether visible or not to help layout
                contentAlignment = Alignment.Center // Center the AnimatedVisibility content within the Box
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = canScrollBackward,
                    enter = fadeIn(animationSpec = tween(durationMillis = 400)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 400))
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                // Scroll to the beginning or by a certain amount
                                val targetIndex =
                                    (repetitionsListState.firstVisibleItemIndex - 5).coerceAtLeast(
                                        0
                                    ) // Scroll back 5 items
                                repetitionsListState.animateScrollToItem(targetIndex)
                            }
                        },
                        modifier = Modifier.fillMaxSize() // Fill the Box
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Scroll Left",
                            tint = AppTextColor
                        )
                    }
                }
            }

            // --- LazyRow ---
            // LazyRow takes the available space between arrows
            LazyRow(
                state = repetitionsListState,
                horizontalArrangement = Arrangement.spacedBy(horizontalSpaceArrangement), // Spacing between number buttons
                modifier = Modifier
                    .weight(1f) // LazyRow takes available space between arrows
                    //.padding(horizontal = 0.dp), // No extra padding here if arrows handle spacing
                    .wrapContentWidth() // Let LazyRow determine its own width
            ) {
                items(
                    count = repetitionsRange.size,
                    key = { index -> repetitionsRange[index] }
                ) { index ->
                    val repetitionNum = repetitionsRange[index]
                    val isNumberSelected = repetitionNum == numberOfRepetitions
                    val isClickable = !isNumberSelected  // true

                    Box(
                        modifier = Modifier
                            .size(repetitionBoxSize)
                            .then(
                                if (isNumberSelected) Modifier.border(
                                    BorderStroke(
                                        borderStrokeWidth,
                                        SelectedButtonBorderColor
                                    ), shape = CircleShape
                                ) else Modifier
                            )
                            .padding(if (isNumberSelected) borderStrokeWidth else 0.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isNumberSelected) AppTitleColor
                                else if (isClickable) AppButtonDarkerColor
                                else AppDimmedButtonColor
                            )
                            .clickable {
                                if (isClickable)
                                    onRepetitionSelected(repetitionNum)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$repetitionNum",
                            style = numbersTextSize.copy(
                                color = if (isNumberSelected) AppButtonTextColor else AppTextColor
                            ),
                            fontWeight = if (isNumberSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // --- Right Arrow ---
            Box(
                modifier = Modifier
                    .size(arrowButtonSizeDp), // Occupy space whether visible or not
                contentAlignment = Alignment.Center // Center the AnimatedVisibility content
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = canScrollForward,
                    enter = fadeIn(animationSpec = tween(durationMillis = 400)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 400))
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                // Scroll to the end or by a certain amount
                                val targetIndex =
                                    (repetitionsListState.firstVisibleItemIndex + 5).coerceAtMost(
                                        repetitionsRange.size - 1
                                    ) // Scroll forward 5 items example
                                repetitionsListState.animateScrollToItem(targetIndex)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,  //.KeyboardArrowRight,
                            contentDescription = "Scroll Right",
                            tint = AppTextColor  //MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
