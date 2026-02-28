package com.android.sun.data. repository

import android.content.Context
import android.content. SharedPreferences

/**
 * Manager pentru salvarea locației selectate în SharedPreferences
 * Persistă locația între sesiuni ale aplicației
 */
class LocationPreferences(context: Context) {
    
    private val prefs:  SharedPreferences = context.getSharedPreferences(
        "sun_location_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_LOCATION_ID = "selected_location_id"
        private const val KEY_LOCATION_NAME = "selected_location_name"
        private const val KEY_LATITUDE = "selected_latitude"
        private const val KEY_LONGITUDE = "selected_longitude"
        private const val KEY_ALTITUDE = "selected_altitude"
        private const val KEY_TIMEZONE = "selected_timezone"
        private const val KEY_IS_GPS = "selected_is_gps"
        private const val KEY_HAS_SAVED_LOCATION = "has_saved_location"
    }
    
    /**
     * Salvează locația selectată
     */
    fun saveSelectedLocation(
        id: Int,
        name:  String,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        timeZone: Double,
        isGPS: Boolean
    ) {
        prefs.edit().apply {
            putInt(KEY_LOCATION_ID, id)
            putString(KEY_LOCATION_NAME, name)
            putFloat(KEY_LATITUDE, latitude. toFloat())
            putFloat(KEY_LONGITUDE, longitude. toFloat())
            putFloat(KEY_ALTITUDE, altitude. toFloat())
            putFloat(KEY_TIMEZONE, timeZone.toFloat())
            putBoolean(KEY_IS_GPS, isGPS)
            putBoolean(KEY_HAS_SAVED_LOCATION, true)
            apply()
        }
        com.android.sun.util.AppLog.d("LocationPreferences", "✅ Saved location: $name ($latitude, $longitude)")
    }
    
    /**
     * Verifică dacă există o locație salvată
     */
    fun hasSavedLocation(): Boolean {
        return prefs.getBoolean(KEY_HAS_SAVED_LOCATION, false)
    }
    
    /**
     * Obține ID-ul locației salvate
     */
    fun getSavedLocationId(): Int {
        return prefs.getInt(KEY_LOCATION_ID, 0)
    }
    
    /**
     * Obține numele locației salvate
     */
    fun getSavedLocationName(): String {
        return prefs.getString(KEY_LOCATION_NAME, com.android.sun.util.AppDefaults.LOCATION_NAME) ?: com.android.sun.util.AppDefaults.LOCATION_NAME
    }
    
    /**
     * Obține latitudinea salvată
     */
    fun getSavedLatitude(): Double {
        return prefs. getFloat(KEY_LATITUDE, com.android.sun.util.AppDefaults.LATITUDE.toFloat()).toDouble()
    }
    
    /**
     * Obține longitudinea salvată
     */
    fun getSavedLongitude(): Double {
        return prefs. getFloat(KEY_LONGITUDE, com.android.sun.util.AppDefaults.LONGITUDE.toFloat()).toDouble()
    }
    
    /**
     * Obține altitudinea salvată
     */
    fun getSavedAltitude(): Double {
        return prefs.getFloat(KEY_ALTITUDE, com.android.sun.util.AppDefaults.ALTITUDE.toFloat()).toDouble()
    }
    
    /**
     * Obține timezone-ul salvat
     */
    fun getSavedTimeZone(): Double {
        return prefs.getFloat(KEY_TIMEZONE, com.android.sun.util.AppDefaults.TIME_ZONE.toFloat()).toDouble()
    }
    
    /**
     * Verifică dacă locația salvată este GPS
     */
    fun isSavedLocationGPS(): Boolean {
        return prefs. getBoolean(KEY_IS_GPS, false)
    }
    
    /**
     * Șterge locația salvată (resetează la default)
     */
    fun clearSavedLocation() {
        prefs.edit().apply {
            putBoolean(KEY_HAS_SAVED_LOCATION, false)
            apply()
        }
        com.android.sun.util.AppLog.d("LocationPreferences", "🗑️ Cleared saved location")
    }
}