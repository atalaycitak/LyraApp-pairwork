package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.song.RetrofitSongRepository
import com.turkcell.lyraapp.data.song.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Song feature'ının repository bağlamaları.
 *
 * [SongRepository] → [RetrofitSongRepository] bağlaması yapılır.
 * Gerçek API implementasyonu değiştiğinde yalnızca bu bağlamanın hedefi değiştirilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SongModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: RetrofitSongRepository): SongRepository
}
