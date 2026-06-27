package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.membership.MembershipRepository
import com.turkcell.lyraapp.data.membership.RetrofitMembershipRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Membership feature'inin repository baglamalari.
 *
 * [MembershipRepository] -> [RetrofitMembershipRepository] baglamasi yapilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MembershipModule {

    @Binds
    @Singleton
    abstract fun bindMembershipRepository(impl: RetrofitMembershipRepository): MembershipRepository
}
