package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import com.turkcell.lyraapp.data.favorites.RetrofitFavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Favorites feature'inin repository baglamalari.
 *
 * API'da favoriler icin ayri endpoint olmadigindan [FavoritesRepository],
 * `/api/v1/songs` yanitindan favori sarki feed'i tureten [RetrofitFavoritesRepository]
 * implementasyonuna baglanir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesModule {

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: RetrofitFavoritesRepository): FavoritesRepository
}
