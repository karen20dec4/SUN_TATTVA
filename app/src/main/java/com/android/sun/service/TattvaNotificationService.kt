package com.android.sun.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.sun.MainActivity
import com.android.sun.R
import com.android.sun.domain.calculator.TattvaType
import com.android.sun.domain.calculator.PlanetType
import com.android.sun.data.repository.AstroRepository
import com.android.sun.data.repository.LocationPreferences
import com.android.sun.data.preferences.SettingsPreferences
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TattvaNotificationService : Service() {

    companion object {
        private const val TAG = "TattvaNotificationService"
        private const val CHANNEL_ID_TATTVA = "tattva_persistent_channel"
        private const val CHANNEL_ID_PLANET = "planet_persistent_channel"
        
        private const val TATTVA_NOTIF_ID = 1001
        private const val PLANET_NOTIF_ID = 1002
        private const val GROUP_KEY = "com.android.sun.STATUS_BAR_NOTIFICATIONS"
        
        const val ACTION_LOCATION_CHANGED = "com.android.sun.LOCATION_CHANGED"
        const val ACTION_SETTINGS_CHANGED = "com.android.sun.SETTINGS_CHANGED"
        
        fun start(context: Context) {
            val intent = Intent(context, TattvaNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, TattvaNotificationService::class.java)
            context.stopService(intent)
        }
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var updateJob: Job? = null
    private var locationChangeReceiver: BroadcastReceiver? = null
    private lateinit var settingsPreferences: SettingsPreferences
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        settingsPreferences = SettingsPreferences(applicationContext)
        createNotificationChannel()
        registerLocationChangeReceiver()
        Log.d(TAG, "Service created successfully")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called")
        
        // Pornire neutră
        val initialNotification = createDetailedNotification("Loading astro data...", R.drawable.icon, CHANNEL_ID_TATTVA)
        startForeground(TATTVA_NOTIF_ID, initialNotification)
        
        Log.d(TAG, "Started in foreground with initial notification")
        
        startPeriodicUpdate()
        return START_STICKY
    }

    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                updateNotification()
                delay(30_000) 
            }
        }
    }

    
	private suspend fun updateNotification() {
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "updateNotification() called")
        
        val repository = AstroRepository(applicationContext)
        val locationPrefs = LocationPreferences(applicationContext)
        val showTattva = settingsPreferences.getTattvaNotification()
        val showPlanet = settingsPreferences.getPlanetaryHourNotification()

        Log.d(TAG, "Settings: showTattva=$showTattva, showPlanet=$showPlanet")

        if (!showTattva && !showPlanet) {
            Log.d(TAG, "Both notifications disabled - stopping service")
            stopSelf()
            return
        }

        try {
            val timeZone = locationPrefs.getSavedTimeZone()
            val astroData = repository.calculateAstroData(
                locationPrefs.getSavedLatitude(),
                locationPrefs.getSavedLongitude(),
                timeZone,
                locationPrefs.getSavedLocationName()
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Formatare GMT (ex: GMT+2.0) - am scos GMT
            val gmtSuffix = "(${if (timeZone >= 0) "+" else ""}${String.format("%.1f", timeZone)})"

            // 1. NOTIFICARE TATTVA
            if (showTattva) {
                val type = astroData.tattva.tattva
                val endTime = formatTime(astroData.tattva.endTime, timeZone)
                val emoji = getTattvaEmoji(type)
                
                // Format: 🔺 - ends 03:02 (GMT+2.0)
                val tattvaText = "$emoji - until $endTime $gmtSuffix"
                
                Log.d(TAG, "Creating Tattva notification: $tattvaText")
                Log.d(TAG, "Tattva icon resource: ${getTattvaIcon(type)}")
                
                val notification = createDetailedNotification(tattvaText, getTattvaIcon(type), CHANNEL_ID_TATTVA)
                
                // Tattva is always the primary foreground notification when enabled
                Log.d(TAG, "Calling startForeground with TATTVA_NOTIF_ID=$TATTVA_NOTIF_ID")
                startForeground(TATTVA_NOTIF_ID, notification)
            } else {
                Log.d(TAG, "Tattva disabled - canceling notification")
                notificationManager.cancel(TATTVA_NOTIF_ID)
            }

            // 2. NOTIFICARE ORA PLANETARĂ
            if (showPlanet) {
				val planet = astroData.planet.planet
				val endTime = formatTime(astroData.planet.endTime, timeZone)
				
				// Folosim getPlanetEmoji în loc de planet.code
				val emoji = getPlanetEmoji(planet)
				val gmtSuffix = "(${if (timeZone >= 0) "+" else ""}${String.format("%.1f", timeZone)})"
				
				// Format: 🪐 - ends 04:06 (+2.0)
				val planetText = "$emoji - until $endTime $gmtSuffix"
				
				Log.d(TAG, "Creating Planet notification: $planetText")
				Log.d(TAG, "Planet icon resource: ${getPlanetIcon(planet)}")
				
				val notification = createDetailedNotification(planetText, getPlanetIcon(planet), CHANNEL_ID_PLANET)
				
				// If Tattva is not shown, Planet becomes the foreground notification
				// If Tattva is shown, Planet is added as a second notification
				if (!showTattva) {
					Log.d(TAG, "Calling startForeground with PLANET_NOTIF_ID=$PLANET_NOTIF_ID")
					startForeground(PLANET_NOTIF_ID, notification)
				} else {
					// Both are enabled - show planet as a separate notification in the same group
					Log.d(TAG, "Both enabled - calling notify() for PLANET_NOTIF_ID=$PLANET_NOTIF_ID")
					notificationManager.notify(PLANET_NOTIF_ID, notification)
				}
			} else {
                Log.d(TAG, "Planet disabled - canceling notification")
                notificationManager.cancel(PLANET_NOTIF_ID)
            }
            
            Log.d(TAG, "updateNotification() completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Update failed", e)
        }
        
        Log.d(TAG, "═══════════════════════════════════════════════════════")
    }

    private fun getTattvaEmoji(type: TattvaType): String {
        return when (type) {
            TattvaType.TEJAS -> "🔺 TEJAS"
            TattvaType.PRITHIVI -> "🟨 PRITHIVI"
            TattvaType.APAS -> "🌙 APAS"
            TattvaType.VAYU -> "🔵 VAYU"
            TattvaType.AKASHA -> "🟣 AKASHA"
        }
    }
	
	
	private fun getPlanetEmoji(type: PlanetType?): String {
		return when (type) {
			PlanetType.SUN -> "☀️Sun"
			PlanetType.MOON -> "🌒Moon"
			PlanetType.MERCURY -> "☿Mercury" 	//🟢
			PlanetType.VENUS -> "♀Venus"		//⚪
			PlanetType.MARS -> "♂Mars"			//🔴
			PlanetType.JUPITER -> "♃Jupiter"	//🟠
			PlanetType.SATURN -> "♄Saturn"		//⚫
			else -> "✨"
		}
	}
	
	
	
    private fun createDetailedNotification(title: String, iconRes: Int, channelId: String): Notification {
        Log.d(TAG, "createDetailedNotification(): title='$title', iconRes=$iconRes, channelId=$channelId")
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, iconRes, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText("")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY)  // Add notifications to a group
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .build()
        
        Log.d(TAG, "Notification created with group key: $GROUP_KEY")
        return notification
    }

    private fun getPlanetIcon(type: PlanetType?): Int {
        return when (type) {
            PlanetType.SUN -> R.drawable.ic_planet_sun
            PlanetType.MOON -> R.drawable.ic_planet_moon
            PlanetType.MERCURY -> R.drawable.ic_planet_mercury
            PlanetType.VENUS -> R.drawable.ic_planet_venus
            PlanetType.MARS -> R.drawable.ic_planet_mars
            PlanetType.JUPITER -> R.drawable.ic_planet_jupiter
            PlanetType.SATURN -> R.drawable.ic_planet_saturn
            else -> R.drawable.icon
        }
    }

    private fun getTattvaIcon(type: TattvaType): Int {
        return when (type) {
            TattvaType.TEJAS -> R.drawable.ic_tattva_tejas
            TattvaType.PRITHIVI -> R.drawable.ic_tattva_prithivi
            TattvaType.APAS -> R.drawable.ic_tattva_apas
            TattvaType.VAYU -> R.drawable.ic_tattva_vayu
            TattvaType.AKASHA -> R.drawable.ic_tattva_akasha
        }
    }

    private fun formatTime(calendar: Calendar, timeZone: Double): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val offsetMillis = (timeZone * 3600 * 1000).toInt()
        sdf.timeZone = SimpleTimeZone(offsetMillis, "Location")
        return sdf.format(calendar.time)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Create Tattva notification channel
            val tattvaChannel = NotificationChannel(
                CHANNEL_ID_TATTVA, 
                "Tattva Updates", 
                NotificationManager.IMPORTANCE_DEFAULT // Schimbat la DEFAULT pentru vizibilitate
            )
            tattvaChannel.setShowBadge(false)
            tattvaChannel.setSound(null, null)
            notificationManager.createNotificationChannel(tattvaChannel)
            
            // Create Planet notification channel
            val planetChannel = NotificationChannel(
                CHANNEL_ID_PLANET, 
                "Planetary Hour Updates", 
                NotificationManager.IMPORTANCE_DEFAULT // Schimbat la DEFAULT pentru vizibilitate
            )
            planetChannel.setShowBadge(false)
            planetChannel.setSound(null, null)
            notificationManager.createNotificationChannel(planetChannel)
        }
    }

    private fun registerLocationChangeReceiver() {
        Log.d(TAG, "Registering broadcast receivers for location and settings changes")
        
        locationChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_LOCATION_CHANGED -> {
                        Log.d(TAG, "Received ACTION_LOCATION_CHANGED broadcast")
                        serviceScope.launch { updateNotification() }
                    }
                    ACTION_SETTINGS_CHANGED -> {
                        Log.d(TAG, "Received ACTION_SETTINGS_CHANGED broadcast")
                        serviceScope.launch { updateNotification() }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(ACTION_LOCATION_CHANGED)
            addAction(ACTION_SETTINGS_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(locationChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(locationChangeReceiver, filter)
        }
        
        Log.d(TAG, "Broadcast receivers registered successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try { unregisterReceiver(locationChangeReceiver) } catch (e: Exception) {}
    }
}