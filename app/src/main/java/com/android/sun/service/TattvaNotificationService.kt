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
        
        // Separate groups to prevent Android from grouping notifications together
        private const val GROUP_KEY_TATTVA = "com.android.sun.TATTVA_GROUP"
        private const val GROUP_KEY_PLANET = "com.android.sun.PLANET_GROUP"
        
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
        // Pornire cu notificare simplă
        val initialNotification = createDetailedNotification("Loading astro data...", R.drawable.icon, CHANNEL_ID_TATTVA, GROUP_KEY_TATTVA)
        startForeground(TATTVA_NOTIF_ID, initialNotification)
        
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

            // Show separate notifications with different groups to prevent auto-grouping
            if (showTattva) {
                val type = astroData.tattva.tattva
                val endTime = formatTime(astroData.tattva.endTime, timeZone)
                val emoji = getTattvaEmoji(type)
                val tattvaText = "$emoji - until $endTime $gmtSuffix"
                
                val notification = createDetailedNotification(
                    tattvaText, 
                    getTattvaIcon(type), 
                    CHANNEL_ID_TATTVA,
                    GROUP_KEY_TATTVA
                )
                
                // Use startForeground for the first notification
                if (!showPlanet) {
                    startForeground(TATTVA_NOTIF_ID, notification)
                } else {
                    notificationManager.notify(TATTVA_NOTIF_ID, notification)
                }
            } else {
                notificationManager.cancel(TATTVA_NOTIF_ID)
            }

            if (showPlanet) {
                val planet = astroData.planet.planet
                val endTime = formatTime(astroData.planet.endTime, timeZone)
                val emoji = getPlanetEmoji(planet)
                val planetText = "$emoji - until $endTime $gmtSuffix"
                
                val notification = createDetailedNotification(
                    planetText, 
                    getPlanetIcon(planet), 
                    CHANNEL_ID_PLANET,
                    GROUP_KEY_PLANET
                )
                
                // Use startForeground for one notification (preferably the first one set)
                if (showTattva) {
                    // If Tattva is shown, we need to make one of them foreground
                    // We'll use Tattva as foreground, so Planet is just notify
                    notificationManager.notify(PLANET_NOTIF_ID, notification)
                    
                    // Re-post Tattva as foreground to ensure service stays alive
                    val tattvaType = astroData.tattva.tattva
                    val tattvaEndTime = formatTime(astroData.tattva.endTime, timeZone)
                    val tattvaEmoji = getTattvaEmoji(tattvaType)
                    val tattvaText = "$tattvaEmoji - until $tattvaEndTime $gmtSuffix"
                    val tattvaNotif = createDetailedNotification(
                        tattvaText, 
                        getTattvaIcon(tattvaType), 
                        CHANNEL_ID_TATTVA,
                        GROUP_KEY_TATTVA
                    )
                    startForeground(TATTVA_NOTIF_ID, tattvaNotif)
                } else {
                    startForeground(PLANET_NOTIF_ID, notification)
                }
            } else {
                notificationManager.cancel(PLANET_NOTIF_ID)
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
	
	
	
    private fun createDetailedNotification(title: String, iconRes: Int, channelId: String, groupKey: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, iconRes, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText("")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setGroup(groupKey) // Each notification in its own group
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