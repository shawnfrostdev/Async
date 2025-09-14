package app.async.app

import android.app.Application
import androidx.media3.common.util.UnstableApi
import app.async.app.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.logcat

@UnstableApi
class AsyncApplication : Application() {
    
    // Application-level coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging
        AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = LogPriority.VERBOSE)
        
        logcat { "AsyncApplication: onCreate started" }
        
        try {
            // Initialize dependency injection
            AppModule.initialize(this)
            logcat { "AsyncApplication: AppModule initialized successfully" }
            
            // Note: Extension initialization moved to be lazy/on-demand
            // to prevent blocking app startup
            
        } catch (e: Exception) {
            logcat { "AsyncApplication: Failed during initialization: ${e.message}" }
            e.printStackTrace()
            // Don't rethrow - allow app to continue with limited functionality
        }
    }
} 
