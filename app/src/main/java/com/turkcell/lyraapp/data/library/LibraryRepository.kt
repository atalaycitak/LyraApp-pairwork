package com.turkcell.lyraapp.data.library

/**
 * Kutuphane iceriginin veri kaynagi soyutlamasi.
 *
 * Backend REST API sozlesmesi henuz hazir olmadigindan gecici implementasyon
 * [MockLibraryRepository]'dir. Gercek API geldiginde yalnizca implementasyon ve
 * `di/LibraryModule.kt` baglamasi degisir.
 */
interface LibraryRepository {

    /** Kutuphane ekraninda gosterilecek tum statik beslemeyi dondurur. */
    suspend fun getLibraryFeed(): Result<LibraryFeed>
}
