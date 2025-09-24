package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.github.schmouk.archerytrainingtimer.R
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonDarkerColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppButtonTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppDimmedButtonColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTextColor
import com.github.schmouk.archerytrainingtimer.ui.theme.AppTitleColor
import com.github.schmouk.archerytrainingtimer.ui.theme.SelectedButtonBorderColor


/**
 * A small text displayed as title for the Repetitions Number selection.
 *
 * @param textStyle: TextStyle, the style to apply to the text
 * @param modifier: Modifier = Modifier, optional modifier for the Text composable
 */
@Composable
fun SeriesNumberTitle(
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Text( // Number of repetitions title
        text = stringResource(id = R.string.series_number_label),
        style = textStyle,
        color = AppTextColor,
        modifier = modifier
    )
}


@Composable
fun SeriesNumbersButtons(
    numberOfSeries : Int?,
    onNumberSelected : (Int) -> Unit,
    seriesOptions : List<Int>,
    borderStrokeWidth : Dp,
    seriesBoxSize : Dp,
    textStyle : TextStyle,
    horizontalSpacing : Dp,
    horizontalArrangement : Arrangement.Horizontal,
    rowModifier : Modifier
) {
    Row(
        modifier = rowModifier,
        horizontalArrangement = horizontalArrangement
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
            seriesOptions.forEach { seriesCount ->
                val isSeriesSelected = seriesCount == numberOfSeries
                val isClickable = !isSeriesSelected
                Box(
                    modifier = Modifier
                        .size(seriesBoxSize)
                        .then(
                            if (isSeriesSelected) Modifier.border(
                                BorderStroke(
                                    borderStrokeWidth,
                                    SelectedButtonBorderColor
                                ), shape = CircleShape
                            ) else Modifier
                        )
                        .padding(if (isSeriesSelected) borderStrokeWidth else 0.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isSeriesSelected) AppTitleColor
                            else if (isClickable) AppButtonDarkerColor
                            else AppDimmedButtonColor
                        )
                        .clickable {
                            if (isClickable)
                                onNumberSelected(seriesCount)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$seriesCount",
                        style = textStyle.copy(
                            color = if (isSeriesSelected) AppButtonTextColor else AppTextColor
                        ),
                        fontWeight = if (isSeriesSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}