package com.android.sun.domain.calculator

import java.util.*

/**
 * Calculator pentru Nakshatra (cele 27 constelații lunare)
 * Fiecare Nakshatra = 13°20' = 13.333° (360° / 27)
 * Ordinea începe de la 0° Berbec (Mesha), folosind zodiacul sideral
 */
class NakshatraCalculator {

    /**
     * Calculează Nakshatra curentă bazată pe longitudinea Lunii (sidereal)
     * 
     * ✅ FIX: Uses current moon position to determine Nakshatra
     * ✅ FIX DRIFT: Uses reference moon position at a fixed time to calculate stable zeroReferenceTime
     * ✅ FIX SPEED: Uses actual moon speed from Swiss Ephemeris instead of static average
     * 
     * @param moonLongitude Current moon longitude (determines which Nakshatra)
     * @param currentTime Current time for countdown calculation
     * @param referenceMoonLongitude Moon longitude at a fixed reference time (e.g., sunrise) to prevent drift
     * @param referenceTime The fixed reference time (e.g., sunrise time)
     * @param moonSpeedDegreesPerDay Actual moon speed in degrees/day from Swiss Ephemeris (default 13.2 for backward compat)
     */
    fun calculateNakshatra(
        moonLongitude: Double,
        currentTime: Calendar,
        referenceMoonLongitude: Double = moonLongitude,  // Default to current for backward compatibility
        referenceTime: Calendar = currentTime,  // Default to current for backward compatibility
        moonSpeedDegreesPerDay: Double = 13.2  // Default to average for backward compatibility
    ): NakshatraResult {
        com.android.sun.util.AppLog.d("NakshatraDebug", "============================================")
        com.android.sun.util.AppLog.d("NakshatraDebug", "🌙 NAKSHATRA CALCULATION START")
        com.android.sun.util.AppLog.d("NakshatraDebug", "============================================")
        
        // Normalizează longitudinea la 0-360
        var normalizedLon = moonLongitude
        while (normalizedLon < 0) normalizedLon += 360.0
        while (normalizedLon >= 360) normalizedLon -= 360.0
        
        com.android.sun.util.AppLog.d("NakshatraDebug", "Moon Longitude (current): %.2f°".format(normalizedLon))
        com.android.sun.util.AppLog.d("NakshatraDebug", "Current Time: ${currentTime.time}")
        
        // Fiecare Nakshatra = 13.333333° (360 / 27)
        val nakshatraDegrees = 360.0 / 27.0  // 13.333333°
        
        // Calculează index-ul Nakshatra (0-26) based on CURRENT moon position
        val nakshatraIndex = (normalizedLon / nakshatraDegrees).toInt().coerceIn(0, 26)
        
        val nakshatra = nakshatraList[nakshatraIndex]
        
        com.android.sun.util.AppLog.d("NakshatraDebug", "Nakshatra Index: $nakshatraIndex")
        com.android.sun.util.AppLog.d("NakshatraDebug", "Nakshatra: ${nakshatra.displayName}")
        
        // Calculează progress în Nakshatra curentă
        val nakshatraStartDegree = nakshatraIndex * nakshatraDegrees
        val nakshatraEndDegree = (nakshatraIndex + 1) * nakshatraDegrees
        val progressInNakshatra = normalizedLon - nakshatraStartDegree
        val nakshatraProgress = progressInNakshatra / nakshatraDegrees
        
        com.android.sun.util.AppLog.d("NakshatraDebug", "Progress: %.2f%% (%.4f° in current Nakshatra)".format(nakshatraProgress * 100, progressInNakshatra))
        
        // ✅ FIX: Folosește viteza reală a Lunii din Swiss Ephemeris
        // Luna se mișcă cu viteza variabilă (~11.8° - 15.2° pe zi, media ~13.2°)
        // Bounds slightly wider than typical range to handle edge cases in ephemeris data
        val actualDegreesPerDay = moonSpeedDegreesPerDay.coerceIn(10.0, 16.0)
        val avgDegreesPerHour = actualDegreesPerDay / 24.0
        
        // ✅ Calculează când luna a intrat și când va ieși din Nakshatra curentă
        // bazat pe poziția lunii la timpul de referință (de obicei răsăritul)
        
        // Câte grade a parcurs luna de la intrarea în Nakshatra?
        val degreesElapsed = progressInNakshatra
        
        // Câte ore în urmă a intrat luna în Nakshatra?
        val hoursElapsedSinceStart = degreesElapsed / avgDegreesPerHour
        
        // Start Time = când luna a intrat în Nakshatra
        val startTime = currentTime.clone() as Calendar
        startTime.add(Calendar.SECOND, -(hoursElapsedSinceStart * 3600).toInt())
        
        // Câte grade mai are de parcurs până la ieșire?
        val degreesRemaining = nakshatraDegrees - progressInNakshatra
        
        // Câte ore mai sunt până când luna iese din Nakshatra?
        val hoursRemainingUntilEnd = degreesRemaining / avgDegreesPerHour
        
        // End Time = când luna va ieși din Nakshatra
        val endTime = currentTime.clone() as Calendar
        endTime.add(Calendar.SECOND, (hoursRemainingUntilEnd * 3600).toInt())
        
        // ✅ FIXED DRIFT: Zero Reference pentru toate cele 27 Nakshatras
        // Calculăm când luna era la 0° longitudinii ecliptice pentru a putea 
        // calcula timpul absolut pentru toate Nakshatra-urile în mod consistent
        // ✅ KEY FIX: Use REFERENCE moon position (at fixed time like sunrise) instead of current
        // This prevents the zeroReferenceTime from drifting as the moon moves throughout the day
        
        // Normalizează longitudinea de referință
        var normalizedRefLon = referenceMoonLongitude
        while (normalizedRefLon < 0) normalizedRefLon += 360.0
        while (normalizedRefLon >= 360) normalizedRefLon -= 360.0
        
        val degreesFromZero = normalizedRefLon  // ✅ Use reference position, not current!
        val hoursFromZero = degreesFromZero / avgDegreesPerHour
        val zeroReferenceTime = referenceTime.clone() as Calendar  // ✅ Use reference time, not current!
        zeroReferenceTime.add(Calendar.SECOND, -(hoursFromZero * 3600).toInt())
        
        com.android.sun.util.AppLog.d("NakshatraDebug", "Hours elapsed since Nakshatra start: %.2f hours".format(hoursElapsedSinceStart))
        com.android.sun.util.AppLog.d("NakshatraDebug", "Hours remaining until Nakshatra end: %.2f hours".format(hoursRemainingUntilEnd))
        com.android.sun.util.AppLog.d("NakshatraDebug", "Start Time: ${startTime.time}")
        com.android.sun.util.AppLog.d("NakshatraDebug", "End Time: ${endTime.time}")
        com.android.sun.util.AppLog.d("NakshatraDebug", "Reference Moon Longitude: %.2f°".format(normalizedRefLon))
        com.android.sun.util.AppLog.d("NakshatraDebug", "Reference Time: ${referenceTime.time}")
        com.android.sun.util.AppLog.d("NakshatraDebug", "Zero Reference Time: ${zeroReferenceTime.time}")
        com.android.sun.util.AppLog.d("NakshatraDebug", "============================================")
        
        // ✅ Convert moon longitude to zodiac sign with degrees and minutes
        val moonZodiacPosition = moonLongitudeToZodiacString(normalizedLon)
        val moonZodiacSignIndex = moonLongitudeToZodiacSignIndex(normalizedLon)
        
        com.android.sun.util.AppLog.d("NakshatraDebug", "Moon Position in Zodiac: $moonZodiacPosition")
        
        return NakshatraResult(
            nakshatra = nakshatra,
            moonLongitude = normalizedLon,
            startDegree = nakshatraStartDegree,
            endDegree = nakshatraEndDegree,
            startTime = startTime,
            endTime = endTime,
            number = nakshatra.number,
            name = nakshatra.displayName,
            code = "NK${nakshatra.number}",
            zeroReferenceTime = zeroReferenceTime,
            moonZodiacPosition = moonZodiacPosition,
            moonZodiacSignIndex = moonZodiacSignIndex,
            moonSpeedDegreesPerDay = actualDegreesPerDay
        )
    }
    
