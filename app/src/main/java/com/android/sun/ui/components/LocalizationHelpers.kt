package com.android.sun.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.sun.R
import com.android.sun.domain.calculator.NakshatraType
import com.android.sun.domain.calculator.PlanetType

/**
 * Localization helpers for translating hardcoded enum values
 * to localized string resources.
 */

/**
 * Returns the localized zodiac sign name for the given index (0-11).
 * 0=Aries, 1=Taurus, ..., 11=Pisces
 */
@Composable
fun getLocalizedZodiacSign(signIndex: Int): String {
    return when (signIndex) {
        0 -> stringResource(R.string.zodiac_aries)
        1 -> stringResource(R.string.zodiac_taurus)
        2 -> stringResource(R.string.zodiac_gemini)
        3 -> stringResource(R.string.zodiac_cancer)
        4 -> stringResource(R.string.zodiac_leo)
        5 -> stringResource(R.string.zodiac_virgo)
        6 -> stringResource(R.string.zodiac_libra)
        7 -> stringResource(R.string.zodiac_scorpio)
        8 -> stringResource(R.string.zodiac_sagittarius)
        9 -> stringResource(R.string.zodiac_capricorn)
        10 -> stringResource(R.string.zodiac_aquarius)
        11 -> stringResource(R.string.zodiac_pisces)
        else -> ""
    }
}

/**
 * Returns the localized planet name for a PlanetType enum value.
 */
@Composable
fun getLocalizedPlanetName(planet: PlanetType): String {
    return when (planet) {
        PlanetType.SUN -> stringResource(R.string.planet_sun)
        PlanetType.MOON -> stringResource(R.string.planet_moon)
        PlanetType.MARS -> stringResource(R.string.planet_mars)
        PlanetType.MERCURY -> stringResource(R.string.planet_mercury)
        PlanetType.JUPITER -> stringResource(R.string.planet_jupiter)
        PlanetType.VENUS -> stringResource(R.string.planet_venus)
        PlanetType.SATURN -> stringResource(R.string.planet_saturn)
    }
}

/**
 * Returns the localized planet name for a nakshatra's governing planet.
 * Handles Rahu and Ketu which are not in PlanetType enum.
 */
@Composable
fun getLocalizedNakshatraPlanet(nakshatra: NakshatraType): String {
    return when (nakshatra.planet) {
        "Soare" -> stringResource(R.string.planet_sun)
        "Lună", "Luna" -> stringResource(R.string.planet_moon)
        "Marte" -> stringResource(R.string.planet_mars)
        "Mercur" -> stringResource(R.string.planet_mercury)
        "Jupiter" -> stringResource(R.string.planet_jupiter)
        "Venus" -> stringResource(R.string.planet_venus)
        "Saturn" -> stringResource(R.string.planet_saturn)
        "Rahu" -> stringResource(R.string.planet_rahu)
        "Ketu" -> stringResource(R.string.planet_ketu)
        else -> nakshatra.planet
    }
}

/**
 * Returns the localized symbol for a nakshatra.
 */
@Composable
fun getLocalizedNakshatraSymbol(nakshatra: NakshatraType): String {
    return when (nakshatra) {
        NakshatraType.ASHWINI -> stringResource(R.string.nakshatra_symbol_ashwini)
        NakshatraType.BHARANI -> stringResource(R.string.nakshatra_symbol_bharani)
        NakshatraType.KRITTIKA -> stringResource(R.string.nakshatra_symbol_krittika)
        NakshatraType.ROHINI -> stringResource(R.string.nakshatra_symbol_rohini)
        NakshatraType.MRIGASHIRA -> stringResource(R.string.nakshatra_symbol_mrigashira)
        NakshatraType.ARDRA -> stringResource(R.string.nakshatra_symbol_ardra)
        NakshatraType.PUNARVASU -> stringResource(R.string.nakshatra_symbol_punarvasu)
        NakshatraType.PUSHYA -> stringResource(R.string.nakshatra_symbol_pushya)
        NakshatraType.ASHLESHA -> stringResource(R.string.nakshatra_symbol_ashlesha)
        NakshatraType.MAGHA -> stringResource(R.string.nakshatra_symbol_magha)
        NakshatraType.PURVA_PHALGUNI -> stringResource(R.string.nakshatra_symbol_purva_phalguni)
        NakshatraType.UTTARA_PHALGUNI -> stringResource(R.string.nakshatra_symbol_uttara_phalguni)
        NakshatraType.HASTA -> stringResource(R.string.nakshatra_symbol_hasta)
        NakshatraType.CHITRA -> stringResource(R.string.nakshatra_symbol_chitra)
        NakshatraType.SWATI -> stringResource(R.string.nakshatra_symbol_swati)
        NakshatraType.VISHAKHA -> stringResource(R.string.nakshatra_symbol_vishakha)
        NakshatraType.ANURADHA -> stringResource(R.string.nakshatra_symbol_anuradha)
        NakshatraType.JYESHTHA -> stringResource(R.string.nakshatra_symbol_jyeshtha)
        NakshatraType.MULA -> stringResource(R.string.nakshatra_symbol_mula)
        NakshatraType.PURVA_ASHADHA -> stringResource(R.string.nakshatra_symbol_purva_ashadha)
        NakshatraType.UTTARA_ASHADHA -> stringResource(R.string.nakshatra_symbol_uttara_ashadha)
        NakshatraType.SHRAVANA -> stringResource(R.string.nakshatra_symbol_shravana)
        NakshatraType.DHANISHTA -> stringResource(R.string.nakshatra_symbol_dhanishta)
        NakshatraType.SHATABHISHA -> stringResource(R.string.nakshatra_symbol_shatabhisha)
        NakshatraType.PURVA_BHADRAPADA -> stringResource(R.string.nakshatra_symbol_purva_bhadrapada)
        NakshatraType.UTTARA_BHADRAPADA -> stringResource(R.string.nakshatra_symbol_uttara_bhadrapada)
        NakshatraType.REVATI -> stringResource(R.string.nakshatra_symbol_revati)
    }
}

