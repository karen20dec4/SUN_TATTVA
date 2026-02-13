package com.android.sun.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.android.sun.data.model.AstroData
import com.android.sun.data.model.SubTattvaItem
import com.android.sun.data.model.TattvaDayItem
import com.android.sun.domain.calculator.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository pentru calcule astronomice
 */
class AstroRepository(private val context: Context) {

    private val swissEphWrapper = SwissEphWrapper(context)
    private val astroCalculator = AstroCalculator(swissEphWrapper)
    private val tattvaCalculator = TattvaCalculator()
    private val subTattvaCalculator = SubTattvaCalculator()
    private val planetCalculator = PlanetCalculator()
    private val nityaCalculator = NityaCalculator()
    private val polarityCalculator = PolarityCalculator()
    private val nakshatraCalculator = NakshatraCalculator()  

    /**
     * ✅ Obține timezone-ul corect pentru o locație (cu suport DST)
     * Pentru orașe cunoscute, folosim timezone ID-uri corecte
     * Pentru alte locații, folosim offset-ul furnizat (fără DST)
     */
    private fun getLocationTimeZone(locationName: String, timeZoneOffset: Double): java.util.TimeZone {
        // Mapare orașe cunoscute -> timezone ID (cu suport DST)
        return when {
            locationName.contains("București", ignoreCase = true) || 
            locationName.contains("Bucharest", ignoreCase = true) ||
            locationName.contains("Cluj", ignoreCase = true) ||
            locationName.contains("Timișoara", ignoreCase = true) ||
            locationName.contains("Iași", ignoreCase = true) ||
            locationName.contains("Constanța", ignoreCase = true) ||
            locationName.contains("Craiova", ignoreCase = true) ||
            locationName.contains("Brașov", ignoreCase = true) -> {
                java.util.TimeZone.getTimeZone("Europe/Bucharest")
            }
            locationName.contains("Tokyo", ignoreCase = true) -> {
                java.util.TimeZone.getTimeZone("Asia/Tokyo")
            }
            locationName.contains("New York", ignoreCase = true) -> {
                java.util.TimeZone.getTimeZone("America/New_York")
            }
            locationName.contains("London", ignoreCase = true) -> {
                java.util.TimeZone.getTimeZone("Europe/London")
            }
            locationName.contains("Paris", ignoreCase = true) -> {
                java.util.TimeZone.getTimeZone("Europe/Paris")
            }
            locationName.contains("Berlin", ignoreCase = true) -> {
                java.util.TimeZone.getTimeZone("Europe/Berlin")
            }
            locationName.contains("Los Angeles", ignoreCase = true) -> {
                java.util.TimeZone.getTimeZone("America/Los_Angeles")
            }
            else -> {
                // Pentru locații necunoscute, folosim offset-ul furnizat
                // Acest lucru nu va gestiona DST, dar va afișa ora corectă pentru zona orară de bază
                val offsetMillis = (timeZoneOffset * 3600.0 * 1000.0).toInt()
                SimpleTimeZone(offsetMillis, "Location")
            }
        }
    }

    /**
     * Calculează toate datele astro pentru locația și timpul curent
     */
    suspend fun calculateAstroData(
        latitude: Double,
        longitude: Double,
        timeZone: Double,
        locationName: String,
        isGPSLocation: Boolean = false
    ): AstroData = withContext(Dispatchers.Default) {
        val currentTime = Calendar.getInstance()
        calculateAstroDataForTime(currentTime, latitude, longitude, timeZone, locationName, isGPSLocation)
    }

    /**
     * Flow pentru actualizări în timp real (fiecare secundă)
     */
    fun getRealtimeAstroData(
        latitude: Double,
        longitude: Double,
        timeZone:  Double,
        locationName: String
    ): Flow<AstroData> = flow {
        while (true) {
            val currentTime = Calendar.getInstance()
            val data = calculateAstroDataForTime(
                currentTime, latitude, longitude, timeZone, locationName
            )
            emit(data)
            kotlinx.coroutines.delay(1000)
        }
    }