    /**
     * Calculează intervalele de timp exacte pentru toate cele 27 Nakshatre folosind
     * date reale din efemeride (Swiss Ephemeris) pentru fiecare limită de trecere.
     * 
     * ✅ FIX CRITICAL: Viteza Lunii NU este constantă (~11.8° - 15.2° pe zi).
     * Folosirea unei singure viteze pentru proiecția pe 27 de zile introduce erori MARI.
     * Această metodă calculează poziția reală a Lunii la fiecare limită de Nakshatra
     * folosind date din efemeride, obținând timpi preciși pentru zilele viitoare.
     * 
     * Algoritm: Forward-stepping cu rafinare Newton-Raphson
     * 1. Pornește de la poziția/viteza curentă a Lunii
     * 2. Estimează momentul trecerii la următoarea limită de Nakshatra
     * 3. Interogează efemerida la momentul estimat pentru poziția reală
     * 4. Rafinează cu Newton's method (ajustează pe baza erorii și vitezei reale)
     * 5. Repetă pentru toate cele 27 de Nakshatre
     * 
     * @param currentMoonLongitude Longitudinea curentă a Lunii (sidereal, 0-360°)
     * @param currentMoonSpeedDegreesPerDay Viteza curentă a Lunii (°/zi)
     * @param currentTime Momentul curent (Calendar)
     * @param getMoonPositionAndSpeed Funcție care returnează (longitudine, viteză °/zi) 
     *        pentru un Calendar dat, folosind Swiss Ephemeris
     * @return Lista cu 27 NakshatraTimeSlot, începând cu Nakshatra curentă
     */
    fun calculateFutureNakshatras(
        currentMoonLongitude: Double,
        currentMoonSpeedDegreesPerDay: Double,
        currentTime: Calendar,
        getMoonPositionAndSpeed: (Calendar) -> Pair<Double, Double>
    ): List<NakshatraTimeSlot> {
        val NAKSHATRA_DEG = 360.0 / 27.0  // 13.333333°
        val slots = mutableListOf<NakshatraTimeSlot>()
        
        // Normalize current moon longitude
        val normalizedLon = ((currentMoonLongitude % 360.0) + 360.0) % 360.0
        var moonSpeed = currentMoonSpeedDegreesPerDay.coerceIn(10.0, 16.0)
        
        // Current Nakshatra index (0-based)
        val startIdx = (normalizedLon / NAKSHATRA_DEG).toInt().coerceIn(0, 26)
        
        // Calculate start time of current Nakshatra (backwards from current position)
        val degreesElapsedInCurrent = normalizedLon - startIdx * NAKSHATRA_DEG
        val hoursElapsedInCurrent = degreesElapsedInCurrent / (moonSpeed / 24.0)
        val currentNakshatraStartTime = currentTime.clone() as Calendar
        currentNakshatraStartTime.add(Calendar.SECOND, -(hoursElapsedInCurrent * 3600).toInt())
        
        // Track the boundary time as we step forward
        var boundaryTime = currentNakshatraStartTime.clone() as Calendar
        
        com.android.sun.util.AppLog.d("NakshatraFuture", "============================================")
        com.android.sun.util.AppLog.d("NakshatraFuture", "🔮 CALCULATING FUTURE NAKSHATRAS WITH EPHEMERIS")
        com.android.sun.util.AppLog.d("NakshatraFuture", "Current Moon: %.4f° Speed: %.2f°/day".format(normalizedLon, moonSpeed))
        com.android.sun.util.AppLog.d("NakshatraFuture", "============================================")
        
        for (i in 0 until 27) {
            val nakshatraIdx = (startIdx + i) % 27
            val nakshatra = nakshatraList[nakshatraIdx]
            
            val nakshatraStartTime = boundaryTime.clone() as Calendar
            
            // Calculate end boundary for this Nakshatra
            // The end boundary degree is (nakshatraIdx + 1) * NAKSHATRA_DEG
            // For idx 26, end boundary = 360° which wraps to 0°
            val endBoundaryDeg = ((nakshatraIdx + 1) * NAKSHATRA_DEG) % 360.0
            
            // Degrees the moon needs to traverse in this Nakshatra
            val degreesToTraverse: Double
            if (i == 0) {
                // For current Nakshatra: remaining degrees from current position
                degreesToTraverse = NAKSHATRA_DEG - degreesElapsedInCurrent
            } else {
                // For future Nakshatras: full 13.333°
                degreesToTraverse = NAKSHATRA_DEG
            }
            
            // Estimate time to traverse using current known speed
            val hoursToTraverse = degreesToTraverse / (moonSpeed / 24.0)
            var estimatedEndTime = (if (i == 0) currentTime.clone() else nakshatraStartTime.clone()) as Calendar
            if (i == 0) {
                // For current Nakshatra, estimate from current time + remaining time
                val hoursRemaining = (NAKSHATRA_DEG - degreesElapsedInCurrent) / (moonSpeed / 24.0)
                estimatedEndTime.add(Calendar.SECOND, (hoursRemaining * 3600).toInt())
            } else {
                estimatedEndTime.add(Calendar.SECOND, (hoursToTraverse * 3600).toInt())
            }
            
            // ✅ REFINE with actual ephemeris data (Newton-Raphson, up to 3 iterations)
            var refinedEndTime = estimatedEndTime
            for (iteration in 0 until 3) {
                try {
                    val (actualLon, actualSpeed) = getMoonPositionAndSpeed(refinedEndTime)
                    val normalizedActualLon = ((actualLon % 360.0) + 360.0) % 360.0
                    val clampedSpeed = actualSpeed.coerceIn(10.0, 16.0)
                    
                    // Calculate error: how far is actual position from the target boundary?
                    var error = normalizedActualLon - endBoundaryDeg
                    
                    // Handle 360°/0° wrap-around
                    if (error > 180.0) error -= 360.0
                    if (error < -180.0) error += 360.0
                    
                    // If error is small enough (< 0.01° ≈ ~1 minute of time), stop refining
                    if (Math.abs(error) < 0.01) {
                        moonSpeed = clampedSpeed  // Update speed for next Nakshatra
                        break
                    }
                    
                    // Newton's method: adjust time by error/speed
                    val speedPerSecond = clampedSpeed / 86400.0  // degrees per second
                    val timeAdjustSeconds = -(error / speedPerSecond).toLong()
                    refinedEndTime = refinedEndTime.clone() as Calendar
                    refinedEndTime.add(Calendar.SECOND, timeAdjustSeconds.toInt().coerceIn(-86400, 86400))
                    
                    moonSpeed = clampedSpeed  // Update speed for next Nakshatra
                } catch (e: Exception) {
                    com.android.sun.util.AppLog.w("NakshatraFuture", 
                        "⚠️ Ephemeris query failed for Nakshatra ${nakshatra.displayName}, iteration $iteration: ${e.message}")
                    // Fall back to the estimate
                    break
                }
            }
            
            com.android.sun.util.AppLog.d("NakshatraFuture", 
                "${nakshatra.number}. ${nakshatra.displayName}: ${nakshatraStartTime.time} → ${refinedEndTime.time} (speed: %.2f°/day)".format(moonSpeed))
            
            slots.add(NakshatraTimeSlot(nakshatra, nakshatraStartTime, refinedEndTime))
            
            // The end of this Nakshatra is the start of the next one
            boundaryTime = refinedEndTime.clone() as Calendar
        }
        
        return slots
    }
    
