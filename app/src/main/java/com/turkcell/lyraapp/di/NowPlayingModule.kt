package com.turkcell.lyraapp.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * NowPlaying feature'inin bagimlilik baglamalari.
 *
 * [ExoPlayer] Singleton olarak ApplicationContext ile olusturulur; ViewModel
 * dogrudan Context tutmaz, bu sayede mvi-viewmodel-rules.md §3.6 cignenmez.
 *
 * AudioAttributes ile muzik oynatma kategorisinde ses odagi (audio focus) yonetimi
 * saglanir. handleAudioBecomingNoisy kulaklik cikarildiginda muzigi durdurur.
 * WakeMode arka planda CPU'nun uyku moduna gecmesini engeller.
 */
@Module
@InstallIn(SingletonComponent::class)
object NowPlayingModule {

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer =
        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
}
