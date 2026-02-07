package com.android.sun.data.model

import com.android.sun.domain.calculator.NakshatraResult

/**
 * Model UI pentru Nakshatra
 */
data class NakshatraInfo(
    val name: String,
    val number: Int,
    val code: String,
    val deity: String,
    val symbol: String,
    val animal: String,
    val planet: String,
    val nature: String,
    val degreeRange: String,
    val moonLongitude: Double
)

/**
 * Extensie pentru conversie din domain model
 */
fun NakshatraResult.toNakshatraInfo(): NakshatraInfo {
    return NakshatraInfo(
        name = nakshatra.displayName,
        number = nakshatra.number,
        code = "NK${nakshatra.number}",
        deity = nakshatra.deity,
        symbol = nakshatra.symbol,
        animal = nakshatra.animal,
        planet = nakshatra.planet,
        nature = nakshatra.nature,
        degreeRange = nakshatra.degreeRange,
        moonLongitude = moonLongitude
    )
}
