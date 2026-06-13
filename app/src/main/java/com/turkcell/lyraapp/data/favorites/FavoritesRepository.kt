package com.turkcell.lyraapp.data.favorites

/**
 * Favorites iceriginin veri kaynagi soyutlamasi.
 *
 * Backend REST API sozlesmesi henuz hazir olmadigindan gecici implementasyon
 * [MockFavoritesRepository]'dir. Gercek API geldiginde yalnizca implementasyon ve
 * `di/FavoritesModule.kt` baglamasi degisir.
 */
interface FavoritesRepository {

    /** Favorites ekraninda gosterilecek favori feed'ini dondurur. */
    suspend fun getFavoritesFeed(): Result<FavoritesFeed>
}
