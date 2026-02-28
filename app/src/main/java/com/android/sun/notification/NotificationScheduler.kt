package com.android.sun.notification

import android.content.Context
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Programeaza notificarile folosind WorkManager
 * Settings checks are handled by the caller (MainActivity) before calling these methods
 */
class NotificationScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        const val WORK_TAG_FULL_MOON_START = "full_moon_start"
        const val WORK_TAG_FULL_MOON_END = "full_moon_end"
        const val WORK_TAG_TRIPURA_SUNDARI = "tripura_sundari"
        const val WORK_TAG_NEW_MOON = "new_moon"
        
        const val KEY_EVENT_TIME = "event_time"
        const val KEY_NOTIFICATION_TYPE = "notification_type"
    }

    /**
     * Programeaza notificarea pentru Luna Plina
     * - 18h inainte de momentul maxim
     * - 18h dupa momentul maxim
     */
    fun scheduleFullMoonNotifications(fullMoonTimeMillis: Long) {
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        val fullMoonTimeFormatted = timeFormat.format(Date(fullMoonTimeMillis))

        // 18h inainte de luna plina
        val startNotificationTime = fullMoonTimeMillis - (18 * 60 * 60 * 1000L)
        if (startNotificationTime > now) {
            val delayMillis = startNotificationTime - now
            
            val inputData = workDataOf(
                KEY_EVENT_TIME to fullMoonTimeFormatted,
                KEY_NOTIFICATION_TYPE to "FULL_MOON_START"
            )

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG_FULL_MOON_START)
                .build()

            workManager.enqueueUniqueWork(
                WORK_TAG_FULL_MOON_START,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            com.android.sun.util.AppLog.d("NotificationScheduler", 
                "Scheduled Full Moon START notification in ${delayMillis / 3600000}h")
        }

        // 18h dupa luna plina
        val endNotificationTime = fullMoonTimeMillis + (18 * 60 * 60 * 1000L)
        if (endNotificationTime > now) {
            val delayMillis = endNotificationTime - now
            
            val inputData = workDataOf(
                KEY_NOTIFICATION_TYPE to "FULL_MOON_END"
            )

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG_FULL_MOON_END)
                .build()

            workManager.enqueueUniqueWork(
                WORK_TAG_FULL_MOON_END,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            com.android.sun.util.AppLog.d("NotificationScheduler", 
                "Scheduled Full Moon END notification in ${delayMillis / 3600000}h")
        }
    }

    /**
     * Programeaza notificarea pentru Tripura Sundari (24h inainte)
     */
    fun scheduleTripuraSundariNotification(tripuraTimeMillis:  Long) {
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        val tripuraTimeFormatted = timeFormat.format(Date(tripuraTimeMillis))

        // 24h inainte
        val notificationTime = tripuraTimeMillis - (24 * 60 * 60 * 1000L)
        if (notificationTime > now) {
            val delayMillis = notificationTime - now
            
            val inputData = workDataOf(
                KEY_EVENT_TIME to tripuraTimeFormatted,
                KEY_NOTIFICATION_TYPE to "TRIPURA_SUNDARI"
            )

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG_TRIPURA_SUNDARI)
                .build()

            workManager.enqueueUniqueWork(
                WORK_TAG_TRIPURA_SUNDARI,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            com.android.sun.util.AppLog.d("NotificationScheduler", 
                "Scheduled Tripura Sundari notification in ${delayMillis / 3600000}h")
        }
    }

    /**
     * Programeaza notificarea pentru Luna Noua (24h inainte)
     */
    fun scheduleNewMoonNotification(newMoonTimeMillis:  Long) {
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        val newMoonTimeFormatted = timeFormat.format(Date(newMoonTimeMillis))

        // 24h inainte
        val notificationTime = newMoonTimeMillis - (24 * 60 * 60 * 1000L)
        if (notificationTime > now) {
            val delayMillis = notificationTime - now
            
            val inputData = workDataOf(
                KEY_EVENT_TIME to newMoonTimeFormatted,
                KEY_NOTIFICATION_TYPE to "NEW_MOON"
            )

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG_NEW_MOON)
                .build()

            workManager.enqueueUniqueWork(
                WORK_TAG_NEW_MOON,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            com.android.sun.util.AppLog.d("NotificationScheduler", 
                "Scheduled New Moon notification in ${delayMillis / 3600000}h")
        }
    }

    /**
     * Anuleaza toate notificarile programate
     */
    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag(WORK_TAG_FULL_MOON_START)
        workManager.cancelAllWorkByTag(WORK_TAG_FULL_MOON_END)
        workManager.cancelAllWorkByTag(WORK_TAG_TRIPURA_SUNDARI)
        workManager.cancelAllWorkByTag(WORK_TAG_NEW_MOON)
        com.android.sun.util.AppLog.d("NotificationScheduler", "All notifications cancelled")
    }
}