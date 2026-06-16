package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.home.HomeRepository
import com.turkcell.lyraapp.data.home.RetrofitHomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Home feature'ının repository bağlamaları.
 *
 * [HomeRepository] artık [RetrofitHomeRepository]'ye bağlıdır; gerçek API verisi kullanılır.
 * Mock implementasyon ([MockHomeRepository]) ağ bağımsız test senaryoları için korunmaktadır.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: RetrofitHomeRepository): HomeRepository
}

