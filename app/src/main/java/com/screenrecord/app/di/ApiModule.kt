package com.screenrecord.app.di

import android.content.Context
import com.screenrecord.app.utils.PREFERENCE
import com.screenrecord.app.utils.PreferenceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provides remote APIs dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Singleton
    @Provides
    @Named(PREFERENCE)
    fun providePreference(@ApplicationContext context: Context): PreferenceProvider {
        return PreferenceProvider(context)
    }
}