    /**
     * ✅ Determină începutul "Tattva Day" (de la răsărit la răsărit)
     */
    private fun getTattvaDayStart(
        currentTime: Calendar,
        latitude: Double,
        longitude: Double,
        timeZone: Double
    ): Calendar {
        val todayYear = currentTime.get(Calendar.YEAR)
        val todayMonth = currentTime.get(Calendar.MONTH) + 1
        val todayDay = currentTime.get(Calendar.DAY_OF_MONTH)
        
        val todaySunrise = astroCalculator.calculateSunrise(
            todayYear, todayMonth, todayDay,
            longitude, latitude, timeZone
        )
        
        if (currentTime.timeInMillis < todaySunrise.timeInMillis) {
            val yesterdayDate = currentTime.clone() as Calendar
            yesterdayDate.add(Calendar.DAY_OF_MONTH, -1)
            
            val yesterdayYear = yesterdayDate.get(Calendar.YEAR)
            val yesterdayMonth = yesterdayDate.get(Calendar.MONTH) + 1
            val yesterdayDay = yesterdayDate.get(Calendar.DAY_OF_MONTH)
            
            return astroCalculator.calculateSunrise(
                yesterdayYear, yesterdayMonth, yesterdayDay,
                longitude, latitude, timeZone
            )
        }
        
        return todaySunrise
    }

