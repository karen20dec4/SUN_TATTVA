package com.android.sun.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.domain.calculator.MoonPhaseResult
import com.android.sun.domain.calculator.ShivaratriDate
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card pentru afișarea fazelor lunii
 */
@Composable
fun MoonPhaseCard(
    moonSign: String,          // "29° Scorpion"
    moonPhase: MoonPhaseResult,
    modifier: Modifier = Modifier
) {
    var isFullMoonExpanded by remember { mutableStateOf(false) }
    var isShivaratriExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(7.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header:   Moon sign + Illumination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Moon:   $moonSign",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${moonPhase.illuminationPercent}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            // Tripura Sundari
            MoonEventRow(
                label = "Tripura Sundari:",
                date = moonPhase.nextTripuraSundari
            )
            
            // Full Moon (highlighted when in influence period, clickable to expand)
            MoonEventRow(
                label = "Full moon:",
                date = moonPhase.nextFullMoon,
                isHighlighted = moonPhase.isInFullMoonInfluence,
                onClick = { isFullMoonExpanded = !isFullMoonExpanded },
                showExpandArrow = true,
                isExpanded = isFullMoonExpanded
            )
            
            // Expandable Full Moon influence period
            AnimatedVisibility(
                visible = isFullMoonExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FullMoonInfluencePeriod(
                    fullMoonPeak = moonPhase.nextFullMoon,
                    isHighlighted = moonPhase.isInFullMoonInfluence
                )
            }
            
            // New Moon
            MoonEventRow(
                label = "New moon:",
                date = moonPhase.nextNewMoon
            )
            
            // Shivaratri (if available)
            if (moonPhase.nextShivaratri != null) {
                ShivaratriRow(
                    shivaratri = moonPhase.nextShivaratri,
                    onClick = { isShivaratriExpanded = !isShivaratriExpanded },
                    isExpanded = isShivaratriExpanded
                )
                
                // Expandable yearly Shivaratri list
                AnimatedVisibility(
                    visible = isShivaratriExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ShivaratriYearlyList(
                        yearlyDates = moonPhase.yearlyShivaratri,
                        nextShivaratri = moonPhase.nextShivaratri
                    )
                }
            }
        }
    }
}

/**
 * Displays the 18h influence period of the full moon
 * ✅ Responsive: single line display with auto-sizing text
 * Format: "1 Apr 11:11  ────  2 Apr 23:11"
 */
@Composable
private fun FullMoonInfluencePeriod(
    fullMoonPeak: Calendar,
    isHighlighted: Boolean = false
) {
    val highlightBg = Color(0xFF423e48)
    val highlightText = Color(0xFFd0ccd1)
    
    // Calculate 18h before and after peak
    val influenceStart = fullMoonPeak.clone() as Calendar
    influenceStart.add(Calendar.HOUR_OF_DAY, -18)
    
    val influenceEnd = fullMoonPeak.clone() as Calendar
    influenceEnd.add(Calendar.HOUR_OF_DAY, 18)
    
    // Format dates using the timezone from the fullMoonPeak Calendar
    val dateFormat = SimpleDateFormat("d MMM HH:mm", Locale.getDefault())
    dateFormat.timeZone = fullMoonPeak.timeZone
    
    val startText = dateFormat.format(influenceStart.time)
    val endText = dateFormat.format(influenceEnd.time)
    
    val textColor = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isHighlighted) {
                    Modifier
                        .background(
                            color = highlightBg,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                } else {
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                }
            ),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ Single Text element to guarantee single-line display
        Text(
            text = "$startText  ────  $endText",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}

/**
 * Shivaratri row with expand arrow
 * Format: "Shivaratri:    17 Mar / 18 Mar ▾"
 */
@Composable
private fun ShivaratriRow(
    shivaratri: ShivaratriDate,
    onClick: () -> Unit,
    isExpanded: Boolean
) {
    val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    dateFormat.timeZone = shivaratri.eveningDate.timeZone
    
    val eveningText = dateFormat.format(shivaratri.eveningDate.time)
    val morningText = dateFormat.format(shivaratri.morningDate.time)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Shivaratri:",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$eveningText / $morningText",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Expandable yearly Shivaratri list
 * Shows all Shivaratri dates for the current year, right-aligned
 */
@Composable
private fun ShivaratriYearlyList(
    yearlyDates: List<ShivaratriDate>,
    nextShivaratri: ShivaratriDate
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        yearlyDates.forEach { date ->
            val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
            dateFormat.timeZone = date.eveningDate.timeZone
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            yearFormat.timeZone = date.eveningDate.timeZone
            
            val eveningText = dateFormat.format(date.eveningDate.time)
            val morningText = dateFormat.format(date.morningDate.time)
            val yearText = yearFormat.format(date.eveningDate.time)
            
            val isNext = date.eveningDate.get(Calendar.DAY_OF_YEAR) == nextShivaratri.eveningDate.get(Calendar.DAY_OF_YEAR) &&
                         date.eveningDate.get(Calendar.YEAR) == nextShivaratri.eveningDate.get(Calendar.YEAR)
            
            val isPast = date.morningDate.timeInMillis < System.currentTimeMillis()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$eveningText / $morningText  $yearText",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isNext -> MaterialTheme.colorScheme.primary
                        isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    }
                )
            }
        }
    }
}

/**
 * ✅ Afișează data folosind timezone-ul din Calendar
 * ✅ Format: "3 Jan - 12:02 (GMT+2)" în loc de "GMT+02:00"
 * ✅ Optional expand arrow for expandable rows
 */
@Composable
private fun MoonEventRow(
    label: String,
    date: Calendar,
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    showExpandArrow: Boolean = false,
    isExpanded: Boolean = false
) {
    // ✅ Format pentru dată și oră (fără timezone)
    val dateFormat = SimpleDateFormat("d MMM - HH:mm", Locale.getDefault())
    dateFormat.timeZone = date.timeZone
    
    // ✅ Calculează offset-ul timezone-ului în ore
    val offsetMillis = date.timeZone.getOffset(date.timeInMillis)
    val offsetHours = offsetMillis / (1000 * 60 * 60)
    val timezoneText = if (offsetHours >= 0) {
        "(GMT+$offsetHours)"
    } else {
        "(GMT$offsetHours)"  // Minus-ul e deja inclus
    }
    
    // ✅ DEBUG: Log timezone-ul Calendar-ului primit
    com.android.sun.util.AppLog.d("MoonPhaseCard", "🕐 $label Calendar TZ: ${date.timeZone.id}, millis: ${date.timeInMillis}")
    
    val highlightBg = Color(0xFF423e48)
    val highlightText = Color(0xFFd0ccd1)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .then(
                if (isHighlighted) {
                    Modifier
                        .background(
                            color = highlightBg,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // ✅ Afișează data + timezone custom format + optional expand arrow
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateFormat.format(date.time),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timezoneText,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            if (showExpandArrow) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}