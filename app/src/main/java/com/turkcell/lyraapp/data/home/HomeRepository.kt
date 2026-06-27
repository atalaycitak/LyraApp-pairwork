package com.turkcell.lyraapp.data.home

/**
 * Ana sayfa iceriginin veri kaynagi soyutlamasi.
 *
 * [RetrofitHomeRepository] bu interface'i implement eder;
 * gercek API verisi kullanilir.
 */
interface HomeRepository {

    /** Ana sayfa beslemesinin tamamini tek seferde dondurur. */
    suspend fun getHomeFeed(): Result<HomeFeed>
}