    companion object {
        /**
         * Convert moon longitude (0-360°) to zodiac sign with degrees, minutes, and seconds
         * Example: 283.5° -> "13°30'00" Capricorn"
         * Includes raw longitude for debugging
         */
        fun moonLongitudeToZodiacString(longitude: Double): String {
            var normalizedLon = longitude
            while (normalizedLon < 0) normalizedLon += 360.0
            while (normalizedLon >= 360) normalizedLon -= 360.0
            
            val degreeInSign = normalizedLon % 30.0
            val degrees = degreeInSign.toInt()
            val fractionalDegrees = degreeInSign - degrees
            val totalMinutes = fractionalDegrees * 60.0
            val minutes = totalMinutes.toInt()
            val seconds = ((totalMinutes - minutes) * 60.0).toInt()
            
            // Return degrees/minutes/seconds without zodiac sign name (localized in UI)
            return "${degrees}° ${minutes}' ${String.format("%02d", seconds)}\""
        }
        
        /**
         * Get the zodiac sign index (0-11) for a given longitude
         * 0=Aries, 1=Taurus, ..., 11=Pisces
         */
        fun moonLongitudeToZodiacSignIndex(longitude: Double): Int {
            var normalizedLon = longitude
            while (normalizedLon < 0) normalizedLon += 360.0
            while (normalizedLon >= 360) normalizedLon -= 360.0
            return (normalizedLon / 30.0).toInt().coerceIn(0, 11)
        }
        
        // Lista completă a celor 27 Nakshatra
        val nakshatraList = listOf(
            NakshatraType.ASHWINI,           // 1:  0° - 13°20' Berbec
            NakshatraType.BHARANI,           // 2:  13°20' - 26°40' Berbec
            NakshatraType.KRITTIKA,          // 3:  26°40' Berbec - 10° Taur
            NakshatraType.ROHINI,            // 4:  10° - 23°20' Taur
            NakshatraType.MRIGASHIRA,        // 5:  23°20' Taur - 6°40' Gemeni
            NakshatraType.ARDRA,             // 6:  6°40' - 20° Gemeni
            NakshatraType.PUNARVASU,         // 7:  20° Gemeni - 3°20' Rac
            NakshatraType.PUSHYA,            // 8:  3°20' - 16°40' Rac
            NakshatraType.ASHLESHA,          // 9:  16°40' - 30° Rac
            NakshatraType.MAGHA,             // 10: 0° - 13°20' Leu
            NakshatraType.PURVA_PHALGUNI,    // 11: 13°20' - 26°40' Leu
            NakshatraType.UTTARA_PHALGUNI,   // 12: 26°40' Leu - 10° Fecioară
            NakshatraType.HASTA,             // 13: 10° - 23°20' Fecioară
            NakshatraType.CHITRA,            // 14: 23°20' Fecioară - 6°40' Balanță
            NakshatraType.SWATI,             // 15: 6°40' - 20° Balanță
            NakshatraType.VISHAKHA,          // 16: 20° Balanță - 3°20' Scorpion
            NakshatraType.ANURADHA,          // 17: 3°20' - 16°40' Scorpion
            NakshatraType.JYESHTHA,          // 18: 16°40' - 30° Scorpion
            NakshatraType.MULA,              // 19: 0° - 13°20' Săgetător
            NakshatraType.PURVA_ASHADHA,     // 20: 13°20' - 26°40' Săgetător
            NakshatraType.UTTARA_ASHADHA,    // 21: 26°40' Săgetător - 10° Capricorn
            NakshatraType.SHRAVANA,          // 22: 10° - 23°20' Capricorn
            NakshatraType.DHANISHTA,         // 23: 23°20' Capricorn - 6°40' Vărsător
            NakshatraType.SHATABHISHA,       // 24: 6°40' - 20° Vărsător
            NakshatraType.PURVA_BHADRAPADA,  // 25: 20° Vărsător - 3°20' Pești
            NakshatraType.UTTARA_BHADRAPADA, // 26: 3°20' - 16°40' Pești
            NakshatraType.REVATI             // 27: 16°40' - 30° Pești
        )
    }
}

