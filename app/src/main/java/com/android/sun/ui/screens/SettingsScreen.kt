package com.android.sun.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.android.sun.BuildConfig
import com.android.sun.ui.components.GradientNavigationBar

/**
 * Ecran Settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    isFullMoonNotification: Boolean,
    onFullMoonNotificationChange: (Boolean) -> Unit,
    isTripuraSundariNotification: Boolean,
    onTripuraSundariNotificationChange: (Boolean) -> Unit,
    isNewMoonNotification: Boolean,
    onNewMoonNotificationChange: (Boolean) -> Unit,
	isTattvaNotification:  Boolean,                    
    onTattvaNotificationChange:  (Boolean) -> Unit,    
    isPlanetaryHourNotification: Boolean,
    onPlanetaryHourNotificationChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) 
{
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // SECTIUNEA:  APARENTA
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Dark Theme Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.Star,
                    title = "Dark Theme",
                    subtitle = "Enable dark mode for better visibility at night",
                    checked = isDarkTheme,
                    onCheckedChange = onDarkThemeChange
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // SECTIUNEA: NOTIFICARI
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                if (! hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notification permission required",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Tap to enable notifications",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                            }
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    }
                                }
                            ) {
                                Text("Enable")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Combined Notification Card - Full Moon, Tripura Sundari, New Moon
                NotificationGroupCard(
                    title = "Notification",
                    items = listOf(
                        NotificationItem("Full Moon", isFullMoonNotification) { enabled ->
                            if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onFullMoonNotificationChange(enabled)
                            }
                        },
                        NotificationItem("Tripura Sundari", isTripuraSundariNotification) { enabled ->
                            if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onTripuraSundariNotificationChange(enabled)
                            }
                        },
                        NotificationItem("New Moon", isNewMoonNotification) { enabled ->
                            if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onNewMoonNotificationChange(enabled)
                            }
                        }
                    ),
                    enabled = hasNotificationPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                )
                
                
				
				// Combined Status Bar Card - Tattva and Planetary Hour
				NotificationGroupCard(
					title = "Status Bar",
					items = listOf(
						NotificationItem("Tattva", isTattvaNotification) { enabled ->
							onTattvaNotificationChange(enabled)
						},
						NotificationItem("Planetary Hour", isPlanetaryHourNotification) { enabled ->
							onPlanetaryHourNotificationChange(enabled)
						}
					),
					enabled = true
				)
				
				HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // SECTIUNEA: DESPRE
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Versiune
                Card(
					shape = RoundedCornerShape(7.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Version",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "SUN TIME",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
				
				// Spatiu pentru gradient navigation bar
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        
        // Gradient navigation bar
        GradientNavigationBar(
            isDarkTheme = isDarkTheme,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(7.dp),
		colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

/**
 * Data class for notification items in grouped card
 */
data class NotificationItem(
    val title: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

/**
 * Grouped notification card with horizontal checkboxes
 */
@Composable
private fun NotificationGroupCard(
    title: String,
    items: List<NotificationItem>,
    enabled: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(7.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card title
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Horizontal row with checkboxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (enabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = item.onCheckedChange,
                            enabled = enabled
                        )
                    }
                }
            }
        }
    }
}