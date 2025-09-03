package app.async.app.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import logcat.logcat

data class PermissionRequest(
    val permission: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isRequired: Boolean = true
)

@Composable
fun PermissionManager(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var currentPermissionIndex by remember { mutableIntStateOf(0) }
    var hasCheckedPermissions by remember { mutableStateOf(false) }
    
    // Check if we've already shown permission dialogs before
    val sharedPrefs = remember {
        context.getSharedPreferences("permissions", Context.MODE_PRIVATE)
    }
    val hasShownPermissions = remember {
        sharedPrefs.getBoolean("permissions_shown", false)
    }
    
    // Define all permissions we need
    val permissions = remember {
        buildList {
            // Storage permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(PermissionRequest(
                    permission = Manifest.permission.READ_MEDIA_AUDIO,
                    title = "Media Access",
                    description = "Access your music files to play audio content",
                    icon = Icons.Default.LibraryMusic
                ))
            } else {
                add(PermissionRequest(
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                    title = "Storage Access",
                    description = "Access your device storage to read music files and download extensions",
                    icon = Icons.Default.Storage
                ))
                add(PermissionRequest(
                    permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    title = "Storage Write",
                    description = "Save downloaded files and cache music data",
                    icon = Icons.Default.Save
                ))
            }
            
            // All files access permission (Android 11+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(PermissionRequest(
                    permission = Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    title = "All Files Access",
                    description = "Access all files for extension management and deep music scanning",
                    icon = Icons.Default.FolderOpen
                ))
            }
            
            // Notification permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(PermissionRequest(
                    permission = Manifest.permission.POST_NOTIFICATIONS,
                    title = "Notifications",
                    description = "Show music playback controls and download progress",
                    icon = Icons.Default.Notifications
                ))
            }
            
            // System overlay permission
            add(PermissionRequest(
                permission = Manifest.permission.SYSTEM_ALERT_WINDOW,
                title = "Display Over Other Apps",
                description = "Show mini player and controls over other apps",
                icon = Icons.Default.PictureInPicture,
                isRequired = false
            ))
            
            // Install packages permission
            add(PermissionRequest(
                permission = Manifest.permission.REQUEST_INSTALL_PACKAGES,
                title = "Install Extensions",
                description = "Install extension packages to add new music sources",
                icon = Icons.Default.Extension
            ))
            
            // Battery optimization permission
            add(PermissionRequest(
                permission = Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                title = "Battery Optimization",
                description = "Run in background for uninterrupted music playback",
                icon = Icons.Default.BatteryFull,
                isRequired = false
            ))
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
                 logcat("PermissionManager") { "Permission result for ${permissions[currentPermissionIndex].permission}: $isGranted" }
        
        if (isGranted) {
            // Move to next permission
            currentPermissionIndex++
            if (currentPermissionIndex >= permissions.size) {
                // All permissions processed
                showPermissionDialog = false
                sharedPrefs.edit().putBoolean("permissions_shown", true).apply()
                onPermissionsGranted()
                                } else {
                        // Check if next permission is already granted
                        checkNextPermission(context, permissions, currentPermissionIndex) { shouldShow ->
                            if (shouldShow) {
                                showPermissionDialog = true
                            } else {
                                currentPermissionIndex++
                                if (currentPermissionIndex >= permissions.size) {
                                    showPermissionDialog = false
                                    sharedPrefs.edit().putBoolean("permissions_shown", true).apply()
                                    onPermissionsGranted()
                                }
                            }
                        }
                    }
        } else {
            // Permission denied, show next permission or finish
            currentPermissionIndex++
            if (currentPermissionIndex >= permissions.size) {
                showPermissionDialog = false
                sharedPrefs.edit().putBoolean("permissions_shown", true).apply()
                onPermissionsGranted() // Continue anyway
            } else {
                checkNextPermission(context, permissions, currentPermissionIndex) { shouldShow ->
                    showPermissionDialog = shouldShow
                    if (!shouldShow) {
                        currentPermissionIndex++
                        if (currentPermissionIndex >= permissions.size) {
                            showPermissionDialog = false
                            onPermissionsGranted()
                        }
                    }
                }
            }
        }
    }
    
    // Check permissions on first composition
    LaunchedEffect(Unit) {
        if (!hasCheckedPermissions) {
            hasCheckedPermissions = true
            logcat("PermissionManager") { "Checking permissions on app start" }
            
            // If we've already shown permissions before, skip to main app
            if (hasShownPermissions) {
                logcat("PermissionManager") { "Permissions already shown before, proceeding to app" }
                onPermissionsGranted()
                return@LaunchedEffect
            }
            
            val firstMissingPermission = permissions.indexOfFirst { permissionRequest ->
                !isPermissionGranted(context, permissionRequest.permission)
            }
            
            if (firstMissingPermission != -1) {
                currentPermissionIndex = firstMissingPermission
                showPermissionDialog = true
                logcat("PermissionManager") { "Found missing permission at index $firstMissingPermission" }
            } else {
                logcat("PermissionManager") { "All permissions already granted" }
                // Mark as shown and proceed
                sharedPrefs.edit().putBoolean("permissions_shown", true).apply()
                onPermissionsGranted()
            }
        }
    }
    
    // Show permission dialog
    if (showPermissionDialog && currentPermissionIndex < permissions.size) {
        val currentPermission = permissions[currentPermissionIndex]
        
        PermissionDialog(
            permissionRequest = currentPermission,
            onGrantClick = {
                logcat("PermissionManager") { "Requesting permission: ${currentPermission.permission}" }
                
                // Handle special permissions that need different approaches
                when (currentPermission.permission) {
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to general manage all files settings
                                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                context.startActivity(intent)
                            }
                        }
                        // Move to next permission after launching settings
                        currentPermissionIndex++
                        if (currentPermissionIndex >= permissions.size) {
                            showPermissionDialog = false
                            sharedPrefs.edit().putBoolean("permissions_shown", true).apply()
                            onPermissionsGranted()
                        }
                    }
                    Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            logcat("PermissionManager") { "Failed to open overlay settings: ${e.message}" }
                        }
                        // Move to next permission
                        currentPermissionIndex++
                        if (currentPermissionIndex >= permissions.size) {
                            showPermissionDialog = false
                            sharedPrefs.edit().putBoolean("permissions_shown", true).apply()
                            onPermissionsGranted()
                        }
                    }
                    Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to battery optimization settings
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                        // Move to next permission
                        currentPermissionIndex++
                        if (currentPermissionIndex >= permissions.size) {
                            showPermissionDialog = false
                            sharedPrefs.edit().putBoolean("permissions_shown", true).apply()
                            onPermissionsGranted()
                        }
                    }
                    else -> {
                        // Handle normal runtime permissions
                        permissionLauncher.launch(currentPermission.permission)
                    }
                }
            },
            onSkipClick = {
                logcat("PermissionManager") { "Skipping permission: ${currentPermission.permission}" }
                currentPermissionIndex++
                if (currentPermissionIndex >= permissions.size) {
                    showPermissionDialog = false
                    onPermissionsGranted()
                } else {
                    checkNextPermission(context, permissions, currentPermissionIndex) { shouldShow ->
                        showPermissionDialog = shouldShow
                        if (!shouldShow) {
                            currentPermissionIndex++
                            if (currentPermissionIndex >= permissions.size) {
                                showPermissionDialog = false
                                sharedPrefs.edit().putBoolean("permissions_shown", true).apply()
                                onPermissionsGranted()
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun PermissionDialog(
    permissionRequest: PermissionRequest,
    onGrantClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Don't allow dismissing */ },
        icon = {
            Icon(
                imageVector = permissionRequest.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = permissionRequest.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    text = permissionRequest.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (permissionRequest.isRequired) {
                                "This permission is recommended for the best experience"
                            } else {
                                "This permission is optional but enhances functionality"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onGrantClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onSkipClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for Now")
            }
        }
    )
}

private fun isPermissionGranted(context: Context, permission: String): Boolean {
    return when (permission) {
        Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true // Not needed on older versions
            }
        }
        Manifest.permission.SYSTEM_ALERT_WINDOW -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true // Not needed on older versions
            }
        }
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true // Not needed on older versions
            }
        }
        else -> {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

private fun checkNextPermission(
    context: Context,
    permissions: List<PermissionRequest>,
    index: Int,
    callback: (Boolean) -> Unit
) {
    if (index >= permissions.size) {
        callback(false)
        return
    }
    
    val isGranted = isPermissionGranted(context, permissions[index].permission)
    callback(!isGranted)
} 
