package com.plcoding.echojournal.echos.presentation.preview

import com.plcoding.echojournal.echos.presentation.echos.models.PlaybackState
import com.plcoding.echojournal.echos.presentation.echos.models.TrackSizeInfo
import com.plcoding.echojournal.echos.presentation.models.EchoUi
import com.plcoding.echojournal.echos.presentation.models.MoodUi
import java.time.Instant
import kotlin.random.Random
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

data object PreviewUi {
    val echoUi = EchoUi(
        id = 1,
        title = "Sample Title",
        mood = MoodUi.EXCITED,
        recordedAt = Instant.now() - 15.minutes.toJavaDuration(),
        note = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        topics = listOf("Work", "Play", "Home", "Family"),
        filepath = "path.to.file/music",
        amplitudes = List(50) { Random.nextFloat() },
        playbackTotalDuration = 5.minutes,
        playbackCurrentDuration = ZERO,
        playbackState = PlaybackState.STOPPED
    )

    val trackSizeInfo = TrackSizeInfo(
        trackWidth = 250f,
        barWidth = 14f,
        spacing = 12f
    )
}