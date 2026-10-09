package com.trazavoz.di

import com.trazavoz.ui.audio.GameAudio
import com.trazavoz.ui.audio.TrazavozTtsManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class GameAudioModule {
    @Binds
    abstract fun bindGameAudio(manager: TrazavozTtsManager): GameAudio
}
