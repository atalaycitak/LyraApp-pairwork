package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.library.LibraryRepository
import com.turkcell.lyraapp.data.library.MockLibraryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Library feature'inin repository baglamalari.
 *
 * Backend hazir olmadigindan [LibraryRepository], MOCK implementasyona
 * ([MockLibraryRepository]) baglanir. Gercek API geldiginde yalnizca bu baglamanin
 * hedefi degistirilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LibraryModule {

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: MockLibraryRepository): LibraryRepository
}
