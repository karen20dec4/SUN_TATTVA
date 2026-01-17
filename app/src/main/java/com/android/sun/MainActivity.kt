package com.android.sun

import java.util.Calendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
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
import com.android.sun.ui.theme.SunTheme
import com.android.sun.viewmodel.AstroViewModel
import com.android.sun.viewmodel.LocationViewModel
import com.android.sun.viewmodel.MainViewModel
import com.android.sun.service.TattvaNotificationService
import androidx.compose.ui.platform.LocalContext

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


@Composable
fun AppNavigation(
    application: android.app.Application,
    settingsPreferences: SettingsPreferences,
    notificationScheduler: NotificationScheduler,
    isDarkTheme: Boolean
) {
    val isTattvaNotification by settingsPreferences.tattvaNotification.collectAsState()
	val isPlanetaryHourNotification by settingsPreferences.planetaryHourNotification.collectAsState()
	
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
    
    LaunchedEffect(astroData, isFullMoonNotification, isTripuraSundariNotification, isNewMoonNotification) {
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
            MainScreen(
                astroData = astroData,
                isLoading = isLoading,
                codeMode = codeMode,
                isDarkTheme = isDarkTheme,
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
				},
				isPlanetaryHourNotification = isPlanetaryHourNotification,
				onPlanetaryHourNotificationChange = { enabled ->
					settingsPreferences.setPlanetaryHourNotification(enabled)
					if (enabled || isTattvaNotification) {
						TattvaNotificationService.start(context)
					} else {
						TattvaNotificationService.stop(context)
					}
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
                        currentTime = Calendar.getInstance()
                    )
                }
                
                // Format sunrise and sunset times
                val offsetMillis = (astroData!!.timeZone * 3600 * 1000).toInt()
                val locationTimeZone = java.util.SimpleTimeZone(offsetMillis, "Location")
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
    }
}