/**
 * Returns the localized animal for a nakshatra.
 */
@Composable
fun getLocalizedNakshatraAnimal(nakshatra: NakshatraType): String {
    return when (nakshatra) {
        NakshatraType.ASHWINI -> stringResource(R.string.nakshatra_animal_ashwini)
        NakshatraType.BHARANI -> stringResource(R.string.nakshatra_animal_bharani)
        NakshatraType.KRITTIKA -> stringResource(R.string.nakshatra_animal_krittika)
        NakshatraType.ROHINI -> stringResource(R.string.nakshatra_animal_rohini)
        NakshatraType.MRIGASHIRA -> stringResource(R.string.nakshatra_animal_mrigashira)
        NakshatraType.ARDRA -> stringResource(R.string.nakshatra_animal_ardra)
        NakshatraType.PUNARVASU -> stringResource(R.string.nakshatra_animal_punarvasu)
        NakshatraType.PUSHYA -> stringResource(R.string.nakshatra_animal_pushya)
        NakshatraType.ASHLESHA -> stringResource(R.string.nakshatra_animal_ashlesha)
        NakshatraType.MAGHA -> stringResource(R.string.nakshatra_animal_magha)
        NakshatraType.PURVA_PHALGUNI -> stringResource(R.string.nakshatra_animal_purva_phalguni)
        NakshatraType.UTTARA_PHALGUNI -> stringResource(R.string.nakshatra_animal_uttara_phalguni)
        NakshatraType.HASTA -> stringResource(R.string.nakshatra_animal_hasta)
        NakshatraType.CHITRA -> stringResource(R.string.nakshatra_animal_chitra)
        NakshatraType.SWATI -> stringResource(R.string.nakshatra_animal_swati)
        NakshatraType.VISHAKHA -> stringResource(R.string.nakshatra_animal_vishakha)
        NakshatraType.ANURADHA -> stringResource(R.string.nakshatra_animal_anuradha)
        NakshatraType.JYESHTHA -> stringResource(R.string.nakshatra_animal_jyeshtha)
        NakshatraType.MULA -> stringResource(R.string.nakshatra_animal_mula)
        NakshatraType.PURVA_ASHADHA -> stringResource(R.string.nakshatra_animal_purva_ashadha)
        NakshatraType.UTTARA_ASHADHA -> stringResource(R.string.nakshatra_animal_uttara_ashadha)
        NakshatraType.SHRAVANA -> stringResource(R.string.nakshatra_animal_shravana)
        NakshatraType.DHANISHTA -> stringResource(R.string.nakshatra_animal_dhanishta)
        NakshatraType.SHATABHISHA -> stringResource(R.string.nakshatra_animal_shatabhisha)
        NakshatraType.PURVA_BHADRAPADA -> stringResource(R.string.nakshatra_animal_purva_bhadrapada)
        NakshatraType.UTTARA_BHADRAPADA -> stringResource(R.string.nakshatra_animal_uttara_bhadrapada)
        NakshatraType.REVATI -> stringResource(R.string.nakshatra_animal_revati)
    }
}

/**
 * Returns the localized nature for a nakshatra.
 */
