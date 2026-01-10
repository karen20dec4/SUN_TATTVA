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
import com.android.sun.domain.calculator.PlanetCalculator
import com.android.sun.data.repository.AstroRepository
import com.android.sun.data.repository.LocationPreferences
import com.android.sun.data.preferences.SettingsPreferences
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TattvaNotificationService :  Service() {

    companion object {
        private const val TAG = "TattvaNotificationService"
        private const val CHANNEL_ID = "tattva_persistent_channel"
        private const val TATTVA_NOTIFICATION_ID = 1001
        private const val PLANET_NOTIFICATION_ID = 1002
        const val ACTION_LOCATION_CHANGED = "com.android.sun.LOCATION_CHANGED"
        
        fun start(context: Context) {
            val intent = Intent(context, TattvaNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context:  Context) {
            val intent = Intent(context, TattvaNotificationService::class.java)
            context.stopService(intent)
        }
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var updateJob: Job?  = null
    private var locationChangeReceiver: BroadcastReceiver? = null
    private lateinit var settingsPreferences: SettingsPreferences
    
    override fun onBind(intent:  Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🟢 TattvaNotificationService onCreate")
        settingsPreferences = SettingsPreferences(applicationContext)
        createNotificationChannel()
        registerLocationChangeReceiver()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🟢 TattvaNotificationService onStartCommand")
        
        // Start ca foreground service cu notificare inițială pentru tattva
        val initialNotification = createTattvaNotification(
            tattvaName = "Loading...",
            tattvaType = TattvaType.PRITHIVI,
            tattvaEndsAt = "",
            locationName = "",
            timeZone = 0.0
        )
        startForeground(TATTVA_NOTIFICATION_ID, initialNotification)
        
        // Pornește actualizarea periodică
        startPeriodicUpdate()
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔴 TattvaNotificationService onDestroy")
        updateJob?.cancel()
        serviceScope.cancel()
        unregisterLocationChangeReceiver()
    }
    
    /**
     * ✅ Înregistrează receiver pentru schimbări de locație
     */
    private fun registerLocationChangeReceiver() {
        locationChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "📍 Location changed broadcast received!  Updating notification NOW...")
                serviceScope.launch {
                    updateNotification()
                }
            }
        }
        
        val filter = IntentFilter(ACTION_LOCATION_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(locationChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(locationChangeReceiver, filter)
        }
        
        Log.d(TAG, "✅ Location change receiver registered")
    }
    
    /**
     * ✅ Deînregistrează receiver-ul
     */
    private fun unregisterLocationChangeReceiver() {
        try {
            locationChangeReceiver?.let {
                unregisterReceiver(it)
                Log.d(TAG, "✅ Location change receiver unregistered")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Current Tattva & Planetary Hour",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows the current Tattva element and planetary hour"
                setShowBadge(false)
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager:: class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                try {
                    updateNotification()
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating notification", e)
                }
                
                // Actualizează la fiecare 30 secunde
                delay(30_000)
            }
        }
    }
    
    private suspend fun updateNotification() {
        val repository = AstroRepository(applicationContext)
        val locationPreferences = LocationPreferences(applicationContext)
        
        // Check settings
        val showTattva = settingsPreferences.getTattvaNotification()
        val showPlanet = settingsPreferences.getPlanetaryHourNotification()
        
        // If both are disabled, stop the service
        if (!showTattva && !showPlanet) {
            Log.d(TAG, "Both notifications disabled, stopping service")
            stopSelf()
            return
        }
        
        try {
            // Obține locația din LocationPreferences
            val latitude = locationPreferences.getSavedLatitude()
            val longitude = locationPreferences.getSavedLongitude()
            val altitude = locationPreferences.getSavedAltitude()
            val timeZone = locationPreferences.getSavedTimeZone()
            val locationName = locationPreferences.getSavedLocationName()
            
            Log.d(TAG, "📍 Using location: $locationName (GMT${if (timeZone >= 0) "+" else ""}$timeZone)")
            
            // Calculează timezone-ul locației
            val locationOffsetMillis = (timeZone * 3600 * 1000).toInt()
            val locationTimeZone = SimpleTimeZone(locationOffsetMillis, "Location")
            
            // Folosește ora curentă din timezone-ul locației
            val currentTime = Calendar.getInstance(locationTimeZone)
            
            val astroData = repository.calculateAstroData(
                latitude = latitude,
                longitude = longitude,
                timeZone = timeZone,
                locationName = locationName
            )
            
            // Get Tattva data
            val tattvaResult = astroData.tattva
            val tattvaType = tattvaResult.tattva
            val tattvaName = tattvaType.displayName
            
            // Get Planet data
            val planetResult = astroData.planet
            val planetType = planetResult.planet
            val planetName = planetType.displayName
            
            // Formatează timpul cu timezone-ul locației
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                this.timeZone = locationTimeZone
            }
            val tattvaEndsAtFormatted = timeFormat.format(tattvaResult.endTime.time)
            val planetEndsAtFormatted = timeFormat.format(planetResult.endTime.time)
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Create and show Tattva notification if enabled
            if (showTattva) {
                val tattvaNotification = createTattvaNotification(
                    tattvaName = tattvaName,
                    tattvaType = tattvaType,
                    tattvaEndsAt = tattvaEndsAtFormatted,
                    locationName = locationName,
                    timeZone = timeZone
                )
                notificationManager.notify(TATTVA_NOTIFICATION_ID, tattvaNotification)
                Log.d(TAG, "📍 Tattva notification updated: $tattvaName ends at $tattvaEndsAtFormatted")
            } else {
                // Cancel tattva notification if disabled
                notificationManager.cancel(TATTVA_NOTIFICATION_ID)
            }
            
            // Create and show Planet notification if enabled
            if (showPlanet) {
                val planetNotification = createPlanetNotification(
                    planetName = planetName,
                    planetType = planetType,
                    planetEndsAt = planetEndsAtFormatted,
                    locationName = locationName,
                    timeZone = timeZone
                )
                notificationManager.notify(PLANET_NOTIFICATION_ID, planetNotification)
                Log.d(TAG, "📍 Planet notification updated: $planetName ends at $planetEndsAtFormatted")
            } else {
                // Cancel planet notification if disabled
                notificationManager.cancel(PLANET_NOTIFICATION_ID)
            }
            
            Log.d(TAG, "✅ Notifications updated (Tattva=$showTattva, Planet=$showPlanet)")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }
    
    /**
     * Helper function to get planet icon drawable resource
     */
    private fun getPlanetIcon(planetType: PlanetType?): Int {
        return when (planetType) {
            PlanetType.SUN -> R.drawable.ic_planet_sun
            PlanetType.MOON -> R.drawable.ic_planet_moon
            PlanetType.MERCURY -> R.drawable.ic_planet_mercury
            PlanetType.VENUS -> R.drawable.ic_planet_venus
            PlanetType.MARS -> R.drawable.ic_planet_mars
            PlanetType.JUPITER -> R.drawable.ic_planet_jupiter
            PlanetType.SATURN -> R.drawable.ic_planet_saturn
            null -> R.drawable.icon // fallback
        }
    }
    
    /**
     * Create notification for Tattva only
     */
    private fun createTattvaNotification(
        tattvaName: String,
        tattvaType: TattvaType,
        tattvaEndsAt: String,
        locationName: String,
        timeZone: Double
    ): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val iconRes = when (tattvaType) {
            TattvaType.TEJAS -> R.drawable.ic_tattva_tejas
            TattvaType.PRITHIVI -> R.drawable.ic_tattva_prithivi
            TattvaType.APAS -> R.drawable.ic_tattva_apas
            TattvaType.VAYU -> R.drawable.ic_tattva_vayu
            TattvaType.AKASHA -> R.drawable.ic_tattva_akasha
        }
        
        val tattvaEmoji = when (tattvaType) {
            TattvaType.TEJAS -> "🔺"
            TattvaType.PRITHIVI -> "🟨"
            TattvaType.APAS -> "🌙"
            TattvaType.VAYU -> "🔵"
            TattvaType.AKASHA -> "🟣"
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(tattvaEmoji)
            .setContentText("$tattvaName - SUN TATTVA")
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("SUN TATTVA - $locationName")
                .bigText("$tattvaEmoji $tattvaName - ends at $tattvaEndsAt (GMT${if (timeZone >= 0) "+" else ""}${String.format("%.1f", timeZone)})"))
            .build()
    }
    
    /**
     * Create notification for Planet only
     */
    private fun createPlanetNotification(
        planetName: String,
        planetType: PlanetType,
        planetEndsAt: String,
        locationName: String,
        timeZone: Double
    ): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 
            1, // Different request code from tattva
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val iconRes = getPlanetIcon(planetType)
        val planetEmoji = planetType.code
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(planetEmoji)
            .setContentText("$planetName - Planetary Hour")
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("SUN TATTVA - $locationName")
                .bigText("$planetEmoji $planetName - ends at $planetEndsAt (GMT${if (timeZone >= 0) "+" else ""}${String.format("%.1f", timeZone)})"))
            .build()
    }
}