package com.android.sun.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.android.sun.BuildConfig
import com.android.sun.R
import com.android.sun.ui.components.GradientNavigationBar
import com.android.sun.ui.components.getTattvaIconRes
import com.android.sun.util.TattvaColors

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
    // Tattva Sound per-tattva toggles
    isSoundAkasha: Boolean,
    onSoundAkashaChange: (Boolean) -> Unit,
    isSoundVayu: Boolean,
    onSoundVayuChange: (Boolean) -> Unit,
    isSoundTejas: Boolean,
    onSoundTejasChange: (Boolean) -> Unit,
    isSoundApas: Boolean,
    onSoundApasChange: (Boolean) -> Unit,
    isSoundPrithivi: Boolean,
    onSoundPrithiviChange: (Boolean) -> Unit,
    soundVolume: Float,
    onSoundVolumeChange: (Float) -> Unit,
    customSoundUris: Map<String, String?> = emptyMap(),
    onCustomSoundUriChange: (String, String?) -> Unit = { _, _ -> },
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) 
{
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // State for the full-screen tattva sound picker overlay: (tattvaName, tattvaCode)
    var tattvaPickerFor by remember { mutableStateOf<Pair<String, String>?>(null) }
    
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
                            text = stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
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
                    text = stringResource(R.string.section_appearance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Dark Theme Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.dark_theme_title),
                    subtitle = stringResource(R.string.dark_theme_subtitle),
                    checked = isDarkTheme,
                    onCheckedChange = onDarkThemeChange
                )

                // Language Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.language_title),
                    subtitle = stringResource(R.string.language_subtitle),
                    checked = currentLanguage == "en",
                    onCheckedChange = { isEnglish ->
                        onLanguageChange(if (isEnglish) "en" else "ro")
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // SECTIUNEA: NOTIFICARI
                Text(
                    text = stringResource(R.string.section_notifications),
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
                                    text = stringResource(R.string.notification_permission_required),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = stringResource(R.string.tap_to_enable_notifications),
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
                                Text(stringResource(R.string.enable))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Combined Notification Card - Full Moon, Tripura Sundari, New Moon
                NotificationGroupCard(
                    title = stringResource(R.string.notification_title),
                    items = listOf(
                        NotificationItem(stringResource(R.string.full_moon), isFullMoonNotification) { enabled ->
                            if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onFullMoonNotificationChange(enabled)
                            }
                        },
                        NotificationItem(stringResource(R.string.tripura_sundari), isTripuraSundariNotification) { enabled ->
                            if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onTripuraSundariNotificationChange(enabled)
                            }
                        },
                        NotificationItem(stringResource(R.string.new_moon), isNewMoonNotification) { enabled ->
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
					title = stringResource(R.string.status_bar),
					items = listOf(
						NotificationItem(stringResource(R.string.tattva_label), isTattvaNotification) { enabled ->
							onTattvaNotificationChange(enabled)
							context.sendBroadcast(android.content.Intent("com.android.sun.ACTION_SETTINGS_CHANGED"))
						},
						NotificationItem(stringResource(R.string.planetary_hour), isPlanetaryHourNotification) { enabled ->
							onPlanetaryHourNotificationChange(enabled)
							context.sendBroadcast(android.content.Intent("com.android.sun.ACTION_SETTINGS_CHANGED"))
						}
					),
					enabled = true
				)
				
				// Tattva Sound Card - colored symbols with checkboxes
				TattvaSoundCard(
				    isSoundAkasha = isSoundAkasha,
				    onSoundAkashaChange = onSoundAkashaChange,
				    isSoundVayu = isSoundVayu,
				    onSoundVayuChange = onSoundVayuChange,
				    isSoundTejas = isSoundTejas,
				    onSoundTejasChange = onSoundTejasChange,
				    isSoundApas = isSoundApas,
				    onSoundApasChange = onSoundApasChange,
				    isSoundPrithivi = isSoundPrithivi,
				    onSoundPrithiviChange = onSoundPrithiviChange,
				    soundVolume = soundVolume,
				    onSoundVolumeChange = onSoundVolumeChange,
				    customSoundUris = customSoundUris,
				    onTattvaIconClick = { name, code -> tattvaPickerFor = name to code }
				)
				
				HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // SECTIUNEA: DESPRE
                Text(
                    text = stringResource(R.string.section_about),
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
                                text = stringResource(R.string.version),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.sun_time),
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
        
        // Full-screen tattva sound picker overlay
        tattvaPickerFor?.let { (tattvaName, tattvaCode) ->
            val tattvaColor = when (tattvaCode) {
                "A"  -> com.android.sun.util.TattvaColors.Akasha
                "V"  -> com.android.sun.util.TattvaColors.Vayu
                "T"  -> com.android.sun.util.TattvaColors.Tejas
                "Ap" -> com.android.sun.util.TattvaColors.Apas
                "P"  -> com.android.sun.util.TattvaColors.Prithivi
                else -> androidx.compose.ui.graphics.Color.Gray
            }
            TattvaSoundPickerScreen(
                tattvaName = tattvaName,
                tattvaColor = tattvaColor,
                currentUri = customSoundUris[tattvaCode],
                onUriSelected = { uri ->
                    onCustomSoundUriChange(tattvaCode, uri)
                },
                onResetToDefault = {
                    onCustomSoundUriChange(tattvaCode, null)
                },
                onDismiss = { tattvaPickerFor = null }
            )
        }
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

/**
 * Card for Tattva Sound settings — colored tattva symbols with checkboxes.
 * No text labels, just the colored tattva icon + checkbox per tattva.
 * Checking a tattva plays a sound preview at the current volume.
 * A volume slider below the checkboxes controls playback level.
 */
@Composable
private fun TattvaSoundCard(
    isSoundAkasha: Boolean,
    onSoundAkashaChange: (Boolean) -> Unit,
    isSoundVayu: Boolean,
    onSoundVayuChange: (Boolean) -> Unit,
    isSoundTejas: Boolean,
    onSoundTejasChange: (Boolean) -> Unit,
    isSoundApas: Boolean,
    onSoundApasChange: (Boolean) -> Unit,
    isSoundPrithivi: Boolean,
    onSoundPrithiviChange: (Boolean) -> Unit,
    soundVolume: Float,
    onSoundVolumeChange: (Float) -> Unit,
    customSoundUris: Map<String, String?> = emptyMap(),
    onTattvaIconClick: (tattvaName: String, tattvaCode: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(7.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.tattva_sound),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(R.string.tattva_sound_tap_to_pick),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Akasha
                TattvaSoundItem(
                    tattvaName = "akasha",
                    color = TattvaColors.Akasha,
                    checked = isSoundAkasha,
                    onCheckedChange = onSoundAkashaChange,
                    hasCustomSound = customSoundUris["A"] != null,
                    onIconClick = { onTattvaIconClick("akasha", "A") },
                    onPlaySound = { playTattvaPreviewSound(context, "akasha", soundVolume, customSoundUris["A"]) }
                )
                // Vayu
                TattvaSoundItem(
                    tattvaName = "vayu",
                    color = TattvaColors.Vayu,
                    checked = isSoundVayu,
                    onCheckedChange = onSoundVayuChange,
                    hasCustomSound = customSoundUris["V"] != null,
                    onIconClick = { onTattvaIconClick("vayu", "V") },
                    onPlaySound = { playTattvaPreviewSound(context, "vayu", soundVolume, customSoundUris["V"]) }
                )
                // Tejas
                TattvaSoundItem(
                    tattvaName = "tejas",
                    color = TattvaColors.Tejas,
                    checked = isSoundTejas,
                    onCheckedChange = onSoundTejasChange,
                    hasCustomSound = customSoundUris["T"] != null,
                    onIconClick = { onTattvaIconClick("tejas", "T") },
                    onPlaySound = { playTattvaPreviewSound(context, "tejas", soundVolume, customSoundUris["T"]) }
                )
                // Apas
                TattvaSoundItem(
                    tattvaName = "apas",
                    color = TattvaColors.Apas,
                    checked = isSoundApas,
                    onCheckedChange = onSoundApasChange,
                    hasCustomSound = customSoundUris["Ap"] != null,
                    onIconClick = { onTattvaIconClick("apas", "Ap") },
                    onPlaySound = { playTattvaPreviewSound(context, "apas", soundVolume, customSoundUris["Ap"]) }
                )
                // Prithivi
                TattvaSoundItem(
                    tattvaName = "prithivi",
                    color = TattvaColors.Prithivi,
                    checked = isSoundPrithivi,
                    onCheckedChange = onSoundPrithiviChange,
                    hasCustomSound = customSoundUris["P"] != null,
                    onIconClick = { onTattvaIconClick("prithivi", "P") },
                    onPlaySound = { playTattvaPreviewSound(context, "prithivi", soundVolume, customSoundUris["P"]) }
                )
            }

            // Volume slider
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_volume_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Slider(
                    value = soundVolume,
                    onValueChange = onSoundVolumeChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_volume_up),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Single tattva sound item: colored icon wrapped in a raised Surface (button-like),
 * tappable to pick a custom sound. A small dot badge is shown when a custom sound is active.
 * Plays a sound preview when the checkbox is checked.
 */
@Composable
private fun TattvaSoundItem(
    tattvaName: String,
    color: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    hasCustomSound: Boolean = false,
    onIconClick: () -> Unit = {},
    onPlaySound: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Wrap icon in a Surface so it looks like a pressable button
        Surface(
            onClick = onIconClick,
            shape = RoundedCornerShape(1.dp),
            color = color.copy(alpha = 0.13f),
            shadowElevation = 1.dp,
            modifier = Modifier.padding(2.dp)
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.padding(6.dp)
            ) {
                Icon(
                    painter = painterResource(id = getTattvaIconRes(tattvaName)),
                    contentDescription = tattvaName,
                    modifier = Modifier.size(35.dp),   // +10% from 32dp
                    tint = color
                )
                if (hasCustomSound) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, androidx.compose.foundation.shape.CircleShape)
                            .offset(x = 2.dp, y = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Checkbox(
            checked = checked,
            onCheckedChange = { newValue ->
                onCheckedChange(newValue)
                if (newValue) onPlaySound()
            }
        )
    }
}

/**
 * Plays a one-shot preview of the tattva sound at the specified volume.
 * If [customUri] is non-null, the custom file is used; otherwise the built-in raw resource.
 * The MediaPlayer is released automatically on completion.
 */
private fun playTattvaPreviewSound(
    context: Context,
    tattvaName: String,
    volume: Float,
    customUri: String? = null
) {
    try {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val mediaPlayer: MediaPlayer?
        if (customUri != null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioAttributes(audioAttributes)
            mediaPlayer.setDataSource(context, android.net.Uri.parse(customUri))
            mediaPlayer.prepare()
        } else {
            val resId = when (tattvaName) {
                "akasha"   -> com.android.sun.R.raw.sound_akasha
                "vayu"     -> com.android.sun.R.raw.sound_vayu
                "tejas"    -> com.android.sun.R.raw.sound_tejas
                "apas"     -> com.android.sun.R.raw.sound_apas
                "prithivi" -> com.android.sun.R.raw.sound_prithivi
                else       -> return
            }
            mediaPlayer = MediaPlayer.create(
                context, resId, audioAttributes,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
        }
        mediaPlayer ?: return
        mediaPlayer.setVolume(volume, volume)
        mediaPlayer.setOnCompletionListener { player -> player.release() }
        mediaPlayer.setOnErrorListener { player, what, extra ->
            com.android.sun.util.AppLog.e("SettingsScreen", "❌ Preview MediaPlayer error: what=$what extra=$extra")
            player.release()
            true
        }
        mediaPlayer.start()
    } catch (e: Exception) {
        com.android.sun.util.AppLog.e("SettingsScreen", "❌ playTattvaPreviewSound failed: ${e.message}")
    }
}