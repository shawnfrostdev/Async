package com.shawnfrost.async

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.shawnfrost.async.ui.theme.AsyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AsyncTheme {
                Surface(color = MaterialTheme.colors.background) {
                    // Main content will be added here
                }
            }
        }
    }
} 