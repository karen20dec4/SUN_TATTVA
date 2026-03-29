package com.android.sun.util

import java.util.SimpleTimeZone
import java.util.TimeZone

/**
 * Utilitar centralizat pentru maparea numelui locației la TimeZone cu suport DST.
 * 
 * Strategia de rezolvare (în ordinea priorității):
 * 1. Potrivire explicită (orașe românești, cazuri speciale)
 * 2. Potrivire IANA automată după numele orașului (acoperă ~400 orașe)
 * 3. Mapare pe baza țării (acoperă toate cele ~900 orașe din PredefinedCities)
 * 4. Fallback la offset static (fără DST)
 */
object TimeZoneUtils {

    /**
     * Obține TimeZone-ul corect pentru o locație (cu suport DST complet).
     *
     * @param locationName Numele locației (ex: "București, România" sau "London, United Kingdom")
     * @param timeZoneOffset Offset-ul timezone-ului în ore (ex: 2.0 pentru UTC+2)
     * @return TimeZone-ul corespunzător locației, cu suport DST
     */
    fun getLocationTimeZone(locationName: String, timeZoneOffset: Double): TimeZone {
        // Parse "City, Country" format
        val parts = locationName.split(",").map { it.trim() }
        val cityName = parts.firstOrNull() ?: locationName
        val country = parts.getOrNull(1) ?: ""

        // 1. Explicit Romanian cities (handle diacritics and GPS-geocoded names)
        getExplicitRomanianTimeZone(locationName)?.let { return it }

        // 2. IANA timezone ID matching by city name
        findIanaTimeZone(cityName)?.let { return it }

        // 3. Country-based mapping (handles DST for all countries)
        getCountryTimeZone(country, cityName, timeZoneOffset)?.let { return it }

        // 4. Fallback to static offset (no DST)
        val offsetMillis = (timeZoneOffset * 3600.0 * 1000.0).toInt()
        return SimpleTimeZone(offsetMillis, "Location")
    }

    // ═══════════════════════════════════════════════════════════════
    // 1. EXPLICIT ROMANIAN CITIES
    // ═══════════════════════════════════════════════════════════════

