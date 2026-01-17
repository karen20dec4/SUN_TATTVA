package com.android.sun.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.sun.data.model.AstroData
import com.android.sun.data.model.TattvaDayItem
import com.android.sun.data.repository.AstroRepository
import java.util.Calendar

/**
 * ViewModel dedicat pentru calcule astrologice și All Day View
 */
class AstroViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AstroRepository(application)
    
    /**
     * Generează schedule-ul complet pentru All Day View
     */
    fun generateTattvaDaySchedule(astroData: AstroData): List<TattvaDayItem> {
        return repository.generateTattvaDaySchedule(
            sunriseTime = astroData.sunrise,
            latitude = astroData.latitude,
            longitude = astroData.longitude,
            timeZone = astroData.timeZone,
            currentTime = astroData.currentTime,
            locationName = astroData.locationName
        )
    }
    
    /**
     * ✅ Generează schedule-ul cu timpul CURENT specificat (nu cel din astroData)
     */
    fun generateTattvaDayScheduleWithCurrentTime(
        astroData: AstroData,
        currentTime: Calendar
    ): List<TattvaDayItem> {
        return repository.generateTattvaDaySchedule(
            sunriseTime = astroData.sunrise,
            latitude = astroData.latitude,
            longitude = astroData.longitude,
            timeZone = astroData.timeZone,
            currentTime = currentTime,  // ✅ Timpul CURENT, nu cel vechi!
            locationName = astroData.locationName
        )
    }
    
    /**
     * Generează schedule pentru ziua următoare
     */
    fun generateNextDaySchedule(astroData: AstroData): List<TattvaDayItem> {
        val nextDaySunrise = astroData.sunrise.clone() as Calendar
        nextDaySunrise.add(Calendar.DAY_OF_MONTH, 1)
        
        return repository.generateTattvaDaySchedule(
            sunriseTime = nextDaySunrise,
            latitude = astroData.latitude,
            longitude = astroData.longitude,
            timeZone = astroData.timeZone,
            currentTime = astroData.currentTime,
            locationName = astroData.locationName
        )
    }
    
    /**
     * Calculează răsăritul și apusul pentru o dată specifică
     */
    fun calculateSunriseSunsetForDate(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        timeZone: Double
    ): Pair<Calendar, Calendar> {
        return repository.calculateSunriseSunsetForDate(
            year, month, day, latitude, longitude, timeZone
        )
    }
    
    /**
     * Generează schedule pentru o dată specifică
     */
    fun generateScheduleForDate(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        timeZone: Double,
        currentTime: Calendar = Calendar.getInstance(),
        locationName: String = "București"  // ✅ Adăugat pentru timezone corect
    ): List<TattvaDayItem> {
        return repository.generateScheduleForDate(
            year, month, day, latitude, longitude, timeZone, currentTime, locationName
        )
    }
}