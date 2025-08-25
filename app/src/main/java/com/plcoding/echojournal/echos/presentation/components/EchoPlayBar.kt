package com.plcoding.echojournal.echos.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.plcoding.echojournal.core.presentation.designsystem.theme.EchoJournalTheme
import com.plcoding.echojournal.echos.presentation.models.MoodColorSet
import com.plcoding.echojournal.echos.presentation.models.MoodUi
import kotlin.random.Random

@Composable
fun EchoPlayBar(
    amplitudeBarWidth: Dp,
    amplitudeBarSpacing: Dp,
    powerRatios: List<Float>,
    moodUi: MoodColorSet,
    playerProgress: () -> Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val amplitudeBarWidthPx = amplitudeBarWidth.toPx()
        val amplitudeBarSpacingPx = amplitudeBarSpacing.toPx()

        val clipPath = Path()

        powerRatios.forEachIndexed { i, ratio ->
            val height = (ratio * size.height)//.coerceAtLeast(amplitudeBarWidthPx)
            val xOffset = i * (amplitudeBarWidthPx + amplitudeBarSpacingPx)
            val yTopStart = center.y - height / 2f

            val topLeft = Offset(x = xOffset, y = yTopStart)
            val size = Size(width = amplitudeBarWidthPx, height = height)
            val roundRect = RoundRect(
                rect = Rect(offset = topLeft, size = size),
                cornerRadius = CornerRadius(100f),
            )
            clipPath.addRoundRect(roundRect)

            drawRoundRect(
                color = moodUi.desaturated,
                topLeft = topLeft,
                size = size,
                cornerRadius = CornerRadius(100f)
            )
        }

        clipPath(clipPath) {
            drawRect(
                color = moodUi.vivid,
                size = Size(
                    width = size.width * playerProgress(),
                    height = size.height
                )
            )
        }
    }
}

@Preview
@Composable
private fun EchoPlayBarPreview() {
    val ratios = remember { List(50) { Random.nextFloat() } }
    EchoJournalTheme {
        EchoPlayBar(
            amplitudeBarWidth = 2.dp,
            amplitudeBarSpacing = 2.dp,
            powerRatios = ratios,
            moodUi = MoodUi.SAD.colorSet,
            playerProgress = { 0.328f },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )
    }
}