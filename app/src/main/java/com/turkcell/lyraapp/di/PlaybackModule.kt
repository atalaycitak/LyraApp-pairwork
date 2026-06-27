package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.playback.PlaybackRepository
import com.turkcell.lyraapp.data.playback.RetrofitPlaybackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Playback feature'inin repository baglamalari.
 *
 * [PlaybackRepository] -> [RetrofitPlaybackRepository] baglamasi yapilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlaybackModule {

    @Binds
    @Singleton
    abstract fun bindPlaybackRepository(impl: RetrofitPlaybackRepository): PlaybackRepository
}
