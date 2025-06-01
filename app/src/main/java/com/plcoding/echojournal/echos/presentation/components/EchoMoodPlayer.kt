package com.plcoding.echojournal.echos.presentation.components

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.plcoding.echojournal.core.presentation.designsystem.theme.EchoJournalTheme
import com.plcoding.echojournal.core.presentation.util.formatMMSS
import com.plcoding.echojournal.echos.presentation.echos.models.PlaybackState
import com.plcoding.echojournal.echos.presentation.echos.models.TrackSizeInfo
import com.plcoding.echojournal.echos.presentation.models.MoodColorSet
import com.plcoding.echojournal.echos.presentation.models.MoodUi
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Composable
fun EchoMoodPlayer(
    moodUi: MoodUi?,
    playbackState: PlaybackState,
    playerProgress: () -> Float,
    totalPlaybackDuration: Duration,
    powerRations: List<Float>,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onTrackSizeAvailable: (TrackSizeInfo) -> Unit,
    modifier: Modifier = Modifier,
    amplitudeBarWidth: Dp = 5.dp,
    amplitudeBarSpacing: Dp = 4.dp
) {
    val formattedDuration = remember(playerProgress, totalPlaybackDuration) {
        "${(totalPlaybackDuration * playerProgress().toDouble()).formatMMSS()}/${totalPlaybackDuration.formatMMSS()}"
    }

    Surface(
        shape = CircleShape,
        color = moodUi?.colorSet?.faded ?: MoodColorSet.UNDEFINED_COLOR_SET.faded,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EchoPlaybackButton(
                playbackState = playbackState,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
                moodUi = moodUi?.colorSet ?: MoodColorSet.UNDEFINED_COLOR_SET
            )

            EchoPlayBar(
                amplitudeBarWidth = amplitudeBarWidth,
                amplitudeBarSpacing = amplitudeBarSpacing,
                powerRatios = powerRations,
                moodUi = moodUi?.colorSet ?: MoodColorSet.UNDEFINED_COLOR_SET,
                playerProgress = playerProgress,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp, horizontal = 8.dp)
                    .fillMaxHeight()
            )

            Text(
                text = formattedDuration,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun EchoMoodPlayerPreview() {
    val ratios = remember { List(30) { Random.nextFloat() } }
    EchoJournalTheme {
        EchoMoodPlayer(
            moodUi = MoodUi.STRESSED,
            playbackState = PlaybackState.PLAYING,
            playerProgress = { .375f },
            totalPlaybackDuration = 5.minutes,
            powerRations = ratios,
            onPlayClick = {},
            onPauseClick = {},
            onTrackSizeAvailable = {}
        )
    }
}