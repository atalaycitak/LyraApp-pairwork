package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.playlist.MockPlaylistRepository
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaylistModule {

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        impl: MockPlaylistRepository
    ): PlaylistRepository
}
