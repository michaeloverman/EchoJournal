package com.plcoding.echojournal.core.presentation.echos.presentation.echos.models

import com.plcoding.echojournal.R
import com.plcoding.echojournal.core.presentation.util.UiText

data class MoodChipContent(
    val iconRes: List<Int> = emptyList(),
    val title: UiText = UiText.StringResource(R.string.all_moods)
)
