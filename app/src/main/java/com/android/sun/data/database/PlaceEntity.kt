package com.android.sun.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity pentru Room Database
 * Stochează locațiile salvate de utilizator
 */
@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val name: String,           // Ex: "Bucuresti"
    val longitude: Double,      // Ex: 26.05
    val latitude: Double,       // Ex: 44.26
    val altitude: Double,       // Ex: 80.0 (metri)
    val timeZone: Double,       // Ex: 2.0 (UTC+2)
    val dst: Int                // 0 sau 1 (Daylight Saving Time)
) {
    /**
     * Calculează timezone-ul efectiv (cu DST)
     */
    fun getEffectiveTimeZone(): Double = timeZone + dst
    
    /**
     * Returnează coordonatele în format array pentru Swiss Ephemeris
     * [longitudine, latitudine, altitudine]
     */
    fun getGeoCoordinates(): DoubleArray {
        return doubleArrayOf(longitude, latitude, altitude)
    }
    
    /**
     * Afișare pentru liste
     */
    override fun toString(): String = name
}

/**
 * Extensie pentru a crea locația default (București)
 */
fun getDefaultPlace(): PlaceEntity {
    return com.android.sun.util.AppDefaults.getDefaultPlaceEntity()
}