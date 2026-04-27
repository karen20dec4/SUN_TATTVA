package com.android.sun.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.R
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
    var isTripuraSundariExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val toastCopied = stringResource(R.string.copied_to_clipboard)
    val labelTripura = stringResource(R.string.tripura_sundari_label)
    val labelFullMoon = stringResource(R.string.full_moon_label)
    val labelShivaratri = stringResource(R.string.shivaratri_label)
    val headerTripura = stringResource(R.string.copy_header_tripura_sundari)
    val headerFullMoon = stringResource(R.string.copy_header_full_moon)
    val headerShivaratri = stringResource(R.string.copy_header_shivaratri)
    val labelNext = stringResource(R.string.copy_label_next)
    val labelPeak = stringResource(R.string.copy_label_peak)
    val labelUpcoming = stringResource(R.string.copy_label_upcoming)
    val labelInfluenceFmt = stringResource(R.string.copy_label_influence)
    val labelEveningMorningFmt = stringResource(R.string.copy_shivaratri_evening_morning)

    val onLongClickTripura: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val text = buildTripuraSundariCopyText(
            header = headerTripura,
            nextLabel = labelNext,
            upcomingLabel = labelUpcoming,
            nextDate = moonPhase.nextTripuraSundari,
            futureDates = moonPhase.futureTripuraSundari
        )
        copyToClipboard(context, labelTripura, text, toastCopied)
    }
    val onLongClickFullMoon: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val text = buildFullMoonCopyText(
            header = headerFullMoon,
            peakLabel = labelPeak,
            influenceFmt = labelInfluenceFmt,
            upcomingLabel = labelUpcoming,
            peakDate = moonPhase.nextFullMoon,
            futureDates = moonPhase.futureFullMoons
        )
        copyToClipboard(context, labelFullMoon, text, toastCopied)
    }
    val onLongClickShivaratri: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val sh = moonPhase.nextShivaratri
        if (sh != null) {
            val text = buildShivaratriCopyText(
                header = headerShivaratri,
                nextLabel = labelNext,
                upcomingLabel = labelUpcoming,
                eveningMorningFmt = labelEveningMorningFmt,
                next = sh,
                yearly = moonPhase.yearlyShivaratri
            )
            copyToClipboard(context, labelShivaratri, text, toastCopied)
        }
    }
    
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
                    text = stringResource(R.string.moon_label, moonSign),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${moonPhase.illuminationPercent}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            // Tripura Sundari (clickable to expand, long-press to copy)
            MoonEventRow(
                label = stringResource(R.string.tripura_sundari_label),
                date = moonPhase.nextTripuraSundari,
                onClick = { isTripuraSundariExpanded = !isTripuraSundariExpanded },
                onLongClick = onLongClickTripura,
                showExpandArrow = true,
                isExpanded = isTripuraSundariExpanded
            )
            
            // Expandable Tripura Sundari future dates list
            AnimatedVisibility(
                visible = isTripuraSundariExpanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 80)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                if (moonPhase.futureTripuraSundari.isNotEmpty()) {
                    TripuraSundariYearlyList(
                        futureTripuraSundari = moonPhase.futureTripuraSundari,
                        nextTripuraSundari = moonPhase.nextTripuraSundari
                    )
                }
            }
            
            // Full Moon (highlighted when in influence period, clickable to expand, long-press to copy)
            MoonEventRow(
                label = stringResource(R.string.full_moon_label),
                date = moonPhase.nextFullMoon,
                isHighlighted = moonPhase.isInFullMoonInfluence,
                onClick = { isFullMoonExpanded = !isFullMoonExpanded },
                onLongClick = onLongClickFullMoon,
                showExpandArrow = true,
                isExpanded = isFullMoonExpanded
            )
            
            // Expandable Full Moon influence period + future full moons list
            AnimatedVisibility(
                visible = isFullMoonExpanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 80)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FullMoonInfluencePeriod(
                        fullMoonPeak = moonPhase.nextFullMoon,
                        isHighlighted = moonPhase.isInFullMoonInfluence
                    )
                    
                    if (moonPhase.futureFullMoons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FullMoonYearlyList(
                            futureFullMoons = moonPhase.futureFullMoons,
                            nextFullMoon = moonPhase.nextFullMoon
                        )
                    }
                }
            }
            
            // Shivaratri (before New Moon)
            if (moonPhase.nextShivaratri != null) {
                ShivaratriRow(
                    shivaratri = moonPhase.nextShivaratri,
                    isHighlighted = moonPhase.isInShivaratriPeriod,
                    onClick = { isShivaratriExpanded = !isShivaratriExpanded },
                    onLongClick = onLongClickShivaratri,
                    isExpanded = isShivaratriExpanded
                )
                
                // Expandable yearly Shivaratri list
                AnimatedVisibility(
                    visible = isShivaratriExpanded,
                    enter = expandVertically(
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Top
                    ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 80)),
                    exit = shrinkVertically(
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.Top
                    ) + fadeOut(animationSpec = tween(durationMillis = 200))
                ) {
                    ShivaratriYearlyList(
                        yearlyDates = moonPhase.yearlyShivaratri,
                        nextShivaratri = moonPhase.nextShivaratri
                    )
                }
            }
            
            // New Moon (last)
            MoonEventRow(
                label = stringResource(R.string.new_moon_label),
                date = moonPhase.nextNewMoon
            )
        }
    }
}

