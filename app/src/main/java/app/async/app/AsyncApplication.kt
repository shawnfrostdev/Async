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
            logcat { "AsyncApplication: AppModule initialized" }
            
            // Initialize extension system asynchronously
            applicationScope.launch {
                try {
                    logcat { "AsyncApplication: Starting extension service initialization" }
                    val extensionService = AppModule.getExtensionService()
                    logcat { "AsyncApplication: Got extension service instance" }
                    extensionService.initialize()
                    logcat { "AsyncApplication: Extension service initialized successfully" }
                } catch (e: Exception) {
                    logcat { "AsyncApplication: Failed to initialize extension service: ${e.message}" }
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            logcat { "AsyncApplication: Failed during initialization: ${e.message}" }
            e.printStackTrace()
        }
    }
} 
