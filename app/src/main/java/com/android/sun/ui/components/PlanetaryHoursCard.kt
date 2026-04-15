package com.android.sun.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.domain.calculator.PlanetType
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.android.sun.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card COMPACT pentru Planetary Hours cu Expand/Collapse
 * ✅ COLLAPSED: afișează doar ora curentă
 * ✅ EXPANDED: afișează toate orele cu auto-highlight la ora curentă
 */
@Composable
fun PlanetaryHoursCard(
    sunrise: Calendar,
    sunset: Calendar,
    nextSunrise: Calendar,
    currentPlanetIndex: Int,  
    timeZone: Double,
    locationName: String = "București",
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // ✅ FIX DST: Folosim timezone cu suport pentru DST
    val locationTimeZone = com.android.sun.util.TimeZoneUtils.getLocationTimeZone(timeZone)
    
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
            // ✅ HEADER:  Afișează ÎNTOTDEAUNA (collapsed sau expanded)
            CurrentPlanetaryHourHeader(
                sunrise = sunrise,
                sunset = sunset,
                nextSunrise = nextSunrise,
                currentPlanetIndex = currentPlanetIndex,
                timeZone = timeZone,
                locationTimeZone = locationTimeZone,
                isExpanded = isExpanded
            )
            
            // ✅ EXPANDED CONTENT: Toate orele
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 80)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Date range
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).apply {
                        this.timeZone = locationTimeZone
                    }
                    Text(
                        text = "${dateFormat.format(sunrise.time)} - ${dateFormat.format(nextSunrise.time)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    
                    
					
					Spacer(modifier = Modifier.height(12.dp))
                    
                    // Scrollable list of all hours - reordered with current first
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Build reordered list: current hour first, then remaining in circular order
                        val dayOfWeek = sunrise.get(Calendar.DAY_OF_WEEK) - 1
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                            this.timeZone = locationTimeZone
                        }
                        
                        // Generate all 24 hours
                        val allHours = buildList {
                            // Day hours (0-11)
                            for (i in 0 until 12) {
                                val startTime = sunrise
                                val endTime = sunset
                                val totalDuration = endTime.timeInMillis - startTime.timeInMillis
                                val hourDuration = totalDuration / 12
                                
                                val hourStartMillis = startTime.timeInMillis + (i * hourDuration)
                                val hourEndMillis = hourStartMillis + hourDuration
                                
                                val hourStart = Calendar.getInstance(locationTimeZone).apply { timeInMillis = hourStartMillis }
                                val hourEnd = Calendar.getInstance(locationTimeZone).apply { timeInMillis = hourEndMillis }
                                
                                val planet = getPlanetForGlobalIndex(dayOfWeek, i)
                                add(Triple(planet, timeFormat.format(hourStart.time), timeFormat.format(hourEnd.time)))
                            }
                            
                            // Night hours (12-23)
                            for (i in 12 until 24) {
                                val startTime = sunset
                                val endTime = nextSunrise
                                val totalDuration = endTime.timeInMillis - startTime.timeInMillis
                                val hourDuration = totalDuration / 12
                                val hourIndex = i - 12
                                
                                val hourStartMillis = startTime.timeInMillis + (hourIndex * hourDuration)
                                val hourEndMillis = hourStartMillis + hourDuration
                                
                                val hourStart = Calendar.getInstance(locationTimeZone).apply { timeInMillis = hourStartMillis }
                                val hourEnd = Calendar.getInstance(locationTimeZone).apply { timeInMillis = hourEndMillis }
                                
                                val planet = getPlanetForGlobalIndex(dayOfWeek, i)
                                add(Triple(planet, timeFormat.format(hourStart.time), timeFormat.format(hourEnd.time)))
                            }
                        }
                        
                        // Reorder: current first, then circular
                        val reorderedHours = buildList {
                            add(allHours[currentPlanetIndex])
                            for (i in 1 until 24) {
                                val index = (currentPlanetIndex + i) % 24
                                add(allHours[index])
                            }
                        }
                        
                        reorderedHours.forEachIndexed { displayIndex, (planet, start, end) ->
                            val isCurrent = displayIndex == 0
                            PlanetaryHourRow(
                                planet = planet,
                                startTime = start,
                                endTime = end,
                                isCurrent = isCurrent
                            )
                        }
                    }
					
					
					
					
					
					
					Spacer(modifier = Modifier.height(12.dp))
					
					// DEBUG INFO:  Sunrise + GMT (aliniază la dreapta)
						val sunriseFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
							this.timeZone = locationTimeZone
						}
						// ✅ DST-aware GMT offset (not static timeZone Double)
						val actualDebugOffsetMs = locationTimeZone.getOffset(System.currentTimeMillis())
						val actualDebugOffsetHours = actualDebugOffsetMs / 3600000.0
						val gmtDebugSign = if (actualDebugOffsetHours >= 0) "+" else ""
						val gmtDebugFormatted = if (actualDebugOffsetHours == actualDebugOffsetHours.toLong().toDouble()) {
							String.format("GMT%s%d", gmtDebugSign, actualDebugOffsetHours.toLong())
						} else {
							String.format("GMT%s%.1f", gmtDebugSign, actualDebugOffsetHours)
						}
						Text(
							modifier = Modifier.fillMaxWidth(),
							text = "debug️: Sunrise - ${sunriseFormat.format(sunrise.time)} • $gmtDebugFormatted",
							textAlign = TextAlign.Center,
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
							fontSize = 12.sp
						)
					// END DEBUG INFO:  Sunrise + GMT
					
					
					
					
					
					
					
                }
            }
        }
    }
}

