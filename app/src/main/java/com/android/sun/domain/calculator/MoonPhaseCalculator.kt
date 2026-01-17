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
        
        android.util.Log. d("MoonPhaseCalculator", "=== MOON PHASE DEBUG ===")
        android.util.Log.d("MoonPhaseCalculator", "Location TZ: ${locationTimeZone. id}")
        android.util. Log.d("MoonPhaseCalculator", "Moon:      $moonLongitude°, Sun:   $sunLongitude°")
        android.util.Log. d("MoonPhaseCalculator", "Phase angle: $diff°")
        android.util.Log.d("MoonPhaseCalculator", "Illumination:  $illuminationPercent%")
        android.util.Log. d("MoonPhaseCalculator", "Next Tripura Sundari: ${formatDate(nextTripuraSundari)}")
        android.util.Log.d("MoonPhaseCalculator", "Next Full Moon:    ${formatDate(nextFullMoon)}")
        android.util.Log. d("MoonPhaseCalculator", "Next New Moon:   ${formatDate(nextNewMoon)}")
        
        return MoonPhaseResult(
            phaseAngle = diff,
            illuminationPercent = illuminationPercent,
            nextTripuraSundari = nextTripuraSundari,
            nextFullMoon = nextFullMoon,
            nextNewMoon = nextNewMoon
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
        
        android.util.Log.d("MoonPhaseCalculator", "📍 START SEARCH: target=$targetAngle°, current=$currentAngle°")
        
        // Estimează câte zile până la fază
        val degreesToTarget = if (targetAngle == 0.0) {
            if (currentAngle < 180) 360.0 - currentAngle else 360.0 - currentAngle
        } else if (currentAngle < targetAngle) {
            targetAngle - currentAngle
        } else {
            360.0 - currentAngle + targetAngle
        }
        
        val estimatedDays = ((degreesToTarget / 13.2).toInt() + 1).coerceIn(2, 35)
        
        android.util.Log.d("MoonPhaseCalculator", "  Degrees to go: $degreesToTarget°, estimated days: $estimatedDays")
        
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
        
        android.util.Log.d("MoonPhaseCalculator", "  After DAY search:   ${formatDate(bestTime)}")
        
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
        
        android.util.Log. d("MoonPhaseCalculator", "  After HOUR search:  ${formatDate(bestTime)}")
        
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
        
        android.util.Log.d("MoonPhaseCalculator", "✅ FOUND phase $targetAngle° (UTC):  ${formatDate(bestTime)}, diff=$bestDiff°")
        
        // ✅ Convertim rezultatul de la UTC la timezone-ul locației
        val locationResult = Calendar.getInstance(locationTimeZone)
        locationResult. timeInMillis = bestTime. timeInMillis
        
        android.util.Log.d("MoonPhaseCalculator", "✅ Converted to ${locationTimeZone.id}: ${formatDate(locationResult)}")
        
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
    val nextNewMoon: Calendar
)