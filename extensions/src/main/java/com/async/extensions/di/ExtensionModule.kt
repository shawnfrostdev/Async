package com.async.extensions.di

import android.content.Context
import com.async.extensions.service.ExtensionService

/**
 * Simple dependency injection module for extensions
 */
object ExtensionModule {
    private lateinit var context: Context
    
    private val _extensionService: ExtensionService by lazy {
        ExtensionService(context)
    }
    
    /**
     * Initialize the extension module with application context
     */
    fun initialize(applicationContext: Context) {
        context = applicationContext
    }
    
    /**
     * Get the extension service instance
     */
    fun getExtensionService(): ExtensionService {
        return _extensionService
    }
} 