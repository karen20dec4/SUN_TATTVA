package com.android.sun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.data.model.PlanetInfo
import com.android.sun.data.model.stopTime
import com.android.sun.data.model.remaining
import com.android.sun.data.model.getFormattedRemainingTime

/**
 * Culori Tattva pentru planete (bazate pe codul planetei):
 * ♃ Jupiter → Prithivi (Galben/Gold)
 * ☽ Luna, ♀ Venus → Apas (Albastru deschis)
 * ☉ Soare, ♂ Marte → Tejas (Roșu)
 * ☿ Mercur → Vayu (Albastru)
 * ♄ Saturn → Akasha (Violet)
 */
private fun getPlanetTattvaColor(code: String): Color {
    return when (code) {
        "♃" -> Color(0xFFFFC107)  // Jupiter - Prithivi - Yellow/Amber
        "☽" -> Color(0xFF81D4FA)  // Moon - Apas - Light Blue
        "♀" -> Color(0xFF4FC3F7)  // Venus - Apas - Light Blue
        "☉" -> Color(0xFFFF5722)  // Sun - Tejas - Deep Orange-Red
        "♂" -> Color(0xFFE53935)  // Mars - Tejas - Red
        "☿" -> Color(0xFF42A5F5)  // Mercury - Vayu - Blue
        "♄" -> Color(0xFF9C27B0)  // Saturn - Akasha - Purple
        else -> Color(0xFF808080) // fallback gray
    }
}

/**
 * Simbolul Tattva asociat planetei (bazat pe codul planetei)
 */
private fun getPlanetTattvaEmoji(code: String): String {
    return when (code) {
        "♃" -> "🟨"  // Jupiter - Prithivi
        "☽", "♀" -> "🌙"  // Moon, Venus - Apas
        "☉", "♂" -> "🔺"  // Sun, Mars - Tejas
        "☿" -> "🔵"  // Mercury - Vayu
        "♄" -> "🟣"  // Saturn - Akasha
        else -> "⬜"
    }
}

/**
 * Card pentru afișarea planetei curente (ora planetară)
 */
@Composable
fun PlanetCard(
    planet: PlanetInfo,
    showCode: Boolean,
    modifier: Modifier = Modifier
) {
    val tattvaColor = getPlanetTattvaColor(planet.code)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(7.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PLANETĂ (Ora Planetară)",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Numele sau codul planetei - colorat cu culoarea Tattva
            Box(
                modifier = Modifier
                    .background(
                        color = tattvaColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(7.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (showCode) planet.code else planet.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = tattvaColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getPlanetTattvaEmoji(planet.code),
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ora planetară
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ora Planetară:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${planet.hour}/12",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = tattvaColor
                )
            }

            // Timing (start/stop) — afișat mereu (câmpurile sunt non-null în model)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Ora de început
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Start:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCalendar(planet.startTime),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Ora de sfârșit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Stop:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCalendar(planet.stopTime),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Timpul rămas dacă există
                if (planet.remaining != "00:00:00") {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = tattvaColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Remaining: ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = planet.getFormattedRemainingTime(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = tattvaColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper pentru formatarea Calendar-ului
 */
private fun formatCalendar(calendar: java.util.Calendar): String {
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val minute = calendar.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')
    val second = calendar.get(java.util.Calendar.SECOND).toString().padStart(2, '0')
    return "$hour:$minute:$second"
}