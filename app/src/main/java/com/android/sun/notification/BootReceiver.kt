package com.android.sun.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver pentru reprogramarea notificarilor dupa restart telefon
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            com.android.sun.util.AppLog.d("BootReceiver", "Boot completed - rescheduling notifications")
            
            // TODO: Reprogrameaza notificarile din datele salvate
            // Aceasta va fi implementata cand avem datele despre luna plina/noua salvate
        }
    }
}