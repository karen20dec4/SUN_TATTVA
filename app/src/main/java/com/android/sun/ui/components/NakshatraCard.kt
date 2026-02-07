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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val locationTimeZone = when {
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
            val locationOffsetMillis = (timeZone * 3600 * 1000).toInt()
            SimpleTimeZone(locationOffsetMillis, "Location")
        }
    }
    
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
                        text = "Toate cele 27 Nakshatra",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Scrollable list of all Nakshatras - start with current, then remaining in order
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Build reordered list: current Nakshatra first, then the rest in circular order
                        val currentIndex = nakshatraResult.number - 1
                        val reorderedList = buildList {
                            // Add current Nakshatra first
                            add(NakshatraCalculator.nakshatraList[currentIndex])
                            // Add remaining Nakshatras in circular order
                            for (i in 1 until 27) {
                                val index = (currentIndex + i) % 27
                                add(NakshatraCalculator.nakshatraList[index])
                            }
                        }
                        
                        // Calculate time for each Nakshatra based on current one
                        val nakshatraDegrees = 360.0 / 27.0  // 13.333333°
                        val avgDegreesPerHour = 13.2 / 24.0  // ~0.55° per hour
                        
                        reorderedList.forEachIndexed { displayIndex, nakshatra ->
                            val isCurrent = displayIndex == 0 // First item is always current
                            
                            // Calculate start and end time for this Nakshatra
                            val nakshatraIndex = nakshatra.number - 1
                            val offsetFromCurrent = (nakshatraIndex - currentIndex + 27) % 27
                            
                            val hoursOffset = offsetFromCurrent * nakshatraDegrees / avgDegreesPerHour
                            val startTime = nakshatraResult.startTime.clone() as Calendar
                            startTime.add(Calendar.MINUTE, (hoursOffset * 60).toInt())
                            
                            val endTime = startTime.clone() as Calendar
                            endTime.add(Calendar.MINUTE, (nakshatraDegrees / avgDegreesPerHour * 60).toInt())
                            
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
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Debug info removed for production
                }
            }
        }
    }
}

/**
 * Header compact: Shows current Nakshatra + countdown (no title, single line)
 */
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
    
    // Calculează countdown
    val timeRemaining = nakshatraResult.endTime.timeInMillis - currentTime.timeInMillis
    val hoursRemaining = timeRemaining / (1000 * 60 * 60)
    val minutesRemaining = (timeRemaining / (1000 * 60)) % 60
    val secondsRemaining = (timeRemaining / 1000) % 60
    
    val countdownText = when {
        hoursRemaining > 0 -> String.format("%dh %dm %ds", hoursRemaining, minutesRemaining, secondsRemaining)
        minutesRemaining > 0 -> String.format("%dm %ds", minutesRemaining, secondsRemaining)
        else -> String.format("%ds", secondsRemaining)
    }
    
    // Single row with Nakshatra name and countdown
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ NAKSHATRA (stânga) - colored by Tattva
        Text(
            text = nakshatraResult.nakshatra.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = getNakshatraTattvaColor(nakshatraResult.nakshatra)
        )
        
        // ✅ COUNTDOWN + ICON (dreapta)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = countdownText,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Icon(
                imageVector = if (isExpanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
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
            text = nakshatra.planet,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (isCurrent) {
            Text(
                text = "◄ NOW",
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
    return when (tattva) {
        "A" -> Color(0xFF4A00D3)   // Akasha - Purple (not currently used)
        "V" -> Color(0xFF009AD3)   // Vayu - Blue (7 Nakshatras)
        "T" -> Color(0xFFFF0000)   // Tejas - Red (7 Nakshatras)
        "Ap" -> Color(0xFF8A8A8A)  // Apas - Gray (7 Nakshatras)
        "P" -> Color(0xFFDFCD00)   // Prithivi - Yellow (6 Nakshatras)
        else -> Color.Gray
    }
}
