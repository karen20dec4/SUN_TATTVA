package com.android.sun.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de culori pentru aplicația SUN
 * Material Design 3 + Culori specifice pentru Tatvas
 */

// ============================================
// CULORI PRINCIPALE (Material 3)
// ============================================

// Light Theme
val Primary = Color(0xFF2D2A34)
//val Primary = Color(0xFF6750A4)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFEADDFF)
val OnPrimaryContainer = Color(0xFF21005D)

val Secondary = Color(0xFF625B71)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFE8DEF8)
val OnSecondaryContainer = Color(0xFF1D192B)

val Tertiary = Color(0xFF7D5260)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFFFD8E4)
val OnTertiaryContainer = Color(0xFF31111D)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF9DEDC)
val OnErrorContainer = Color(0xFF410E0B)

val Background = Color(0xFFFFFBFE)
val OnBackground = Color(0xFF1C1B1F)
val Surface = Color(0xFFFFFBFE)
val OnSurface = Color(0xFF1C1B1F)

// val SurfaceVariant = Color(0xFFE7E0EC)  // de inlocuit CU F1EDF0
val SurfaceVariant = Color(0xFFF1EDF0)  
// val OnSurfaceVariant = Color(0xFF49454F) // inlocuim cu 2D2A34
val OnSurfaceVariant = Color(0xFF2D2A34)
val Outline = Color(0xFF79747E)

// Dark Theme
val PrimaryDark = Color(0xFFCAC4D0) // era D0BCFF
val OnPrimaryDark = Color(0xFF381E72)
val PrimaryContainerDark = Color(0xFF4F378B)
val OnPrimaryContainerDark = Color(0xFFEADDFF)

val SecondaryDark = Color(0xFFCCC2DC)
val OnSecondaryDark = Color(0xFF332D41)
val SecondaryContainerDark = Color(0xFF4A4458)
val OnSecondaryContainerDark = Color(0xFFE8DEF8)

val TertiaryDark = Color(0xFFEFB8C8)
val OnTertiaryDark = Color(0xFF492532)
val TertiaryContainerDark = Color(0xFF633B48)
val OnTertiaryContainerDark = Color(0xFFFFD8E4)

val ErrorDark = Color(0xFFF2B8B5)
val OnErrorDark = Color(0xFF601410)
val ErrorContainerDark = Color(0xFF8C1D18)
val OnErrorContainerDark = Color(0xFFF9DEDC)

val BackgroundDark = Color(0xFF1C1B1F)
val OnBackgroundDark = Color(0xFFE6E1E5)
val SurfaceDark = Color(0xFF1C1B1F)
val OnSurfaceDark = Color(0xFFE6E1E5)

// val SurfaceVariantDark = Color(0xFF49454F) // Ilocuim cu 2D2A34
val SurfaceVariantDark = Color(0xFF2D2A34)

val OnSurfaceVariantDark = Color(0xFFCAC4D0)
val OutlineDark = Color(0xFF938F99)

// ============================================
// CULORI TATTVA (din AstroData.java)
// ============================================

/**
 * Akasha - Ether/Space
 * Culoare: #2c1745 (mov întunecat)
 */
val TattvaAkasha = Color(0xFF2C1745)
val OnTattvaAkasha = Color(0xFFFFFFFF)

/**
 * Vayu - Air
 * Culoare: #000088 (albastru închis)
 */
val TattvaVayu = Color(0xFF000088)
val OnTattvaVayu = Color(0xFFFFFFFF)

/**
 * Tejas - Fire
 * Culoare: #880000 (roșu închis)
 */
val TattvaTejas = Color(0xFF880000)
val OnTattvaTejas = Color(0xFFFFFFFF)

/**
 * Apas - Water
 * Culoare: #5a5a5a (gri)
 */
val TattvaApas = Color(0xFF5A5A5A)
val OnTattvaApas = Color(0xFFFFFFFF)

/**
 * Prithivi - Earth
 * Culoare: #504008 (maro/galben închis)
 */
val TattvaPrithivi = Color(0xFF504008)
val OnTattvaPrithivi = Color(0xFFFFFFFF)

// ============================================
// FUNCȚII HELPER PENTRU CULORI TATTVA
// ============================================

/**
 * Convertește string hex în Color
 * Ex: "2c1745" → Color(0xFF2C1745)
 */
fun String.toColor(): Color {
    return try {
        Color(("FF" + this).toLong(16))
    } catch (e: Exception) {
        Color.Gray  // Fallback
    }
}

/**
 * Returnează culoarea tattva bazată pe hex string
 */
fun getTattvaColor(hexColor: String): Color {
    return when (hexColor.lowercase()) {
        "2c1745" -> TattvaAkasha
        "000088" -> TattvaVayu
        "880000" -> TattvaTejas
        "5a5a5a" -> TattvaApas
        "504008" -> TattvaPrithivi
        else -> hexColor.toColor()
    }
}

/**
 * Returnează culoarea textului pentru fundal tattva
 * (toate au text alb pentru contrast bun)
 */
fun getTattvaOnColor(hexColor: String): Color {
    return Color.White
}

// ============================================
// CULORI SUPLIMENTARE
// ============================================

val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val InfoBlue = Color(0xFF2196F3)

// Transparențe
val ScrimDark = Color(0x99000000)  // 60% opacitate
val ScrimLight = Color(0x4D000000)  // 30% opacitate