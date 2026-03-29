package com.android.sun.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.sun.R

/**
 * Tipografie pentru aplicația SUN — 3 profiluri selectabile din Settings
 *
 * Profil 1 „Quicksand+":
 *   Titluri:   Quicksand  Bold (700) — max weight for this font
 *   Subtitluri: Work Sans  Bold (700)
 *   Cifre:     Nunito Sans SemiBold (600) / ExtraBold (800)
 *
 * Profil 2 „Kanit":
 *   Titluri:   Kanit Bold (700)
 *   Subtitluri: Open Sans Medium (500)
 *   Cifre:     Lato SemiBold (600)
 *
 * Profil 3 „Claritate Maximă":
 *   Titluri:   Public Sans SemiBold (600)
 *   Subtitluri: Inter Medium (500)
 *   Cifre:     Rubik Normal (400) / SemiBold (600)
 *
 * All fonts use separate STATIC .ttf files per weight (not variable fonts)
 * to ensure Android Compose correctly differentiates FontWeight variants.
 */

// ── Font Profile IDs ──────────────────────────────────────────
const val FONT_PROFILE_QUICKSAND = 1
const val FONT_PROFILE_KANIT = 2
const val FONT_PROFILE_PUBLIC_SANS = 3

// ── CompositionLocal for current font profile ─────────────────
val LocalFontProfile = staticCompositionLocalOf { FONT_PROFILE_QUICKSAND }

// ══════════════════════════════════════════════════════════════
//  PROFIL 1  —  Quicksand + Work Sans + Nunito Sans (40% bolder)
//  NOTE: Quicksand max weight is 700 (Bold) — no ExtraBold/Black
// ══════════════════════════════════════════════════════════════

val QuicksandFamily = FontFamily(
    Font(R.font.quicksand_bold, FontWeight.Bold)
)

val WorkSansFamily = FontFamily(
    Font(R.font.work_sans_medium, FontWeight.Medium),
    Font(R.font.work_sans_semibold, FontWeight.SemiBold),
    Font(R.font.work_sans_bold, FontWeight.Bold)
)

val NunitoSansFamily = FontFamily(
    Font(R.font.nunito_sans_regular, FontWeight.Normal),
    Font(R.font.nunito_sans_medium, FontWeight.Medium),
    Font(R.font.nunito_sans_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_sans_bold, FontWeight.Bold),
    Font(R.font.nunito_sans_extrabold, FontWeight.ExtraBold)
)

// ══════════════════════════════════════════════════════════════
//  PROFIL 2  —  Kanit + Open Sans + Lato
// ══════════════════════════════════════════════════════════════

val KanitFamily = FontFamily(
    Font(R.font.kanit_bold, FontWeight.Bold),
    Font(R.font.kanit_extrabold, FontWeight.ExtraBold)
)

val OpenSansFamily = FontFamily(
    Font(R.font.open_sans_regular, FontWeight.Normal),
    Font(R.font.open_sans_medium, FontWeight.Medium),
    Font(R.font.open_sans_semibold, FontWeight.SemiBold),
    Font(R.font.open_sans_bold, FontWeight.Bold)
)

val LatoFamily = FontFamily(
    Font(R.font.lato_semibold, FontWeight.SemiBold),
    Font(R.font.lato_bold, FontWeight.Bold)
)

// ══════════════════════════════════════════════════════════════
//  PROFIL 3  —  Public Sans + Inter + Rubik
// ══════════════════════════════════════════════════════════════

val PublicSansFamily = FontFamily(
    Font(R.font.public_sans_regular, FontWeight.Normal),
    Font(R.font.public_sans_medium, FontWeight.Medium),
    Font(R.font.public_sans_semibold, FontWeight.SemiBold),
    Font(R.font.public_sans_bold, FontWeight.Bold),
    Font(R.font.public_sans_extrabold, FontWeight.ExtraBold),
    Font(R.font.public_sans_black, FontWeight.Black)
)

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val RubikFamily = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_semibold, FontWeight.SemiBold),
    Font(R.font.rubik_bold, FontWeight.Bold)
)

// ══════════════════════════════════════════════════════════════
//  Font profile data class
// ══════════════════════════════════════════════════════════════

private data class FontProfileSpec(
    val titleFamily: FontFamily,
    val titleWeight: FontWeight,
    val subtitleFamily: FontFamily,
    val subtitleWeight: FontWeight,
    val numericFamily: FontFamily,
    val numericWeight: FontWeight,
    val numericBoldWeight: FontWeight
)

