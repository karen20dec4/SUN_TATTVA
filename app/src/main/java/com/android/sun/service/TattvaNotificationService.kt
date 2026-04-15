package com.android.sun.service

import android.app.AlarmManager
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
import com.android.sun.util.AppLog
import androidx.core.app.NotificationCompat
import com.android.sun.MainActivity
import com.android.sun.R
import com.android.sun.domain.calculator.TattvaType
import com.android.sun.domain.calculator.PlanetType
import com.android.sun.data.repository.AstroRepository
import com.android.sun.data.repository.LocationPreferences
import com.android.sun.data.preferences.SettingsPreferences
import com.android.sun.util.TimeZoneUtils
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
        
        private const val ALARM_REQUEST_CODE = 2001
        // Safety net interval: check every 5 minutes in case alarms are missed
        private const val SAFETY_NET_INTERVAL_MS = 5 * 60 * 1000L
        
        const val ACTION_LOCATION_CHANGED = "com.android.sun.LOCATION_CHANGED"
        const val ACTION_SETTINGS_CHANGED = "com.android.sun.SETTINGS_CHANGED"
        const val ACTION_ALARM_UPDATE = "com.android.sun.ACTION_ALARM_UPDATE"
        
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
    private var safetyNetJob: Job? = null
    private var locationChangeReceiver: BroadcastReceiver? = null
    private lateinit var settingsPreferences: SettingsPreferences
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        AppLog.d(TAG, "onCreate() called")
        settingsPreferences = SettingsPreferences(applicationContext)
        createNotificationChannel()
        registerLocationChangeReceiver()
        AppLog.d(TAG, "Service created successfully")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLog.d(TAG, "onStartCommand() called, action=${intent?.action}")
        
        // Always ensure foreground with a notification (required within 5s of startForegroundService)
        val initialNotification = createDetailedNotification("Loading astro data...", R.drawable.icon, CHANNEL_ID_TATTVA, "GROUP_TATTVA")
        startForeground(TATTVA_NOTIF_ID, initialNotification)
        
        AppLog.d(TAG, "Started in foreground with initial notification")
        
        // Trigger immediate update + schedule next alarm
        triggerUpdate()
        // Start safety net loop as fallback for missed alarms
        startSafetyNetLoop()
        return START_STICKY
    }

    /**
     * Triggers an immediate notification update and schedules an exact alarm
     * for the next Tattva/Planet change. Uses AlarmManager.setExactAndAllowWhileIdle()
     * so the alarm fires even during Android Doze mode.
     */
    private fun triggerUpdate() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            val delayMs = updateNotificationAndGetDelay()
            scheduleNextAlarm(delayMs)
        }
    }

    /**
     * Schedule an exact alarm to re-trigger the service at the next Tattva/Planet transition.
     * setExactAndAllowWhileIdle() works even in Doze mode, unlike coroutine delay().
     */
    private fun scheduleNextAlarm(delayMs: Long) {
        val waitMs = (delayMs + 1000L).coerceAtLeast(1000L)
        
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // On Android 12+ (API 31+), check if exact alarms are permitted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                AppLog.w(TAG, "Exact alarm permission not granted, relying on safety net loop")
                return
            }
            
            val intent = Intent(this, TattvaNotificationService::class.java).apply {
                action = ACTION_ALARM_UPDATE
            }
            val pendingIntent = PendingIntent.getForegroundService(
                this, ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val triggerAtMillis = System.currentTimeMillis() + waitMs
            
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            
            AppLog.d(TAG, "Exact alarm scheduled for next update in ${waitMs / 1000}s")
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to schedule exact alarm, relying on safety net loop", e)
        }
    }

    /**
     * Safety net: periodically update the notification every 5 minutes as a fallback.
     * This handles cases where exact alarms are missed (OEM restrictions, permission issues).
     * The coroutine delay may be deferred in Doze, but 5 minutes is short enough to limit staleness.
     */
    private fun startSafetyNetLoop() {
        safetyNetJob?.cancel()
        safetyNetJob = serviceScope.launch {
            while (isActive) {
                delay(SAFETY_NET_INTERVAL_MS)
                AppLog.d(TAG, "Safety net update triggered")
                val delayMs = updateNotificationAndGetDelay()
                scheduleNextAlarm(delayMs)
            }
        }
    }

    
	
	
	
	
	
	/**
	 * Updates the notification and returns the delay (in ms) until the next Tattva/Planet change.
	 * Returns 30_000 as fallback if calculation fails.
	 */
	private suspend fun updateNotificationAndGetDelay(): Long {
        AppLog.d(TAG, "updateNotificationAndGetDelay() called")
        
        val repository = AstroRepository(applicationContext)
        val locationPrefs = LocationPreferences(applicationContext)
        val showTattva = settingsPreferences.getTattvaNotification()
        val showPlanet = settingsPreferences.getPlanetaryHourNotification()

        if (!showTattva && !showPlanet) {
            stopSelf()
            return 30_000L
        }

        try {
            val baseTimeZone = locationPrefs.getSavedTimeZone()
            // ✅ DST: Aplică ora de vară (+1h) dacă e activat
            val dstOffset = if (settingsPreferences.getDstEnabled()) 1.0 else 0.0
            val timeZone = baseTimeZone + dstOffset
            val astroData = repository.calculateAstroData(
                locationPrefs.getSavedLatitude(),
                locationPrefs.getSavedLongitude(),
                timeZone,
                locationPrefs.getSavedLocationName()
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // ✅ Timezone uses the DST-adjusted offset directly
            val locationTimeZone = TimeZoneUtils.getLocationTimeZone(timeZone)
            val actualOffsetMs = locationTimeZone.getOffset(System.currentTimeMillis())
            val actualOffsetHours = actualOffsetMs / 3600000.0
            val gmtSuffix = "(${if (actualOffsetHours >= 0) "+" else ""}${String.format("%.1f", actualOffsetHours)})"
            
            // ✅ Localized "until" / "pana la" - use app language, not system locale
            val language = settingsPreferences.getLanguage()
            val locale = java.util.Locale(language)
            val config = android.content.res.Configuration(resources.configuration)
            config.setLocale(locale)
            val localizedContext = createConfigurationContext(config)
            val untilText = localizedContext.getString(R.string.notification_until)

            val now = System.currentTimeMillis()
            var nextChangeMs = Long.MAX_VALUE

            // 1. NOTIFICARE TATTVA
            if (showTattva) {
                val type = astroData.tattva.tattva
                val endTime = formatTime(astroData.tattva.endTime, locationTimeZone)
                val emoji = getTattvaEmoji(type)
                val tattvaText = "$emoji - $untilText $endTime $gmtSuffix"
                
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
                    AppLog.w(TAG, "Tattva already ended (stale data), retrying soon")
                    nextChangeMs = minOf(nextChangeMs, 2_000L)
                }
            } else {
                notificationManager.cancel(TATTVA_NOTIF_ID)
            }

            // 2. NOTIFICARE ORA PLANETARĂ
            if (showPlanet) {
                val planet = astroData.planet.planet
                val endTime = formatTime(astroData.planet.endTime, locationTimeZone)
                val emoji = getPlanetEmoji(planet)
                val planetText = "$emoji - $untilText $endTime $gmtSuffix"
                
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
                    AppLog.w(TAG, "Planetary hour already ended (stale data), retrying soon")
                    nextChangeMs = minOf(nextChangeMs, 2_000L)
                }
            } else {
                notificationManager.cancel(PLANET_NOTIF_ID)
            }

            AppLog.d(TAG, "═══════════════════════════════════════════════════════")
            
            // Return the time until the next change (or 30s fallback)
            return if (nextChangeMs == Long.MAX_VALUE) 30_000L else nextChangeMs

        } catch (e: Exception) {
            AppLog.e(TAG, "Update failed", e)
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
		AppLog.d(TAG, "createDetailedNotification(): title='$title', iconRes=$iconRes, channelId=$channelId, group=$groupKey")
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

    private fun formatTime(calendar: Calendar, tz: TimeZone): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = tz
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
        AppLog.d(TAG, "Registering broadcast receivers for location and settings changes")
        
        locationChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_LOCATION_CHANGED -> {
                        AppLog.d(TAG, "Received ACTION_LOCATION_CHANGED broadcast")
                        triggerUpdate() // Recalculate and reschedule alarm
                    }
                    ACTION_SETTINGS_CHANGED -> {
                        AppLog.d(TAG, "Received ACTION_SETTINGS_CHANGED broadcast")
                        triggerUpdate() // Recalculate and reschedule alarm
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
        
        AppLog.d(TAG, "Broadcast receivers registered successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelPendingAlarm()
        serviceScope.cancel()
        try { unregisterReceiver(locationChangeReceiver) } catch (e: Exception) {}
    }

    /**
     * Cancel any pending alarm to prevent orphaned wakeups after the service stops.
     */
    private fun cancelPendingAlarm() {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, TattvaNotificationService::class.java).apply {
                action = ACTION_ALARM_UPDATE
            }
            val pendingIntent = PendingIntent.getForegroundService(
                this, ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            AppLog.d(TAG, "Pending alarm cancelled")
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to cancel pending alarm", e)
        }
    }
}