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
        private const val NOTIFICATION_ID = 1001
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
        
        // Start ca foreground service cu notificare inițială
        val initialNotification = createNotification(
            tattvaName = "Loading...",
            tattvaType = TattvaType.PRITHIVI,
            tattvaEndsAt = "",
            planetName = "",
            planetType = null,
            planetEndsAt = "",
            locationName = "",
            timeZone = 0.0
        )
        startForeground(NOTIFICATION_ID, initialNotification)
        
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
        
        Log.d(TAG, "⚙️ Settings: showTattva=$showTattva, showPlanet=$showPlanet")
        
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
            
            Log.d(TAG, "🔵 Tattva: $tattvaName ($tattvaType)")
            Log.d(TAG, "🪐 Planet: $planetName (${planetType.code})")
            
            // Formatează timpul cu timezone-ul locației
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                this.timeZone = locationTimeZone
            }
            val tattvaEndsAtFormatted = timeFormat.format(tattvaResult.endTime.time)
            val planetEndsAtFormatted = timeFormat.format(planetResult.endTime.time)
            
            val notification = createNotification(
                tattvaName = tattvaName,
                tattvaType = tattvaType,
                tattvaEndsAt = tattvaEndsAtFormatted,
                planetName = planetName,
                planetType = planetType,
                planetEndsAt = planetEndsAtFormatted,
                locationName = locationName,
                timeZone = timeZone
            )
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notification)
            
            Log.d(TAG, "✅ Notification updated successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }
    
    private fun createNotification(
        tattvaName: String,
        tattvaType: TattvaType,
        tattvaEndsAt: String,
        planetName: String,
        planetType: PlanetType?,
        planetEndsAt: String,
        locationName: String,
        timeZone: Double
    ): Notification {
        // Intent pentru a deschide aplicația când se apasă pe notificare
        val intent = Intent(this, MainActivity:: class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Check settings
        val showTattva = settingsPreferences.getTattvaNotification()
        val showPlanet = settingsPreferences.getPlanetaryHourNotification()
        
        Log.d(TAG, "📱 Creating notification: showTattva=$showTattva, showPlanet=$showPlanet")
        
        // Alege iconul în funcție de ce este activat
        val iconRes = if (showTattva) {
            // When tattva is enabled (with or without planet), use tattva icon
            when (tattvaType) {
                TattvaType.TEJAS -> R.drawable.ic_tattva_tejas
                TattvaType.PRITHIVI -> R.drawable.ic_tattva_prithivi
                TattvaType.APAS -> R.drawable.ic_tattva_apas
                TattvaType.VAYU -> R.drawable.ic_tattva_vayu
                TattvaType.AKASHA -> R.drawable.ic_tattva_akasha
            }
        } else {
            // When only planet is enabled, use generic sun icon
            R.drawable.icon
        }
        
        // Emoji pentru tattva
        val tattvaEmoji = when (tattvaType) {
            TattvaType.TEJAS -> "🔺"
            TattvaType.PRITHIVI -> "🟨"
            TattvaType.APAS -> "🌙"
            TattvaType.VAYU -> "🔵"
            TattvaType.AKASHA -> "🟣"
        }
        
        // Planet emoji/symbol
        val planetEmoji = planetType?.code ?: ""
        
        // Build title - collapsed view (symbols only)
        val collapsedTitle = buildString {
            if (showTattva) append(tattvaEmoji)
            if (showPlanet && planetType != null) append(planetEmoji)
        }
        
        Log.d(TAG, "📱 Collapsed title: '$collapsedTitle' (tattva=$tattvaEmoji, planet=$planetEmoji)")
        
        // Build expanded content
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setSilent(true)
        
        // Set title and text based on what's enabled
        if (showTattva && showPlanet && planetType != null) {
            // Both enabled
            Log.d(TAG, "📱 Mode: BOTH tattva and planet enabled")
            builder.setContentTitle(collapsedTitle)
            builder.setContentText("SUN TATTVA - $locationName")
            
            builder.setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("SUN TATTVA - $locationName")
                .bigText("$tattvaEmoji $tattvaName - ends at $tattvaEndsAt (GMT${if (timeZone >= 0) "+" else ""}${String.format("%.1f", timeZone)})\n$planetEmoji $planetName - ends at $planetEndsAt (GMT${if (timeZone >= 0) "+" else ""}${String.format("%.1f", timeZone)})"))
        } else if (showTattva) {
            // Only Tattva
            Log.d(TAG, "📱 Mode: ONLY tattva enabled")
            builder.setContentTitle(collapsedTitle)
            builder.setContentText("SUN TATTVA - $locationName")
            
            val expandedText = "$tattvaEmoji $tattvaName - ends at $tattvaEndsAt (GMT${if (timeZone >= 0) "+" else ""}${String.format("%.1f", timeZone)})"
            builder.setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("SUN TATTVA - $locationName")
                .bigText(expandedText))
        } else if (showPlanet && planetType != null) {
            // Only Planet
            Log.d(TAG, "📱 Mode: ONLY planet enabled")
            builder.setContentTitle(collapsedTitle)
            builder.setContentText("SUN TATTVA - $locationName")
            
            val expandedText = "$planetEmoji $planetName - ends at $planetEndsAt (GMT${if (timeZone >= 0) "+" else ""}${String.format("%.1f", timeZone)})"
            builder.setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("SUN TATTVA - $locationName")
                .bigText(expandedText))
        }
        
        return builder.build()
    }
}
