package com.amazon.ivs.gridfeed.di

import android.content.Context
import com.amazon.ivs.gridfeed.repository.PreferenceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StageModule {
    @Provides
    @Singleton
    fun providePreferenceProvider(@ApplicationContext context: Context) = PreferenceProvider(context)
}
