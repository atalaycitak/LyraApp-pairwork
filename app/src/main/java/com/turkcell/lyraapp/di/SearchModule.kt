package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.search.RetrofitSearchRepository
import com.turkcell.lyraapp.data.search.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Search feature'ının repository bağlamaları.
 *
 * [SearchRepository], `/api/v1/songs` uzerinden arama yapan [RetrofitSearchRepository]
 * implementasyonuna baglanir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: RetrofitSearchRepository): SearchRepository
}
