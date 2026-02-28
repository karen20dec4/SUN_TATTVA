package com.android.sun.domain.calculator

import java.text.SimpleDateFormat
import java.util.*

/**
 * Calculator pentru Orele Planetare (Planetary Hours)
 * ✅ VERSIUNE OPTIMIZATĂ: primește previousSunset și nextSunrise pentru precizie maximă
 * ✅ CORECTATĂ: folosește globalHourIndex (0-23) pentru secvența continuă de planete
 */
class PlanetCalculator {

    /**
     * Calculează Planeta dominantă pentru ora curentă
     * ✅ OPTIMIZAT: primește previousSunset și nextSunrise ca parametri pentru precizie maximă
     */
    fun calculatePlanetaryHour(
        currentTime: Calendar,
        sunrise: Calendar,
        sunset: Calendar,
        previousSunset: Calendar,  // ✅ ADĂUGAT: sunset de IERI
        nextSunrise: Calendar      // ✅ ADĂUGAT: sunrise de MÂINE
    ): PlanetResult {
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        com.android.sun.util.AppLog.d("PlanetDebug", "============================================")
        com.android.sun.util.AppLog.d("PlanetDebug", "🔍 PLANETARY HOUR CALCULATION START")
        com.android.sun.util.AppLog.d("PlanetDebug", "============================================")
        com.android.sun.util.AppLog.d("PlanetDebug", "📅 currentTime:     ${timeFormat.format(currentTime.time)}")
        com.android.sun.util.AppLog.d("PlanetDebug", "🌅 sunrise:         ${timeFormat.format(sunrise.time)}")
        com.android.sun.util.AppLog.d("PlanetDebug", "🌇 sunset:          ${timeFormat.format(sunset.time)}")
        com.android.sun.util.AppLog.d("PlanetDebug", "🌇 previousSunset:  ${timeFormat.format(previousSunset.time)}")
        com.android.sun.util.AppLog.d("PlanetDebug", "🌅 nextSunrise:     ${timeFormat.format(nextSunrise.time)}")
        
        val currentMillis = currentTime.timeInMillis
        val sunriseMillis = sunrise.timeInMillis
        val sunsetMillis = sunset.timeInMillis
        
        val isDayTime = currentMillis in sunriseMillis..sunsetMillis
        
        com.android.sun.util.AppLog.d("PlanetDebug", "☀️ isDayTime: $isDayTime")
        
        // Calculează interval
        val startMillis: Long
        val endMillis: Long
        
        if (isDayTime) {
            // ✅ ZI: de la sunrise la sunset
            startMillis = sunriseMillis
            endMillis = sunsetMillis
            com.android.sun.util.AppLog.d("PlanetDebug", "🌞 DAY TIME: from sunrise to sunset")
        } else {
            if (currentMillis < sunriseMillis) {
                // ✅ NOAPTE ÎNAINTE DE SUNRISE: de la previousSunset (ieri) la sunrise (azi)
                startMillis = previousSunset.timeInMillis
                endMillis = sunriseMillis
                com.android.sun.util.AppLog.d("PlanetDebug", "🌙 NIGHT (before sunrise): from previousSunset to sunrise")
            } else {
                // ✅ NOAPTE DUPĂ SUNSET: de la sunset (azi) la nextSunrise (mâine)
                startMillis = sunsetMillis
                endMillis = nextSunrise.timeInMillis
                com.android.sun.util.AppLog.d("PlanetDebug", "🌙 NIGHT (after sunset): from sunset to nextSunrise")
            }
        }
        
        com.android.sun.util.AppLog.d("PlanetDebug", "⏰ startMillis: $startMillis (${timeFormat.format(Date(startMillis))})")
        com.android.sun.util.AppLog.d("PlanetDebug", "⏰ endMillis:   $endMillis (${timeFormat.format(Date(endMillis))})")
        
        // 12 ore planetare
        val totalDuration = endMillis - startMillis
        val planetaryHourDuration = totalDuration / 12
        val elapsedTime = currentMillis - startMillis
        
        com.android.sun.util.AppLog.d("PlanetDebug", "⏱ totalDuration: ${totalDuration / 60000.0} minutes")
        com.android.sun.util.AppLog.d("PlanetDebug", "⏱ planetaryHourDuration: ${planetaryHourDuration / 60000.0} minutes")
        com.android.sun.util.AppLog.d("PlanetDebug", "⏱ elapsedTime: ${elapsedTime / 60000.0} minutes")
        
        val hourIndex = (elapsedTime / planetaryHourDuration).toInt().coerceIn(0, 11)
        
        com.android.sun.util.AppLog.d("PlanetDebug", "🔢 hourIndex: $hourIndex (hour ${hourIndex + 1} of 12)")
        
        // ✅ CORECTARE: Calculează index-ul GLOBAL (0-23) pentru secvența continuă!
        val globalHourIndex: Int
        val referenceDay: Calendar

        if (isDayTime) {
            // Zi: orele 0-11 (ora 1-12)
            globalHourIndex = hourIndex
            referenceDay = sunrise
            com.android.sun.util.AppLog.d("PlanetDebug", "🌞 DAY: globalHourIndex = $globalHourIndex (hours 1-12 of day)")
        } else {
            // Noapte: orele 12-23 (ora 13-24)
            globalHourIndex = 12 + hourIndex
            // ✅ Pentru noapte, folosește sunrise (nu previousSunset!)
            // Fiindcă planeta zilei e determinată de sunrise!
            referenceDay = sunrise
            com.android.sun.util.AppLog.d("PlanetDebug", "🌙 NIGHT: globalHourIndex = $globalHourIndex (hours 13-24 of day)")
        }

        val dayOfWeek = referenceDay.get(Calendar.DAY_OF_WEEK) - 1
        
        val hourStartMillis = startMillis + (hourIndex * planetaryHourDuration)
        val hourEndMillis = hourStartMillis + planetaryHourDuration
        
        val hourStart = Calendar.getInstance()
        hourStart.timeInMillis = hourStartMillis
        
        val hourEnd = Calendar.getInstance()
        hourEnd.timeInMillis = hourEndMillis
        
        com.android.sun.util.AppLog.d("PlanetDebug", "⏰ hourStart: ${timeFormat.format(hourStart.time)}")
        com.android.sun.util.AppLog.d("PlanetDebug", "⏰ hourEnd:   ${timeFormat.format(hourEnd.time)}")
        
        val dayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        val planetRulers = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")
        
        com.android.sun.util.AppLog.d("PlanetDebug", "📅 referenceDay: ${timeFormat.format(referenceDay.time)}")
        com.android.sun.util.AppLog.d("PlanetDebug", "📅 dayOfWeek: $dayOfWeek (${dayNames[dayOfWeek]})")
        com.android.sun.util.AppLog.d("PlanetDebug", "📅 dayRuler: ${planetRulers[dayOfWeek]}")
        com.android.sun.util.AppLog.d("PlanetDebug", "🔢 globalHourIndex: $globalHourIndex (hour ${globalHourIndex + 1} of 24)")
        
        // ✅ Secvența planetelor folosind globalHourIndex
        val planet = getPlanetForHour(dayOfWeek, globalHourIndex)
        
        com.android.sun.util.AppLog.d("PlanetDebug", "🪐 RESULT: ${planet.displayName} (${planet.code})")
        com.android.sun.util.AppLog.d("PlanetDebug", "============================================")
        
        return PlanetResult(
            planet = planet,
            startTime = hourStart,
            endTime = hourEnd,
            hourNumber = globalHourIndex + 1,  // ✅ 1-24 (nu 1-12!)
            isDayTime = isDayTime
        )
    }
    
