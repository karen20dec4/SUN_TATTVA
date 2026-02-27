package com.android.sun.domain.calculator

import java.util.*

/**
 * Calculator pentru Tattva (perioadă fixă de 24 minute)
 * Ciclul ÎNCEPE de la RĂSĂRIT cu Tattva Akasha
 * 1 Tattva = 24 minute (1440 secunde)
 * 1 Ciclu = 5 Tattva-uri = 120 minute (2 ore)
 */
class TattvaCalculator {

    companion object {
        const val TATTVA_DURATION_MS = 24 * 60 * 1000L // 24 minute
        const val CYCLE_DURATION_MS = 5 * TATTVA_DURATION_MS // 120 minute (2 ore)
        const val TATTVAS_PER_CYCLE = 5
    }

    /**
     * Calculează Tattva-ul curent bazat pe răsăritul soarelui
     */
    @Suppress("UNUSED_PARAMETER")
    fun calculateCurrentTattva(
        currentTime: Calendar,
        sunrise: Calendar,
        sunset: Calendar
    ): TattvaResult {
        val current = currentTime.clone() as Calendar
        val sunriseLocal = sunrise.clone() as Calendar
        
        // Calculează timpul scurs de la răsărit
        val elapsedSinceSunrise = current.timeInMillis - sunriseLocal.timeInMillis
        
        // Dacă suntem ÎNAINTE de răsărit, folosim răsăritul de ieri
        val adjustedElapsed = if (elapsedSinceSunrise < 0) {
            // Înainte de răsărit → calculăm de la răsăritul de ieri
            val yesterdaySunrise = sunriseLocal.clone() as Calendar
            yesterdaySunrise.add(Calendar.DAY_OF_MONTH, -1)
            current.timeInMillis - yesterdaySunrise.timeInMillis
        } else {
            elapsedSinceSunrise
        }
        
        // Calculează index-ul Tattva de la răsărit
        val tattvaIndexFromSunrise = (adjustedElapsed / TATTVA_DURATION_MS).toInt()
        
        // Index-ul în ciclu (0-4: Akasha, Vayu, Tejas, Apas, Prithivi)
        val tattvaIndexInCycle = tattvaIndexFromSunrise % TATTVAS_PER_CYCLE
        
        // Numărul ciclului
        val cycleNumber = tattvaIndexFromSunrise / TATTVAS_PER_CYCLE
        
        // Calculează start și end pentru Tattva curentă
        val referencePoint = if (elapsedSinceSunrise < 0) {
            val yesterdaySunrise = sunriseLocal.clone() as Calendar
            yesterdaySunrise.add(Calendar.DAY_OF_MONTH, -1)
            yesterdaySunrise.timeInMillis
        } else {
            sunriseLocal.timeInMillis
        }
        
        val tattvaStartMillis = referencePoint + (tattvaIndexFromSunrise * TATTVA_DURATION_MS)
        val tattvaEndMillis = tattvaStartMillis + TATTVA_DURATION_MS
        
        val tattvaStart = Calendar.getInstance()
        tattvaStart.timeInMillis = tattvaStartMillis
        
        val tattvaEnd = Calendar.getInstance()
        tattvaEnd.timeInMillis = tattvaEndMillis
        
        // Secvența Tattva (începe cu Akasha la răsărit)
        val tattvaSequence = listOf(
            TattvaType.AKASHA,    // Index 0
            TattvaType.VAYU,      // Index 1
            TattvaType.TEJAS,     // Index 2
            TattvaType.APAS,      // Index 3
            TattvaType.PRITHIVI   // Index 4
        )
        
        val currentTattva = tattvaSequence[tattvaIndexInCycle]
        
        // ✅ CALCUL CORECT pentru timpul rămas
        val remainingMillis = tattvaEndMillis - current.timeInMillis
        
        // Verifică că e pozitiv
        if (remainingMillis < 0) {
            android.util.Log.e("TattvaCalculator", "ERROR: Remaining time is negative! $remainingMillis ms")
        }
        
        val safeRemainingMillis = if (remainingMillis > 0) remainingMillis else 0L
        
        // Convertim în minute și secunde
        val remainingMinutes = (safeRemainingMillis / 60000).toInt()
        val remainingSeconds = ((safeRemainingMillis % 60000) / 1000).toInt()
        
        // Debug logging
        android.util.Log.d("TattvaCalculator", "=== TATTVA DEBUG START ===")
        android.util.Log.d("TattvaCalculator", "Current time: ${formatCalendar(current)}")
        android.util.Log.d("TattvaCalculator", "Sunrise time: ${formatCalendar(sunriseLocal)}")
        android.util.Log.d("TattvaCalculator", "Elapsed since sunrise: ${adjustedElapsed / 60000} minutes")
        android.util.Log.d("TattvaCalculator", "Tattva index from sunrise: $tattvaIndexFromSunrise")
        android.util.Log.d("TattvaCalculator", "Cycle number: $cycleNumber")
        android.util.Log.d("TattvaCalculator", "Tattva index in cycle: $tattvaIndexInCycle")
        android.util.Log.d("TattvaCalculator", "Current Tattva: ${currentTattva.displayName}")
        android.util.Log.d("TattvaCalculator", "Tattva starts at: ${formatCalendar(tattvaStart)}")
        android.util.Log.d("TattvaCalculator", "Tattva ends at: ${formatCalendar(tattvaEnd)}")
        android.util.Log.d("TattvaCalculator", "Current millis: ${current.timeInMillis}")
        android.util.Log.d("TattvaCalculator", "Tattva end millis: $tattvaEndMillis")
        android.util.Log.d("TattvaCalculator", "Remaining millis: $remainingMillis")
        android.util.Log.d("TattvaCalculator", "Remaining: ${remainingMinutes}m ${remainingSeconds}s")
        android.util.Log.d("TattvaCalculator", "=== TATTVA DEBUG END ===")
        
        return TattvaResult(
            tattva = currentTattva,
            startTime = tattvaStart,
            endTime = tattvaEnd,
            remainingMinutes = remainingMinutes,
            remainingSeconds = remainingSeconds
        )
    }
}

/**
 * Enum pentru cele 5 Tattva-uri
 */
enum class TattvaType(val displayName: String, val code: String, val element: String) {
    AKASHA("Akasha", "A", "Eter"),
    VAYU("Vayu", "V", "Aer"),
    TEJAS("Tejas", "T", "Foc"),
    APAS("Apas", "Ap", "Apă"),
    PRITHIVI("Prithivi", "P", "Pământ")
}

/**
 * Rezultatul calculului Tattva
 */
data class TattvaResult(
    val tattva: TattvaType,
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