    /**
     * ✅ Calculează datele astro pentru un moment specific
     * ✅ OPTIMIZAT: calculează previousSunset și nextSunrise pentru Planetary Hours
     * ✅ ADĂUGAT: calculează polaritatea la răsărit și apus
     * ✅ FIX: Creează MoonPhaseCalculator cu timezone-ul locației
     * ✅ NAKSHATRA: Folosește poziția lunii la miezul nopții UTC pentru stabilitate zilnică
     */
    private fun calculateAstroDataForTime(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timeZone: Double,
        locationName: String,
        isGPSLocation: Boolean = false
    ): AstroData {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        
        // ✅ NAKSHATRA FIX: Calculează poziția lunii la miezul nopții UTC pentru stabilitate zilnică
        // Aceasta asigură că Nakshatra este aceeași în Tokyo și București la același moment UTC
        val midnightUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        midnightUTC.set(Calendar.YEAR, year)
        midnightUTC.set(Calendar.MONTH, month - 1)  // Calendar.MONTH is 0-based
        midnightUTC.set(Calendar.DAY_OF_MONTH, day)
        midnightUTC.set(Calendar.HOUR_OF_DAY, 0)
        midnightUTC.set(Calendar.MINUTE, 0)
        midnightUTC.set(Calendar.SECOND, 0)
        midnightUTC.set(Calendar.MILLISECOND, 0)

        val sunrise = getTattvaDayStart(calendar, latitude, longitude, timeZone)
        
        val sunriseYear = sunrise.get(Calendar.YEAR)
        val sunriseMonth = sunrise.get(Calendar.MONTH) + 1
        val sunriseDay = sunrise.get(Calendar.DAY_OF_MONTH)

        val sunset = astroCalculator.calculateSunset(
            sunriseYear, sunriseMonth, sunriseDay,
            longitude, latitude, timeZone
        )

        // ✅ CORECT: Calculează previousSunset bazat pe SUNRISE (Tattva Day start!)
        val yesterdayDate = sunrise.clone() as Calendar
        yesterdayDate.add(Calendar.DAY_OF_MONTH, -1)
        val yesterdayYear = yesterdayDate.get(Calendar.YEAR)
        val yesterdayMonth = yesterdayDate.get(Calendar.MONTH) + 1
        val yesterdayDay = yesterdayDate.get(Calendar.DAY_OF_MONTH)

        val previousSunset = astroCalculator.calculateSunset(
            yesterdayYear, yesterdayMonth, yesterdayDay,
            longitude, latitude, timeZone
        )

        // ✅ CORECT: Calculează nextSunrise bazat pe SUNRISE (Tattva Day start!)
        val tomorrowDate = sunrise.clone() as Calendar
        tomorrowDate.add(Calendar.DAY_OF_MONTH, 1)

        val tomorrowYear = tomorrowDate.get(Calendar.YEAR)
        val tomorrowMonth = tomorrowDate.get(Calendar.MONTH) + 1
        val tomorrowDay = tomorrowDate.get(Calendar.DAY_OF_MONTH)
        
        val nextSunrise = astroCalculator.calculateSunrise(
            tomorrowYear, tomorrowMonth, tomorrowDay,
            longitude, latitude, timeZone
        )

        val tattvaResult = tattvaCalculator.calculateCurrentTattva(
            calendar, sunrise, sunset
        )

        val subTattvaResult = subTattvaCalculator.calculateCurrentSubTattva(
            calendar, sunrise, sunset
        )

        val moonLongitude = astroCalculator.calculateMoonLongitude(
            year, month, day, hour, minute, second
        )
        val sunLongitude = astroCalculator.calculateSunLongitude(
            year, month, day, hour, minute, second
        )

        // ✅ FIX DST: Folosim timezone-ul cu suport pentru DST
        val locationTimeZone = getLocationTimeZone(locationName, timeZone)
        
        // ✅ Creăm MoonPhaseCalculator cu timezone-ul locației
        val moonPhaseCalculator = MoonPhaseCalculator(astroCalculator, locationTimeZone)
        
        val moonPhase = moonPhaseCalculator.calculateMoonPhase(
            moonLongitude, sunLongitude, calendar
        )

        // ✅ MODIFICAT: apelează calculatePlanetaryHour cu previousSunset și nextSunrise
        val planet = planetCalculator.calculatePlanetaryHour(
            calendar, sunrise, sunset, previousSunset, nextSunrise
        )
        
        val nitya = nityaCalculator.calculateNitya(
            moonLongitude, sunLongitude, calendar
        )
		
		// ✅ NAKSHATRA FIX: Calculate moon position at midnight UTC for stable daily reference
        // This ensures Nakshatra times are location-independent and stable throughout the day
        val moonLongitudeAtMidnightUTC = astroCalculator.calculateMoonLongitude(
            midnightUTC.get(Calendar.YEAR),
            midnightUTC.get(Calendar.MONTH) + 1,  // Calendar.MONTH is 0-based
            midnightUTC.get(Calendar.DAY_OF_MONTH),
            0, 0, 0  // Midnight: 00:00:00
        )
        
        // Calculate Nakshatra using midnight UTC moon position
        // This gives the same Nakshatra worldwide and stable times for the entire day
        val nakshatra = nakshatraCalculator.calculateNakshatra(
            moonLongitudeAtMidnightUTC,  // Moon position at midnight UTC (universal reference)
            midnightUTC  // Midnight UTC as reference time (universal baseline)
        )

        // ✅ ADĂUGAT:  Calculează polaritatea la răsărit
        val sunriseHour = sunrise.get(Calendar.HOUR_OF_DAY)
        val sunriseMinute = sunrise.get(Calendar.MINUTE)
        val sunriseSecond = sunrise.get(Calendar.SECOND)
        
        val moonLongitudeAtSunrise = astroCalculator.calculateMoonLongitude(
            sunriseYear, sunriseMonth, sunriseDay, sunriseHour, sunriseMinute, sunriseSecond
        )
        val sunLongitudeAtSunrise = astroCalculator.calculateSunLongitude(
            sunriseYear, sunriseMonth, sunriseDay, sunriseHour, sunriseMinute, sunriseSecond
        )
        
        // ✅ FIX: Apelează cu 2 parametri Double, nu Calendar
        val sunrisePolarity = polarityCalculator.calculateSunrisePolarity(
            moonLongitudeAtSunrise, sunLongitudeAtSunrise
        )

        // ✅ ADĂUGAT:  Calculează polaritatea la apus
        val sunsetYear = sunset.get(Calendar.YEAR)
        val sunsetMonth = sunset.get(Calendar.MONTH) + 1
        val sunsetDay = sunset.get(Calendar.DAY_OF_MONTH)
        val sunsetHour = sunset.get(Calendar.HOUR_OF_DAY)
        val sunsetMinute = sunset.get(Calendar.MINUTE)
        val sunsetSecond = sunset.get(Calendar.SECOND)
        
        val moonLongitudeAtSunset = astroCalculator.calculateMoonLongitude(
            sunsetYear, sunsetMonth, sunsetDay, sunsetHour, sunsetMinute, sunsetSecond
        )
        val sunLongitudeAtSunset = astroCalculator.calculateSunLongitude(
            sunsetYear, sunsetMonth, sunsetDay, sunsetHour, sunsetMinute, sunsetSecond
        )
        
        // ✅ FIX: Apelează cu 2 parametri Double, nu Calendar
        val sunsetPolarity = polarityCalculator.calculateSunsetPolarity(
            moonLongitudeAtSunset, sunLongitudeAtSunset
        )

        // ═══════════════════════════════════════════════════════════════════
        // ✅ FIX MAJOR: Folosim timezone-ul LOCAȚIEI pentru formatare, nu al telefonului!
        // ═══════════════════════════════════════════════════════════════════
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeFormat.timeZone = locationTimeZone  // ✅ ACEASTA ESTE CHEIA FIX-ULUI!
        
        val sunriseFormatted = timeFormat.format(sunrise.time)
        val sunsetFormatted = timeFormat.format(sunset.time)
        
        // ✅ DEBUG LOG - pentru verificare
        android.util.Log.d("AstroRepository", "═══════════════════════════════════════")
        android.util.Log.d("AstroRepository", "📍 Location: $locationName")
        android.util.Log.d("AstroRepository", "📍 TimeZone offset: $timeZone ore")
        android.util.Log.d("AstroRepository", "📍 LocationTimeZone: ${locationTimeZone.id}")
        android.util.Log.d("AstroRepository", "🌅 Sunrise formatted: $sunriseFormatted")
        android.util.Log.d("AstroRepository", "🌇 Sunset formatted:  $sunsetFormatted")
        android.util.Log.d("AstroRepository", "═══════════════════════════════════════")

        val sunSign = getZodiacSign(sunLongitude)
        val moonSign = getZodiacSign(moonLongitude)
        
        // ✅ CALCULEAZĂ APUSUL DE MÂINE
        val nextSunset = astroCalculator.calculateSunset(
            tomorrowYear, tomorrowMonth, tomorrowDay,
            longitude, latitude, timeZone
        )

        // ✅ FORMATEAZĂ ORELE PENTRU MÂINE (folosind același timeFormat cu timezone-ul locației)
        val nextSunriseFormatted = timeFormat.format(nextSunrise.time)
        val nextSunsetFormatted = timeFormat.format(nextSunset.time)

        // ✅ CALCULEAZĂ POLARITATEA pentru răsăritul de mâine
        val nextSunriseHour = nextSunrise.get(Calendar.HOUR_OF_DAY)
        val nextSunriseMinute = nextSunrise.get(Calendar.MINUTE)
        val nextSunriseSecond = nextSunrise.get(Calendar.SECOND)

        val moonLongitudeAtNextSunrise = astroCalculator.calculateMoonLongitude(
            tomorrowYear, tomorrowMonth, tomorrowDay, nextSunriseHour, nextSunriseMinute, nextSunriseSecond
        )
        val sunLongitudeAtNextSunrise = astroCalculator.calculateSunLongitude(
            tomorrowYear, tomorrowMonth, tomorrowDay, nextSunriseHour, nextSunriseMinute, nextSunriseSecond
        )

        val nextSunrisePolarity = polarityCalculator.calculateSunrisePolarity(
            moonLongitudeAtNextSunrise, sunLongitudeAtNextSunrise
        )

        // ✅ CALCULEAZĂ POLARITATEA pentru apusul de mâine
        val nextSunsetYear = nextSunset.get(Calendar.YEAR)
        val nextSunsetMonth = nextSunset.get(Calendar.MONTH) + 1
        val nextSunsetDay = nextSunset.get(Calendar.DAY_OF_MONTH)
        val nextSunsetHour = nextSunset.get(Calendar.HOUR_OF_DAY)
        val nextSunsetMinute = nextSunset.get(Calendar.MINUTE)
        val nextSunsetSecond = nextSunset.get(Calendar.SECOND)

        val moonLongitudeAtNextSunset = astroCalculator.calculateMoonLongitude(
            nextSunsetYear, nextSunsetMonth, nextSunsetDay, nextSunsetHour, nextSunsetMinute, nextSunsetSecond
        )
        val sunLongitudeAtNextSunset = astroCalculator.calculateSunLongitude(
            nextSunsetYear, nextSunsetMonth, nextSunsetDay, nextSunsetHour, nextSunsetMinute, nextSunsetSecond
        )

        val nextSunsetPolarity = polarityCalculator.calculateSunsetPolarity(
            moonLongitudeAtNextSunset, sunLongitudeAtNextSunset
        )

        return AstroData(
            timestamp = calendar.timeInMillis,
            currentTime = calendar,
            sunrise = sunrise,
            sunset = sunset,
            sunriseFormatted = sunriseFormatted,
            sunsetFormatted = sunsetFormatted,
            tattva = tattvaResult,
            subTattva = subTattvaResult,
            planet = planet,
            nitya = nitya,
            moonLongitude = moonLongitude,
            sunLongitude = sunLongitude,
            sunSign = sunSign,
            moonSign = moonSign,
            locationName = locationName,
            isGPSLocation = isGPSLocation,
            latitude = latitude,
            longitude = longitude,
            timeZone = timeZone,
            // ✅ POLARITATE AZI
            sunrisePolarity = sunrisePolarity,
            sunsetPolarity = sunsetPolarity,
            sunrisePolaritySymbol = polarityCalculator.getPolaritySymbol(sunrisePolarity),
            sunsetPolaritySymbol = polarityCalculator.getPolaritySymbol(sunsetPolarity),
            // ✅ RĂSĂRIT/APUS MÂINE
            nextSunrise = nextSunrise,
            nextSunset = nextSunset,
            nextSunriseFormatted = nextSunriseFormatted,
            nextSunsetFormatted = nextSunsetFormatted,
            nextSunrisePolarity = nextSunrisePolarity,
            nextSunsetPolarity = nextSunsetPolarity,
            nextSunrisePolaritySymbol = polarityCalculator.getPolaritySymbol(nextSunrisePolarity),
            nextSunsetPolaritySymbol = polarityCalculator.getPolaritySymbol(nextSunsetPolarity),
            // ✅ Faze luna
            moonPhase = moonPhase,
			// ✅ Nakshatra
            nakshatra = nakshatra
        )
    }

