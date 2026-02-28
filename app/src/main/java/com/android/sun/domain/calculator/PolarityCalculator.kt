package com.android.sun.domain.calculator

import java.util.*

/**
 * Calculator pentru polaritatea suflului pranic (Ida/Pingala) la răsărit și apus
 * 
 * Formula (din codul original Java):
 * 1. Calculează Nitya (1-15) la momentul răsăritului/apusului
 * 2. nn = floor((nitya - 1) / 3) → rezultă 0, 1, 2, 3, 4
 * 3. Dacă nn e PAR → polaritate = -paksha (pentru răsărit) sau paksha (pentru apus)
 *    Dacă nn e IMPAR → polaritate = paksha (pentru răsărit) sau -paksha (pentru apus)
 * 4. paksha = +1 (Shukla/crescătoare) sau -1 (Krishna/descrescătoare)
 * 
 * Rezultat: +1 = Pingala (+), -1 = Ida (-)
 */
class PolarityCalculator {

    /**
     * Calculează polaritatea la răsărit
     * @return +1 pentru Pingala (+), -1 pentru Ida (-)
     */
    fun calculateSunrisePolarity(
        moonLongitude: Double,
        sunLongitude: Double
    ): Int {
        return calculatePolarity(moonLongitude, sunLongitude, isSunrise = true)
    }

    /**
     * Calculează polaritatea la apus
     * @return +1 pentru Pingala (+), -1 pentru Ida (-)
     */
    fun calculateSunsetPolarity(
        moonLongitude: Double,
        sunLongitude: Double
    ): Int {
        return calculatePolarity(moonLongitude, sunLongitude, isSunrise = false)
    }

    /**
     * Calculează polaritatea bazată pe Nitya și Paksha
     * Logica exactă din AstroData. java original
     */
    private fun calculatePolarity(
        moonLongitude:  Double,
        sunLongitude: Double,
        isSunrise: Boolean
    ): Int {
        // Calculează diferența dintre Lună și Soare
        var moonLon = moonLongitude
        val sunLon = sunLongitude
        
        if (moonLon < sunLon) {
            moonLon += 360.0
        }
        
        var diff = moonLon - sunLon
        
        // Determină paksha
        val paksha:  Int
        if (diff > 180.0) {
            diff = 360.0 - diff
            paksha = -1  // Krishna Paksha (Luna descrescătoare)
        } else {
            paksha = 1   // Shukla Paksha (Luna crescătoare)
        }
        
        // Calculează Nitya (1-15)
        val nitya = (diff / 12.0).toInt() + 1
        
        // Calculează nn = floor((nitya - 1) / 3)
        val nn = (nitya - 1) / 3
        
        // Determină polaritatea (logica din codul Java original)
        val polarity = if (nn % 2 == 0) {
            // nn PAR (0, 2, 4)
            if (isSunrise) {
                paksha * -1  // La răsărit:  invers față de paksha
            } else {
                paksha       // La apus:  egal cu paksha
            }
        } else {
            // nn IMPAR (1, 3)
            if (isSunrise) {
                paksha       // La răsărit: egal cu paksha
            } else {
                paksha * -1  // La apus:  invers față de paksha
            }
        }
        
        com.android.sun.util.AppLog.d("PolarityCalculator", "=== POLARITY DEBUG ===")
        com.android.sun.util.AppLog.d("PolarityCalculator", "Moon: $moonLongitude°, Sun:  $sunLongitude°")
        com.android.sun.util.AppLog.d("PolarityCalculator", "Diff: $diff°, Paksha: ${if (paksha == 1) "Shukla" else "Krishna"}")
        com.android.sun.util.AppLog.d("PolarityCalculator", "Nitya: $nitya, nn: $nn (${if (nn % 2 == 0) "PAR" else "IMPAR"})")
        com.android.sun.util.AppLog.d("PolarityCalculator", "${if (isSunrise) "Sunrise" else "Sunset"} Polarity: ${if (polarity == 1) "(+) Pingala" else "(-) Ida"}")
        
        return polarity
    }

    /**
     * Returnează simbolul polarității
     */
    fun getPolaritySymbol(polarity: Int): String {
        return if (polarity == 1) "(+)" else "(-)"
    }
}