/**
 * Header compact: Shows current planet + countdown (no title, single line like Nakshatra)
 */
@Suppress("UNUSED_PARAMETER")
@Composable
private fun CurrentPlanetaryHourHeader(
    sunrise: Calendar,
    sunset: Calendar,
    nextSunrise: Calendar,
    currentPlanetIndex: Int,
    timeZone: Double,
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
    
    // Logica de calcul rămâne aceeași
    val isDayTime = currentPlanetIndex < 12
    val startTime = if (isDayTime) sunrise else sunset
    val endTime = if (isDayTime) sunset else nextSunrise
    val hourIndexInPeriod = currentPlanetIndex % 12
    
    val totalDuration = endTime.timeInMillis - startTime.timeInMillis
    val planetaryHourDuration = totalDuration / 12
    
    val hourEndMillis = startTime.timeInMillis + ((hourIndexInPeriod + 1) * planetaryHourDuration)
    val dayOfWeek = sunrise.get(Calendar.DAY_OF_WEEK) - 1
    val planet = getPlanetForGlobalIndex(dayOfWeek, currentPlanetIndex)
    
    val timeRemaining = hourEndMillis - currentTime.timeInMillis
    
    // ✅ FIX: Handle negative time (when planetary hour has ended)
    val countdownText = if (timeRemaining <= 0) {
        "0s"  // Show 0s when time has expired
    } else {
        // Calculate hours, minutes, seconds from positive time remaining
        val totalSeconds = timeRemaining / 1000L
        val hoursRemaining = totalSeconds / 3600L
        val minutesRemaining = (totalSeconds % 3600L) / 60L
        val secondsRemaining = totalSeconds % 60L
        
        when {
            hoursRemaining > 0 -> String.format("%dh %dm %ds", hoursRemaining, minutesRemaining, secondsRemaining)
            minutesRemaining > 0 -> String.format("%dm %ds", minutesRemaining, secondsRemaining)
            else -> String.format("%ds", secondsRemaining)
        }
    }

    // ✅ HEADER-UL CU DIMENSIUNE FIXĂ (50.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp), // Aici am fixat înălțimea pentru a nu fi lărgită de fontul mare
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ PARTEA STÂNGĂ: Simbol Planetă + Nume
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = planet.code,
				modifier = Modifier.offset(y = (-10).dp),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 36.sp,
                    lineHeight = 36.sp,
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                color = getPlanetColor(planet)
            )
            
            Text(
                text = getLocalizedPlanetName(planet),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = getPlanetColor(planet),
                maxLines = 1
            )
        }
        
        // ✅ CENTRU: Simbol Tattva
        Text(
            text = getPlanetTattvaEmoji(planet),
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        // ✅ PARTEA DREAPTĂ: Countdown + Iconiță
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = countdownText,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
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
    } // Sfârșit Row principal
} // Sfârșit funcție Composable







/**
 * Secțiune pentru ZI sau NOAPTE
 */
@Composable
private fun PlanetaryHourSection(
    title:  String,
    startTime: Calendar,
    endTime: Calendar,
    hourStartIndex: Int,
    hourCount: Int,
    currentPlanetIndex: Int,
    dayOfWeek:  Int,
    locationTimeZone: TimeZone
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section title
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calculează durata unei ore planetare
        val totalDuration = endTime.timeInMillis - startTime.timeInMillis
        val planetaryHourDuration = totalDuration / 12
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            this.timeZone = locationTimeZone
        }
        
        // Afișează fiecare oră planetară
        repeat(hourCount) { index ->
            val globalIndex = hourStartIndex + index
            val planet = getPlanetForGlobalIndex(dayOfWeek, globalIndex)
            
            val hourStartMillis = startTime.timeInMillis + (index * planetaryHourDuration)
            val hourEndMillis = hourStartMillis + planetaryHourDuration
            
            val hourStart = Calendar.getInstance(locationTimeZone).apply {
                timeInMillis = hourStartMillis
            }
            val hourEnd = Calendar.getInstance(locationTimeZone).apply {
                timeInMillis = hourEndMillis
            }
            
            val isCurrent = globalIndex == currentPlanetIndex
            
            PlanetaryHourRow(
                planet = planet,
                startTime = timeFormat.format(hourStart.time),
                endTime = timeFormat.format(hourEnd.time),
                isCurrent = isCurrent
            )
        }
    }
}

