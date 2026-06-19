package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.library.LibraryRepository
import com.turkcell.lyraapp.data.library.RetrofitLibraryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Library feature'inin repository baglamalari.
 *
 * [LibraryRepository], streaming API uzerinden sarki listesini ceken
 * [RetrofitLibraryRepository] implementasyonuna baglanir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LibraryModule {

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: RetrofitLibraryRepository): LibraryRepository
}
