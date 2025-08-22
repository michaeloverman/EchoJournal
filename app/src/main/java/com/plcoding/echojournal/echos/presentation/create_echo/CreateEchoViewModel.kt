package com.plcoding.echojournal.echos.presentation.create_echo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.Selectable.Companion.asUnselectedItems
import com.plcoding.echojournal.echos.presentation.models.MoodUi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CreateEchoViewModel : ViewModel() {

    private var hasLoadedInitialData = false

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
            CreateEchoAction.OnCancelClick -> TODO()
            CreateEchoAction.OnConfirmMood -> onConfirmMood()
            CreateEchoAction.OnDismissMoodSelector -> onDismissMoodSelector()
            is CreateEchoAction.OnMoodClick -> onMoodClick(action.mood)
            CreateEchoAction.OnNavigateBackClick -> TODO()
            is CreateEchoAction.OnNoteTextChange -> TODO()
            CreateEchoAction.OnPauseAudioClick -> TODO()
            CreateEchoAction.OnPlayAudioClick -> TODO()

            is CreateEchoAction.OnAddTopicTextChange -> onAddTopicChange(action.text)
            is CreateEchoAction.OnRemoveTopicClick -> onRemoveTopic(action.topic)
            CreateEchoAction.OnDismissTopicSuggestions -> onDismissTopicSuggestions()
            is CreateEchoAction.OnTopicClick -> onTopicClick(action.topic)

            CreateEchoAction.OnSaveClick -> TODO()
            is CreateEchoAction.OnTitleTextChange -> TODO()
            is CreateEchoAction.OnTrackSizeAvailable -> TODO()
            CreateEchoAction.OnSelectMoodClick -> onSelectMoodClick()
        }
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