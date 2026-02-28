package com.android.sun.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker pentru trimiterea notificarilor programate
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        
        val notificationType = inputData.getString(NotificationScheduler.KEY_NOTIFICATION_TYPE) ?: ""
        val eventTime = inputData.getString(NotificationScheduler.KEY_EVENT_TIME) ?: ""

        com.android.sun.util.AppLog.d("NotificationWorker", "Executing notification:  $notificationType")

        return try {
            when (notificationType) {
                "FULL_MOON_START" -> {
                    notificationHelper.sendFullMoonStartNotification(eventTime)
                }
                "FULL_MOON_END" -> {
                    notificationHelper.sendFullMoonEndNotification()
                }
                "TRIPURA_SUNDARI" -> {
                    notificationHelper.sendTripuraSundariNotification(eventTime)
                }
                "NEW_MOON" -> {
                    notificationHelper.sendNewMoonNotification(eventTime)
                }
                else -> {
                    com.android.sun.util.AppLog.w("NotificationWorker", "Unknown notification type: $notificationType")
                }
            }
            Result.success()
        } catch (e: Exception) {
            com.android.sun.util.AppLog.e("NotificationWorker", "Error sending notification", e)
            Result.failure()
        }
    }
}