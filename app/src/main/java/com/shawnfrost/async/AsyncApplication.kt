package com.shawnfrost.async

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AsyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize components here
    }
} 