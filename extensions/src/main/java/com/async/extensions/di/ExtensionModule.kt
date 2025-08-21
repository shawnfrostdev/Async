package com.async.extensions.di

import android.content.Context
import com.async.extensions.loader.ExtensionLoader
import com.async.extensions.manager.ExtensionManager
import com.async.extensions.security.ExtensionValidator
import com.async.extensions.storage.ExtensionStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing dependencies for the extension system.
 */
@Module
@InstallIn(SingletonComponent::class)
object ExtensionModule {
    
    @Provides
    @Singleton
    fun provideExtensionStorage(
        @ApplicationContext context: Context
    ): ExtensionStorage {
        return ExtensionStorage(context)
    }
    
    @Provides
    @Singleton
    fun provideExtensionValidator(
        @ApplicationContext context: Context
    ): ExtensionValidator {
        return ExtensionValidator(context)
    }
    
    @Provides
    @Singleton
    fun provideExtensionLoader(
        @ApplicationContext context: Context
    ): ExtensionLoader {
        return ExtensionLoader(context)
    }
    
    @Provides
    @Singleton
    fun provideExtensionManager(
        @ApplicationContext context: Context,
        extensionLoader: ExtensionLoader,
        extensionStorage: ExtensionStorage
    ): ExtensionManager {
        return ExtensionManager(context, extensionLoader, extensionStorage)
    }
} 