package com.android.sun.domain.calculator

import java.util.*

/**
 * Calculator pentru Nakshatra (cele 27 constelatii lunare)
 * Fiecare Nakshatra = 13°20' = 13.333° (360° / 27)
 * Ordinea începe de la 0° Berbec (Mesha), folosind zodiacul sideral
 */
class NakshatraCalculator {

    /**
     * Calculează Nakshatra curentă bazată pe longitudinea Lunii (sidereal)
     */
    fun calculateNakshatra(
        moonLongitude: Double,
        currentTime: Calendar = Calendar.getInstance()
    ): NakshatraResult {
        android.util.Log.d("NakshatraDebug", "============================================")
        android.util.Log.d("NakshatraDebug", "🌙 NAKSHATRA CALCULATION START")
        android.util.Log.d("NakshatraDebug", "============================================")
        
        // Normalizează longitudinea la 0-360
        var normalizedLon = moonLongitude
        while (normalizedLon < 0) normalizedLon += 360.0
        while (normalizedLon >= 360) normalizedLon -= 360.0
        
        android.util.Log.d("NakshatraDebug", "Moon Longitude: %.2f°".format(normalizedLon))
        
        // Fiecare Nakshatra = 13.333333° (360 / 27)
        val nakshatraDegrees = 360.0 / 27.0  // 13.333333°
        
        // Calculează index-ul Nakshatra (0-26)
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
        
        // Calculează timpul rămas
        // Luna se mișcă cu aproximativ 13.2° pe zi
        // Deci pentru 13.333° (o Nakshatra) sunt necesare aproximativ 24 ore
        val avgDegreesPerHour = 13.2 / 24.0  // ~0.55° per oră
        
        // Câte ore au trecut de la start până acum?
        val hoursElapsedInNakshatra = progressInNakshatra / avgDegreesPerHour
        
        // Câte ore mai sunt până la final?
        val degreesRemaining = nakshatraDegrees - progressInNakshatra
        val hoursRemainingInNakshatra = degreesRemaining / avgDegreesPerHour
        
        // Calculează Start Time
        val startTime = currentTime.clone() as Calendar
        startTime.add(Calendar.MINUTE, -(hoursElapsedInNakshatra * 60).toInt())
        
        // Calculează End Time
        val endTime = currentTime.clone() as Calendar
        endTime.add(Calendar.MINUTE, (hoursRemainingInNakshatra * 60).toInt())
        
        android.util.Log.d("NakshatraDebug", "Hours elapsed: %.2f hours".format(hoursElapsedInNakshatra))
        android.util.Log.d("NakshatraDebug", "Hours remaining: %.2f hours".format(hoursRemainingInNakshatra))
        android.util.Log.d("NakshatraDebug", "Start Time: ${startTime.time}")
        android.util.Log.d("NakshatraDebug", "End Time: ${endTime.time}")
        android.util.Log.d("NakshatraDebug", "============================================")
        
        return NakshatraResult(
            nakshatra = nakshatra,
            moonLongitude = normalizedLon,
            startDegree = nakshatraStartDegree,
            endDegree = nakshatraEndDegree,
            startTime = startTime,
            endTime = endTime,
            number = nakshatra.number,
            name = nakshatra.displayName,
            code = "NK${nakshatra.number}"
        )
    }
    
    companion object {
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
 */
data class NakshatraResult(
    val nakshatra: NakshatraType,
    val moonLongitude: Double,
    val startDegree: Double,
    val endDegree: Double,
    val startTime: Calendar = Calendar.getInstance(),
    val endTime: Calendar = Calendar.getInstance(),
    val number: Int = nakshatra.number,
    val name: String = nakshatra.displayName,
    val code: String = "NK${nakshatra.number}"
)
