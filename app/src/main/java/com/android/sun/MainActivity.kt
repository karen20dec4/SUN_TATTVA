package com.android.sun

import android.content.Intent
import android.content.res.Configuration
import java.util.Calendar
import java.util.Locale
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.sun.data.preferences.SettingsPreferences
import com.android.sun.notification.NotificationHelper
import com.android.sun.notification.NotificationScheduler
import com.android.sun.ui.screens.AllDayScreen
import com.android.sun.ui.screens.CalendarPickerScreen
import com.android.sun.ui.screens.LocationScreen
import com.android.sun.ui.screens.MainScreen
import com.android.sun.ui.screens.SettingsScreen
import com.android.sun.ui.screens.NakshatraDetailScreen
import com.android.sun.ui.theme.SunTheme
import com.android.sun.viewmodel.AstroViewModel
import com.android.sun.viewmodel.LocationViewModel
import com.android.sun.viewmodel.MainViewModel
import com.android.sun.service.TattvaNotificationService
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalActivityResultRegistryOwner
import com.android.sun.domain.calculator.NakshatraCalculator

class MainActivity : ComponentActivity() {
    
    private lateinit var settingsPreferences: SettingsPreferences
    private lateinit var notificationScheduler: NotificationScheduler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        settingsPreferences = SettingsPreferences(this)
        notificationScheduler = NotificationScheduler(this)
        NotificationHelper(this)
        