/**
 * Enum pentru cele 27 Nakshatra cu toate detaliile
 */
enum class NakshatraType(
    val displayName: String,
    val number: Int,
    val deity: String,
    val symbol: String,
    val animal: String,
    val planet: String,
    val nature: String,
    val degreeRange: String
) {
    ASHWINI("Ashwini", 1, "Ashwini Kumara", "🐎 cap de cal", "cal", "Ketu", "ușoară / rapidă", "0°–13°20′ Berbec"),
    BHARANI("Bharani", 2, "Yama", "yoni", "elefant", "Venus", "dură", "13°20′–26°40′ Berbec"),
    KRITTIKA("Krittika", 3, "Agni", "🔥 lamă / foc", "oaie", "Soare", "aspră / tăioasă", "26°40′ Berbec – 10° Taur"),
    ROHINI("Rohini", 4, "Prajapati", "car / germinare", "șarpe", "Luna", "blândă", "10°–23°20′ Taur"),
    MRIGASHIRA("Mrigashira", 5, "Soma", "🦌 cap de cerb", "cerb", "Marte", "blândă", "23°20′ Taur – 6°40′ Gemeni"),
    ARDRA("Ardra", 6, "Rudra (Shiva)", "💧 lacrimă / furtună", "câine", "Rahu", "dură", "6°40′–20° Gemeni"),
    PUNARVASU("Punarvasu", 7, "Aditi", "🏹 arc", "pisică", "Jupiter", "blândă", "20° Gemeni – 3°20′ Rac"),
    PUSHYA("Pushya", 8, "Brihaspati", "uger / floare de lotus", "berbec", "Saturn", "blândă", "3°20′–16°40′ Rac"),
    ASHLESHA("Ashlesha", 9, "Naga", "🐍 șarpe", "șarpe", "Mercur", "aspră", "16°40′–30° Rac"),
    MAGHA("Magha", 10, "Pitri", "👑 tron regal", "șobolan", "Ketu", "dură", "0°–13°20′ Leu"),
    PURVA_PHALGUNI("Purva Phalguni", 11, "Bhaga", "pat / hamac", "șobolan", "Venus", "blândă", "13°20′–26°40′ Leu"),
    UTTARA_PHALGUNI("Uttara Phalguni", 12, "Aryaman", "pat", "vacă", "Soare", "blândă", "26°40′ Leu – 10° Fecioară"),
    HASTA("Hasta", 13, "Savitar", "✋ mână", "bivol", "Luna", "ușoară", "10°–23°20′ Fecioară"),
    CHITRA("Chitra", 14, "Tvashtar", "💎 bijuterie", "tigru", "Marte", "dură", "23°20′ Fecioară – 6°40′ Balanță"),
    SWATI("Swati", 15, "Vayu", "🍃 frunză în vânt", "bivol", "Rahu", "flexibilă", "6°40′–20° Balanță"),
    VISHAKHA("Vishakha", 16, "Indra & Agni", "arc triunfal", "tigru", "Jupiter", "dură", "20° Balanță – 3°20′ Scorpion"),
    ANURADHA("Anuradha", 17, "Mitra", "🪷 lotus", "cerb", "Saturn", "blândă", "3°20′–16°40′ Scorpion"),
    JYESHTHA("Jyeshtha", 18, "Indra", "cerc / talisman", "cerb", "Mercur", "aspră", "16°40′–30° Scorpion"),
    MULA("Mula", 19, "Nirriti", "rădăcină", "câine", "Ketu", "dură", "0°–13°20′ Săgetător"),
    PURVA_ASHADHA("Purva Ashadha", 20, "Apah", "evantai", "maimuță", "Venus", "ușoară", "13°20′–26°40′ Săgetător"),
    UTTARA_ASHADHA("Uttara Ashadha", 21, "Vishvadeva", "🐘 colți de elefant", "mangustă", "Soare", "fixă", "26°40′ Săgetător – 10° Capricorn"),
    SHRAVANA("Shravana", 22, "Vishnu", "👂 ureche", "maimuță", "Luna", "blândă", "10°–23°20′ Capricorn"),
    DHANISHTA("Dhanishta", 23, "Vasus", "🥁 tobă", "leu", "Marte", "mobilă", "23°20′ Capricorn – 6°40′ Vărsător"),
    SHATABHISHA("Shatabhisha", 24, "Varuna", "⭕ cerc", "cal", "Rahu", "aspră", "6°40′–20° Vărsător"),
    PURVA_BHADRAPADA("Purva Bhadrapada", 25, "Aja Ekapada", "⚔️ sabie", "leu", "Jupiter", "dură", "20° Vărsător – 3°20′ Pești"),
    UTTARA_BHADRAPADA("Uttara Bhadrapada", 26, "Ahirbudhnya", "🐍 șarpe", "vacă", "Saturn", "fixă", "3°20′–16°40′ Pești"),
    REVATI("Revati", 27, "Pushan", "🐟 pește", "elefant", "Mercur", "blândă", "16°40′–30° Pești")
}