    /**
     * ✅ CORECTAT: Folosește durate FIXE pentru Tattva (1440 sec) și SubTattva (288 sec)
     * Fiecare Tattva mare = 24 minute = 1440 secunde
     * Fiecare SubTattva = 4 min 48 sec = 288 secunde
     */
    fun generateTattvaDaySchedule(
        sunriseTime: Calendar,
        latitude: Double,
        longitude: Double,
        timeZone:  Double,
        currentTime: Calendar = Calendar.getInstance(),
        locationName: String = "București"  // ✅ Adăugat pentru timezone corect
    ): List<TattvaDayItem> {
        val tattvaList = mutableListOf<TattvaDayItem>()
        
        // ✅ FIX DST: Folosim timezone cu suport pentru DST
        val locationTimeZone = getLocationTimeZone(locationName, timeZone)
        
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeFormat.timeZone = locationTimeZone  // ✅ IMPORTANT! 
        
        android.util.Log.d("TattvaDebug", "============================================")
        android.util.Log.d("TattvaDebug", "🚀 FIXED: Using exact 1440sec/Tattva, 288sec/SubTattva")
        android.util.Log.d("TattvaDebug", "============================================")
        android.util.Log.d("TattvaDebug", "🌅 Sunrise Calendar TimeZone: ${sunriseTime.timeZone.id}")
        android.util.Log.d("TattvaDebug", "🌅 Sunrise timeInMillis: ${sunriseTime.timeInMillis}")
        android.util.Log.d("TattvaDebug", "🌅 Sunrise formatted with locationTimeZone: ${timeFormat.format(sunriseTime.time)}")
        android.util.Log.d("TattvaDebug", "🌍 Location TimeZone: ${locationTimeZone.id} (offset: $timeZone hours)")
        
        val startTime = System.currentTimeMillis()
        
        val tattvaSequence = listOf("A", "V", "T", "Ap", "P")
        var cycleNumber = 1
        
        // ✅ DURATĂ FIXĂ - NU mai calculăm dinamic! 
        val tattvaDurationMillis = 1440 * 1000L   // 24 minute = 1440 secunde
        val subTattvaDurationMillis = 288 * 1000L // 4 min 48 sec = 288 secunde
        
        // ✅ Generează 60 Tattva-uri cu durate FIXE
        for (tattvaIndex in 0 until 60) {
            val tattvaCode = tattvaSequence[tattvaIndex % 5]
            
            // ✅ CORECT: Calculează timpul EXACT pentru Tattva
            val tattvaStartMillis = sunriseTime.timeInMillis + (tattvaDurationMillis * tattvaIndex)
            val tattvaEndMillis = tattvaStartMillis + tattvaDurationMillis
            
            // ✅ FIX: Creează Calendar cu timezone-ul locației
            val tattvaStartTime = Calendar.getInstance(locationTimeZone).apply {
                timeInMillis = tattvaStartMillis
            }
            val tattvaEndTime = Calendar.getInstance(locationTimeZone).apply {
                timeInMillis = tattvaEndMillis
            }
            
            // ✅ Generează 5 SubTattva-uri cu durată FIXĂ de 288 secunde
            val subTattvas = mutableListOf<SubTattvaItem>()
            
            for (subIndex in 0 until 5) {
                val subCode = tattvaSequence[subIndex]
                
                // ✅ Calculează START și END EXACT pentru fiecare SubTattva
                val subStartMillis = tattvaStartMillis + (subTattvaDurationMillis * subIndex)
                val subEndMillis = subStartMillis + subTattvaDurationMillis
                
                // ✅ FIX: Creează Calendar cu timezone-ul locației
                val subStartTime = Calendar.getInstance(locationTimeZone).apply {
                    timeInMillis = subStartMillis
                }
                
                // Verifică dacă e curentă
                val isSubCurrent = currentTime.timeInMillis >= subStartMillis &&
                                   currentTime.timeInMillis < subEndMillis
                
                subTattvas.add(
                    SubTattvaItem(
                        name = getTattvaName(subCode),
                        code = subCode,
                        startTime = subStartTime,
                        color = getTattvaColor(subCode),
                        isCurrent = isSubCurrent
                    )
                )
            }
            
            // Verifică dacă Tattva e curentă
            val isTattvaCurrent = currentTime.timeInMillis >= tattvaStartMillis &&
                                  currentTime.timeInMillis < tattvaEndMillis
            
            tattvaList.add(
                TattvaDayItem(
                    tattvaName = getTattvaName(tattvaCode),
                    tattvaCode = tattvaCode,
                    tattvaColor = getTattvaColor(tattvaCode),
                    startTime = tattvaStartTime,
                    endTime = tattvaEndTime,
                    subTattvas = subTattvas,
                    cycleNumber = cycleNumber,
                    isCurrent = isTattvaCurrent
                )
            )
            
            if ((tattvaIndex + 1) % 5 == 0) {
                cycleNumber++
            }
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        android.util.Log.d("TattvaDebug", "✅ Generated ${tattvaList.size} Tattvas in ${duration}ms!")
        android.util.Log.d("TattvaDebug", "🎯 First Tattva: ${timeFormat.format(tattvaList[0].startTime.time)}")
        android.util.Log.d("TattvaDebug", "🎯 Second Tattva (VAYU): ${timeFormat.format(tattvaList[1].startTime.time)}")
        android.util.Log.d("TattvaDebug", "============================================")
        
        return tattvaList
    }

    private fun getZodiacSign(longitude: Double): String {
        val signs = listOf(
            "Berbec", "Taur", "Gemeni", "Rac", "Leu", "Fecioară",
            "Balanță", "Scorpion", "Săgetător", "Capricorn", "Vărsător", "Pești"
        )
        
        // ✅ CORECTARE: Normalizează longitudinea la [0, 360)
        var normalizedLon = longitude
        while (normalizedLon < 0) normalizedLon += 360.0
        while (normalizedLon >= 360) normalizedLon -= 360.0
        
        // Calculează index-ul corect
        val index = (normalizedLon / 30.0).toInt()
        
        // Calculează gradele în semnul curent
        val degreesInSign = (normalizedLon % 30.0).toInt()
        
        android.util.Log.d("AstroRepository", "🌙 Zodiac:  lon=$longitude° → $normalizedLon° → ${signs[index]} $degreesInSign°")
        
        return "${degreesInSign}° ${signs[index]}"
    }
    
    private fun getTattvaName(code: String): String {
        return when (code) {
            "A" -> "Akasha"
            "V" -> "Vayu"
            "T" -> "Tejas"
            "Ap" -> "Apas"
            "P" -> "Prithivi"
            else -> "Unknown"
        }
    }
    
    private fun getTattvaColor(code: String): Color {
        return when (code) {
            "A" -> Color(0xFF4A00D3)
            "V" -> Color(0xFF009AD3)
            "T" -> Color(0xFFFF0000)
            "Ap" -> Color(0xFF8A8A8A)
            "P" -> Color(0xFFDFCD00)
            else -> Color.Gray
        }
    }
    
    /**
     * Calculează răsăritul și apusul pentru o dată specifică
     */
    fun calculateSunriseSunsetForDate(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        timeZone: Double
    ): Pair<Calendar, Calendar> {
        val sunrise = astroCalculator.calculateSunrise(year, month, day, longitude, latitude, timeZone)
        val sunset = astroCalculator.calculateSunset(year, month, day, longitude, latitude, timeZone)
        return Pair(sunrise, sunset)
    }
    
    /**
     * Generează schedule pentru o dată specifică
     */
    fun generateScheduleForDate(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        timeZone: Double,
        currentTime: Calendar = Calendar.getInstance(),
        locationName: String = "București"  // ✅ Adăugat pentru timezone corect
    ): List<TattvaDayItem> {
        val sunrise = astroCalculator.calculateSunrise(year, month, day, longitude, latitude, timeZone)
        return generateTattvaDaySchedule(
            sunriseTime = sunrise,
            latitude = latitude,
            longitude = longitude,
            timeZone = timeZone,
            currentTime = currentTime,
            locationName = locationName
        )
    }
}