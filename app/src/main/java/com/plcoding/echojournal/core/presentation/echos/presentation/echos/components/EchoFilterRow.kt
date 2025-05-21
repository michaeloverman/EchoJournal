package com.plcoding.echojournal.core.presentation.echos.presentation.echos.components

import android.adservices.topics.Topic
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plcoding.echojournal.core.presentation.designsystem.chips.MultiChoiceChip
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.Selectable
import com.plcoding.echojournal.core.presentation.designsystem.dropdowns.SelectableDropDownOptionsMenu
import com.plcoding.echojournal.core.presentation.designsystem.theme.EchoJournalTheme
import com.plcoding.echojournal.core.presentation.echos.models.MoodUi
import com.plcoding.echojournal.core.presentation.echos.presentation.echos.EchosAction
import com.plcoding.echojournal.core.presentation.echos.presentation.echos.models.EchoFilterChip
import com.plcoding.echojournal.core.presentation.echos.presentation.echos.models.MoodChipContent

@Composable
fun EchoFilterRow(
    moodChipContent: MoodChipContent,
    hasActiveMoodFilters: Boolean,
    selectedEchoFilterChip: EchoFilterChip?,
    moods: List<Selectable<MoodUi>>,
    hasActiveTopicFilters: Boolean,
    topics: List<Selectable<Topic>>,
    onAction: (EchosAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    FlowRow(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MultiChoiceChip(
            displayText = moodChipContent.title.toString(),
            onClick = { onAction(EchosAction.OnMoodChipClick) },
            leadingContent = {
                if (moodChipContent.iconRes.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-4).dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        moodChipContent.iconRes.forEach { icon ->
                            Image(
                                imageVector = ImageVector.vectorResource(icon),
                                contentDescription = null
                            )
                        }
                    }
                }
            },
            isClearVisible = hasActiveMoodFilters,
            isDropDownVisible = selectedEchoFilterChip == EchoFilterChip.MOODS,
            isHighlighted = hasActiveTopicFilters || selectedEchoFilterChip == EchoFilterChip.MOODS,
            onClearButtonClick = { onAction(EchosAction.OnRemoveFilters(EchoFilterChip.MOODS)) },
            dropDownMenu = {
                SelectableDropDownOptionsMenu(
                    items = moods,
                    itemDisplayText = { moodUi -> moodUi.title.toString(context) },
                    onDismiss = { onAction() },
                    onItemClick = { onAction(EchosAction.) }
                )
            }
        )
    }
}

@Preview
@Composable
private fun EchoFilterRowPreview() {
    EchoJournalTheme {
        EchoFilterRow()
    }
}