private fun getFontProfileSpec(profile: Int): FontProfileSpec = when (profile) {
    FONT_PROFILE_KANIT -> FontProfileSpec(
        titleFamily = KanitFamily,
        titleWeight = FontWeight.Bold,
        subtitleFamily = OpenSansFamily,
        subtitleWeight = FontWeight.Medium,
        numericFamily = LatoFamily,
        numericWeight = FontWeight.SemiBold,
        numericBoldWeight = FontWeight.SemiBold
    )
    FONT_PROFILE_PUBLIC_SANS -> FontProfileSpec(
        titleFamily = PublicSansFamily,
        titleWeight = FontWeight.SemiBold,
        subtitleFamily = InterFamily,
        subtitleWeight = FontWeight.Medium,
        numericFamily = RubikFamily,
        numericWeight = FontWeight.Normal,
        numericBoldWeight = FontWeight.SemiBold
    )
    else -> FontProfileSpec(  // FONT_PROFILE_QUICKSAND (default)
        titleFamily = QuicksandFamily,
        titleWeight = FontWeight.Bold,           // Quicksand max is Bold (700)
        subtitleFamily = WorkSansFamily,
        subtitleWeight = FontWeight.Bold,        // was Medium (500) → Bold (700)
        numericFamily = NunitoSansFamily,
        numericWeight = FontWeight.SemiBold,     // was Normal (400) → SemiBold (600)
        numericBoldWeight = FontWeight.ExtraBold // was Bold (700) → ExtraBold (800)
    )
}

// ══════════════════════════════════════════════════════════════
//  Build Typography from profile
// ══════════════════════════════════════════════════════════════

fun buildTypography(profile: Int): Typography {
    val spec = getFontProfileSpec(profile)
    return Typography(
        // Display styles — titluri mari
        displayLarge = TextStyle(
            fontFamily = spec.titleFamily,
            fontWeight = spec.titleWeight,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = spec.titleFamily,
            fontWeight = spec.titleWeight,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = spec.titleFamily,
            fontWeight = spec.titleWeight,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),

        // Headline styles — titluri secțiuni
        headlineLarge = TextStyle(
            fontFamily = spec.titleFamily,
            fontWeight = spec.titleWeight,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = spec.titleFamily,
            fontWeight = spec.titleWeight,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = spec.titleFamily,
            fontWeight = spec.titleWeight,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),

        // Title styles — titluri carduri
        titleLarge = TextStyle(
            fontFamily = spec.titleFamily,
            fontWeight = spec.titleWeight,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = spec.titleFamily,
            fontWeight = spec.titleWeight,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = spec.titleFamily,
            fontWeight = spec.titleWeight,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),

        // Body styles — text normal / subtitluri
        bodyLarge = TextStyle(
            fontFamily = spec.subtitleFamily,
            fontWeight = spec.subtitleWeight,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = spec.subtitleFamily,
            fontWeight = spec.subtitleWeight,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = spec.subtitleFamily,
            fontWeight = spec.subtitleWeight,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        // Label styles — label-uri și butoane
        labelLarge = TextStyle(
            fontFamily = spec.subtitleFamily,
            fontWeight = spec.subtitleWeight,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = spec.subtitleFamily,
            fontWeight = spec.subtitleWeight,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = spec.subtitleFamily,
            fontWeight = spec.subtitleWeight,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

// Default typography (Profile 1) — for backwards compat
val Typography = buildTypography(FONT_PROFILE_QUICKSAND)

// ══════════════════════════════════════════════════════════════
//  Extra TextStyles — profile-aware via Composable accessors
// ══════════════════════════════════════════════════════════════

/**
 * Nunito Sans / Lato / Rubik — date numerice, ore, countdown-uri
 */
fun buildMonospaceTextStyle(profile: Int): TextStyle {
    val spec = getFontProfileSpec(profile)
    return TextStyle(
        fontFamily = spec.numericFamily,
        fontWeight = spec.numericWeight,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )
}

/**
 * Titluri Tattva/SubTattva/Planet
 */
fun buildTattvaNameStyle(profile: Int): TextStyle {
    val spec = getFontProfileSpec(profile)
    return TextStyle(
        fontFamily = spec.titleFamily,
        fontWeight = spec.titleWeight,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
}

/**
 * Coduri Tattva (G, O, A, U, L)
 */
fun buildTattvaCodeStyle(profile: Int): TextStyle {
    val spec = getFontProfileSpec(profile)
    return TextStyle(
        fontFamily = spec.numericFamily,
        fontWeight = spec.numericBoldWeight,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )
}

// ── Static defaults for non-Composable contexts ───────────────
val MonospaceTextStyle = buildMonospaceTextStyle(FONT_PROFILE_QUICKSAND)
val TattvaNameStyle = buildTattvaNameStyle(FONT_PROFILE_QUICKSAND)
val TattvaCodeStyle = buildTattvaCodeStyle(FONT_PROFILE_QUICKSAND)
