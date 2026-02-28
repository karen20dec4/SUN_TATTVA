package com.android.sun.util

import androidx.compose.ui.graphics.Color

/**
 * Sursă unică de adevăr pentru culorile Tattva.
 * Folosite în UI pentru Tattva, SubTattva și Nakshatra.
 *
 * Înlocuiește funcțiile duplicate din:
 * - TattvaInfo.kt → getTattvaColor()
 * - AstroRepository.kt → getTattvaColor()
 * - NakshatraCard.kt → getNakshatraTattvaColor()
 * - NakshatraDetailScreen.kt → getNakshatraColor()
 */
object TattvaColors {

    val Akasha  = Color(0xFF4A00D3)   // Akasha - Violet
    val Vayu    = Color(0xFF009AD3)   // Vayu - Albastru
    val Tejas   = Color(0xFFFF0000)   // Tejas - Roșu
    val Apas    = Color(0xFF8A8A8A)   // Apas - Argintiu/Gri
    val Prithivi = Color(0xFFDFCD00)  // Prithivi - Galben-Pământ

    /**
     * Obține culoarea Tattva după cod.
     * @param code Codul Tattva: "A" (Akasha), "V" (Vayu), "T" (Tejas), "Ap" (Apas), "P" (Prithivi)
     * @return Culoarea corespunzătoare Tattva-ului
     */
    fun getByCode(code: String): Color {
        return when (code) {
            "A"  -> Akasha
            "V"  -> Vayu
            "T"  -> Tejas
            "Ap" -> Apas
            "P"  -> Prithivi
            else -> Color.Gray
        }
    }
}
