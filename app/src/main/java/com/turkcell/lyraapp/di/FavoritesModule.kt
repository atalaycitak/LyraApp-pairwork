package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import com.turkcell.lyraapp.data.favorites.MockFavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Favorites feature'inin repository baglamalari.
 *
 * Backend hazir olmadigindan [FavoritesRepository], MOCK implementasyona
 * ([MockFavoritesRepository]) baglanir. Gercek API geldiginde yalnizca bu baglamanin
 * hedefi degistirilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesModule {

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: MockFavoritesRepository): FavoritesRepository
}
