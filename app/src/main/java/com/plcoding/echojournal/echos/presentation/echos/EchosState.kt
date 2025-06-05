package com.plcoding.echojournal.echos.presentation.echos

import com.plcoding.echojournal.R
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.Selectable
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.Selectable.Companion.asUnselectedItems
import com.plcoding.echojournal.core.presentation.util.UiText
import com.plcoding.echojournal.echos.presentation.echos.models.AudioCaptureMethod
import com.plcoding.echojournal.echos.presentation.echos.models.EchoDaySection
import com.plcoding.echojournal.echos.presentation.echos.models.EchoFilterChip
import com.plcoding.echojournal.echos.presentation.echos.models.MoodChipContent
import com.plcoding.echojournal.echos.presentation.echos.models.RecordingState
import com.plcoding.echojournal.echos.presentation.models.EchoUi
import com.plcoding.echojournal.echos.presentation.models.MoodUi
import java.util.Locale
import kotlin.time.Duration

data class EchosState(
    val echos: Map<UiText, List<EchoUi>> = emptyMap(),
    val recordingState: RecordingState = RecordingState.NOT_RECORDING,
    val currentCaptureMethod: AudioCaptureMethod? = null,
    val recordingElapsedDuration: Duration = Duration.ZERO,
    val hasEchosRecorded: Boolean = false,
    val hasActiveTopicFilters: Boolean = false,
    val hasActiveMoodFilters: Boolean = false,
    val isLoadingData: Boolean = false,
    val moods: List<Selectable<MoodUi>> = MoodUi.entries.asUnselectedItems(),
    val topics: List<Selectable<String>> = listOf("Happy", "Work", "Passion").asUnselectedItems(),
    val moodChipContent: MoodChipContent = MoodChipContent(),
    val selectedEchoFilterChip: EchoFilterChip? = null,
    val topicChipTitle: UiText = UiText.StringResource(R.string.all_topics)
) {
    val sections = echos.entries.map {
        EchoDaySection(
            dateHeader = it.key,
            echos = it.value
        )
    }

    val formattedRecordDuration: String
        get() {
            val minutes = (recordingElapsedDuration.inWholeMinutes).toInt()
            val seconds = (recordingElapsedDuration.inWholeSeconds % 60).toInt()
            val centiseconds = ((recordingElapsedDuration.inWholeMilliseconds % 1000) / 10.0).toInt()

            return String.format(
                locale = Locale.US,
                format = "%02d:%02d.%02d",
                minutes, seconds, centiseconds
            )
        }
}