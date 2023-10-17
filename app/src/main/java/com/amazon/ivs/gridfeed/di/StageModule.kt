package com.amazon.ivs.gridfeed.di

import android.content.Context
import com.amazon.ivs.gridfeed.repository.GridFeedRepository
import com.amazon.ivs.gridfeed.repository.GridFeedRepositoryImpl
import com.amazon.ivs.gridfeed.repository.appSettingsStore
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
    fun provideAppSettingsStore(@ApplicationContext context: Context) = context.appSettingsStore

    @Provides
    @Singleton
    fun provideStageRepository(repository: GridFeedRepositoryImpl): GridFeedRepository = repository
}