/**
 * Rând individual pentru o oră planetară
 * ✅ Font +2 (de la 12sp la 14sp)
 */
@Composable
private fun PlanetaryHourRow(
    planet: PlanetType,
    startTime: String,
    endTime: String,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isCurrent) {
                    getPlanetColor(planet).copy(alpha = 0.2f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(4.dp)
            )
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ PLANETĂ PRIMUL (stânga) - symbol + name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = planet.code,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 18.sp,
                color = getPlanetColor(planet)
            )
            Text(
                text = getLocalizedPlanetName(planet),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) {
                    getPlanetColor(planet)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                }
            )
        }
        
        // ✅ CENTRU: Simbol Tattva
        Text(
            text = getPlanetTattvaEmoji(planet),
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
            textAlign = TextAlign.Center
        )
        
        // ✅ TIMP (dreapta)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$startTime - $endTime",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                softWrap = false
            )
            
            if (isCurrent) {
                Text(
                    text = stringResource(R.string.now_indicator),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = getPlanetColor(planet)
                )
            }
        }
    }
}

/**
 * Obține planeta pentru index global (0-23)
 */
private fun getPlanetForGlobalIndex(dayOfWeek: Int, globalIndex: Int): PlanetType {
    val chaldeanOrder = listOf(
        PlanetType.SATURN, PlanetType.JUPITER, PlanetType.MARS,
        PlanetType.SUN, PlanetType.VENUS, PlanetType.MERCURY, PlanetType.MOON
    )
    
    val dayRuler = listOf(
        PlanetType.SUN,     // Sunday (0)
        PlanetType.MOON,    // Monday (1)
        PlanetType.MARS,    // Tuesday (2)
        PlanetType.MERCURY, // Wednesday (3)
        PlanetType.JUPITER, // Thursday (4)
        PlanetType.VENUS,   // Friday (5)
        PlanetType.SATURN   // Saturday (6)
    )
    
    val startPlanet = dayRuler[dayOfWeek]
    val startIndex = chaldeanOrder.indexOf(startPlanet)
    val planetIndex = (startIndex + globalIndex) % 7
    
    return chaldeanOrder[planetIndex]
}

/**
 * Culori Tattva pentru planete:
 * Jupiter → Prithivi (Galben/Gold)
 * Luna, Venus → Apas (Albastru deschis)
 * Soare, Marte → Tejas (Roșu)
 * Mercur → Vayu (Albastru)
 * Saturn → Akasha (Violet)
 */
private fun getPlanetColor(planet: PlanetType): Color {
    return when (planet) {
        PlanetType.JUPITER -> Color(0xFFFFC107)  // Prithivi - Yellow/Amber
        PlanetType.MOON -> Color(0xFF81D4FA)     // Apas - Light Blue
        PlanetType.VENUS -> Color(0xFF4FC3F7)    // Apas - Light Blue
        PlanetType.SUN -> Color(0xFFFF5722)      // Tejas - Deep Orange-Red
        PlanetType.MARS -> Color(0xFFE53935)     // Tejas - Red
        PlanetType.MERCURY -> Color(0xFF42A5F5)  // Vayu - Blue
        PlanetType.SATURN -> Color(0xFF9C27B0)   // Akasha - Purple
    }
}

/**
 * Simbolul Tattva asociat fiecărei planete:
 * Jupiter → Prithivi 🟨
 * Luna, Venus → Apas 🌙
 * Soare, Marte → Tejas 🔺
 * Mercur → Vayu 🔵
 * Saturn → Akasha 🟣
 */
private fun getPlanetTattvaEmoji(planet: PlanetType): String {
    return when (planet) {
        PlanetType.JUPITER -> "🟨"
        PlanetType.MOON, PlanetType.VENUS -> "🌙"
        PlanetType.SUN, PlanetType.MARS -> "🔺"
        PlanetType.MERCURY -> "🔵"
        PlanetType.SATURN -> "🟣"
    }
}