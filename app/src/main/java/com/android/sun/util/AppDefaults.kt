package com.android.sun.util

import com.android.sun.data.database.PlaceEntity
import com.android.sun.data.model.LocationData

/**
 * Constantele default ale aplicației.
 * Sursă unică de adevăr pentru locația default (București).
 *
 * Înlocuiește valorile hardcodate din:
 * - MainViewModel.getDefaultLocation()
 * - LocationViewModel.deleteLocation()
 * - LocationRepository.loadDefaultLocations()
 * - LocationRepository.clearSavedLocations()
 * - LocationRepository.ensureBucharestExists()
 * - PlaceDatabase.DatabaseCallback.populateDatabase()
 * - PlaceEntity.getDefaultPlace()
 * - LocationPreferences (default values)
 */
object AppDefaults {

    // Locația default: București, România
    const val LOCATION_NAME = "București"
    const val LATITUDE = 44.4268
    const val LONGITUDE = 26.1025
    const val ALTITUDE = 80.0
    const val TIME_ZONE = 2.0
    const val DST = 0

    /**
     * Creează un PlaceEntity default (București)
     */
    fun getDefaultPlaceEntity(): PlaceEntity {
        return PlaceEntity(
            name = LOCATION_NAME,
            longitude = LONGITUDE,
            latitude = LATITUDE,
            altitude = ALTITUDE,
            timeZone = TIME_ZONE,
            dst = DST
        )
    }

    /**
     * Creează un LocationData default (București)
     */
    fun getDefaultLocationData(): LocationData {
        return LocationData(
            id = 0,
            name = LOCATION_NAME,
            latitude = LATITUDE,
            longitude = LONGITUDE,
            altitude = ALTITUDE,
            timeZone = TIME_ZONE,
            isCurrentLocation = false
        )
    }
}
