package com.android.sun.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.R
import com.android.sun.domain.calculator.NityaResult
import java.util.Calendar
import kotlinx.coroutines.delay

/**
 * Card SIMPLU pentru Nitya - o singură linie
 * Afișează: Nitya | 14/15 - Jwalamalini 18h 3m
 */
@Composable
fun NityaExpandableCard(
    currentNitya: NityaResult,
    modifier: Modifier = Modifier
) {
    // Live countdown - update every minute
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000) // Update every minute
            currentTime = Calendar.getInstance()
        }
    }

    // Calculate time remaining until end of current Nitya
    val timeRemaining = currentNitya.endTime.timeInMillis - currentTime.timeInMillis
    val countdownText = if (timeRemaining <= 0) {
        "0m"
    } else {
        val totalSeconds = timeRemaining / 1000L
        val hoursRemaining = totalSeconds / 3600L
        val minutesRemaining = (totalSeconds % 3600L) / 60L
        when {
            hoursRemaining > 0 -> String.format("%dh %dm", hoursRemaining, minutesRemaining)
            minutesRemaining > 0 -> String.format("%dm", minutesRemaining)
            else -> String.format("%ds", totalSeconds % 60L)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stânga: Titlu
            Text(
                text = stringResource(R.string.nitya_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Mijloc: "11/15 - Nume" - ia tot spațiul disponibil, o singură linie
            Text(
                text = "${currentNitya.number}/15 - ${currentNitya.name}",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            // Dreapta: Timp rămas - format "18h 3m" ca la Nakshatra
            Text(
                text = countdownText,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(min = 56.dp)
            )
        }
    }
}