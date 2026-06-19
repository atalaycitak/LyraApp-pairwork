package com.turkcell.lyraapp.data.favorites

/**
 * Favorites iceriginin veri kaynagi soyutlamasi.
 *
 * Backend REST API sozlesmesi henuz hazir olmadigindan gecici implementasyon
 * Aktif implementasyon [RetrofitFavoritesRepository]'dir. API'da favoriler icin ayri endpoint
 * olmadigindan feed `/api/v1/songs` yanitindan turetilir.
 */
interface FavoritesRepository {

    /** Favorites ekraninda gosterilecek favori feed'ini dondurur. */
    suspend fun getFavoritesFeed(): Result<FavoritesFeed>
}