/**
 * Displays the 18h influence period of the full moon
 * ✅ Responsive: single line display with auto-sizing text
 * ✅ Always shows a background for visibility
 * Format: "1 Apr 11:11  ────  2 Apr 23:11"
 */
@Composable
private fun FullMoonInfluencePeriod(
    fullMoonPeak: Calendar,
    isHighlighted: Boolean = false
) {
    // Active influence: more prominent dark background with accent border
    val activeBg = Color(0xFF2E1A47)       // Deep purple/indigo
    val activeText = Color(0xFFE1BEE7)     // Light purple text
    val activeBorder = Color(0xFFCE93D8)   // Purple accent border
    
    // Inactive: subtle contrasting background
    val inactiveBg = Color(0xFF37474F)     // Blue-grey dark  
    val inactiveText = Color(0xFFB0BEC5)   // Light blue-grey text
    
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
    
    val bgColor = if (isHighlighted) activeBg else inactiveBg
    val textColor = if (isHighlighted) activeText else inactiveText
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isHighlighted) {
                    Modifier
                        .background(
                            color = bgColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .then(
                            Modifier.padding(1.dp)
                                .background(
                                    color = activeBorder.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(7.dp)
                                )
                        )
                } else {
                    Modifier
                        .background(
                            color = bgColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
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
 * Expandable list of future full moon dates.
 * - First (nearest) full moon is highlighted with cyan
 * - Shows date + year for each full moon
 * - Alternating backgrounds for readability
 * - Same visual style as ShivaratriYearlyList
 */
@Composable
private fun FullMoonYearlyList(
    futureFullMoons: List<Calendar>,
    nextFullMoon: Calendar
) {
    val nextHighlightBg = Color(0xFFB2EBF2) // Light cyan
    val nextHighlightText = Color(0xFF004D40) // Dark teal
    val primaryRowBg = MaterialTheme.colorScheme.surfaceVariant
    val secondaryRowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        futureFullMoons.forEachIndexed { index, fullMoonDate ->
            val dateFormat = SimpleDateFormat("d MMM - HH:mm", Locale.getDefault())
            dateFormat.timeZone = fullMoonDate.timeZone
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            yearFormat.timeZone = fullMoonDate.timeZone
            
            val dateText = dateFormat.format(fullMoonDate.time)
            val yearText = yearFormat.format(fullMoonDate.time)
            
            val isNext = isSameDate(fullMoonDate, nextFullMoon)
            
            val rowBg = when {
                isNext -> nextHighlightBg
                index % 2 == 0 -> primaryRowBg
                else -> secondaryRowBg
            }
            
            val textColor = when {
                isNext -> nextHighlightText
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
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = yearText,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
            
            if (index < futureFullMoons.size - 1) {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

/**
 * Expandable list of future Tripura Sundari dates.
 * - First (nearest) date is highlighted with cyan
 * - Shows date + year for each Tripura Sundari
 * - Alternating backgrounds for readability
 * - Same visual style as FullMoonYearlyList
 */
@Composable
private fun TripuraSundariYearlyList(
    futureTripuraSundari: List<Calendar>,
    nextTripuraSundari: Calendar
) {
    val nextHighlightBg = Color(0xFFB2EBF2) // Light cyan
    val nextHighlightText = Color(0xFF004D40) // Dark teal
    val primaryRowBg = MaterialTheme.colorScheme.surfaceVariant
    val secondaryRowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        futureTripuraSundari.forEachIndexed { index, tripuraDate ->
            val dateFormat = SimpleDateFormat("d MMM - HH:mm", Locale.getDefault())
            dateFormat.timeZone = tripuraDate.timeZone
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            yearFormat.timeZone = tripuraDate.timeZone
            
            val dateText = dateFormat.format(tripuraDate.time)
            val yearText = yearFormat.format(tripuraDate.time)
            
            val isNext = isSameDate(tripuraDate, nextTripuraSundari)
            
            val rowBg = when {
                isNext -> nextHighlightBg
                index % 2 == 0 -> primaryRowBg
                else -> secondaryRowBg
            }
            
            val textColor = when {
                isNext -> nextHighlightText
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
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = yearText,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
            
            if (index < futureTripuraSundari.size - 1) {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

/**
 * Shivaratri row with expand arrow and highlight marker
 * Format: "Shivaratri:    17 Mar / 18 Mar ▾"
 * ✅ When highlighted (on Shivaratri day), shows same styling as full moon highlight
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShivaratriRow(
    shivaratri: ShivaratriDate,
    isHighlighted: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isExpanded: Boolean
) {
    val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    dateFormat.timeZone = shivaratri.eveningDate.timeZone
    
    val eveningText = dateFormat.format(shivaratri.eveningDate.time)
    val morningText = dateFormat.format(shivaratri.morningDate.time)
    
    val highlightBg = Color(0xFF423e48)
    val highlightText = Color(0xFFd0ccd1)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
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
            text = stringResource(R.string.shivaratri_label),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$eveningText / $morningText",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                tint = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Expandable yearly Shivaratri list with improved visual design:
 * - Shows next 12 future Shivaratri periods (past ones filtered out)
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
    val nextShivaratriHighlight = Color(0xFFB2EBF2) // Light cyan for next Shivaratri
    val nextShivaratriText = Color(0xFF004D40) // Dark teal on cyan background
    val primaryRowBg = MaterialTheme.colorScheme.surfaceVariant
    val secondaryRowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    
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
            
            // Determine row background: cyan for next, alternating for others
            val rowBg = when {
                isNext -> nextShivaratriHighlight
                index % 2 == 0 -> primaryRowBg
                else -> secondaryRowBg
            }
            
            // Text color: dark on cyan, normal for others
            val textColor = when {
                isNext -> nextShivaratriText
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
                    color = textColor,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = yearText,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    softWrap = false
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoonEventRow(
    label: String,
    date: Calendar,
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
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
                when {
                    onClick != null && onLongClick != null -> Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    onClick != null -> Modifier.clickable { onClick() }
                    else -> Modifier
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
            color = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
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
                color = if (isHighlighted) highlightText else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
            if (showExpandArrow) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
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

// =====================================================================
// Long-press copy helpers (v2.38+)
// =====================================================================

/**
 * Copies the given text to the system clipboard and shows a Toast confirmation.
 */
private fun copyToClipboard(
    context: Context,
    label: String,
    text: String,
    toastMessage: String
) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        com.android.sun.util.AppLog.e("MoonPhaseCard", "Clipboard copy failed", e)
    }
}

private fun formatDateLong(cal: Calendar): String {
    val fmt = SimpleDateFormat("EEEE, d MMMM yyyy - HH:mm", Locale.getDefault())
    fmt.timeZone = cal.timeZone
    return fmt.format(cal.time)
}

private fun formatDateShort(cal: Calendar): String {
    val fmt = SimpleDateFormat("d MMM yyyy - HH:mm", Locale.getDefault())
    fmt.timeZone = cal.timeZone
    return fmt.format(cal.time)
}

private fun formatDateInfluence(cal: Calendar): String {
    val fmt = SimpleDateFormat("d MMM HH:mm", Locale.getDefault())
    fmt.timeZone = cal.timeZone
    return fmt.format(cal.time)
}

private fun formatDateDayOnly(cal: Calendar): String {
    val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    fmt.timeZone = cal.timeZone
    return fmt.format(cal.time)
}

private fun buildTripuraSundariCopyText(
    header: String,
    nextLabel: String,
    upcomingLabel: String,
    nextDate: Calendar,
    futureDates: List<Calendar>
): String {
    val sb = StringBuilder()
    sb.append(header).append('\n')
    sb.append(String.format(nextLabel, formatDateLong(nextDate))).append('\n')
    if (futureDates.isNotEmpty()) {
        sb.append('\n').append(upcomingLabel).append('\n')
        futureDates.forEach { d ->
            sb.append("• ").append(formatDateShort(d)).append('\n')
        }
    }
    return sb.toString().trimEnd()
}

private fun buildFullMoonCopyText(
    header: String,
    peakLabel: String,
    influenceFmt: String,
    upcomingLabel: String,
    peakDate: Calendar,
    futureDates: List<Calendar>
): String {
    val sb = StringBuilder()
    sb.append(header).append('\n')
    sb.append(String.format(peakLabel, formatDateLong(peakDate))).append('\n')
    val influenceStart = (peakDate.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -18) }
    val influenceEnd = (peakDate.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, 18) }
    sb.append(
        String.format(influenceFmt, formatDateInfluence(influenceStart), formatDateInfluence(influenceEnd))
    ).append('\n')
    if (futureDates.isNotEmpty()) {
        sb.append('\n').append(upcomingLabel).append('\n')
        futureDates.forEach { d ->
            sb.append("• ").append(formatDateShort(d)).append('\n')
        }
    }
    return sb.toString().trimEnd()
}

private fun buildShivaratriCopyText(
    header: String,
    nextLabel: String,
    upcomingLabel: String,
    eveningMorningFmt: String,
    next: ShivaratriDate,
    yearly: List<ShivaratriDate>
): String {
    val sb = StringBuilder()
    sb.append(header).append('\n')
    val nextText = String.format(
        eveningMorningFmt,
        formatDateDayOnly(next.eveningDate),
        formatDateDayOnly(next.morningDate)
    )
    sb.append(String.format(nextLabel, nextText)).append('\n')
    if (yearly.isNotEmpty()) {
        sb.append('\n').append(upcomingLabel).append('\n')
        yearly.forEach { d ->
            sb.append("• ").append(
                String.format(
                    eveningMorningFmt,
                    formatDateDayOnly(d.eveningDate),
                    formatDateDayOnly(d.morningDate)
                )
            ).append('\n')
        }
    }
    return sb.toString().trimEnd()
}