package com.example.async.ui.animation

import android.content.Context
import com.example.async.ui.animation.config.AnimationConfig
import com.example.async.ui.animation.config.AnimationConfigProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing animation-related dependencies throughout the Async music player app.
 */
@Module
@InstallIn(SingletonComponent::class)
object AnimationModule {
    
    /**
     * Provides a singleton AnimationConfigProvider that reads from system settings
     * and manages user preferences for animations throughout the app.
     */
    @Provides
    @Singleton
    fun provideAnimationConfigProvider(
        @ApplicationContext context: Context
    ): AnimationConfigProvider {
        return AnimationConfigProvider(context)
    }
    
    /**
     * Provides the current AnimationConfig from the provider.
     * This will be updated reactively when system settings change or user preferences are modified.
     */
    @Provides
    fun provideAnimationConfig(
        configProvider: AnimationConfigProvider
    ): AnimationConfig {
        return configProvider.getCurrentConfig()
    }
} 