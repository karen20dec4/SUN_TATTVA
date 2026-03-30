package com.android.sun.domain.calculator

import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.PI

/**
 * ✅ Calculator OPTIMIZAT pentru fazele lunii
 * - Folosește pași mari (1 zi) apoi rafinează cu ore/minute
 * - Reduce de 10x numărul de apeluri SwissEph
 * - ✅ FIX: Lucrează în UTC, apoi convertește la timezone-ul locației
 */
class MoonPhaseCalculator(
    private val astroCalculator:  AstroCalculator,
    private val locationTimeZone: TimeZone  // ✅ Primim timezone-ul locației
) {

    private val utcTimeZone = TimeZone.getTimeZone("UTC")

    /**
     * Calculează informațiile despre fazele lunii
     */
    fun calculateMoonPhase(
        moonLongitude: Double,
        sunLongitude: Double,
        currentTime: Calendar
    ): MoonPhaseResult {
        var diff = moonLongitude - sunLongitude
        
        while (diff < 0) diff += 360.0
        while (diff >= 360) diff -= 360.0
        
        val illuminationPercent = calculateIllumination(diff)
        
        // ✅ Calculează următoarele evenimente (returnează în timezone locației)
        val nextTripuraSundari = findNextPhase(currentTime, 154.2833)
        val nextFullMoon = findNextPhase(currentTime, 180.0)
        val nextNewMoon = findNextPhase(currentTime, 0.0)
        
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "=== MOON PHASE DEBUG ===")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Location TZ: ${locationTimeZone. id}")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Moon:      $moonLongitude°, Sun:   $sunLongitude°")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Phase angle: $diff°")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Illumination:  $illuminationPercent%")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Next Tripura Sundari: ${formatDate(nextTripuraSundari)}")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Next Full Moon:    ${formatDate(nextFullMoon)}")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Next New Moon:   ${formatDate(nextNewMoon)}")
        
        // ✅ Check if we're within ~18h of full moon peak
        // Moon-Sun relative speed averages ~12.2°/day (moon ~13.2° - sun ~1°)
        // 18 hours ≈ 9.15° of relative motion. Use 9.5° for safety margin.
        val fullMoonAngleDiff = getAngleDifference(diff, 180.0)
        val isInFullMoonInfluence = fullMoonAngleDiff <= 9.5
        
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Full Moon angle diff: $fullMoonAngleDiff°, influence: $isInFullMoonInfluence")
        
        // ✅ Calculate Shivaratri dates (Krishna Chaturdashi night - phase angle ~342°)
        val nextShivaratriTime = findNextPhase(currentTime, 342.0)
        val nextShivaratri = calculateShivaratriNight(nextShivaratriTime)
        val yearlyShivaratri = calculateNextShivaratriPeriods(currentTime, 12)
        
        // ✅ Calculate next 12 full moon dates
        val futureFullMoons = calculateNextFullMoons(currentTime, 12)
        
        // ✅ Calculate next 12 Tripura Sundari dates
        val futureTripuraSundari = calculateNextTripuraSundariDates(currentTime, 12)
        
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Next Shivaratri: ${formatDate(nextShivaratriTime)}")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Future Shivaratri dates: ${yearlyShivaratri.size}")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Future Full Moons: ${futureFullMoons.size}")
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "Future Tripura Sundari: ${futureTripuraSundari.size}")
        
        return MoonPhaseResult(
            phaseAngle = diff,
            illuminationPercent = illuminationPercent,
            nextTripuraSundari = nextTripuraSundari,
            nextFullMoon = nextFullMoon,
            nextNewMoon = nextNewMoon,
            isInFullMoonInfluence = isInFullMoonInfluence,
            nextShivaratri = nextShivaratri,
            yearlyShivaratri = yearlyShivaratri,
            futureFullMoons = futureFullMoons,
            futureTripuraSundari = futureTripuraSundari
        )
    }

    private fun calculateIllumination(phaseAngle: Double): Int {
        // Convert angle to radians using Kotlin's idiomatic approach
        val phaseRadians = phaseAngle * PI / 180.0
        // Use the correct formula: (1 - cos(angle)) / 2 * 100
        // This gives accurate illumination throughout the lunar cycle:
        // 0° = 0%, 90° = 50%, 180° = 100%, 270° = 50%, 360° = 0%
        return (((1.0 - cos(phaseRadians)) / 2.0) * 100).toInt()
    }

    /**
     * ✅ OPTIMIZAT:  Găsește faza în 3 pași (zi → oră → minut)
     * ✅ FIX:  Lucrează în UTC pentru calcul, apoi convertește rezultatul la timezone locației
     */
    private fun findNextPhase(startTime:  Calendar, targetAngle: Double): Calendar {
        // ✅ Convertim startTime la UTC pentru calcul
        val utcStartTime = Calendar.getInstance(utcTimeZone)
        utcStartTime.timeInMillis = startTime.timeInMillis
        
        val currentAngle = getCurrentPhaseAngle(utcStartTime)
        
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "📍 START SEARCH: target=$targetAngle°, current=$currentAngle°")
        
        // Estimează câte zile până la fază
        val degreesToTarget = if (targetAngle == 0.0) {
            if (currentAngle < 180) 360.0 - currentAngle else 360.0 - currentAngle
        } else if (currentAngle < targetAngle) {
            targetAngle - currentAngle
        } else {
            360.0 - currentAngle + targetAngle
        }
        
        val estimatedDays = ((degreesToTarget / 13.2).toInt() + 1).coerceIn(2, 35)
        
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "  Degrees to go: $degreesToTarget°, estimated days: $estimatedDays")
        
        // ✅ PAS 1: Caută cu pași de 1 ZI (în UTC)
        var bestTime = utcStartTime.clone() as Calendar
        var bestDiff = 999.0
        
        val searchEnd = utcStartTime.clone() as Calendar
        searchEnd.add(Calendar.DAY_OF_MONTH, estimatedDays + 5)
        
        var current = utcStartTime.clone() as Calendar
        while (current.timeInMillis < searchEnd.timeInMillis) {
            val angle = getCurrentPhaseAngle(current)
            val diff = getAngleDifference(angle, targetAngle)
            
            if (diff < bestDiff) {
                bestDiff = diff
                bestTime = current.clone() as Calendar
            }
            
            current.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "  After DAY search:   ${formatDate(bestTime)}")
        
        // ✅ PAS 2: Rafinare cu pași de 1 ORĂ
        val hourStart = bestTime.clone() as Calendar
        hourStart. add(Calendar.HOUR_OF_DAY, -12)
        val hourEnd = bestTime. clone() as Calendar
        hourEnd.add(Calendar.HOUR_OF_DAY, 12)
        
        current = hourStart
        while (current.timeInMillis < hourEnd.timeInMillis) {
            val angle = getCurrentPhaseAngle(current)
            val diff = getAngleDifference(angle, targetAngle)
            
            if (diff < bestDiff) {
                bestDiff = diff
                bestTime = current.clone() as Calendar
            }
            
            current.add(Calendar. HOUR_OF_DAY, 1)
        }
        
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "  After HOUR search:  ${formatDate(bestTime)}")
        
        // ✅ PAS 3: Rafinare FINALĂ cu pași de 1 MINUT
        val minuteStart = bestTime.clone() as Calendar
        minuteStart.add(Calendar. HOUR_OF_DAY, -1)
        val minuteEnd = bestTime.clone() as Calendar
        minuteEnd.add(Calendar.HOUR_OF_DAY, 1)
        
        current = minuteStart
        while (current.timeInMillis < minuteEnd.timeInMillis) {
            val angle = getCurrentPhaseAngle(current)
            val diff = getAngleDifference(angle, targetAngle)
            
            if (diff < bestDiff) {
                bestDiff = diff
                bestTime = current.clone() as Calendar
            }
            
            current.add(Calendar.MINUTE, 1)
        }
        
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "✅ FOUND phase $targetAngle° (UTC):  ${formatDate(bestTime)}, diff=$bestDiff°")
        
        // ✅ Convertim rezultatul de la UTC la timezone-ul locației
        val locationResult = Calendar.getInstance(locationTimeZone)
        locationResult. timeInMillis = bestTime. timeInMillis
        
        com.android.sun.util.AppLog.d("MoonPhaseCalculator", "✅ Converted to ${locationTimeZone.id}: ${formatDate(locationResult)}")
        
        return locationResult
    }

    /**
     * ✅ Calculează unghiul fazei pentru un moment dat
     * IMPORTANT: time TREBUIE să fie în UTC! 
     */
    private fun getCurrentPhaseAngle(time: Calendar): Double {
        val year = time.get(Calendar.YEAR)
        val month = time. get(Calendar.MONTH) + 1
        val day = time.get(Calendar.DAY_OF_MONTH)
        val hour = time.get(Calendar. HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        val second = time.get(Calendar.SECOND)
        
        val moonLon = astroCalculator.calculateMoonLongitude(year, month, day, hour, minute, second)
        val sunLon = astroCalculator.calculateSunLongitude(year, month, day, hour, minute, second)
        
        var diff = moonLon - sunLon
        while (diff < 0) diff += 360.0
        while (diff >= 360) diff -= 360.0
        
        return diff
    }

    private fun getAngleDifference(angle1: Double, angle2: Double): Double {
        var diff = abs(angle1 - angle2)
        if (diff > 180) diff = 360 - diff
        return diff
    }

    /**
     * Calculate the Shivaratri night from the Krishna Chaturdashi midpoint time.
     * Shivaratri = night of Krishna Chaturdashi (14th tithi of dark fortnight, phase angle 336°-348°).
     * The night spans from evening of one date to morning of the next.
     */
    private fun calculateShivaratriNight(chaturdashiMidpoint: Calendar): ShivaratriDate {
        val hour = chaturdashiMidpoint.get(Calendar.HOUR_OF_DAY)
        val eveningDate = chaturdashiMidpoint.clone() as Calendar
        val morningDate = chaturdashiMidpoint.clone() as Calendar
        
        if (hour < 12) {
            // Midpoint is in the morning - the night started the previous evening
            eveningDate.add(Calendar.DAY_OF_MONTH, -1)
        } else {
            // Midpoint is in the afternoon/evening - the night starts this evening
            morningDate.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return ShivaratriDate(eveningDate, morningDate)
    }

    /**
     * Calculate all Shivaratri dates for the current year.
     * Krishna Chaturdashi occurs approximately every 29.5 days (one per synodic month).
     * There are ~12-13 Shivaratri nights per year.
     */
    private fun calculateYearlyShivaratri(referenceTime: Calendar): List<ShivaratriDate> {
        // Max possible Shivaratri dates in a year (12-13) plus buffer for edge cases
        val maxShivaratriPerYear = 15
        // Min days between consecutive Krishna Chaturdashi phases (~29.5 day synodic month)
        // Use 25 days to safely avoid detecting the same phase twice
        val minDaysBetweenPhases = 25
        
        val year = referenceTime.get(Calendar.YEAR)
        val results = mutableListOf<ShivaratriDate>()
        
        val searchStart = Calendar.getInstance(locationTimeZone)
        searchStart.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        searchStart.set(Calendar.MILLISECOND, 0)
        
        var currentSearch = searchStart.clone() as Calendar
        
        for (i in 0 until maxShivaratriPerYear) {
            val chaturdashiTime = findNextPhase(currentSearch, 342.0)
            if (chaturdashiTime.get(Calendar.YEAR) > year) break
            
            results.add(calculateShivaratriNight(chaturdashiTime))
            
            currentSearch = chaturdashiTime.clone() as Calendar
            currentSearch.add(Calendar.DAY_OF_MONTH, minDaysBetweenPhases)
        }
        
        return results
    }

    /**
     * Calculate the next N Shivaratri periods starting from referenceTime.
     * A Shivaratri is considered past after 6:00 AM on the morning date.
     * Returns only future periods (not yet expired), up to count items.
     */
    private fun calculateNextShivaratriPeriods(referenceTime: Calendar, count: Int): List<ShivaratriDate> {
        val minDaysBetweenPhases = 25
        val maxSearch = count + 5 // Buffer for filtering past dates
        val results = mutableListOf<ShivaratriDate>()
        
        // Start searching from a bit before referenceTime to catch current period
        var currentSearch = referenceTime.clone() as Calendar
        currentSearch.add(Calendar.DAY_OF_MONTH, -2)
        
        for (i in 0 until maxSearch * 2) {
            if (results.size >= count) break
            
            val chaturdashiTime = findNextPhase(currentSearch, 342.0)
            val shivaratri = calculateShivaratriNight(chaturdashiTime)
            
            // A Shivaratri is considered past after 6:00 AM on the morning date
            val expiryTime = shivaratri.morningDate.clone() as Calendar
            expiryTime.set(Calendar.HOUR_OF_DAY, 6)
            expiryTime.set(Calendar.MINUTE, 0)
            expiryTime.set(Calendar.SECOND, 0)
            
            if (expiryTime.timeInMillis > referenceTime.timeInMillis) {
                // Only add if not already in the list (avoid duplicates)
                val isDuplicate = results.any { isSameDay(it.eveningDate, shivaratri.eveningDate) }
                if (!isDuplicate) {
                    results.add(shivaratri)
                }
            }
            
            currentSearch = chaturdashiTime.clone() as Calendar
            currentSearch.add(Calendar.DAY_OF_MONTH, minDaysBetweenPhases)
        }
        
        return results.take(count)
    }

    /**
     * Calculate the next N full moon dates starting from referenceTime.
     * Returns future full moon peak times.
     */
    private fun calculateNextFullMoons(referenceTime: Calendar, count: Int): List<Calendar> {
        val minDaysBetweenPhases = 25
        val results = mutableListOf<Calendar>()
        
        var currentSearch = referenceTime.clone() as Calendar
        
        for (i in 0 until count + 3) {
            if (results.size >= count) break
            
            val fullMoonTime = findNextPhase(currentSearch, 180.0)
            
            // Only add if it's in the future
            if (fullMoonTime.timeInMillis > referenceTime.timeInMillis) {
                results.add(fullMoonTime)
            }
            
            currentSearch = fullMoonTime.clone() as Calendar
            currentSearch.add(Calendar.DAY_OF_MONTH, minDaysBetweenPhases)
        }
        
        return results.take(count)
    }

    /**
     * Calculate the next N Tripura Sundari dates starting from referenceTime.
     * Tripura Sundari occurs at phase angle 154.2833° — approximately once per synodic month.
     * Returns future Tripura Sundari peak times.
     */
    private fun calculateNextTripuraSundariDates(referenceTime: Calendar, count: Int): List<Calendar> {
        val minDaysBetweenPhases = 25
        val results = mutableListOf<Calendar>()
        
        var currentSearch = referenceTime.clone() as Calendar
        
        for (i in 0 until count + 3) {
            if (results.size >= count) break
            
            val tripuraTime = findNextPhase(currentSearch, 154.2833)
            
            // Only add if it's in the future
            if (tripuraTime.timeInMillis > referenceTime.timeInMillis) {
                results.add(tripuraTime)
            }
            
            currentSearch = tripuraTime.clone() as Calendar
            currentSearch.add(Calendar.DAY_OF_MONTH, minDaysBetweenPhases)
        }
        
        return results.take(count)
    }

    /**
     * Helper to compare two Calendar dates (same day)
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
               cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    private fun formatDate(cal: Calendar): String {
        return String.format(
            "%04d-%02d-%02d %02d:%02d:%02d %s",
            cal.get(Calendar. YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND),
            cal.timeZone. id
        )
    }
}

data class MoonPhaseResult(
    val phaseAngle: Double,
    val illuminationPercent: Int,
    val nextTripuraSundari: Calendar,
    val nextFullMoon:  Calendar,
    val nextNewMoon: Calendar,
    val isInFullMoonInfluence: Boolean = false,
    val nextShivaratri: ShivaratriDate? = null,
    val yearlyShivaratri: List<ShivaratriDate> = emptyList(),
    val futureFullMoons: List<Calendar> = emptyList(),
    val futureTripuraSundari: List<Calendar> = emptyList()
)

data class ShivaratriDate(
    val eveningDate: Calendar,  // Evening when Shivaratri night starts
    val morningDate: Calendar   // Morning when Shivaratri night ends
)