package com.android.sun.domain.calculator

import java.util.*

class AstroCalculator(private val swissEph: SwissEphWrapper) {

    fun calculateSunrise(
        year: Int,
        month: Int,
        day:  Int,
        longitude: Double,
        latitude: Double,
        timeZone:  Double
    ): Calendar {
        val jd = swissEph.getJulianDay(year, month, day, 0.0)
        
        val sunriseJD = swissEph.calculateRiseTransSet(
            jd,
            SwissEphWrapper. SE_SUN,
            longitude,
            latitude,
            1
        )
        
        // ✅ FIX: Pasăm timezone-ul locației pentru conversie corectă
        return julianDayToCalendar(sunriseJD, timeZone)
    }

    fun calculateSunset(
        year: Int,
        month:  Int,
        day: Int,
        longitude: Double,
        latitude: Double,
        timeZone: Double
    ): Calendar {
        val jd = swissEph.getJulianDay(year, month, day, 0.0)
        
        val sunsetJD = swissEph.calculateRiseTransSet(
            jd,
            SwissEphWrapper. SE_SUN,
            longitude,
            latitude,
            2
        )
        
        // ✅ FIX: Pasăm timezone-ul locației pentru conversie corectă
        return julianDayToCalendar(sunsetJD, timeZone)
    }

    /**
     * ✅ FIX: Convertește Julian Day la Calendar folosind timezone-ul LOCAȚIEI
     * 
     * PROBLEMA VECHE: 
     * - Swiss Ephemeris returnează ora în UTC (Julian Day)
     * - SimpleTimeZone cu offset fix nu ține cont de DST (Daylight Saving Time)
     * - Pentru București în iunie: ar trebui UTC+3 (EEST), nu UTC+2 (EET)
     * 
     * SOLUȚIA:
     * - Folosim Calendar cu UTC timezone
     * - Ora UTC este corectă (din Swiss Ephemeris)
     * - Formatarea cu timezone-ul corect se face în repository/UI
     * 
     * @param julianDay - Julian Day returnat de Swiss Ephemeris (în UTC)
     * @param timeZoneOffset - Offset-ul timezone-ului locației (UNUSED acum, păstrat pentru compatibilitate)
     * @return Calendar cu ora în UTC
     */
    private fun julianDayToCalendar(julianDay: Double, timeZoneOffset: Double): Calendar {
        // JD 2440587.5 = 1 ianuarie 1970, 00:00:00 UTC (Unix epoch)
        val unixMillis = ((julianDay - 2440587.5) * 86400000.0).toLong()
        
        // ✅ FIX DST: Folosim UTC timezone
        // Convertirea la timezone local cu DST se face la formatare
        val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = unixMillis
        
        android.util.Log.d("AstroCalculator", "═══════════════════════════════════════")
        android.util.Log.d("AstroCalculator", "📍 Julian Day: $julianDay")
        android.util.Log.d("AstroCalculator", "📍 Unix millis (UTC): $unixMillis")
        android.util.Log.d("AstroCalculator", "📍 Calendar TimeZone: ${calendar.timeZone.id}")
        android.util.Log.d("AstroCalculator", "📍 Ora UTC: ${calendar.get(Calendar.HOUR_OF_DAY)}:${String.format("%02d", calendar.get(Calendar.MINUTE))}:${String.format("%02d", calendar.get(Calendar.SECOND))}")
        android.util.Log.d("AstroCalculator", "═══════════════════════════════════════")
        
        return calendar
    }

    fun calculateMoonLongitude(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int, second: Int
    ): Double {
        val dayFraction = hour + minute / 60.0 + second / 3600.0
        val jd = swissEph.getJulianDay(year, month, day, dayFraction)
        return swissEph.calculateBodyPosition(jd, SwissEphWrapper. SE_MOON)
    }

    /**
     * Calculează longitudinea Lunii și viteza ei (°/zi)
     * Returnează Pair(longitude, speedDegreesPerDay)
     */
    fun calculateMoonLongitudeWithSpeed(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int, second: Int
    ): Pair<Double, Double> {
        val dayFraction = hour + minute / 60.0 + second / 3600.0
        val jd = swissEph.getJulianDay(year, month, day, dayFraction)
        return swissEph.calculateBodyPositionWithSpeed(jd, SwissEphWrapper.SE_MOON)
    }

    fun calculateSunLongitude(
        year: Int, month: Int, day:  Int,
        hour: Int, minute:  Int, second: Int
    ): Double {
        val dayFraction = hour + minute / 60.0 + second / 3600.0
        val jd = swissEph.getJulianDay(year, month, day, dayFraction)
        return swissEph. calculateBodyPosition(jd, SwissEphWrapper.SE_SUN)
    }
}