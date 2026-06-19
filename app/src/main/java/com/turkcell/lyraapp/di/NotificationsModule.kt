package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.notifications.MockNotificationsRepository
import com.turkcell.lyraapp.data.notifications.NotificationsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(
        impl: MockNotificationsRepository,
    ): NotificationsRepository
}
