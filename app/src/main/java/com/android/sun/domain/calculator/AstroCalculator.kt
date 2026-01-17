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
     * ✅ FIX: Convertește Julian Day la Calendar folosind timezone-ul LOCAȚIEI cu suport DST
     * 
     * PROBLEMA: 
     * - Swiss Ephemeris returnează ora în UTC (Julian Day)
     * - SimpleTimeZone cu offset fix nu ține cont de Daylight Saving Time (DST)
     * - Pentru București în vară (EEST): UTC+3, nu UTC+2
     * 
     * SOLUȚIA:
     * - Convertim JD la Calendar în UTC
     * - Aplicăm DST rules automat folosind TimeZone.getAvailableIDs pentru regiune
     * - Dacă nu găsim timezone ID, folosim offset-ul standard
     * 
     * @param julianDay - Julian Day returnat de Swiss Ephemeris (în UTC)
     * @param timeZoneOffset - Offset-ul timezone-ului standard al locației (ex: 2.0 pentru București EET)
     * @return Calendar cu ora corectă în timezone-ul locației (cu DST aplicat automat)
     */
    private fun julianDayToCalendar(julianDay: Double, timeZoneOffset: Double): Calendar {
        // JD 2440587.5 = 1 ianuarie 1970, 00:00:00 UTC (Unix epoch)
        val unixMillis = ((julianDay - 2440587.5) * 86400000.0).toLong()
        
        // ✅ Creăm Calendar în UTC mai întâi
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCalendar.timeInMillis = unixMillis
        
        // ✅ Determinăm timezone-ul potrivit bazat pe offset
        // Pentru locații cu DST, Java va aplica automat regulile DST
        val timezone = getTimezoneForOffset(timeZoneOffset)
        
        // ✅ Creează Calendar cu timezone-ul corespunzător
        val calendar = Calendar.getInstance(timezone)
        calendar.timeInMillis = unixMillis
        
        // ✅ DEBUG LOG
        android.util.Log.d("AstroCalculator", "═══════════════════════════════════════")
        android.util.Log.d("AstroCalculator", "📍 TimeZone offset: $timeZoneOffset ore")
        android.util.Log.d("AstroCalculator", "📍 TimeZone ID: ${timezone.id}")
        android.util.Log.d("AstroCalculator", "📍 DST in effect: ${timezone.inDaylightTime(calendar.time)}")
        android.util.Log.d("AstroCalculator", "📍 Actual offset: ${timezone.getOffset(calendar.timeInMillis) / 3600000.0} ore")
        android.util.Log.d("AstroCalculator", "📍 Ora rezultată: ${String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))}")
        android.util.Log.d("AstroCalculator", "═══════════════════════════════════════")
        
        return calendar
    }
    
    /**
     * Determină timezone-ul corect bazat pe offset-ul standard
     * Încearcă să găsească un timezone cu DST rules pentru offset-ul dat
     */
    private fun getTimezoneForOffset(standardOffset: Double): TimeZone {
        // Mapare specială pentru timezone-uri cunoscute cu DST
        val timezoneMap = mapOf(
            2.0 to "Europe/Bucharest",  // România: EET (UTC+2) / EEST (UTC+3)
            1.0 to "Europe/Paris",       // Europa Centrală: CET (UTC+1) / CEST (UTC+2)
            0.0 to "Europe/London",      // UK: GMT (UTC+0) / BST (UTC+1)
            -5.0 to "America/New_York",  // EST (UTC-5) / EDT (UTC-4)
            -6.0 to "America/Chicago",   // CST (UTC-6) / CDT (UTC-5)
            -7.0 to "America/Denver",    // MST (UTC-7) / MDT (UTC-6)
            -8.0 to "America/Los_Angeles", // PST (UTC-8) / PDT (UTC-7)
            3.0 to "Europe/Moscow",      // MSK (UTC+3) - no DST from 2014
            5.5 to "Asia/Kolkata",       // IST (UTC+5:30) - no DST
            8.0 to "Asia/Shanghai",      // CST (UTC+8) - no DST
            9.0 to "Asia/Tokyo"          // JST (UTC+9) - no DST
        )
        
        // Încearcă să găsească timezone din mapare
        val timezoneId = timezoneMap[standardOffset]
        if (timezoneId != null) {
            return TimeZone.getTimeZone(timezoneId)
        }
        
        // Fallback: creează SimpleTimeZone cu offset fix (fără DST)
        val offsetMillis = (standardOffset * 3600.0 * 1000.0).toInt()
        return SimpleTimeZone(offsetMillis, "GMT${if (standardOffset >= 0) "+" else ""}${standardOffset.toInt()}")
    }

    fun calculateMoonLongitude(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int, second: Int
    ): Double {
        val dayFraction = hour + minute / 60.0 + second / 3600.0
        val jd = swissEph.getJulianDay(year, month, day, dayFraction)
        return swissEph.calculateBodyPosition(jd, SwissEphWrapper. SE_MOON)
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