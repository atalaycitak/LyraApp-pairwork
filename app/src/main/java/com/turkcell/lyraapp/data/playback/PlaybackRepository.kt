package com.turkcell.lyraapp.data.playback

/**
 * Playback veri kaynagi soyutlamasi.
 *
 * [RetrofitPlaybackRepository] bu interface'i implement eder;
 * DI baglamasi [di/PlaybackModule.kt] uzerinden yapilir.
 */
interface PlaybackRepository {

    /**
     * Bir sarki icin siradaki oynatma ogelerini cozumler.
     *
     * Premium kullanicilar dogrudan [PlaybackResult.SongPlayback] alir.
     * Ucretsiz kullanicilar her 3 sarkida bir [PlaybackResult.AdPlayback] alir.
     *
     * Bu metot ayni zamanda sunucu tarafinda dinleme kaydini da olusturur;
     * ayrica recordPlay() cagirilmamalidir.
     */
    suspend fun getNextPlayback(songId: String): Result<PlaybackResult>

    /**
     * Reklamin basariyla izlendigini sunucuya bildirir.
     *
     * [impressionId]: Onceki [getNextPlayback] yanitindaki AdPlayback.impressionId.
     * Basarili: true doner.
     */
    suspend fun markAdComplete(impressionId: String): Result<Boolean>
}
