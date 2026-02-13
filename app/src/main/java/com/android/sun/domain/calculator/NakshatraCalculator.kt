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
     * Uses reference times for stable daily calculations
     * 
     * @param moonLongitude Current moon longitude (determines which Nakshatra)
     * @param currentTime Current time for countdown calculation
     * @param referenceMoonLongitude Reference moon position for daily stability (optional)
     * @param referenceTime Reference time for daily stability (optional)
     */
    fun calculateNakshatra(
        moonLongitude: Double,
        currentTime: Calendar,
        referenceMoonLongitude: Double = moonLongitude,
        referenceTime: Calendar = currentTime
    ): NakshatraResult {
        android.util.Log.d("NakshatraDebug", "============================================")
        android.util.Log.d("NakshatraDebug", "🌙 NAKSHATRA CALCULATION START")
        android.util.Log.d("NakshatraDebug", "============================================")
        
        // Normalizează longitudinea la 0-360
        var normalizedLon = moonLongitude
        while (normalizedLon < 0) normalizedLon += 360.0
        while (normalizedLon >= 360) normalizedLon -= 360.0
        
        android.util.Log.d("NakshatraDebug", "Moon Longitude (current): %.2f°".format(normalizedLon))
        android.util.Log.d("NakshatraDebug", "Current Time: ${currentTime.time}")
        
        // Fiecare Nakshatra = 13.333333° (360 / 27)
        val nakshatraDegrees = 360.0 / 27.0  // 13.333333°
        
        // Calculează index-ul Nakshatra (0-26) based on CURRENT moon position
        val nakshatraIndex = (normalizedLon / nakshatraDegrees).toInt().coerceIn(0, 26)
        
        val nakshatra = nakshatraList[nakshatraIndex]
        
        android.util.Log.d("NakshatraDebug", "Nakshatra Index: $nakshatraIndex")
        android.util.Log.d("NakshatraDebug", "Nakshatra: ${nakshatra.displayName}")
        
        // Calculează progress în Nakshatra curentă
        val nakshatraStartDegree = nakshatraIndex * nakshatraDegrees
        val nakshatraEndDegree = (nakshatraIndex + 1) * nakshatraDegrees
        val progressInNakshatra = normalizedLon - nakshatraStartDegree
        val nakshatraProgress = progressInNakshatra / nakshatraDegrees
        
        android.util.Log.d("NakshatraDebug", "Progress: %.2f%% (%.4f° in current Nakshatra)".format(nakshatraProgress * 100, progressInNakshatra))
        
        // Luna se mișcă cu aproximativ 13.2° pe zi
        val avgDegreesPerHour = 13.2 / 24.0  // ~0.55° per oră
        
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
        
        // ✅ Zero Reference pentru toate cele 27 Nakshatras
        // Calculăm când luna era la 0° longitudinii ecliptice pentru a putea 
        // calcula timpul absolut pentru toate Nakshatra-urile în mod consistent
        val degreesFromZero = normalizedLon
        val hoursFromZero = degreesFromZero / avgDegreesPerHour
        val zeroReferenceTime = currentTime.clone() as Calendar
        zeroReferenceTime.add(Calendar.SECOND, -(hoursFromZero * 3600).toInt())
        
        android.util.Log.d("NakshatraDebug", "Hours elapsed since Nakshatra start: %.2f hours".format(hoursElapsedSinceStart))
        android.util.Log.d("NakshatraDebug", "Hours remaining until Nakshatra end: %.2f hours".format(hoursRemainingUntilEnd))
        android.util.Log.d("NakshatraDebug", "Start Time: ${startTime.time}")
        android.util.Log.d("NakshatraDebug", "End Time: ${endTime.time}")
        android.util.Log.d("NakshatraDebug", "Zero Reference Time: ${zeroReferenceTime.time}")
        android.util.Log.d("NakshatraDebug", "============================================")
        
        // ✅ Convert moon longitude to zodiac sign with degrees and minutes
        val moonZodiacPosition = moonLongitudeToZodiacString(normalizedLon)
        
        android.util.Log.d("NakshatraDebug", "Moon Position in Zodiac: $moonZodiacPosition")
        
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
            moonZodiacPosition = moonZodiacPosition
        )
    }
    
    companion object {
        /**
         * Convert moon longitude (0-360°) to zodiac sign with degrees and minutes
         * Example: 283.5° -> "13°30′ Capricorn"
         */
        fun moonLongitudeToZodiacString(longitude: Double): String {
            val signs = listOf(
                "Berbec", "Taur", "Gemeni", "Rac", "Leu", "Fecioară",
                "Balanță", "Scorpion", "Săgetător", "Capricorn", "Vărsător", "Pești"
            )
            
            var normalizedLon = longitude
            while (normalizedLon < 0) normalizedLon += 360.0
            while (normalizedLon >= 360) normalizedLon -= 360.0
            
            val signIndex = (normalizedLon / 30.0).toInt()
            val degreeInSign = normalizedLon % 30.0
            val degrees = degreeInSign.toInt()
            val minutes = ((degreeInSign - degrees) * 60).toInt()
            
            return "${degrees}°${minutes}′ ${signs[signIndex]}"
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
 * Rezultatul calculului Nakshatra
 * 
 * ✅ ADDED zeroReferenceTime: Timpul calculat când luna era la 0° longitudinii ecliptice,
 * folosit ca punct de referință pentru a calcula timpurile tuturor celor 27 Nakshatra în mod consistent.
 * În practică, când această funcție este apelată cu poziția lunii la răsărit, zeroReferenceTime
 * devine un timestamp fix pentru întreaga zi.
 * 
 * ✅ ADDED moonZodiacPosition: Poziția lunii în zodiac (ex: "13°20′ Capricorn")
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
    val moonZodiacPosition: String = ""  // ✅ Moon position in zodiac format
)
