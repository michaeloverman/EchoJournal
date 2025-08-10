package com.plcoding.echojournal.echos.presentation.echos

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plcoding.echojournal.R
import com.plcoding.echojournal.core.presentation.designsystem.theme.EchoJournalTheme
import com.plcoding.echojournal.core.presentation.designsystem.theme.bgGradient
import com.plcoding.echojournal.core.presentation.util.ObserveAsEvents
import com.plcoding.echojournal.core.presentation.util.isAppInForeground
import com.plcoding.echojournal.echos.domain.recording.RecordingDetails
import com.plcoding.echojournal.echos.presentation.echos.components.EchoFilterRow
import com.plcoding.echojournal.echos.presentation.echos.components.EchoList
import com.plcoding.echojournal.echos.presentation.echos.components.EchoQuickRecordFloatingActionButton
import com.plcoding.echojournal.echos.presentation.echos.components.EchoRecordingSheet
import com.plcoding.echojournal.echos.presentation.echos.components.EchosEmptyBackground
import com.plcoding.echojournal.echos.presentation.echos.components.EchosTopBar
import com.plcoding.echojournal.echos.presentation.echos.models.RecordingState
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun EchosRoot(
    onNavigateToCreateEcho: (RecordingDetails) -> Unit,
    viewModel: EchosViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onAction(EchosAction.OnAudioPermissionGranted)
        }
    }

    val context = LocalContext.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            EchosEvent.RequestAudioPermission -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            EchosEvent.RecordingTooShort -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.audio_recording_was_too_short),
                    Toast.LENGTH_LONG
                ).show()
            }

            is EchosEvent.OnRecordingDone -> {
                onNavigateToCreateEcho(event.details)
            }
        }
    }

    val isAppInForeground by isAppInForeground()

    LaunchedEffect(isAppInForeground, state.recordingState) {
        if (state.recordingState == RecordingState.NORMAL_CAPTURE && !isAppInForeground) {
            viewModel.onAction(EchosAction.OnPauseRecordingClick)
        }
    }

    EchosScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun EchosScreen(
    state: EchosState,
    onAction: (EchosAction) -> Unit,
) {
    Scaffold(
        topBar = {
            EchosTopBar(
                onSettingsClick = { onAction(EchosAction.OnSettingsClick) }
            )
        },
        floatingActionButton = {
            EchoQuickRecordFloatingActionButton(
                isQuickRecording = state.recordingState == RecordingState.QUICK_CAPTURE,
                onClick = { onAction(EchosAction.OnRecordFabClick) },
                onLongPressStart = { onAction(EchosAction.OnRecordFabLongClick) },
                onLongPressEnd = { isCancelled ->
                    if (isCancelled) onAction(EchosAction.OnCancelRecording)
                    else onAction(EchosAction.OnCompleteRecordingClick)
                },
                modifier = Modifier,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = MaterialTheme.colorScheme.bgGradient)
                .padding(innerPadding)
        ) {
            EchoFilterRow(
                moodChipContent = state.moodChipContent,
                hasActiveMoodFilters = state.hasActiveMoodFilters,
                selectedEchoFilterChip = state.selectedEchoFilterChip,
                moods = state.moods,
                topicChipTitle = state.topicChipTitle,
                hasActiveTopicFilters = state.hasActiveTopicFilters,
                topics = state.topics,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth()
            )
            when {
                state.isLoadingData -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .wrapContentSize(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                !state.hasEchosRecorded -> {
                    EchosEmptyBackground(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }

                else -> {
                    EchoList(
                        sections = state.sections,
                        onPlayClick = {
                            onAction(EchosAction.OnEchoPlayClick(it))
                        },
                        onPauseClick = {
                            onAction(EchosAction.OnPauseAudioClick)
                        },
                        onTrackSizeAvailable = {
                            onAction(EchosAction.OnTrackSizeAvailable(it))
                        },
                    )
                }
            }
        }

        if (state.recordingState in listOf(RecordingState.PAUSED, RecordingState.NORMAL_CAPTURE)) {
            EchoRecordingSheet(
                formattedRecordDuration = state.formattedRecordDuration,
                isRecording = state.recordingState == RecordingState.NORMAL_CAPTURE,
                onDismiss = { onAction(EchosAction.OnCancelRecording) },
                onPauseClick = { onAction(EchosAction.OnPauseRecordingClick) },
                onResumeClick = { onAction(EchosAction.OnResumeRecordingClick) },
                onCompleteRecording = { onAction(EchosAction.OnCompleteRecordingClick) }
            )
        }
    }
}

@Preview
@Composable
private fun EchosPreview() {
    EchoJournalTheme {
        EchosScreen(
            state = EchosState(
                hasEchosRecorded = false
            ),
            onAction = {}
        )
    }
}