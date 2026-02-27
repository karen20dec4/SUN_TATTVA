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
        val initialNotification = createDetailedNotification("Loading astro data...", R.drawable.icon, CHANNEL_ID_TATTVA, "GROUP_TATTVA")
        startForeground(TATTVA_NOTIF_ID, initialNotification)
        
        Log.d(TAG, "Started in foreground with initial notification")
        
        startPeriodicUpdate()
        return START_STICKY
    }
	
	

    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                val delayMs = updateNotificationAndGetDelay()
                // Wait until the next Tattva/Planet change (with 1s buffer to ensure the change happened)
                val waitMs = (delayMs + 1000L).coerceAtLeast(1000L)
                Log.d(TAG, "Next update in ${waitMs / 1000}s")
                delay(waitMs)
            }
        }
    }

    
	
	
	
	
	
	/**
	 * Updates the notification and returns the delay (in ms) until the next Tattva/Planet change.
	 * Returns 30_000 as fallback if calculation fails.
	 */
	private suspend fun updateNotificationAndGetDelay(): Long {
        Log.d(TAG, "updateNotificationAndGetDelay() called")
        
        val repository = AstroRepository(applicationContext)
        val locationPrefs = LocationPreferences(applicationContext)
        val showTattva = settingsPreferences.getTattvaNotification()
        val showPlanet = settingsPreferences.getPlanetaryHourNotification()

        if (!showTattva && !showPlanet) {
            stopSelf()
            return 30_000L
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
            val gmtSuffix = "(${if (timeZone >= 0) "+" else ""}${String.format("%.1f", timeZone)})"

            val now = System.currentTimeMillis()
            var nextChangeMs = Long.MAX_VALUE

            // 1. NOTIFICARE TATTVA
            if (showTattva) {
                val type = astroData.tattva.tattva
                val endTime = formatTime(astroData.tattva.endTime, timeZone)
                val emoji = getTattvaEmoji(type)
                val tattvaText = "$emoji - until $endTime $gmtSuffix"
                
                val notification = createDetailedNotification(
					tattvaText, 
					getTattvaIcon(type), 
					CHANNEL_ID_TATTVA,
					"GROUP_TATTVA"
				)
                
                startForeground(TATTVA_NOTIF_ID, notification)

                // Track when this Tattva ends
                val tattvaEndMs = astroData.tattva.endTime.timeInMillis - now
                if (tattvaEndMs > 0) {
                    nextChangeMs = minOf(nextChangeMs, tattvaEndMs)
                } else {
                    Log.w(TAG, "Tattva already ended (stale data), retrying soon")
                    nextChangeMs = minOf(nextChangeMs, 2_000L)
                }
            } else {
                notificationManager.cancel(TATTVA_NOTIF_ID)
            }

            // 2. NOTIFICARE ORA PLANETARĂ
            if (showPlanet) {
                val planet = astroData.planet.planet
                val endTime = formatTime(astroData.planet.endTime, timeZone)
                val emoji = getPlanetEmoji(planet)
                val planetText = "$emoji - until $endTime $gmtSuffix"
                
                val notification = createDetailedNotification(
					planetText, 
					getPlanetIcon(planet), 
					CHANNEL_ID_PLANET,
					"GROUP_PLANET"
				)
                
                if (!showTattva) {
                    startForeground(PLANET_NOTIF_ID, notification)
                } else {
                    notificationManager.notify(PLANET_NOTIF_ID, notification)
                }

                // Track when this planetary hour ends
                val planetEndMs = astroData.planet.endTime.timeInMillis - now
                if (planetEndMs > 0) {
                    nextChangeMs = minOf(nextChangeMs, planetEndMs)
                } else {
                    Log.w(TAG, "Planetary hour already ended (stale data), retrying soon")
                    nextChangeMs = minOf(nextChangeMs, 2_000L)
                }
            } else {
                notificationManager.cancel(PLANET_NOTIF_ID)
            }

            Log.d(TAG, "═══════════════════════════════════════════════════════")
            
            // Return the time until the next change (or 30s fallback)
            return if (nextChangeMs == Long.MAX_VALUE) 30_000L else nextChangeMs

        } catch (e: Exception) {
            Log.e(TAG, "Update failed", e)
            return 30_000L
        }
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
	
	
	
    
	private fun createDetailedNotification(title: String, iconRes: Int, channelId: String, groupKey: String): Notification {
		Log.d(TAG, "createDetailedNotification(): title='$title', iconRes=$iconRes, channelId=$channelId, group=$groupKey")
		val intent = Intent(this, MainActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(this, iconRes, intent, PendingIntent.FLAG_IMMUTABLE)

		return NotificationCompat.Builder(this, channelId)
			.setSmallIcon(iconRes)
			.setContentTitle(title)
			.setOngoing(true)
			.setSilent(true)
			// Am pus Priority HIGH pentru a forța sistemul să le dea importanță vizuală
			.setPriority(NotificationCompat.PRIORITY_HIGH) 
			.setContentIntent(pendingIntent)
			.setGroup(groupKey) // <--- Aici e magia
			.setGroupSummary(false) // Îi spunem că e notificare individuală
			.build()
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
                        startPeriodicUpdate() // Restart loop to pick up new timing
                    }
                    ACTION_SETTINGS_CHANGED -> {
                        Log.d(TAG, "Received ACTION_SETTINGS_CHANGED broadcast")
                        startPeriodicUpdate() // Restart loop to pick up new timing
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