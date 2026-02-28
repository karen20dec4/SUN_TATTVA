package com.android.sun.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.sun.MainActivity
import com.android.sun.R

/**
 * Helper pentru crearea si trimiterea notificarilor
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_MOON = "moon_notifications"
        const val CHANNEL_NAME_MOON = "Moon Phase Notifications"
        
        const val NOTIFICATION_ID_FULL_MOON_START = 1001
        const val NOTIFICATION_ID_FULL_MOON_END = 1002
        const val NOTIFICATION_ID_TRIPURA_SUNDARI = 1003
        const val NOTIFICATION_ID_NEW_MOON = 1004
    }

    init {
        createNotificationChannels()
    }

    /**
     * Creeaza canalele de notificari (necesar pentru Android 8+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_MOON,
                CHANNEL_NAME_MOON,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for moon phases and special moments"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            
            com.android.sun.util.AppLog.d("NotificationHelper", "Notification channel created:  $CHANNEL_ID_MOON")
        }
    }

    /**
     * Trimite notificare pentru inceputul influentei Lunii Pline (18h inainte)
     */
    fun sendFullMoonStartNotification(fullMoonTime: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MOON)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("🌕 Full Moon Influence Started")
            .setContentText("Full moon peak at $fullMoonTime. The influence period has begun!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("The full moon influence period has started. Full moon peak will be at $fullMoonTime. This influence lasts 18 hours before and after the peak."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_FULL_MOON_START, notification)
            com.android.sun.util.AppLog.d("NotificationHelper", "Full Moon START notification sent")
        } catch (e:  SecurityException) {
            com.android.sun.util.AppLog.e("NotificationHelper", "No permission to send notification", e)
        }
    }

    /**
     * Trimite notificare pentru sfarsitul influentei Lunii Pline (18h dupa)
     */
    fun sendFullMoonEndNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MOON)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("🌕 Full Moon Influence Ended")
            .setContentText("The full moon influence period has ended.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_FULL_MOON_END, notification)
            com.android.sun.util.AppLog.d("NotificationHelper", "Full Moon END notification sent")
        } catch (e: SecurityException) {
            com.android.sun.util.AppLog.e("NotificationHelper", "No permission to send notification", e)
        }
    }

    /**
     * Trimite notificare pentru Tripura Sundari (24h inainte)
     */
    fun sendTripuraSundariNotification(tripuraTime: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MOON)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("⭐ Tripura Sundari Tomorrow")
            .setContentText("Tripura Sundari moment at $tripuraTime")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("The auspicious Tripura Sundari moment will occur tomorrow at $tripuraTime. Prepare for meditation and spiritual practices."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TRIPURA_SUNDARI, notification)
            com.android.sun.util.AppLog.d("NotificationHelper", "Tripura Sundari notification sent")
        } catch (e: SecurityException) {
            com.android.sun.util.AppLog.e("NotificationHelper", "No permission to send notification", e)
        }
    }

    /**
     * Trimite notificare pentru Luna Noua (24h inainte)
     */
    fun sendNewMoonNotification(newMoonTime: String) {
        val intent = Intent(context, MainActivity:: class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MOON)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("🌑 New Moon Tomorrow")
            .setContentText("New moon at $newMoonTime")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("The new moon will occur tomorrow at $newMoonTime.This is an auspicious time for new beginnings."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_NEW_MOON, notification)
            com.android.sun.util.AppLog.d("NotificationHelper", "New Moon notification sent")
        } catch (e: SecurityException) {
            com.android.sun.util.AppLog.e("NotificationHelper", "No permission to send notification", e)
        }
    }
}