@Composable
fun getLocalizedNakshatraNature(nakshatra: NakshatraType): String {
    return when (nakshatra) {
        NakshatraType.ASHWINI -> stringResource(R.string.nakshatra_nature_ashwini)
        NakshatraType.BHARANI -> stringResource(R.string.nakshatra_nature_bharani)
        NakshatraType.KRITTIKA -> stringResource(R.string.nakshatra_nature_krittika)
        NakshatraType.ROHINI -> stringResource(R.string.nakshatra_nature_rohini)
        NakshatraType.MRIGASHIRA -> stringResource(R.string.nakshatra_nature_mrigashira)
        NakshatraType.ARDRA -> stringResource(R.string.nakshatra_nature_ardra)
        NakshatraType.PUNARVASU -> stringResource(R.string.nakshatra_nature_punarvasu)
        NakshatraType.PUSHYA -> stringResource(R.string.nakshatra_nature_pushya)
        NakshatraType.ASHLESHA -> stringResource(R.string.nakshatra_nature_ashlesha)
        NakshatraType.MAGHA -> stringResource(R.string.nakshatra_nature_magha)
        NakshatraType.PURVA_PHALGUNI -> stringResource(R.string.nakshatra_nature_purva_phalguni)
        NakshatraType.UTTARA_PHALGUNI -> stringResource(R.string.nakshatra_nature_uttara_phalguni)
        NakshatraType.HASTA -> stringResource(R.string.nakshatra_nature_hasta)
        NakshatraType.CHITRA -> stringResource(R.string.nakshatra_nature_chitra)
        NakshatraType.SWATI -> stringResource(R.string.nakshatra_nature_swati)
        NakshatraType.VISHAKHA -> stringResource(R.string.nakshatra_nature_vishakha)
        NakshatraType.ANURADHA -> stringResource(R.string.nakshatra_nature_anuradha)
        NakshatraType.JYESHTHA -> stringResource(R.string.nakshatra_nature_jyeshtha)
        NakshatraType.MULA -> stringResource(R.string.nakshatra_nature_mula)
        NakshatraType.PURVA_ASHADHA -> stringResource(R.string.nakshatra_nature_purva_ashadha)
        NakshatraType.UTTARA_ASHADHA -> stringResource(R.string.nakshatra_nature_uttara_ashadha)
        NakshatraType.SHRAVANA -> stringResource(R.string.nakshatra_nature_shravana)
        NakshatraType.DHANISHTA -> stringResource(R.string.nakshatra_nature_dhanishta)
        NakshatraType.SHATABHISHA -> stringResource(R.string.nakshatra_nature_shatabhisha)
        NakshatraType.PURVA_BHADRAPADA -> stringResource(R.string.nakshatra_nature_purva_bhadrapada)
        NakshatraType.UTTARA_BHADRAPADA -> stringResource(R.string.nakshatra_nature_uttara_bhadrapada)
        NakshatraType.REVATI -> stringResource(R.string.nakshatra_nature_revati)
    }
}

/**
 * Returns the localized degree range for a nakshatra.
 */
@Composable
fun getLocalizedNakshatraDegreeRange(nakshatra: NakshatraType): String {
    return when (nakshatra) {
        NakshatraType.ASHWINI -> stringResource(R.string.nakshatra_range_ashwini)
        NakshatraType.BHARANI -> stringResource(R.string.nakshatra_range_bharani)
        NakshatraType.KRITTIKA -> stringResource(R.string.nakshatra_range_krittika)
        NakshatraType.ROHINI -> stringResource(R.string.nakshatra_range_rohini)
        NakshatraType.MRIGASHIRA -> stringResource(R.string.nakshatra_range_mrigashira)
        NakshatraType.ARDRA -> stringResource(R.string.nakshatra_range_ardra)
        NakshatraType.PUNARVASU -> stringResource(R.string.nakshatra_range_punarvasu)
        NakshatraType.PUSHYA -> stringResource(R.string.nakshatra_range_pushya)
        NakshatraType.ASHLESHA -> stringResource(R.string.nakshatra_range_ashlesha)
        NakshatraType.MAGHA -> stringResource(R.string.nakshatra_range_magha)
        NakshatraType.PURVA_PHALGUNI -> stringResource(R.string.nakshatra_range_purva_phalguni)
        NakshatraType.UTTARA_PHALGUNI -> stringResource(R.string.nakshatra_range_uttara_phalguni)
        NakshatraType.HASTA -> stringResource(R.string.nakshatra_range_hasta)
        NakshatraType.CHITRA -> stringResource(R.string.nakshatra_range_chitra)
        NakshatraType.SWATI -> stringResource(R.string.nakshatra_range_swati)
        NakshatraType.VISHAKHA -> stringResource(R.string.nakshatra_range_vishakha)
        NakshatraType.ANURADHA -> stringResource(R.string.nakshatra_range_anuradha)
        NakshatraType.JYESHTHA -> stringResource(R.string.nakshatra_range_jyeshtha)
        NakshatraType.MULA -> stringResource(R.string.nakshatra_range_mula)
        NakshatraType.PURVA_ASHADHA -> stringResource(R.string.nakshatra_range_purva_ashadha)
        NakshatraType.UTTARA_ASHADHA -> stringResource(R.string.nakshatra_range_uttara_ashadha)
        NakshatraType.SHRAVANA -> stringResource(R.string.nakshatra_range_shravana)
        NakshatraType.DHANISHTA -> stringResource(R.string.nakshatra_range_dhanishta)
        NakshatraType.SHATABHISHA -> stringResource(R.string.nakshatra_range_shatabhisha)
        NakshatraType.PURVA_BHADRAPADA -> stringResource(R.string.nakshatra_range_purva_bhadrapada)
        NakshatraType.UTTARA_BHADRAPADA -> stringResource(R.string.nakshatra_range_uttara_bhadrapada)
        NakshatraType.REVATI -> stringResource(R.string.nakshatra_range_revati)
    }
}
