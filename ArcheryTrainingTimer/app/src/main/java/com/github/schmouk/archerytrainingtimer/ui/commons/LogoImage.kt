package com.github.schmouk.archerytrainingtimer.ui.commons

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

import com.github.schmouk.archerytrainingtimer.R

/**
 * Composable to display the application logo.
 *
 * @param modifier: Modifier, the modifier to be applied to the Image composable
 */
@Composable
fun LogoImage(modifier : Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ps_logo),
        contentDescription = stringResource(id = R.string.editor_logo),
        modifier = modifier
    )
}
