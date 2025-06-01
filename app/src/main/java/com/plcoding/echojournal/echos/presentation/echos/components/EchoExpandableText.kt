package com.plcoding.echojournal.echos.presentation.echos.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.plcoding.echojournal.R
import com.plcoding.echojournal.core.presentation.designsystem.theme.EchoJournalTheme

@Composable
fun EchoExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLine: Int = 3
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isClickable by remember { mutableStateOf(false) }
    var lastCharacterIndex by remember { mutableIntStateOf(0) }
    val showMoreText = stringResource(R.string.show_more)
    val primary = MaterialTheme.colorScheme.primary

    val textToShow = remember(isClickable, isExpanded) {
        buildAnnotatedString {
            when {
                isClickable && !isExpanded -> {
                    val adjustedText = text
                        .substring(0, lastCharacterIndex)
                        .dropLast(showMoreText.length + 3)
                        .dropLastWhile {
                            Character.isWhitespace(it) || it == '.'
                        }
                    append(adjustedText)
                    append("...")

                    withStyle(
                        style = SpanStyle(
                            color = primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(showMoreText)
                    }
                }
                else -> append(text)
            }
        }
    }

    Text(
        text = textToShow,
        maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLine,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isClickable,
                interactionSource = null,
                indication = null
            ) {
                if (isClickable) {
                    isExpanded = !isExpanded
                }
            }
            .animateContentSize(),
        onTextLayout = { result ->
            if (!isExpanded && result.hasVisualOverflow) {
                isClickable = true
                lastCharacterIndex = result.getLineEnd(lineIndex = collapsedMaxLine - 1)

            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun EchoExpandableTextPreview() {
    EchoJournalTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            EchoExpandableText(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            )
        }
    }
}