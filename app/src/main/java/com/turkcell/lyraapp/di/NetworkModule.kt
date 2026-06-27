package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.BuildConfig
import com.turkcell.lyraapp.data.song.SongApiService
import com.turkcell.lyraapp.data.playlist.PlaylistApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Retrofit + OkHttp singleton sağlayıcısı.
 *
 * Base URL canlı ortam sunucusudur (openapi.json servers[0]).
 * Geliştirme sırasında localhost kullanmak için bu modülde veya
 * BuildConfig üzerinden URL'i değiştirmek yeterlidir; başka hiçbir
 * katman değişmez.
 *
 * Günlükleme: Debug build'lerde HTTP istek/yanıt gövdeleri loglanır;
 * release build'lerde günlükleme tamamen kapatılır (bkz. decisions.md §Guvenlik).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://streaming-api.halitkalayci.com/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: com.turkcell.lyraapp.data.common.AuthInterceptor,
        tokenAuthenticator: com.turkcell.lyraapp.data.common.TokenAuthenticator
    ): OkHttpClient =
        OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideSongApiService(retrofit: Retrofit): SongApiService =
        retrofit.create(SongApiService::class.java)

    @Provides
    @Singleton
    fun providePlaylistApiService(retrofit: Retrofit): PlaylistApiService =
        retrofit.create(PlaylistApiService::class.java)

    @Provides
    @Singleton
    fun provideHomeApiService(retrofit: Retrofit): com.turkcell.lyraapp.data.home.HomeApiService =
        retrofit.create(com.turkcell.lyraapp.data.home.HomeApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): com.turkcell.lyraapp.data.auth.AuthApiService =
        retrofit.create(com.turkcell.lyraapp.data.auth.AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): com.turkcell.lyraapp.data.profile.ProfileApiService =
        retrofit.create(com.turkcell.lyraapp.data.profile.ProfileApiService::class.java)

    @Provides
    @Singleton
    fun provideMembershipApiService(retrofit: Retrofit): com.turkcell.lyraapp.data.membership.MembershipApiService =
        retrofit.create(com.turkcell.lyraapp.data.membership.MembershipApiService::class.java)

    @Provides
    @Singleton
    fun providePlaybackApiService(retrofit: Retrofit): com.turkcell.lyraapp.data.playback.PlaybackApiService =
        retrofit.create(com.turkcell.lyraapp.data.playback.PlaybackApiService::class.java)
}
