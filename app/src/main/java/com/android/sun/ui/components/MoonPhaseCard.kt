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
            
            // Shivaratri (before New Moon)
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
            
            // New Moon (last)
            MoonEventRow(
                label = "New moon:",
                date = moonPhase.nextNewMoon
            )
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
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
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
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$eveningText / $morningText",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
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
 * Expandable yearly Shivaratri list with improved visual design:
 * - Full-width rows with alternating backgrounds for readability
 * - Next Shivaratri highlighted with light cyan background
 * - Uniform 16sp bold font matching the rest of the Moon card
 */
@Composable
private fun ShivaratriYearlyList(
    yearlyDates: List<ShivaratriDate>,
    nextShivaratri: ShivaratriDate
) {
    // Colors for alternating rows and highlight
    val cyanHighlight = Color(0xFFB2EBF2) // Light cyan for next Shivaratri
    val evenRowBg = MaterialTheme.colorScheme.surfaceVariant
    val oddRowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        // Thin separator at top of expanded list
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        yearlyDates.forEachIndexed { index, date ->
            val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
            dateFormat.timeZone = date.eveningDate.timeZone
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            yearFormat.timeZone = date.eveningDate.timeZone
            
            val eveningText = dateFormat.format(date.eveningDate.time)
            val morningText = dateFormat.format(date.morningDate.time)
            val yearText = yearFormat.format(date.eveningDate.time)
            
            val isNext = isSameDate(date.eveningDate, nextShivaratri.eveningDate)
            val isPast = date.morningDate.timeInMillis < System.currentTimeMillis()
            
            // Determine row background: cyan for next, alternating for others
            val rowBg = when {
                isNext -> cyanHighlight
                index % 2 == 0 -> evenRowBg
                else -> oddRowBg
            }
            
            // Text color: dark on cyan, dimmed for past
            val textColor = when {
                isNext -> Color(0xFF004D40) // Dark teal on cyan background
                isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = rowBg,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$eveningText / $morningText",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = yearText,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            // Small spacing between rows
            if (index < yearlyDates.size - 1) {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

/**
 * ✅ Afișează data folosind timezone-ul din Calendar
 * ✅ Format: "3 Jan - 12:02" (no GMT info)
 * ✅ Uniform 16sp bold font matching header
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
    // Format pentru dată și oră
    val dateFormat = SimpleDateFormat("d MMM - HH:mm", Locale.getDefault())
    dateFormat.timeZone = date.timeZone
    
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
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Afișează data + optional expand arrow (no GMT info)
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateFormat.format(date.time),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * Helper to compare two Calendar dates (same day of year and year)
 */
private fun isSameDate(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
           cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}