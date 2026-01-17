package com.android.sun.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.domain.calculator.NityaResult

/**
 * Card SIMPLU pentru Nitya - o singură linie
 * Afișează: Nitya | 14/15 - Jwalamalini 92%
 */
@Composable
fun NityaExpandableCard(
    currentNitya: NityaResult,
    modifier: Modifier = Modifier
) {
    // Calcul procent RĂMAS
    val tithiStartDegree = (currentNitya.difference / 12.0).toInt() * 12.0
    val progressInTithi = currentNitya.difference - tithiStartDegree
    val percentRemaining = ((12.0 - progressInTithi) / 12.0 * 100).toInt()

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
                text = "Nitya",
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

            // Dreapta: Procent - fără wrap, cu lățime minimă (încapă și 100%)
            Text(
                text = "$percentRemaining%",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(min = 44.dp)
            )
        }
    }
}