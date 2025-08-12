package com.plcoding.echojournal.echos.di

import com.plcoding.echojournal.echos.data.recording.AndroidVoiceRecorder
import com.plcoding.echojournal.echos.domain.recording.VoiceRecorder
import com.plcoding.echojournal.echos.presentation.create_echo.CreateEchoViewModel
import com.plcoding.echojournal.echos.presentation.echos.EchosViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val echosModule = module {
    single {
        AndroidVoiceRecorder(
            context = androidApplication(),
            applicationScope = get()
        )
    } bind VoiceRecorder::class

    viewModelOf(::EchosViewModel)
    viewModelOf(::CreateEchoViewModel)
}