    private fun getExplicitRomanianTimeZone(locationName: String): TimeZone? {
        val roKeywords = listOf(
            "București", "Bucharest", "Cluj", "Timișoara", "Timisoara",
            "Iași", "Iasi", "Constanța", "Constanta", "Craiova",
            "Brașov", "Brasov", "Galați", "Galati", "Ploiești", "Ploiesti",
            "Oradea", "Arad", "Pitești", "Pitesti", "Sibiu", "Bacău", "Bacau",
            "Târgu Mureș", "Targu Mures", "Baia Mare", "Buzău", "Buzau",
            "Satu Mare", "Botoșani", "Botosani", "Suceava", "Deva",
            "Alba Iulia", "Focșani", "Focsani", "Reșița", "Resita",
            "Tulcea", "Slatina", "Zalău", "Zalau", "Călărași", "Calarasi",
            "Giurgiu", "Hunedoara", "Câmpina", "Campina", "Mediaș", "Medias",
            "România", "Romania"
        )
        return if (roKeywords.any { locationName.contains(it, ignoreCase = true) }) {
            TimeZone.getTimeZone("Europe/Bucharest")
        } else null
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. IANA TIMEZONE MATCHING BY CITY NAME
    // ═══════════════════════════════════════════════════════════════

    /**
     * Caută un IANA timezone ID care conține numele orașului.
     * Ex: "London" → "Europe/London", "Tokyo" → "Asia/Tokyo",
     *     "New York" → "America/New_York", "Canberra" → "Australia/Canberra"
     */
    private fun findIanaTimeZone(cityName: String): TimeZone? {
        val normalized = normalizeForIana(cityName)
        if (normalized.isBlank()) return null

        val allIds = TimeZone.getAvailableIDs()
        val matchingId = allIds.find { id ->
            id.substringAfterLast("/").equals(normalized, ignoreCase = true)
        }

        if (matchingId != null) {
            val tz = TimeZone.getTimeZone(matchingId)
            // Verify it resolved correctly (not defaulting to GMT)
            if (tz.id == matchingId) return tz
        }
        return null
    }

    private fun normalizeForIana(name: String): String {
        return name
            .replace(" ", "_")
            .replace("'", "")
            .replace("ã", "a").replace("á", "a").replace("à", "a").replace("â", "a").replace("ä", "a").replace("å", "a")
            .replace("é", "e").replace("è", "e").replace("ê", "e").replace("ë", "e").replace("ě", "e")
            .replace("í", "i").replace("ì", "i").replace("î", "i").replace("ï", "i")
            .replace("ó", "o").replace("ò", "o").replace("ô", "o").replace("ö", "o").replace("õ", "o").replace("ø", "o")
            .replace("ú", "u").replace("ù", "u").replace("û", "u").replace("ü", "u")
            .replace("ñ", "n").replace("ń", "n")
            .replace("ç", "c").replace("č", "c").replace("ć", "c")
            .replace("ș", "s").replace("ş", "s").replace("š", "s")
            .replace("ț", "t").replace("ţ", "t")
            .replace("ž", "z").replace("ź", "z")
            .replace("ł", "l")
            .replace("đ", "d").replace("ð", "d")
            .replace("ý", "y").replace("ÿ", "y")
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. COUNTRY-BASED MAPPING
    // ═══════════════════════════════════════════════════════════════

    private fun getCountryTimeZone(country: String, cityName: String, offset: Double): TimeZone? {
        val countryLower = country.lowercase()

        // Multi-timezone countries need city-level resolution
        return when (countryLower) {
            "usa", "united states" -> getUSTimeZone(cityName, offset)
            "canada" -> getCanadaTimeZone(cityName, offset)
            "australia" -> getAustraliaTimeZone(cityName, offset)
            "russia" -> getRussiaTimeZone(cityName, offset)
            "brazil" -> getBrazilTimeZone(cityName, offset)
            else -> {
                // Single-timezone countries
                val tzId = singleTzCountries[countryLower] ?: return null
                TimeZone.getTimeZone(tzId)
            }
        }
    }

    /**
     * Mapare completă țară → IANA timezone ID.
     * Acoperă toate țările din PredefinedCities.kt (~50 țări).
     * Include suport DST unde este cazul.
     */
    private val singleTzCountries = mapOf(
        // ── Europa CET/CEST (UTC+1 / UTC+2 vara) ──
        "germany" to "Europe/Berlin",
        "france" to "Europe/Paris",
        "italy" to "Europe/Rome",
        "spain" to "Europe/Madrid",
        "poland" to "Europe/Warsaw",
        "netherlands" to "Europe/Amsterdam",
        "belgium" to "Europe/Brussels",
        "switzerland" to "Europe/Zurich",
        "austria" to "Europe/Vienna",
        "czech republic" to "Europe/Prague",
        "hungary" to "Europe/Budapest",
        "slovakia" to "Europe/Bratislava",
        "croatia" to "Europe/Zagreb",
        "slovenia" to "Europe/Ljubljana",
        "serbia" to "Europe/Belgrade",
        "albania" to "Europe/Tirane",
        "luxembourg" to "Europe/Luxembourg",
        "norway" to "Europe/Oslo",
        "sweden" to "Europe/Stockholm",
        "denmark" to "Europe/Copenhagen",
        "bosnia and herzegovina" to "Europe/Sarajevo",
        "montenegro" to "Europe/Podgorica",
        "north macedonia" to "Europe/Skopje",
        "andorra" to "Europe/Andorra",
        "monaco" to "Europe/Monaco",
        "san marino" to "Europe/San_Marino",
        "liechtenstein" to "Europe/Vaduz",
        "malta" to "Europe/Malta",

        // ── Europa EET/EEST (UTC+2 / UTC+3 vara) ──
        "greece" to "Europe/Athens",
        "bulgaria" to "Europe/Sofia",
        "romania" to "Europe/Bucharest",
        "românia" to "Europe/Bucharest",
        "finland" to "Europe/Helsinki",
        "lithuania" to "Europe/Vilnius",
        "latvia" to "Europe/Riga",
        "estonia" to "Europe/Tallinn",
        "moldova" to "Europe/Chisinau",
        "ukraine" to "Europe/Kiev",
        "cyprus" to "Asia/Nicosia",

        // ── Europa WET/WEST (UTC+0 / UTC+1 vara) ──
        "united kingdom" to "Europe/London",
        "ireland" to "Europe/Dublin",
        "portugal" to "Europe/Lisbon",

        // ── Europa fără DST ──
        "turkey" to "Europe/Istanbul",
        "iceland" to "Atlantic/Reykjavik",

        // ── Orientul Mijlociu ──
        "israel" to "Asia/Jerusalem",
        "iran" to "Asia/Tehran",
        "jordan" to "Asia/Amman",
        "lebanon" to "Asia/Beirut",
        "syria" to "Asia/Damascus",
        "saudi arabia" to "Asia/Riyadh",
        "uae" to "Asia/Dubai",
        "iraq" to "Asia/Baghdad",

        // ── Asia (majoritatea fără DST) ──
        "japan" to "Asia/Tokyo",
        "china" to "Asia/Shanghai",
        "india" to "Asia/Kolkata",
        "south korea" to "Asia/Seoul",
        "thailand" to "Asia/Bangkok",
        "vietnam" to "Asia/Ho_Chi_Minh",
        "indonesia" to "Asia/Jakarta",
        "philippines" to "Asia/Manila",
        "singapore" to "Asia/Singapore",
        "malaysia" to "Asia/Kuala_Lumpur",
        "taiwan" to "Asia/Taipei",
        "hong kong" to "Asia/Hong_Kong",
        "afghanistan" to "Asia/Kabul",
        "pakistan" to "Asia/Karachi",
        "bangladesh" to "Asia/Dhaka",
        "sri lanka" to "Asia/Colombo",
        "nepal" to "Asia/Kathmandu",
        "bhutan" to "Asia/Thimphu",
        "mongolia" to "Asia/Ulaanbaatar",
        "north korea" to "Asia/Pyongyang",
        "myanmar" to "Asia/Rangoon",
        "cambodia" to "Asia/Phnom_Penh",
        "laos" to "Asia/Vientiane",

        // ── Africa ──
        "morocco" to "Africa/Casablanca",
        "egypt" to "Africa/Cairo",
        "south africa" to "Africa/Johannesburg",
        "nigeria" to "Africa/Lagos",
        "kenya" to "Africa/Nairobi",
        "ethiopia" to "Africa/Addis_Ababa",
        "algeria" to "Africa/Algiers",
        "tunisia" to "Africa/Tunis",
        "libya" to "Africa/Tripoli",
        "ghana" to "Africa/Accra",
        "senegal" to "Africa/Dakar",
        "dr congo" to "Africa/Kinshasa",
        "angola" to "Africa/Luanda",
        "sudan" to "Africa/Khartoum",
        "tanzania" to "Africa/Dar_es_Salaam",
        "uganda" to "Africa/Kampala",
        "mozambique" to "Africa/Maputo",
        "madagascar" to "Indian/Antananarivo",
        "cameroon" to "Africa/Douala",
        "zimbabwe" to "Africa/Harare",
        "namibia" to "Africa/Windhoek",

        // ── America de Sud ──
        "colombia" to "America/Bogota",
        "peru" to "America/Lima",
        "chile" to "America/Santiago",
        "ecuador" to "America/Guayaquil",
        "venezuela" to "America/Caracas",
        "uruguay" to "America/Montevideo",
        "paraguay" to "America/Asuncion",
        "bolivia" to "America/La_Paz",
        "argentina" to "America/Argentina/Buenos_Aires",

        // ── America Centrală / Caraibe ──
        "mexico" to "America/Mexico_City",
        "cuba" to "America/Havana",
        "guatemala" to "America/Guatemala",
        "panama" to "America/Panama",
        "costa rica" to "America/Costa_Rica",
        "jamaica" to "America/Jamaica",
        "dominican republic" to "America/Santo_Domingo",

        // ── Oceania ──
        "new zealand" to "Pacific/Auckland",
        "fiji" to "Pacific/Fiji"
    )

    // ── Țări cu mai multe timezone-uri ──

    private fun getUSTimeZone(cityName: String, offset: Double): TimeZone {
        val tzId = when {
            cityName.contains("New York", true) || cityName.contains("Washington", true) ||
            cityName.contains("Philadelphia", true) || cityName.contains("Miami", true) ||
            cityName.contains("Atlanta", true) || cityName.contains("Boston", true) ||
            cityName.contains("Detroit", true) || cityName.contains("Charlotte", true) ||
            cityName.contains("Baltimore", true) || cityName.contains("Jacksonville", true) ||
            cityName.contains("Columbus", true) || cityName.contains("Indianapolis", true) ||
            cityName.contains("Pittsburgh", true) || cityName.contains("Cleveland", true) ||
            cityName.contains("Nashville", true) || cityName.contains("Tampa", true) ||
            cityName.contains("Orlando", true) || cityName.contains("Richmond", true) -> "America/New_York"

            cityName.contains("Chicago", true) || cityName.contains("Houston", true) ||
            cityName.contains("Dallas", true) || cityName.contains("San Antonio", true) ||
            cityName.contains("Austin", true) || cityName.contains("Memphis", true) ||
            cityName.contains("Milwaukee", true) || cityName.contains("Kansas City", true) ||
            cityName.contains("Oklahoma", true) || cityName.contains("Minneapolis", true) ||
            cityName.contains("New Orleans", true) || cityName.contains("St. Louis", true) -> "America/Chicago"

            cityName.contains("Denver", true) || cityName.contains("Salt Lake", true) ||
            cityName.contains("Albuquerque", true) || cityName.contains("Boise", true) -> "America/Denver"

            cityName.contains("Phoenix", true) || cityName.contains("Tucson", true) -> "America/Phoenix"

            cityName.contains("Los Angeles", true) || cityName.contains("San Francisco", true) ||
            cityName.contains("San Diego", true) || cityName.contains("San Jose", true) ||
            cityName.contains("Seattle", true) || cityName.contains("Portland", true) ||
            cityName.contains("Las Vegas", true) || cityName.contains("Sacramento", true) -> "America/Los_Angeles"

            cityName.contains("Anchorage", true) -> "America/Anchorage"
            cityName.contains("Honolulu", true) -> "Pacific/Honolulu"

            else -> when (offset) {
                -5.0 -> "America/New_York"
                -6.0 -> "America/Chicago"
                -7.0 -> "America/Denver"
                -8.0 -> "America/Los_Angeles"
                -9.0 -> "America/Anchorage"
                -10.0 -> "Pacific/Honolulu"
                else -> "America/New_York"
            }
        }
        return TimeZone.getTimeZone(tzId)
    }

    private fun getCanadaTimeZone(cityName: String, offset: Double): TimeZone {
        val tzId = when {
            cityName.contains("Toronto", true) || cityName.contains("Ottawa", true) ||
            cityName.contains("Montreal", true) || cityName.contains("Quebec", true) -> "America/Toronto"

            cityName.contains("Vancouver", true) || cityName.contains("Victoria", true) -> "America/Vancouver"

            cityName.contains("Winnipeg", true) -> "America/Winnipeg"

            cityName.contains("Edmonton", true) || cityName.contains("Calgary", true) -> "America/Edmonton"

            cityName.contains("Halifax", true) -> "America/Halifax"

            cityName.contains("St. John", true) -> "America/St_Johns"

            else -> when (offset) {
                -3.5 -> "America/St_Johns"
                -4.0 -> "America/Halifax"
                -5.0 -> "America/Toronto"
                -6.0 -> "America/Winnipeg"
                -7.0 -> "America/Edmonton"
                -8.0 -> "America/Vancouver"
                else -> "America/Toronto"
            }
        }
        return TimeZone.getTimeZone(tzId)
    }

    private fun getAustraliaTimeZone(cityName: String, offset: Double): TimeZone {
        val tzId = when {
            cityName.contains("Sydney", true) || cityName.contains("Canberra", true) -> "Australia/Sydney"
            cityName.contains("Melbourne", true) -> "Australia/Melbourne"
            cityName.contains("Brisbane", true) || cityName.contains("Gold Coast", true) -> "Australia/Brisbane"
            cityName.contains("Perth", true) -> "Australia/Perth"
            cityName.contains("Adelaide", true) -> "Australia/Adelaide"
            cityName.contains("Darwin", true) -> "Australia/Darwin"
            cityName.contains("Hobart", true) -> "Australia/Hobart"
            else -> when (offset) {
                10.0 -> "Australia/Sydney"
                9.5 -> "Australia/Adelaide"
                8.0 -> "Australia/Perth"
                else -> "Australia/Sydney"
            }
        }
        return TimeZone.getTimeZone(tzId)
    }

    private fun getRussiaTimeZone(cityName: String, offset: Double): TimeZone {
        val tzId = when {
            cityName.contains("Moscow", true) || cityName.contains("Saint Petersburg", true) ||
            cityName.contains("St. Petersburg", true) -> "Europe/Moscow"
            cityName.contains("Novosibirsk", true) -> "Asia/Novosibirsk"
            cityName.contains("Yekaterinburg", true) -> "Asia/Yekaterinburg"
            cityName.contains("Vladivostok", true) -> "Asia/Vladivostok"
            cityName.contains("Krasnoyarsk", true) -> "Asia/Krasnoyarsk"
            cityName.contains("Omsk", true) -> "Asia/Omsk"
            cityName.contains("Irkutsk", true) -> "Asia/Irkutsk"
            cityName.contains("Samara", true) -> "Europe/Samara"
            cityName.contains("Volgograd", true) -> "Europe/Volgograd"
            cityName.contains("Kazan", true) || cityName.contains("Nizhny Novgorod", true) -> "Europe/Moscow"
            else -> when (offset) {
                2.0 -> "Europe/Kaliningrad"
                3.0 -> "Europe/Moscow"
                4.0 -> "Europe/Samara"
                5.0 -> "Asia/Yekaterinburg"
                6.0 -> "Asia/Omsk"
                7.0 -> "Asia/Krasnoyarsk"
                8.0 -> "Asia/Irkutsk"
                9.0 -> "Asia/Yakutsk"
                10.0 -> "Asia/Vladivostok"
                11.0 -> "Asia/Magadan"
                12.0 -> "Asia/Kamchatka"
                else -> "Europe/Moscow"
            }
        }
        return TimeZone.getTimeZone(tzId)
    }

    private fun getBrazilTimeZone(cityName: String, offset: Double): TimeZone {
        val tzId = when {
            cityName.contains("São Paulo", true) || cityName.contains("Sao Paulo", true) ||
            cityName.contains("Rio", true) || cityName.contains("Brasília", true) ||
            cityName.contains("Brasilia", true) || cityName.contains("Belo Horizonte", true) ||
            cityName.contains("Salvador", true) || cityName.contains("Curitiba", true) ||
            cityName.contains("Recife", true) || cityName.contains("Porto Alegre", true) ||
            cityName.contains("Fortaleza", true) -> "America/Sao_Paulo"
            cityName.contains("Manaus", true) -> "America/Manaus"
            else -> when (offset) {
                -3.0 -> "America/Sao_Paulo"
                -4.0 -> "America/Manaus"
                else -> "America/Sao_Paulo"
            }
        }
        return TimeZone.getTimeZone(tzId)
    }
}
