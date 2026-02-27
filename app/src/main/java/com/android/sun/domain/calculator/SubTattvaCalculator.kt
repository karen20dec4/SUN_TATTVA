package com.android.sun.domain.calculator

import java.util.*

/**
 * Calculator pentru SubTattva (perioadă fixă de 4.8 minute = 288 secunde)
 * Ciclul ÎNCEPE de la RĂSĂRIT cu SubTattva Akasha
 * 1 SubTattva = 288 secunde (4 minute 48 secunde)
 * 1 Tattva = 5 SubTattva-uri = 1440 secunde (24 minute)
 */
class SubTattvaCalculator {

    companion object {
        const val SUBTATTVA_DURATION_MS = 288 * 1000L // 288 secunde = 4min 48sec
        const val SUBTATTVAS_PER_TATTVA = 5
    }

    /**
     * Calculează SubTattva-ul curent bazat pe răsăritul soarelui
     */
    @Suppress("UNUSED_PARAMETER")
    fun calculateCurrentSubTattva(
        currentTime: Calendar,
        sunrise: Calendar,
        sunset: Calendar
    ): SubTattvaResult {
        val current = currentTime.clone() as Calendar
        val sunriseLocal = sunrise.clone() as Calendar
        
        // Calculează timpul scurs de la răsărit
        val elapsedSinceSunrise = current.timeInMillis - sunriseLocal.timeInMillis
        
        // Dacă suntem ÎNAINTE de răsărit, folosim răsăritul de ieri
        val adjustedElapsed = if (elapsedSinceSunrise < 0) {
            val yesterdaySunrise = sunriseLocal.clone() as Calendar
            yesterdaySunrise.add(Calendar.DAY_OF_MONTH, -1)
            current.timeInMillis - yesterdaySunrise.timeInMillis
        } else {
            elapsedSinceSunrise
        }
        
        // Calculează index-ul SubTattva de la răsărit
        val subTattvaIndexFromSunrise = (adjustedElapsed / SUBTATTVA_DURATION_MS).toInt()
        
        // Index-ul în Tattva (0-4: Akasha, Vayu, Tejas, Apas, Prithivi)
        val subTattvaIndexInTattva = subTattvaIndexFromSunrise % SUBTATTVAS_PER_TATTVA
        
        // Calculează start și end pentru SubTattva curentă
        val referencePoint = if (elapsedSinceSunrise < 0) {
            val yesterdaySunrise = sunriseLocal.clone() as Calendar
            yesterdaySunrise.add(Calendar.DAY_OF_MONTH, -1)
            yesterdaySunrise.timeInMillis
        } else {
            sunriseLocal.timeInMillis
        }
        
        val subTattvaStartMillis = referencePoint + (subTattvaIndexFromSunrise * SUBTATTVA_DURATION_MS)
        val subTattvaEndMillis = subTattvaStartMillis + SUBTATTVA_DURATION_MS
        
        val subTattvaStart = Calendar.getInstance()
        subTattvaStart.timeInMillis = subTattvaStartMillis
        
        val subTattvaEnd = Calendar.getInstance()
        subTattvaEnd.timeInMillis = subTattvaEndMillis
        
        // Secvența SubTattva (aceeași ca Tattva: începe cu Akasha)
        val tattvaSequence = listOf(
            TattvaType.AKASHA,
            TattvaType.VAYU,
            TattvaType.TEJAS,
            TattvaType.APAS,
            TattvaType.PRITHIVI
        )
        
        val currentSubTattva = tattvaSequence[subTattvaIndexInTattva]
        
        // ✅ CALCUL CORECT pentru timpul rămas
        val remainingMillis = subTattvaEndMillis - current.timeInMillis
        
        // Verifică că e pozitiv
        if (remainingMillis < 0) {
            android.util.Log.e("SubTattvaCalculator", "ERROR: Remaining time is negative! $remainingMillis ms")
        }
        
        val safeRemainingMillis = if (remainingMillis > 0) remainingMillis else 0L
        
        // Convertim în minute și secunde
        val remainingMinutes = (safeRemainingMillis / 60000).toInt()
        val remainingSeconds = ((safeRemainingMillis % 60000) / 1000).toInt()
        
        // Debug logging
        android.util.Log.d("SubTattvaCalculator", "=== SUBTATTVA DEBUG START ===")
        android.util.Log.d("SubTattvaCalculator", "Current time: ${formatCalendar(current)}")
        android.util.Log.d("SubTattvaCalculator", "Sunrise time: ${formatCalendar(sunriseLocal)}")
        android.util.Log.d("SubTattvaCalculator", "Elapsed since sunrise: ${adjustedElapsed / 1000} seconds")
        android.util.Log.d("SubTattvaCalculator", "SubTattva index from sunrise: $subTattvaIndexFromSunrise")
        android.util.Log.d("SubTattvaCalculator", "SubTattva index in Tattva: $subTattvaIndexInTattva")
        android.util.Log.d("SubTattvaCalculator", "Current SubTattva: ${currentSubTattva.displayName}")
        android.util.Log.d("SubTattvaCalculator", "SubTattva starts at: ${formatCalendar(subTattvaStart)}")
        android.util.Log.d("SubTattvaCalculator", "SubTattva ends at: ${formatCalendar(subTattvaEnd)}")
        android.util.Log.d("SubTattvaCalculator", "Current millis: ${current.timeInMillis}")
        android.util.Log.d("SubTattvaCalculator", "SubTattva end millis: $subTattvaEndMillis")
        android.util.Log.d("SubTattvaCalculator", "Remaining millis: $remainingMillis")
        android.util.Log.d("SubTattvaCalculator", "Remaining: ${remainingMinutes}m ${remainingSeconds}s")
        android.util.Log.d("SubTattvaCalculator", "=== SUBTATTVA DEBUG END ===")
        
        return SubTattvaResult(
            subTattva = currentSubTattva,
            startTime = subTattvaStart,
            endTime = subTattvaEnd,
            remainingMinutes = remainingMinutes,
            remainingSeconds = remainingSeconds
        )
    }
}

/**
 * Rezultatul calculului SubTattva
 */
data class SubTattvaResult(
    val subTattva: TattvaType,
    val startTime: Calendar,
    val endTime: Calendar,
    val remainingMinutes: Int,
    val remainingSeconds: Int
)

/**
 * Helper function pentru formatare Calendar
 */
private fun formatCalendar(cal: Calendar): String {
    return String.format(
        "%04d-%02d-%02d %02d:%02d:%02d",
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH),
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE),
        cal.get(Calendar.SECOND)
    )
}