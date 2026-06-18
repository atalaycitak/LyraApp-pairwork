package com.turkcell.lyraapp.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * NowPlaying feature'ının bağımlılık bağlamaları.
 *
 * [ExoPlayer] Singleton olarak ApplicationContext ile oluşturulur; ViewModel
 * doğrudan Context tutmaz, bu sayede mvi-viewmodel-rules.md §3.6 çiğnenmez.
 */
@Module
@InstallIn(SingletonComponent::class)
object NowPlayingModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context).build()
}

