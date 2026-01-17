package com.android.sun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.data.model.TattvaInfo
import kotlinx.coroutines.delay
import java.util.*
import androidx.compose.foundation.border
import com.android.sun.R
import androidx.compose.ui.res.painterResource



@Composable
fun getTattvaIconRes(tattvaName: String): Int {
    return when (tattvaName.lowercase()) {
        "akasha" -> R.drawable.ic_tattva_akasha
        "apas" -> R.drawable.ic_tattva_apas
        "prithivi" -> R.drawable.ic_tattva_prithivi
        "tejas" -> R.drawable.ic_tattva_tejas
        "vayu" -> R.drawable.ic_tattva_vayu
        else -> R.drawable.ic_tattva_akasha // Fallback
    }
}




/**
 * Card combinat pentru Tattva si SubTattva (design compact)
 */
@Composable
fun CombinedTattvaCard(
    tattva: TattvaInfo,
    subTattva: TattvaInfo,
    onAllDayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Timp curent pentru countdown
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }
    
    // Calcul timp ramas Tattva
    val tattvaRemainingMillis = (tattva.endTime.timeInMillis - currentTime.timeInMillis).coerceAtLeast(0)
    val tattvaMinutes = (tattvaRemainingMillis / 60000).toInt()
    val tattvaSeconds = ((tattvaRemainingMillis % 60000) / 1000).toInt()
    
    // Calcul timp ramas SubTattva
    val subTattvaRemainingMillis = (subTattva.endTime.timeInMillis - currentTime.timeInMillis).coerceAtLeast(0)
    val subTattvaMinutes = (subTattvaRemainingMillis / 60000).toInt()
    val subTattvaSeconds = ((subTattvaRemainingMillis % 60000) / 1000).toInt()
    

Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(7.dp)),
        shape = RoundedCornerShape(7.dp),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            
            // -----------------------------------
            // TATTVA (MARE) - ACUM COMPACT
            // -----------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(tattva.color, tattva.color.copy(alpha = 0.7f))
                        ),
                        shape = RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp)
                    )
                    // Am redus padding-ul vertical de la 28.dp la 12.dp pentru a face cardul mai mic
                    .padding(vertical = 50.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // Impinge butonul la dreapta
                ) {
                    // Grupul din STÂNGA: Simbol + Timp
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = getTattvaIconRes(tattva.name)),
                            contentDescription = tattva.name,
                            modifier = Modifier.size(45.dp), // Dimensiune optimizată
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = String.format("%02d:%02d", tattvaMinutes, tattvaSeconds),
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 38.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Butonul ALL DAY - Aliniat la DREAPTA
                    Button(
                        onClick = onAllDayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SHOW DAY",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            // -----------------------------------
            // SUBTATTVA (MICA)
            // -----------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
					.shadow(                      // <--- umbra: ajustează aici
						elevation = 0.1.dp,       // încearcă 0.5.dp, 0.8.dp, 1.dp etc.
						shape = RoundedCornerShape(bottomStart = 7.dp, bottomEnd = 7.dp),
						clip = false
					)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(subTattva.color.copy(alpha = 0.25f), subTattva.color.copy(alpha = 0.10f))
                        ),
                        
						shape = RoundedCornerShape(bottomStart = 7.dp, bottomEnd = 7.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SubTattva: Simbol mic + Nume
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = getTattvaIconRes(subTattva.name)),
                            contentDescription = subTattva.name,
                            modifier = Modifier.size(28.dp),
                            tint = subTattva.color
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = subTattva.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 24.sp,
                            color = subTattva.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Timp SubTattva
                    Text(
                        text = String.format("%02d:%02d", subTattvaMinutes, subTattvaSeconds),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 24.sp,
                        color = subTattva.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


/**
 * Formateaza timpul �n HH:mm:ss (cu secunde)
 */
private fun formatTime(calendar: Calendar): String {
    return String.format(
        "%02d:%02d:%02d",
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND)
    )
}