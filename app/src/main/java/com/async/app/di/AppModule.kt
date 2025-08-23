package com.async.app.di

import android.content.Context
import com.async.extensions.di.ExtensionModule
import com.async.extensions.service.ExtensionService

/**
 * Main dependency injection module for the app
 */
object AppModule {
    private lateinit var context: Context
    
    /**
     * Initialize the app module with application context
     */
    fun initialize(applicationContext: Context) {
        context = applicationContext
        
        // Initialize extension module
        ExtensionModule.initialize(applicationContext)
    }
    
    /**
     * Get the extension service
     */
    fun getExtensionService(): ExtensionService {
        return ExtensionModule.getExtensionService()
    }
} 