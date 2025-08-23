package com.async.extensions.di

import android.content.Context
import com.async.extensions.installer.ExtensionInstaller
import com.async.extensions.loader.ExtensionLoader
import com.async.extensions.manager.ExtensionManager
import com.async.extensions.repository.ExtensionRepository
import com.async.extensions.runtime.ExtensionRuntime
import com.async.extensions.security.ExtensionValidator
import com.async.extensions.service.ExtensionService
import com.async.extensions.storage.ExtensionStorage
import javax.inject.Singleton

/**
 * Manual dependency injection module for extension system components.
 * Provides all extension-related dependencies without Hilt.
 */
object ExtensionModule {
    private lateinit var context: Context
    
    // Lazy initialized dependencies
    private val _extensionStorage: ExtensionStorage by lazy {
        ExtensionStorage(context)
    }
    
    private val _extensionValidator: ExtensionValidator by lazy {
        ExtensionValidator(context)
    }
    
    private val _extensionLoader: ExtensionLoader by lazy {
        ExtensionLoader(context)
    }
    
    private val _extensionRepository: ExtensionRepository by lazy {
        ExtensionRepository(context)
    }
    
    private val _extensionInstaller: ExtensionInstaller by lazy {
        ExtensionInstaller(context, _extensionLoader, _extensionValidator, _extensionStorage)
    }
    
    private val _extensionManager: ExtensionManager by lazy {
        ExtensionManager(context, _extensionLoader, _extensionStorage)
    }
    
    private val _extensionRuntime: ExtensionRuntime by lazy {
        ExtensionRuntime(context, _extensionManager, _extensionLoader, _extensionStorage)
    }
    
    private val _extensionService: ExtensionService by lazy {
        ExtensionService(context, _extensionRuntime, _extensionManager, _extensionRepository, _extensionInstaller)
    }
    
    /**
     * Initialize the extension module with application context
     */
    fun initialize(applicationContext: Context) {
        context = applicationContext
    }
    
    // Public accessors for extension system components
    fun getExtensionStorage(): ExtensionStorage = _extensionStorage
    fun getExtensionValidator(): ExtensionValidator = _extensionValidator
    fun getExtensionLoader(): ExtensionLoader = _extensionLoader
    fun getExtensionRepository(): ExtensionRepository = _extensionRepository
    fun getExtensionInstaller(): ExtensionInstaller = _extensionInstaller
    fun getExtensionManager(): ExtensionManager = _extensionManager
    fun getExtensionRuntime(): ExtensionRuntime = _extensionRuntime
    fun getExtensionService(): ExtensionService = _extensionService
} 