    /**
     * Obține planeta pentru ora specifică
     * ✅ MODIFICAT: folosește globalHourIndex (0-23) pentru secvența continuă
     */
    private fun getPlanetForHour(dayOfWeek: Int, globalHourIndex: Int): PlanetType {
        // Secvența Chaldeeană: Saturn, Jupiter, Mars, Sun, Venus, Mercury, Moon
        val chaldeanOrder = listOf(
            PlanetType.SATURN, PlanetType.JUPITER, PlanetType.MARS,
            PlanetType.SUN, PlanetType.VENUS, PlanetType.MERCURY, PlanetType.MOON
        )
        
        // Planeta care domnește ziua
        val dayRuler = listOf(
            PlanetType.SUN,     // Duminică (0)
            PlanetType.MOON,    // Luni (1)
            PlanetType.MARS,    // Marți (2)
            PlanetType.MERCURY, // Miercuri (3)
            PlanetType.JUPITER, // Joi (4)
            PlanetType.VENUS,   // Vineri (5)
            PlanetType.SATURN   // Sâmbătă (6)
        )
        
        val startPlanet = dayRuler[dayOfWeek]
        val startIndex = chaldeanOrder.indexOf(startPlanet)
        
        com.android.sun.util.AppLog.d("PlanetDebug", "🔍 getPlanetForHour: dayOfWeek=$dayOfWeek, globalHourIndex=$globalHourIndex")
        com.android.sun.util.AppLog.d("PlanetDebug", "🔍 startPlanet: ${startPlanet.displayName} (index $startIndex in chaldeanOrder)")
        
        // ✅ Calculează index-ul planetei curente folosind globalHourIndex
        val planetIndex = (startIndex + globalHourIndex) % 7
        
        com.android.sun.util.AppLog.d("PlanetDebug", "🔍 planetIndex: ($startIndex + $globalHourIndex) % 7 = $planetIndex")
        com.android.sun.util.AppLog.d("PlanetDebug", "🔍 result: ${chaldeanOrder[planetIndex].displayName}")
        
        return chaldeanOrder[planetIndex]
    }
}

/**
 * Enum pentru planete
 */
enum class PlanetType(val displayName: String, val code: String) {
    SUN("Soare", "☉"),
    MOON("Lună", "☽"),
    MERCURY("Mercur", "☿"),
    VENUS("Venus", "♀"),
    MARS("Marte", "♂"),
    JUPITER("Jupiter", "♃"),
    SATURN("Saturn", "♄")
}

/**
 * Rezultatul calculului Planetar
 */
data class PlanetResult(
    val planet: PlanetType,
    val startTime: Calendar,
    val endTime: Calendar,
    val hourNumber: Int,  // ✅ ACUM: 1-24 (nu 1-12!)
    val isDayTime: Boolean
)