/**
 * Pre-computed time slot for a single Nakshatra
 * Contains the Nakshatra type and its exact start/end times calculated using
 * actual ephemeris data (real moon positions and speeds at each boundary).
 */
data class NakshatraTimeSlot(
    val nakshatra: NakshatraType,
    val startTime: Calendar,
    val endTime: Calendar
)

/**
 * Rezultatul calculului Nakshatra
 * 
 * ✅ FIXED DRIFT: zeroReferenceTime este calculat folosind poziția lunii la un moment fix (răsărit),
 * nu poziția lunii curentă. Aceasta previne "drift-ul" intervalelor Nakshatra pe măsură ce luna
 * se mișcă de-a lungul zilei. Pentru aceeași zi Tattva, zeroReferenceTime va fi întotdeauna același,
 * asigurând că intervalele Nakshatra rămân constante.
 * 
 * ✅ ADDED moonZodiacPosition: Poziția lunii în zodiac (ex: "13°20′ Capricorn")
 * 
 * ✅ FIXED FUTURE NAKSHATRAS: futureNakshatras contains pre-computed time intervals
 * for all 27 Nakshatras using actual ephemeris data for each boundary crossing,
 * instead of extrapolating with a single constant moon speed.
 */
data class NakshatraResult(
    val nakshatra: NakshatraType,
    val moonLongitude: Double,
    val startDegree: Double,
    val endDegree: Double,
    val startTime: Calendar,
    val endTime: Calendar,
    val number: Int = nakshatra.number,
    val name: String = nakshatra.displayName,
    val code: String = "NK${nakshatra.number}",
    val zeroReferenceTime: Calendar,  // ✅ Required parameter - no default to ensure stability
    val moonZodiacPosition: String = "",  // ✅ Moon position in zodiac format (degrees only, no sign name)
    val moonZodiacSignIndex: Int = 0,  // ✅ Zodiac sign index (0=Aries..11=Pisces) for UI localization
    val moonSpeedDegreesPerDay: Double = 13.2,  // ✅ Actual moon speed used for time calculations
    val futureNakshatras: List<NakshatraTimeSlot> = emptyList()  // ✅ Pre-computed Nakshatra intervals using real ephemeris data
)
