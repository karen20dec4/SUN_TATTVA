package com.android.sun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.domain.calculator.MoonPhaseResult
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
            
            /*
					// ✅ DEBUG: Afișează ora curentă (mic și gri)
					val currentTimeZone = moonPhase.nextFullMoon.timeZone
					val currentTime = Calendar.getInstance(currentTimeZone)
					val debugFormat = SimpleDateFormat("HH:mm:ss z (Z)", Locale.getDefault())
					debugFormat.timeZone = currentTimeZone
					
					Text(
						text = "🕐 ${debugFormat.format(currentTime.time)}",
						style = MaterialTheme.typography.bodySmall,
						fontSize = 12.sp,
						color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
					)
			*/
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            // Tripura Sundari
            MoonEventRow(
                label = "Tripura Sundari:",
                date = moonPhase.nextTripuraSundari
            )
            
            // Full Moon (highlighted when in influence period)
            MoonEventRow(
                label = "Full moon:",
                date = moonPhase.nextFullMoon,
                isHighlighted = moonPhase.isInFullMoonInfluence
            )
            
            // New Moon
            MoonEventRow(
                label = "New moon:",
                date = moonPhase.nextNewMoon
            )
        }
    }
}

/**
 * ✅ Afișează data folosind timezone-ul din Calendar
 * ✅ Format: "3 Jan - 12:02 (GMT+2)" în loc de "GMT+02:00"
 */
@Composable
private fun MoonEventRow(
    label: String,
    date: Calendar,
    isHighlighted: Boolean = false
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
    android.util.Log.d("MoonPhaseCard", "🕐 $label Calendar TZ: ${date.timeZone.id}, millis: ${date.timeInMillis}")
    
    val highlightBg = Color(0xFF423e48)
    val highlightText = Color(0xFFd0ccd1)
    
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
        
        // ✅ Afișează data + timezone custom format
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
        }
    }
}