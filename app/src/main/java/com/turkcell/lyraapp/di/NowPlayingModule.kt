package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.nowplaying.MockNowPlayingRepository
import com.turkcell.lyraapp.data.nowplaying.NowPlayingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * NowPlaying feature'ının repository bağlamaları.
 *
 * Backend hazır olmadığından [NowPlayingRepository], MOCK implementasyona
 * ([MockNowPlayingRepository]) bağlanır. Gerçek API geldiğinde yalnızca bu bağlamanın
 * hedefi değiştirilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NowPlayingModule {

    @Binds
    @Singleton
    abstract fun bindNowPlayingRepository(impl: MockNowPlayingRepository): NowPlayingRepository
}
