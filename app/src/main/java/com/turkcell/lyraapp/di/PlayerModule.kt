package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.player.AudioPlayerManager
import com.turkcell.lyraapp.data.player.PlayerController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindPlayerController(impl: AudioPlayerManager): PlayerController
}
