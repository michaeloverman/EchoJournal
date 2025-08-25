package com.plcoding.echojournal.echos.presentation.create_echo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.plcoding.echojournal.app.navigation.NavigationRoute
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.Selectable.Companion.asUnselectedItems
import com.plcoding.echojournal.echos.domain.recording.RecordingStorage
import com.plcoding.echojournal.echos.presentation.echos.models.TrackSizeInfo
import com.plcoding.echojournal.echos.presentation.models.MoodUi
import com.plcoding.echojournal.echos.presentation.util.AmplitudeNormalizer
import com.plcoding.echojournal.echos.presentation.util.toRecordingDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateEchoViewModel(
    savedStateHandle: SavedStateHandle,
    val recordingStorage: RecordingStorage
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val route = savedStateHandle.toRoute<NavigationRoute.CreateEcho>()
    private val recordingDetails = route.toRecordingDetails()

    private val eventChannel = Channel<CreateEchoEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(CreateEchoState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeAddTopicText()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CreateEchoState()
        )

    fun onAction(action: CreateEchoAction) {
        when (action) {
            CreateEchoAction.OnConfirmMood -> onConfirmMood()
            CreateEchoAction.OnDismissMoodSelector -> onDismissMoodSelector()
            is CreateEchoAction.OnMoodClick -> onMoodClick(action.mood)
            is CreateEchoAction.OnNoteTextChange -> onNoteTextChange(action.text)
            CreateEchoAction.OnPauseAudioClick -> {}
            CreateEchoAction.OnPlayAudioClick -> {}

            is CreateEchoAction.OnAddTopicTextChange -> onAddTopicChange(action.text)
            is CreateEchoAction.OnRemoveTopicClick -> onRemoveTopic(action.topic)
            CreateEchoAction.OnDismissTopicSuggestions -> onDismissTopicSuggestions()
            is CreateEchoAction.OnTopicClick -> onTopicClick(action.topic)

            CreateEchoAction.OnSaveClick -> onSaveClick()
            is CreateEchoAction.OnTitleTextChange -> onTitleTextChange(action.text)
            is CreateEchoAction.OnTrackSizeAvailable -> onTrackSizeAvailable(action.trackSizeInfo)
            CreateEchoAction.OnSelectMoodClick -> onSelectMoodClick()

            CreateEchoAction.OnCancelClick,
            CreateEchoAction.OnNavigateBackClick,
            CreateEchoAction.OnGoBack -> onShowConfirmLeaveDialog()
            CreateEchoAction.OnDismissConfirmLeaveDialog -> onDismissConfirmLeaveDialog()
        }
    }

    private fun onTrackSizeAvailable(trackSizeInfo: TrackSizeInfo) {
        viewModelScope.launch(Dispatchers.Default) {
            val finalAmplitudes = AmplitudeNormalizer.normalize(
                source = recordingDetails.amplitudes,
                trackWidth =  trackSizeInfo.trackWidth,
                barWidth = trackSizeInfo.barWidth,
                spacing = trackSizeInfo.spacing
            )
            _state.update { it.copy(
                playbackAmplitudes = finalAmplitudes
            ) }
        }
    }

    private fun onNoteTextChange(text: String) {
        _state.update { it.copy(noteText = text) }
    }

    private fun onTitleTextChange(text: String) {
        _state.update { it.copy(titleText = text) }
    }

    private fun onSaveClick() {
        if (recordingDetails.filePath == null) return

        viewModelScope.launch {
            val savedFilePath = recordingStorage.savePersistently(recordingDetails.filePath)
            if (savedFilePath == null) {
                eventChannel.send(CreateEchoEvent.FailedToSaveFile)
                return@launch
            }

            // TODO put it all in the db, or whatevs
        }
    }

    private fun onShowConfirmLeaveDialog() {
        _state.update { it.copy(showConfirmLeaveDialog = true) }
    }

    private fun onDismissConfirmLeaveDialog() {
        _state.update { it.copy(showConfirmLeaveDialog = false) }
    }

    @OptIn(FlowPreview::class)
    private fun observeAddTopicText() {
        state
            .map { it.addTopicText }
            .distinctUntilChanged()
            .debounce(300)
            .onEach { query ->
                _state.update { it.copy(
                    showTopicSuggestions = query.isNotBlank() && query.trim() !in it.topics,
                    searchResults = listOf("hello", "helloworld").asUnselectedItems()
                ) }
            }
            .launchIn(viewModelScope)
    }

    private fun onDismissTopicSuggestions() {
        _state.update { it.copy(showTopicSuggestions = false) }
    }

    private fun onRemoveTopic(topic: String) {
        _state.update { it.copy(topics = it.topics - topic) }
    }

    private fun onTopicClick(topic: String) {
        _state.update { it.copy(
            addTopicText = "",
            topics = (it.topics + topic).distinct()
        ) }
    }

    private fun onAddTopicChange(text: String) {
        _state.update { state -> state.copy(
            addTopicText = text.filter { it.isLetterOrDigit() }
        ) }
    }

    private fun onConfirmMood() {
        _state.update { it.copy(
            mood = it.selectedMood,
            canSaveEcho = it.titleText.isNotBlank(),
            showMoodSelector = false
        ) }
    }

    private fun onDismissMoodSelector() {
        _state.update { it.copy(
            showMoodSelector = false
        ) }
    }

    private fun onMoodClick(mood: MoodUi) {
        _state.update { it.copy(
            selectedMood = mood,
        ) }
    }

    private fun onSelectMoodClick() {
        _state.update { it.copy(showMoodSelector = true) }
    }

}