package com.android.sun.data.model

import androidx.compose.ui.graphics.Color
import com.android.sun.domain.calculator.TattvaResult
import com.android.sun.domain.calculator.SubTattvaResult
import java.util.*

/**
 * Model UI pentru Tattva/SubTattva
 */
data class TattvaInfo(
    val name: String,
    val code: String,
    val startTime: Calendar,
    val endTime: Calendar,
    val remainingMinutes: Int,
    val remainingSeconds: Int,
    val color: Color
)

/**
 * Extensii pentru conversie din domain models
 */
fun TattvaResult.toTattvaInfo(): TattvaInfo {
    return TattvaInfo(
        name = tattva.displayName,
        code = tattva.code,
        startTime = startTime,
        endTime = endTime,
        remainingMinutes = remainingMinutes,
        remainingSeconds = remainingSeconds,
        color = getTattvaColor(tattva.code)
    )
}

fun SubTattvaResult.toTattvaInfo(): TattvaInfo {
    return TattvaInfo(
        name = subTattva.displayName,
        code = subTattva.code,
        startTime = startTime,
        endTime = endTime,
        remainingMinutes = remainingMinutes,
        remainingSeconds = remainingSeconds,
        color = getTattvaColor(subTattva.code)
    )
}

/**
 * Culori pentru fiecare Tattva (fixe conform specificațiilor)
 */
private fun getTattvaColor(code: String): Color {
    return com.android.sun.util.TattvaColors.getByCode(code)
}

/**
 * Helper functions pentru formatare
 */
fun TattvaInfo.getFormattedStartTime(): String {
    return String.format(
        "%02d:%02d:%02d",
        startTime.get(Calendar.HOUR_OF_DAY),
        startTime.get(Calendar.MINUTE),
        startTime.get(Calendar.SECOND)
    )
}

fun TattvaInfo.getFormattedStopTime(): String {
    return String.format(
        "%02d:%02d:%02d",
        endTime.get(Calendar.HOUR_OF_DAY),
        endTime.get(Calendar.MINUTE),
        endTime.get(Calendar.SECOND)
    )
}

/**
 * Formatează timpul rămas în format MM:SS (fără ore)
 */
fun TattvaInfo.getFormattedRemainingTime(): String {
    // TattvaInfo are deja remainingMinutes și remainingSeconds
    // Formatăm direct ca MM:SS
    return String.format("%02d:%02d", remainingMinutes, remainingSeconds)
}