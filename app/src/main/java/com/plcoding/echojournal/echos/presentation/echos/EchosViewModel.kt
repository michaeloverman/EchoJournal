package com.plcoding.echojournal.echos.presentation.echos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.echojournal.R
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.Selectable
import com.plcoding.echojournal.core.presentation.util.UiText
import com.plcoding.echojournal.echos.domain.recording.VoiceRecorder
import com.plcoding.echojournal.echos.presentation.echos.models.AudioCaptureMethod
import com.plcoding.echojournal.echos.presentation.echos.models.EchoFilterChip
import com.plcoding.echojournal.echos.presentation.echos.models.MoodChipContent
import com.plcoding.echojournal.echos.presentation.echos.models.RecordingState
import com.plcoding.echojournal.echos.presentation.models.MoodUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class EchosViewModel(
    val voiceRecorder: VoiceRecorder
) : ViewModel() {

    companion object {
        private val MIN_RECORD_DURATION = 1.5.seconds
    }

    private var hasLoadedInitialData = false

    private val selectedMoodFilters = MutableStateFlow<List<MoodUi>>(emptyList())
    private val selectedTopicFilters = MutableStateFlow<List<String>>(emptyList())

    private val eventChannel = Channel<EchosEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(EchosState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeFilters()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = EchosState()
        )

    fun onAction(action: EchosAction) {
        when (action) {
            EchosAction.OnFabClick -> {
                requestAudioPermission()
                _state.update { it.copy(currentCaptureMethod = AudioCaptureMethod.STANDARD) }
            }

            EchosAction.OnFabLongClick -> {
                requestAudioPermission()
                _state.update { it.copy(currentCaptureMethod = AudioCaptureMethod.QUICK) }
            }

            EchosAction.OnSettingsClick -> {}

            is EchosAction.OnRemoveFilters -> {
                when (action.filterType) {
                    EchoFilterChip.MOODS -> selectedMoodFilters.update { emptyList() }
                    EchoFilterChip.TOPICS -> selectedTopicFilters.update { emptyList() }
                }
            }

            EchosAction.OnMoodChipClick -> {
                _state.update {
                    it.copy(
                        selectedEchoFilterChip =
                            if (it.selectedEchoFilterChip == EchoFilterChip.MOODS) null
                            else EchoFilterChip.MOODS
                    )
                }
            }

            EchosAction.OnTopicChipClick -> {
                _state.update {
                    it.copy(
                        selectedEchoFilterChip =
                            if (it.selectedEchoFilterChip == EchoFilterChip.TOPICS) null
                            else EchoFilterChip.TOPICS
                    )
                }
            }

            EchosAction.OnDismissMoodDropdown,
            EchosAction.OnDismissTopicDropdown -> {
                _state.update { it.copy(selectedEchoFilterChip = null) }
            }

            is EchosAction.OnFilterByMoodClick -> {
                toggleMoodFilter(action.moodUi)
            }

            is EchosAction.OnFilterByTopicClick -> {
                toggleTopicFilter(action.topic)
            }

            is EchosAction.OnEchoPlayClick -> {}
            EchosAction.OnPauseAudioClick -> {}
            is EchosAction.OnTrackSizeAvailable -> {}

            EchosAction.OnAudioPermissionGranted -> startRecording(AudioCaptureMethod.STANDARD)

            EchosAction.OnPauseRecordingClick -> {
                voiceRecorder.pause()
                _state.update { it.copy(recordingState = RecordingState.PAUSED) }
            }

            EchosAction.OnCancelRecording -> {
                voiceRecorder.cancel()
                _state.update {
                    it.copy(
                        recordingState = RecordingState.NOT_RECORDING,
                        currentCaptureMethod = null
                    )
                }
            }

            EchosAction.OnCompleteRecordingClick -> {
                voiceRecorder.stop()
                _state.update {
                    it.copy(
                        recordingState = RecordingState.NOT_RECORDING,
                        currentCaptureMethod = null
                    )
                }

                val details = voiceRecorder.recordingDetails.value
                viewModelScope.launch {
                    eventChannel.send(
                        if (details.duration < MIN_RECORD_DURATION) EchosEvent.RecordingTooShort
                        else EchosEvent.OnRecordingDone
                    )
                }
            }

            EchosAction.OnResumeRecordingClick -> {
                voiceRecorder.resume()
                _state.update { it.copy(recordingState = RecordingState.NORMAL_CAPTURE) }
            }
        }
    }

    private fun startRecording(method: AudioCaptureMethod) {
        _state.update {
            it.copy(
                recordingState = when (method) {
                    AudioCaptureMethod.STANDARD -> RecordingState.NORMAL_CAPTURE
                    AudioCaptureMethod.QUICK -> RecordingState.QUICK_CAPTURE
                }
            )
        }
        voiceRecorder.start()

        if (method == AudioCaptureMethod.STANDARD) {
            voiceRecorder
                .recordingDetails
                .distinctUntilChangedBy { it.duration }
                .map { it.duration }
                .onEach { duration ->
                    _state.update {
                        it.copy(
                            recordingElapsedDuration = duration
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun observeFilters() {
        combine(
            selectedMoodFilters,
            selectedTopicFilters
        ) { moods, topics ->
            _state.update { state ->
                state.copy(
                    moods = state.moods.map { mood ->
                        Selectable(
                            item = mood.item,
                            selected = moods.contains(mood.item)
                        )
                    },
                    topics = state.topics.map { topic ->
                        Selectable(
                            item = topic.item,
                            selected = topics.contains(topic.item)
                        )
                    },
                    hasActiveTopicFilters = topics.isNotEmpty(),
                    hasActiveMoodFilters = moods.isNotEmpty(),
                    topicChipTitle = topics.deriveTopicChipTitle(),
                    moodChipContent = moods.asMoodChipContent(),
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun requestAudioPermission() = viewModelScope.launch {
        eventChannel.send(EchosEvent.RequestAudioPermission)
    }

    private fun toggleMoodFilter(moodUi: MoodUi) {
        selectedMoodFilters.update { moods ->
            if (moodUi in moods) moods - moodUi
            else moods + moodUi
        }
    }

    private fun toggleTopicFilter(topic: String) {
        selectedTopicFilters.update { topics ->
            if (topic in topics) topics - topic
            else topics + topic
        }
    }

    private fun List<String>.deriveTopicChipTitle(): UiText {
        return when (size) {
            0 -> UiText.StringResource(R.string.all_topics)
            1 -> UiText.Dynamic(first())
            2 -> UiText.Dynamic("${first()}, ${last()}")
            else -> UiText.Dynamic("${first()}, ${this[1]} +${size - 2}")
        }
    }

    private fun List<MoodUi>.asMoodChipContent(): MoodChipContent {
        if (isEmpty()) return MoodChipContent()

        val icons = map { it.iconSet.fill }
        val names = map { it.title }

        return when (size) {
            1 -> MoodChipContent(iconsRes = icons, title = names.first())
            2 -> MoodChipContent(
                iconsRes = icons,
                title = UiText.Combined(
                    format = "%s, %s",
                    uiTexts = names.toTypedArray()
                )
            )

            else -> MoodChipContent(
                iconsRes = icons,
                title = UiText.Combined(
                    format = "%s, %s +${size - 2}",
                    uiTexts = names.take(2).toTypedArray()
                )
            )
        }
    }
}