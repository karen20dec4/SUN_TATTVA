package com.android.sun.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.R
import com.android.sun.domain.calculator.NakshatraResult
import com.android.sun.domain.calculator.NakshatraType
import com.android.sun.domain.calculator.NakshatraCalculator
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card COMPACT pentru Nakshatra cu Expand/Collapse
 * ✅ COLLAPSED: afișează doar Nakshatra curentă cu countdown
 * ✅ EXPANDED: afișează toate cele 27 Nakshatra cu highlight la cea curentă
 */
@Composable
fun NakshatraCard(
    nakshatraResult: NakshatraResult,
    timeZone: Double,
    locationName: String = "București",
    onNakshatraClick: (NakshatraType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // ✅ FIX DST: Folosim timezone cu suport pentru DST
    val locationTimeZone = com.android.sun.util.TimeZoneUtils.getLocationTimeZone(locationName, timeZone)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
		shape = RoundedCornerShape(7.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ✅ HEADER: Afișează ÎNTOTDEAUNA (collapsed sau expanded)
            CurrentNakshatraHeader(
                nakshatraResult = nakshatraResult,
                locationTimeZone = locationTimeZone,
                isExpanded = isExpanded
            )
            
            // ✅ EXPANDED CONTENT: Toate Nakshatra-urile
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = stringResource(R.string.all_27_nakshatras),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    
                    // ✅ DEBUG: Show current moon position in zodiac
                    if (nakshatraResult.moonZodiacPosition.isNotEmpty()) {
                        val zodiacSignName = getLocalizedZodiacSign(nakshatraResult.moonZodiacSignIndex)
                        val localizedMoonPosition = stringResource(R.string.moon_zodiac_position, nakshatraResult.moonZodiacPosition, zodiacSignName)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.moon_position, localizedMoonPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Scrollable list of all Nakshatras - start with current, then remaining in order
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ✅ FIX: Use pre-computed future Nakshatras from ephemeris data
                        // Each Nakshatra's time interval was calculated using actual moon
                        // positions and speeds at each boundary (not a single constant speed).
                        // This eliminates the large errors that accumulated when projecting
                        // days ahead with a single speed value.
                        val futureSlots = nakshatraResult.futureNakshatras
                        
                        if (futureSlots.isNotEmpty()) {
                            // Use pre-computed ephemeris-based time slots
                            futureSlots.forEachIndexed { displayIndex, slot ->
                                val isCurrent = displayIndex == 0
                                
                                NakshatraRow(
                                    nakshatra = slot.nakshatra,
                                    isCurrent = isCurrent,
                                    startTime = slot.startTime,
                                    endTime = slot.endTime,
                                    locationTimeZone = locationTimeZone,
                                    onClick = { onNakshatraClick(slot.nakshatra) }
                                )
                            }
                        } else {
                            // Fallback: old method using single constant speed (backward compatibility)
                            val currentIndex = nakshatraResult.number - 1
                            val reorderedList = buildList {
                                add(NakshatraCalculator.nakshatraList[currentIndex])
                                for (i in 1 until 27) {
                                    val index = (currentIndex + i) % 27
                                    add(NakshatraCalculator.nakshatraList[index])
                                }
                            }
                            
                            val nakshatraDegrees = 360.0 / 27.0
                            val avgDegreesPerHour = nakshatraResult.moonSpeedDegreesPerDay / 24.0
                            val zeroRef = nakshatraResult.zeroReferenceTime
                            
                            reorderedList.forEachIndexed { displayIndex, nakshatra ->
                                val isCurrent = displayIndex == 0
                                val nakshatraIndex = nakshatra.number - 1
                                val cycleOffset = if (nakshatraIndex < currentIndex) 27.0 else 0.0
                                val hoursToStart = (nakshatraIndex + cycleOffset) * nakshatraDegrees / avgDegreesPerHour
                                val hoursToEnd = (nakshatraIndex + cycleOffset + 1) * nakshatraDegrees / avgDegreesPerHour
                                
                                val startTime = zeroRef.clone() as Calendar
                                startTime.add(Calendar.MINUTE, (hoursToStart * 60).toInt())
                                
                                val endTime = zeroRef.clone() as Calendar
                                endTime.add(Calendar.MINUTE, (hoursToEnd * 60).toInt())
                                
                                NakshatraRow(
                                    nakshatra = nakshatra,
                                    isCurrent = isCurrent,
                                    startTime = startTime,
                                    endTime = endTime,
                                    locationTimeZone = locationTimeZone,
                                    onClick = { onNakshatraClick(nakshatra) }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Debug info removed for production
                }
            }
        }
    }
}

/**
 * Header compact: Shows "Nakshatra" as card title on first row,
 * then Nakshatra name + countdown on second row.
 * ✅ FIX: Uses ephemeris-refined endTime from futureNakshatras[0] for accurate countdown
 * ✅ FIX: "Nakshatra:" moved to title row to prevent long names from being cut off
 */
@Suppress("UNUSED_PARAMETER")
@Composable
private fun CurrentNakshatraHeader(
    nakshatraResult: NakshatraResult,
    locationTimeZone: TimeZone,
    isExpanded: Boolean
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }
    
    // ✅ FIX: Use ephemeris-refined endTime from futureNakshatras[0] if available,
    // otherwise fall back to the simple extrapolation endTime.
    // The futureNakshatras endTime is calculated using actual Swiss Ephemeris data
    // at each boundary (Newton-Raphson refinement), while nakshatraResult.endTime
    // uses a simple speed extrapolation which can be off by hours.
    val accurateEndTime = nakshatraResult.futureNakshatras.firstOrNull()?.endTime
        ?: nakshatraResult.endTime
    
    // Calculează countdown folosind endTime-ul precis
    val timeRemaining = accurateEndTime.timeInMillis - currentTime.timeInMillis
    
    // ✅ FIX: Handle negative time (when Nakshatra has ended)
    val countdownText = if (timeRemaining <= 0) {
        "0s"  // Show 0s when time has expired
    } else {
        // Calculate hours, minutes, seconds from positive time remaining
        val totalSeconds = timeRemaining / 1000L
        val hoursRemaining = totalSeconds / 3600L
        val minutesRemaining = (totalSeconds % 3600L) / 60L
        val secondsRemaining = totalSeconds % 60L
        
        when {
            hoursRemaining > 0 -> String.format("%dh %dm", hoursRemaining, minutesRemaining)
            minutesRemaining > 0 -> String.format("%dm", minutesRemaining)
            else -> String.format("%ds", secondsRemaining)
        }
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // ✅ ROW 1: "Nakshatra" title + expand/collapse icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.nakshatra_label),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Icon(
                imageVector = if (isExpanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // ✅ ROW 2: Nakshatra name (colored by Tattva) + countdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nakshatra name (stânga) - colored by Tattva, full space for long names
            Text(
                text = nakshatraResult.nakshatra.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = getNakshatraTattvaColor(nakshatraResult.nakshatra),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            
            // Countdown (dreapta)
            Text(
                text = countdownText,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

/**
 * Rând individual pentru o Nakshatra
 */
@Composable
private fun NakshatraRow(
    nakshatra: NakshatraType,
    isCurrent: Boolean,
    startTime: Calendar,
    endTime: Calendar,
    locationTimeZone: TimeZone,
    onClick: () -> Unit
) {
    // Format date and time
    val dateTimeFormat = SimpleDateFormat("d-MMM HH:mm", Locale.ENGLISH).apply {
        timeZone = locationTimeZone
    }
    val timeInterval = "${dateTimeFormat.format(startTime.time)}   -  ${dateTimeFormat.format(endTime.time)}"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = getNakshatraTattvaColor(nakshatra).copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nume și interval orar
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${nakshatra.number}. ${nakshatra.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = timeInterval,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        // Planetă
        Text(
            text = getLocalizedNakshatraPlanet(nakshatra),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (isCurrent) {
            Text(
                text = stringResource(R.string.now_indicator),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Culori pentru Nakshatra (bazate pe planetă) - DEPRECATED, use getNakshatraTattvaColor instead
 */
private fun getNakshatraColor(nakshatra: NakshatraType): Color {
    return when (nakshatra.planet) {
        "Soare" -> Color(0xFFFFD700)      // Gold
        "Luna" -> Color(0xFFC0C0C0)       // Silver
        "Mercur" -> Color(0xFF808080)     // Gray
        "Venus" -> Color(0xFF00CED1)      // Turquoise
        "Marte" -> Color(0xFFFF4500)      // Red-Orange
        "Jupiter" -> Color(0xFF4169E1)    // Royal Blue
        "Saturn" -> Color(0xFF2F4F4F)     // Dark Slate Gray
        "Rahu" -> Color(0xFF8B4513)       // Saddle Brown
        "Ketu" -> Color(0xFF9370DB)       // Medium Purple
        else -> Color.Gray
    }
}

/**
 * Returns Tattva type for each Nakshatra based on Vedic correspondence
 */
private fun getNakshatraTattva(nakshatra: NakshatraType): String {
    return when (nakshatra) {
        // PRITHIVI (Earth)
        NakshatraType.DHANISHTA -> "P"
        NakshatraType.ROHINI -> "P"
        NakshatraType.JYESHTHA -> "P"
        NakshatraType.ANURADHA -> "P"
        NakshatraType.SHRAVANA -> "P"
        NakshatraType.UTTARA_ASHADHA -> "P"
        
        // APAS (Water)
        NakshatraType.PURVA_ASHADHA -> "Ap"
        NakshatraType.ASHLESHA -> "Ap"
        NakshatraType.MULA -> "Ap"
        NakshatraType.ARDRA -> "Ap"
        NakshatraType.REVATI -> "Ap"
        NakshatraType.UTTARA_BHADRAPADA -> "Ap"
        NakshatraType.SHATABHISHA -> "Ap"
        
        // TEJAS (Fire)
        NakshatraType.BHARANI -> "T"
        NakshatraType.KRITTIKA -> "T"
        NakshatraType.PUSHYA -> "T"
        NakshatraType.MAGHA -> "T"
        NakshatraType.PURVA_PHALGUNI -> "T"
        NakshatraType.PURVA_BHADRAPADA -> "T"
        NakshatraType.SWATI -> "T"
        
        // VAYU (Air)
        NakshatraType.VISHAKHA -> "V"
        NakshatraType.UTTARA_PHALGUNI -> "V"
        NakshatraType.HASTA -> "V"
        NakshatraType.CHITRA -> "V"
        NakshatraType.PUNARVASU -> "V"
        NakshatraType.ASHWINI -> "V"
        NakshatraType.MRIGASHIRA -> "V"
    }
}

/**
 * Returns the color for a Nakshatra based on its governing Tattva
 * Note: Akasha case included for completeness but not currently assigned to any Nakshatra
 */
private fun getNakshatraTattvaColor(nakshatra: NakshatraType): Color {
    val tattva = getNakshatraTattva(nakshatra)
    return com.android.sun.util.TattvaColors.getByCode(tattva)
}
