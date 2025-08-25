package com.plcoding.echojournal.echos.di

import com.plcoding.echojournal.echos.data.recording.AndroidVoiceRecorder
import com.plcoding.echojournal.echos.data.recording.InternalRecordingStorage
import com.plcoding.echojournal.echos.domain.recording.RecordingStorage
import com.plcoding.echojournal.echos.domain.recording.VoiceRecorder
import com.plcoding.echojournal.echos.presentation.create_echo.CreateEchoViewModel
import com.plcoding.echojournal.echos.presentation.echos.EchosViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val echosModule = module {
    singleOf(::AndroidVoiceRecorder) bind VoiceRecorder::class
    singleOf(::InternalRecordingStorage) bind RecordingStorage::class

    viewModelOf(::EchosViewModel)
    viewModelOf(::CreateEchoViewModel)
}