package app.async.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.LocalNavigator
import app.async.app.navigation.HomeScreen
import app.async.app.navigation.DefaultNavigatorScreenTransition
import app.async.app.ui.theme.AsyncTheme
import logcat.logcat

// @AndroidEntryPoint - Temporarily disabled due to missing Hilt setup
@UnstableApi
class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        logcat { "Permissions result: $permissions" }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        logcat { "MainActivity created" }
        
        // Request permissions
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (!allGranted) {
            requestPermissionLauncher.launch(permissions)
        }
        
        setContent {
            AsyncTheme {
                Navigator(
                    screen = HomeScreen,
                    disposeBehavior = NavigatorDisposeBehavior(
                        disposeNestedNavigators = false,
                        disposeSteps = true
                    ),
                ) { navigator ->
                    CompositionLocalProvider(
                        LocalNavigator provides navigator
                    ) {
                        Scaffold(
                            contentWindowInsets = WindowInsets(0)
                        ) { contentPadding ->
                            Box(modifier = Modifier.padding(contentPadding)) {
                                // Use Voyager's SlideTransition for screen navigation
                                DefaultNavigatorScreenTransition(navigator = navigator)
                            }
                        }
                    }
                }
            }
        }
    }
}

