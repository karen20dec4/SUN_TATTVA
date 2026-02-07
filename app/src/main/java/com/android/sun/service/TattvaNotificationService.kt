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
        private const val SUMMARY_NOTIF_ID = 1000
        
        private const val GROUP_KEY_ASTRO = "com.android.sun.ASTRO_NOTIFICATIONS"
        
        const val ACTION_LOCATION_CHANGED = "com.android.sun.LOCATION_CHANGED"
        
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
        settingsPreferences = SettingsPreferences(applicationContext)
        createNotificationChannel()
        registerLocationChangeReceiver()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Pornire cu summary notification
        val initialNotification = createSummaryNotification()
        startForeground(SUMMARY_NOTIF_ID, initialNotification)
        
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
        val repository = AstroRepository(applicationContext)
        val locationPrefs = LocationPreferences(applicationContext)
        val showTattva = settingsPreferences.getTattvaNotification()
        val showPlanet = settingsPreferences.getPlanetaryHourNotification()

        if (!showTattva && !showPlanet) {
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

            // Decide which notification to use for foreground service
            val useSummary = showTattva && showPlanet

            // 1. NOTIFICARE TATTVA
            if (showTattva) {
                val type = astroData.tattva.tattva
                val endTime = formatTime(astroData.tattva.endTime, timeZone)
                val emoji = getTattvaEmoji(type)
                
                // Format: 🔺 - ends 03:02 (GMT+2.0)
                val tattvaText = "$emoji - until $endTime $gmtSuffix"
                
                val notification = createDetailedNotification(tattvaText, getTattvaIcon(type), CHANNEL_ID_TATTVA)
                
                // If both notifications are active, post as regular notification
                // Otherwise, use as foreground notification
                if (useSummary) {
                    notificationManager.notify(TATTVA_NOTIF_ID, notification)
                } else {
                    startForeground(TATTVA_NOTIF_ID, notification)
                }
            } else {
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
				
				val notification = createDetailedNotification(planetText, getPlanetIcon(planet), CHANNEL_ID_PLANET)
				
				// If both notifications are active, post as regular notification
                // Otherwise, use as foreground notification
				if (useSummary) {
					notificationManager.notify(PLANET_NOTIF_ID, notification)
				} else {
					startForeground(PLANET_NOTIF_ID, notification)
				}
			} else {
                notificationManager.cancel(PLANET_NOTIF_ID)
            }
            
            // 3. SUMMARY NOTIFICATION - Only show and use as foreground when both notifications are active
            if (useSummary) {
                val summaryNotification = createSummaryNotification()
                startForeground(SUMMARY_NOTIF_ID, summaryNotification)
            } else {
                notificationManager.cancel(SUMMARY_NOTIF_ID)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Update failed", e)
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
	
	
	
    private fun createDetailedNotification(title: String, iconRes: Int, channelId: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, iconRes, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title) // Textul scurt merge aici
            .setContentText("")      // Textul secundar rămâne gol pentru un singur rând
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // DEFAULT scoate notificarea din "Silent"
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY_ASTRO) // Add to notification group
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY) // Silent notifications use GROUP_ALERT_SUMMARY
            .build()
    }
    
    private fun createSummaryNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID_TATTVA)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Astro Updates")
            .setContentText("Tattva and Planetary Hours")
            .setGroup(GROUP_KEY_ASTRO)
            .setGroupSummary(true)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
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
        locationChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                serviceScope.launch { updateNotification() }
            }
        }
        val filter = IntentFilter(ACTION_LOCATION_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(locationChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(locationChangeReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try { unregisterReceiver(locationChangeReceiver) } catch (e: Exception) {}
    }
}