        // ADAUGĂ ACEASTA:  Pornește service-ul dacă era activat
		if (settingsPreferences.getTattvaNotification() || settingsPreferences.getPlanetaryHourNotification()) {
			TattvaNotificationService.start(this)
		}
		
		
		
		
		setContent {
            val isDarkTheme by settingsPreferences.isDarkTheme.collectAsState()
            val currentLanguage by settingsPreferences.language.collectAsState()
            
            // Apply locale based on language preference
            val context = LocalContext.current
            val localizedContext = remember(currentLanguage) {
                val locale = Locale(currentLanguage)
                Locale.setDefault(locale)
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                context.createConfigurationContext(config)
            }
            
            // Preserve ActivityResultRegistryOwner when overriding LocalContext,
            // otherwise rememberLauncherForActivityResult() crashes in SettingsScreen
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                SunTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            application = application,
                            settingsPreferences = settingsPreferences,
                            notificationScheduler = notificationScheduler,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AppNavigation(
    application: android.app.Application,
    settingsPreferences: SettingsPreferences,
    notificationScheduler: NotificationScheduler,
    isDarkTheme: Boolean
) {
    val isTattvaNotification by settingsPreferences.tattvaNotification.collectAsState()
	val isPlanetaryHourNotification by settingsPreferences.planetaryHourNotification.collectAsState()
	val currentLanguage by settingsPreferences.language.collectAsState()
	
	val context = LocalContext.current
	val navController = rememberNavController()
    
    // ✅ FIX: Use AndroidViewModelFactory with Application from Activity
    val factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    
    val mainViewModel: MainViewModel = viewModel(factory = factory)
    val locationViewModel: LocationViewModel = viewModel(factory = factory)
    val astroViewModel: AstroViewModel = viewModel(factory = factory)
    
    val astroData by mainViewModel.astroData.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val codeMode by mainViewModel.codeMode.collectAsState()
    
    val isFullMoonNotification by settingsPreferences.fullMoonNotification.collectAsState()
    val isTripuraSundariNotification by settingsPreferences.tripuraSundariNotification.collectAsState()
    val isNewMoonNotification by settingsPreferences.newMoonNotification.collectAsState()
    
    // Tattva sound preferences
    val isSoundAkasha by settingsPreferences.tattvaSoundAkasha.collectAsState()
    val isSoundVayu by settingsPreferences.tattvaSoundVayu.collectAsState()
    val isSoundTejas by settingsPreferences.tattvaSoundTejas.collectAsState()
    val isSoundApas by settingsPreferences.tattvaSoundApas.collectAsState()
    val isSoundPrithivi by settingsPreferences.tattvaSoundPrithivi.collectAsState()
    val soundVolume by settingsPreferences.tattvaSoundVolume.collectAsState()
    val customSoundUris by settingsPreferences.customSoundUris.collectAsState()
    
    // Debug date preferences
    val isDebugDateEnabled by settingsPreferences.debugDateEnabled.collectAsState()
    val debugDateMillis by settingsPreferences.debugDateMillis.collectAsState()
    
    // ✅ Use stable keys based on event times to prevent unnecessary rescheduling
    val fullMoonTimeMillis = astroData?.moonPhase?.nextFullMoon?.timeInMillis
    val tripuraTimeMillis = astroData?.moonPhase?.nextTripuraSundari?.timeInMillis
    val newMoonTimeMillis = astroData?.moonPhase?.nextNewMoon?.timeInMillis
    
    LaunchedEffect(fullMoonTimeMillis, tripuraTimeMillis, newMoonTimeMillis, isFullMoonNotification, isTripuraSundariNotification, isNewMoonNotification) {
        astroData?.let { data ->
            if (isFullMoonNotification) {
                notificationScheduler.scheduleFullMoonNotifications(
                    data.moonPhase.nextFullMoon.timeInMillis
                )
            }
            if (isTripuraSundariNotification) {
                notificationScheduler.scheduleTripuraSundariNotification(
                    data.moonPhase.nextTripuraSundari.timeInMillis
                )
            }
            if (isNewMoonNotification) {
                notificationScheduler.scheduleNewMoonNotification(
                    data.moonPhase.nextNewMoon.timeInMillis
                )
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            // Format debug date label if active
            val debugDateLabel = if (isDebugDateEnabled && debugDateMillis > 0L) {
                val fmt = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                fmt.format(java.util.Date(debugDateMillis))
            } else null
            
            MainScreen(
                astroData = astroData,
                isLoading = isLoading,
                codeMode = codeMode,
                isDarkTheme = isDarkTheme,
                debugDateLabel = debugDateLabel,
                onCodeModeChange = { mainViewModel.toggleCodeMode() },
                onRefresh = { mainViewModel.refresh() },
                onNavigateToLocation = {
                    navController.navigate("location")
                },
                onNavigateToAllDay = {
                    navController.navigate("allday")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToNakshatraDetail = { nakshatraNumber ->
                    navController.navigate("nakshatra/$nakshatraNumber")
                }
            )
        }
        
        composable("location") {
            LocationScreen(
                viewModel = locationViewModel,
                mainViewModel = mainViewModel,
                isDarkTheme = isDarkTheme,
                onLocationSelected = { location ->
                    mainViewModel.setLocation(location)
                    navController.popBackStack()
                    mainViewModel.refresh()
                },
                onBack = {
                    navController.popBackStack()
                    mainViewModel.refresh()
                }
            )
        }
        
        composable("settings") {
			SettingsScreen(
				isDarkTheme = isDarkTheme,
				onDarkThemeChange = { enabled ->
					settingsPreferences.setDarkTheme(enabled)
				},
				currentLanguage = currentLanguage,
				onLanguageChange = { lang ->
					settingsPreferences.setLanguage(lang)
				},
				isFullMoonNotification = isFullMoonNotification,
				onFullMoonNotificationChange = { enabled ->
					settingsPreferences.setFullMoonNotification(enabled)
				},
				isTripuraSundariNotification = isTripuraSundariNotification,
				onTripuraSundariNotificationChange = { enabled ->
					settingsPreferences.setTripuraSundariNotification(enabled)
				},
				isNewMoonNotification = isNewMoonNotification,
				onNewMoonNotificationChange = { enabled ->
					settingsPreferences.setNewMoonNotification(enabled)
				},
				isTattvaNotification = isTattvaNotification,  
				onTattvaNotificationChange = { enabled ->
					settingsPreferences.setTattvaNotification(enabled)
					if (enabled || isPlanetaryHourNotification) {
						TattvaNotificationService.start(context)
					} else {
						TattvaNotificationService.stop(context)
					}
					// Send broadcast to trigger immediate notification update
					val intent = Intent(TattvaNotificationService.ACTION_SETTINGS_CHANGED)
					context.sendBroadcast(intent)
				},
				isPlanetaryHourNotification = isPlanetaryHourNotification,
				onPlanetaryHourNotificationChange = { enabled ->
					settingsPreferences.setPlanetaryHourNotification(enabled)
					if (enabled || isTattvaNotification) {
						TattvaNotificationService.start(context)
					} else {
						TattvaNotificationService.stop(context)
					}
					// Send broadcast to trigger immediate notification update
					val intent = Intent(TattvaNotificationService.ACTION_SETTINGS_CHANGED)
					context.sendBroadcast(intent)
				},
				// Tattva Sound toggles
				isSoundAkasha = isSoundAkasha,
				onSoundAkashaChange = { settingsPreferences.setTattvaSoundAkasha(it) },
				isSoundVayu = isSoundVayu,
				onSoundVayuChange = { settingsPreferences.setTattvaSoundVayu(it) },
				isSoundTejas = isSoundTejas,
				onSoundTejasChange = { settingsPreferences.setTattvaSoundTejas(it) },
				isSoundApas = isSoundApas,
				onSoundApasChange = { settingsPreferences.setTattvaSoundApas(it) },
				isSoundPrithivi = isSoundPrithivi,
				onSoundPrithiviChange = { settingsPreferences.setTattvaSoundPrithivi(it) },
				soundVolume = soundVolume,
				onSoundVolumeChange = { settingsPreferences.setTattvaSoundVolume(it) },
				customSoundUris = customSoundUris,
				onCustomSoundUriChange = { tattvaCode, uri ->
					settingsPreferences.setCustomSoundUri(tattvaCode, uri)
				},
				// Debug date override
				isDebugDateEnabled = isDebugDateEnabled,
				debugDateMillis = debugDateMillis,
				onDebugDateChange = { millis ->
					settingsPreferences.setDebugDateMillis(millis)
				},
				onDebugDateEnabledChange = { enabled ->
					settingsPreferences.setDebugDateEnabled(enabled)
				},
				onDebugRecalculate = {
					mainViewModel.calculateAstroData()
				},
				onBackClick = {
					navController.popBackStack()
				}
			)
		}
        
		
		
		
		
        composable("allday") {
            if (astroData != null) {
                val tattvaDaySchedule = remember(astroData) {
                    astroViewModel.generateTattvaDayScheduleWithCurrentTime(
                        astroData = astroData!!,
                        currentTime = Calendar.getInstance()
                    )
                }
                
                AllDayScreen(
                    tattvaDaySchedule = tattvaDaySchedule,
                    sunriseDate = astroData!!.sunrise,
                    sunriseTime = astroData!!.sunriseFormatted,
                    sunsetTime = astroData!!.sunsetFormatted,
                    actualSunriseTime = astroData!! .sunrise,
                    timeZone = astroData!!.timeZone,
                    locationName = astroData!!.locationName,
                    isDarkTheme = isDarkTheme,
                    onBackClick = {
                        navController.popBackStack("main", inclusive = false)
                    },
                    onCalendarClick = {
                        navController.navigate("calendar")
                    }
                )
            }
        }
        
        composable("calendar") {
            if (astroData != null) {
                CalendarPickerScreen(
                    currentDate = Calendar.getInstance(),
                    onDateSelected = { year, month, day ->
                        // Navigate to custom date view with selected date
                        navController.navigate("customday/$year/$month/$day")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable("customday/{year}/{month}/{day}") { backStackEntry ->
            if (astroData != null) {
                val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: return@composable
                val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: return@composable
                val day = backStackEntry.arguments?.getString("day")?.toIntOrNull() ?: return@composable
                
                // Calculate sunrise and sunset for the selected date
                val (sunrise, sunset) = astroViewModel.calculateSunriseSunsetForDate(
                    year = year,
                    month = month,
                    day = day,
                    latitude = astroData!!.latitude,
                    longitude = astroData!!.longitude,
                    timeZone = astroData!!.timeZone
                )
                
                // Generate tattva schedule for the selected date
                val tattvaDaySchedule = remember(year, month, day) {
                    astroViewModel.generateScheduleForDate(
                        year = year,
                        month = month,
                        day = day,
                        latitude = astroData!!.latitude,
                        longitude = astroData!!.longitude,
                        timeZone = astroData!!.timeZone,
                        currentTime = Calendar.getInstance(),
                        locationName = astroData!!.locationName
                    )
                }
                
                // Format sunrise and sunset times with DST support
                // Use the same timezone logic as in the repository
                val locationTimeZone = com.android.sun.util.TimeZoneUtils.getLocationTimeZone(astroData!!.locationName, astroData!!.timeZone)
                val timeFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).apply {
                    this.timeZone = locationTimeZone
                }
                
                AllDayScreen(
                    tattvaDaySchedule = tattvaDaySchedule,
                    sunriseDate = sunrise,
                    sunriseTime = timeFormat.format(sunrise.time),
                    sunsetTime = timeFormat.format(sunset.time),
                    actualSunriseTime = sunrise,
                    timeZone = astroData!!.timeZone,
                    locationName = astroData!!.locationName,
                    isDarkTheme = isDarkTheme,
                    onBackClick = {
                        navController.popBackStack("main", inclusive = false)
                    },
                    onCalendarClick = {
                        navController.navigate("calendar")
                    }
                )
            }
        }
        
        composable("nakshatra/{number}") { backStackEntry ->
            val nakshatraNumber = backStackEntry.arguments?.getString("number")?.toIntOrNull() ?: 1
            val nakshatra = NakshatraCalculator.nakshatraList.getOrNull(nakshatraNumber - 1) 
                ?: NakshatraCalculator.nakshatraList[0]
            
            NakshatraDetailScreen(
                nakshatra = nakshatra,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}