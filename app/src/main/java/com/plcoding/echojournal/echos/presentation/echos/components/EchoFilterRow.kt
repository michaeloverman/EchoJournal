package com.plcoding.echojournal.echos.presentation.echos.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.plcoding.echojournal.R
import com.plcoding.echojournal.core.presentation.designsystem.chips.MultiChoiceChip
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.Selectable
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.SelectableDropDownOptionsMenu
import com.plcoding.echojournal.core.presentation.util.UiText
import com.plcoding.echojournal.echos.presentation.echos.EchosAction
import com.plcoding.echojournal.echos.presentation.echos.models.EchoFilterChip
import com.plcoding.echojournal.echos.presentation.echos.models.MoodChipContent
import com.plcoding.echojournal.echos.presentation.models.MoodUi

@Composable
fun EchoFilterRow(
    moodChipContent: MoodChipContent,
    hasActiveMoodFilters: Boolean,
    selectedEchoFilterChip: EchoFilterChip?,
    moods: List<Selectable<MoodUi>>,
    topicChipTitle: UiText,
    hasActiveTopicFilters: Boolean,
    topics: List<Selectable<String>>,
    onAction: (EchosAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var dropDownOffset by remember { mutableStateOf(IntOffset.Zero) }
    val configuration = LocalWindowInfo.current
    val dropDownMaxHeight = (configuration.containerSize.height * 0.3f).dp
    FlowRow(
        modifier = modifier
            .padding(16.dp)
            .onGloballyPositioned {
                dropDownOffset = IntOffset(
                    x = 0, y = it.size.height
                )
            },
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MultiChoiceChip(
            displayText = moodChipContent.title.asString(),
            onClick = {
                onAction(EchosAction.OnMoodChipClick)
            },
            leadingContent = {
                if (moodChipContent.iconsRes.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-4).dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        moodChipContent.iconsRes.forEach { icon ->
                            Image(
                                imageVector = ImageVector.vectorResource(icon),
                                contentDescription = moodChipContent.title.asString(),
                                modifier = Modifier.height(16.dp)
                            )
                        }
                    }
                }
            },
            isClearVisible = hasActiveMoodFilters,
            isDropDownVisible = selectedEchoFilterChip == EchoFilterChip.MOODS,
            isHighlighted = hasActiveMoodFilters || selectedEchoFilterChip == EchoFilterChip.MOODS,
            onClearButtonClick = { onAction(EchosAction.OnRemoveFilters(EchoFilterChip.MOODS)) },
            dropDownMenu = {
                SelectableDropDownOptionsMenu(
                    items = moods,
                    itemDisplayText = { moodUi -> moodUi.title.asString(context) },
                    onDismiss = {
                        onAction(EchosAction.OnDismissMoodDropdown)
                    },
                    key = { moodUi ->  moodUi.title.asString(context) },
                    onItemClick = { moodUi -> onAction(EchosAction.OnFilterByMoodClick(moodUi.item)) },
                    dropDownOffset = dropDownOffset,
                    maxDropDownHeight = dropDownMaxHeight,
                    leadingIcon = { moodUi ->
                        Image(
                            imageVector = ImageVector.vectorResource(moodUi.iconSet.fill),
                            contentDescription = moodUi.title.asString(),
                        )
                    }
                )
            }
        )

        MultiChoiceChip(
            displayText = topicChipTitle.asString(),
            onClick = {
                onAction(EchosAction.OnTopicChipClick)
            },
            isClearVisible = hasActiveTopicFilters,
            isDropDownVisible = selectedEchoFilterChip == EchoFilterChip.TOPICS,
            isHighlighted = hasActiveTopicFilters || selectedEchoFilterChip == EchoFilterChip.TOPICS,
            onClearButtonClick = { onAction(EchosAction.OnRemoveFilters(EchoFilterChip.TOPICS)) },
            dropDownMenu = {
                if (topics.isEmpty()) {
                    SelectableDropDownOptionsMenu(
                        items = listOf(Selectable(
                            item = stringResource(R.string.you_don_t_have_any_topics_yet),
                            selected = false
                        )),
                        itemDisplayText = { it },
                        onDismiss = {
                            onAction(EchosAction.OnDismissTopicDropdown)
                        },
                        key = { it },
                        onItemClick = {},
                        dropDownOffset = dropDownOffset,
                        maxDropDownHeight = dropDownMaxHeight
                    )
                } else {
                    SelectableDropDownOptionsMenu(
                        items = topics,
                        itemDisplayText = { topic -> topic },
                        onDismiss = { onAction(EchosAction.OnDismissTopicDropdown) },
                        key = { topic -> topic },
                        onItemClick = { topic -> onAction(EchosAction.OnFilterByTopicClick(topic.item)) },
                        dropDownOffset = dropDownOffset,
                        maxDropDownHeight = dropDownMaxHeight,
                        leadingIcon = { topic ->
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.hashtag),
                                contentDescription = topic,
                            )
                        }
                    )
                }
            }
        )
    }
}