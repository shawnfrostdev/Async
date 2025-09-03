package app.async.extensions.di

import android.app.Application
import android.content.Context
import app.async.extensions.service.ExtensionService

/**
 * Simple dependency injection module for extensions
 */
object ExtensionModule {
    private var applicationContext: Context? = null
    
    private val _extensionService: ExtensionService by lazy {
        ExtensionService(requireNotNull(applicationContext) { "ExtensionModule not initialized" })
    }
    
    /**
     * Initialize the extension module with application context
     */
    fun initialize(context: Context) {
        // Ensure we only store Application context to prevent memory leaks
        applicationContext = context.applicationContext
    }
    
    /**
     * Get the extension service instance
     */
    fun getExtensionService(): ExtensionService {
        return _extensionService
    }
} 
