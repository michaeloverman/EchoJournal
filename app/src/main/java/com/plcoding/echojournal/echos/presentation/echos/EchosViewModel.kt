package com.plcoding.echojournal.echos.presentation.echos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.echojournal.R
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.Selectable
import com.plcoding.echojournal.core.presentation.util.UiText
import com.plcoding.echojournal.echos.presentation.echos.models.EchoFilterChip
import com.plcoding.echojournal.echos.presentation.echos.models.MoodChipContent
import com.plcoding.echojournal.echos.presentation.models.MoodUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class EchosViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    private val selectedMoodFilters = MutableStateFlow<List<MoodUi>>(emptyList())
    private val selectedTopicFilters = MutableStateFlow<List<String>>(emptyList())

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
            EchosAction.OnFabClick -> {}
            EchosAction.OnFabLongClick -> {}
            EchosAction.OnSettingsClick -> {}

            is EchosAction.OnRemoveFilters -> {
                when (action.filterType) {
                    EchoFilterChip.MOODS -> selectedMoodFilters.update { emptyList() }
                    EchoFilterChip.TOPICS -> selectedTopicFilters.update { emptyList() }
                }
            }
            EchosAction.OnMoodChipClick -> {
                _state.update { it.copy(
                    selectedEchoFilterChip =
                        if (it.selectedEchoFilterChip == EchoFilterChip.MOODS) null
                        else EchoFilterChip.MOODS
                ) }
            }

            EchosAction.OnTopicChipClick -> {
                _state.update { it.copy(
                    selectedEchoFilterChip =
                    if (it.selectedEchoFilterChip == EchoFilterChip.TOPICS) null
                    else EchoFilterChip.TOPICS) }
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
            EchosAction.OnPauseClick -> {}
            is EchosAction.OnTrackSizeAvailable -> {}
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