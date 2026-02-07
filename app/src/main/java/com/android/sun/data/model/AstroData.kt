package com.android.sun.data. model

import com.android.sun.domain.calculator.*
import java.util.*

/**
 * Model de date pentru toate informațiile astronomice
 */
data class AstroData(
    val timestamp: Long,
    val currentTime: Calendar,
    val sunrise: Calendar,
    val sunset: Calendar,
    val sunriseFormatted: String,
    val sunsetFormatted: String,
    val tattva: TattvaResult,
    val subTattva: SubTattvaResult,
    val planet: PlanetResult,
    val nitya: NityaResult,
    val moonLongitude: Double,
    val sunLongitude: Double,
    val sunSign: String,
    val moonSign: String,
    val locationName: String,
    val isGPSLocation:  Boolean = false,
    val latitude: Double,
    val longitude: Double,
    val timeZone: Double,
    
    // ✅ POLARITATE AZI - Ida/Pingala
    val sunrisePolarity: Int = 1,              // +1 = Pingala, -1 = Ida
    val sunsetPolarity: Int = -1,              // +1 = Pingala, -1 = Ida
    val sunrisePolaritySymbol: String = "(+)", // "(+)" sau "(-)"
    val sunsetPolaritySymbol: String = "(-)",   // "(+)" sau "(-)"
    
    // ✅ RĂSĂRIT/APUS MÂINE
    val nextSunrise: Calendar,                      // Răsărit mâine
    val nextSunset: Calendar,                       // Apus mâine
    val nextSunriseFormatted:  String,               // "07:45:23"
    val nextSunsetFormatted: String,                // "16:37:45"
    val nextSunrisePolarity:  Int = 1,               // +1 sau -1
    val nextSunsetPolarity: Int = -1,               // +1 sau -1
    val nextSunrisePolaritySymbol: String = "(+)",  // "(+)" sau "(-)"
    val nextSunsetPolaritySymbol: String = "(-)",    // "(+)" sau "(-)"
	
	// ✅ Fazele lunii:  
    val moonPhase: MoonPhaseResult,
	
	// ✅ Nakshatra
    val nakshatra: NakshatraResult
)