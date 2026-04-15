package com.android.sun.util

import java.util.SimpleTimeZone
import java.util.TimeZone

/**
 * Utilitar centralizat pentru maparea offset-ului la TimeZone.
 * 
 * ✅ v2.35: Simplificat - folosește ÎNTOTDEAUNA SimpleTimeZone cu offset static.
 * DST (ora de vară) se controlează MANUAL din Settings.
 * Nu mai folosim rezolvare IANA automată (cauza bug-urilor DST pentru GPS).
 */
object TimeZoneUtils {

    /**
     * Obține TimeZone-ul pentru o locație bazat pe offset static.
     *
     * @param locationName Numele locației (nu mai este folosit pentru rezolvare IANA)
     * @param timeZoneOffset Offset-ul timezone-ului în ore (ex: 2.0 pentru UTC+2, 3.0 pentru UTC+3)
     * @return SimpleTimeZone cu offset-ul dat (fără DST automat)
     */
    fun getLocationTimeZone(locationName: String, timeZoneOffset: Double): TimeZone {
        val offsetMillis = (timeZoneOffset * 3600.0 * 1000.0).toInt()
        return SimpleTimeZone(offsetMillis, "Location")
    }
}
