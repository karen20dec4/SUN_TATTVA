package com.android.sun.util

import java.util.SimpleTimeZone
import java.util.TimeZone

/**
 * Utilitar centralizat pentru maparea numelui locației la TimeZone.
 * Suportă DST pentru orașele cunoscute.
 *
 * Înlocuiește logica duplicată din:
 * - AstroRepository.getLocationTimeZone()
 * - AllDayScreen
 * - PlanetaryHoursCard
 * - NakshatraCard
 * - MainActivity (customday route)
 */
object TimeZoneUtils {

    /**
     * Obține TimeZone-ul corect pentru o locație (cu suport DST).
     * Pentru orașe cunoscute, folosim timezone ID-uri corecte.
     * Pentru alte locații, folosim offset-ul furnizat (fără DST).
     *
     * @param locationName Numele locației (ex: "București", "Tokyo")
     * @param timeZoneOffset Offset-ul timezone-ului în ore (ex: 2.0 pentru UTC+2)
     * @return TimeZone-ul corespunzător locației
     */
    fun getLocationTimeZone(locationName: String, timeZoneOffset: Double): TimeZone {
        return when {
            locationName.contains("București", ignoreCase = true) ||
            locationName.contains("Bucharest", ignoreCase = true) ||
            locationName.contains("Cluj", ignoreCase = true) ||
            locationName.contains("Timișoara", ignoreCase = true) ||
            locationName.contains("Iași", ignoreCase = true) ||
            locationName.contains("Constanța", ignoreCase = true) ||
            locationName.contains("Craiova", ignoreCase = true) ||
            locationName.contains("Brașov", ignoreCase = true) ||
            locationName.contains("România", ignoreCase = true) -> {
                TimeZone.getTimeZone("Europe/Bucharest")
            }
            locationName.contains("Tokyo", ignoreCase = true) -> {
                TimeZone.getTimeZone("Asia/Tokyo")
            }
            locationName.contains("New York", ignoreCase = true) -> {
                TimeZone.getTimeZone("America/New_York")
            }
            locationName.contains("London", ignoreCase = true) -> {
                TimeZone.getTimeZone("Europe/London")
            }
            locationName.contains("Paris", ignoreCase = true) -> {
                TimeZone.getTimeZone("Europe/Paris")
            }
            locationName.contains("Berlin", ignoreCase = true) -> {
                TimeZone.getTimeZone("Europe/Berlin")
            }
            locationName.contains("Los Angeles", ignoreCase = true) -> {
                TimeZone.getTimeZone("America/Los_Angeles")
            }
            else -> {
                // Pentru locații necunoscute, folosim offset-ul furnizat
                // Acest lucru nu va gestiona DST, dar va afișa ora corectă pentru zona orară de bază
                val offsetMillis = (timeZoneOffset * 3600.0 * 1000.0).toInt()
                SimpleTimeZone(offsetMillis, "Location")
            }
        }
    }
}
