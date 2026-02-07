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






@Composable
fun CombinedTattvaCard(
    tattva: TattvaInfo,
    subTattva: TattvaInfo,
    onAllDayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }
    
    // Calculele pentru timpi (rămân la fel)
    val tattvaRemainingMillis = (tattva.endTime.timeInMillis - currentTime.timeInMillis).coerceAtLeast(0)
    val tattvaMinutes = (tattvaRemainingMillis / 60000).toInt()
    val tattvaSeconds = ((tattvaRemainingMillis % 60000) / 1000).toInt()
    
    val subTattvaRemainingMillis = (subTattva.endTime.timeInMillis - currentTime.timeInMillis).coerceAtLeast(0)
    val subTattvaMinutes = (subTattvaRemainingMillis / 60000).toInt()
    val subTattvaSeconds = ((subTattvaRemainingMillis % 60000) / 1000).toInt()

    
	
	
	
	Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(7.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(7.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            
            // -----------------------------------
            // TATTVA PRINCIPALĂ (ZONA DE SUS)
            // -----------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(tattva.color, tattva.color.copy(alpha = 0.7f))
                        )
                    )
                    .padding(36.dp) // Padding-ul original
            ) {
                // Rândul principal: Icon + Timp + Buton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // REVENIRE LA 45.dp conform fișierului tău original
                        Icon(
                            painter = painterResource(id = getTattvaIconRes(tattva.name)),
                            contentDescription = tattva.name,
                            modifier = Modifier.size(45.dp),
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // REVENIRE LA 38.sp
                        Text(
                            text = String.format("%02d:%02d", tattvaMinutes, tattvaSeconds),
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 38.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Butonul SHOW DAY (rămâne în dreapta sus)
                    Button(
                        onClick = { onAllDayClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("SHOW DAY", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // ✅ SĂGEATA PENTRU DROPDOWN (Poziționată în dreapta-jos)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd) // O forțează în colțul dreapta-jos
                        .offset(x = 20.dp, y = 32.dp) // O ajustăm ușor să nu stea lipită de margini
                )
            }

            // -----------------------------------
            // SUBTATTVA (ZONA EXPANDABILĂ)
            // -----------------------------------
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(subTattva.color.copy(alpha = 0.2f), subTattva.color.copy(alpha = 0.05f))
                                )
                            )
                            .padding(vertical = 12.dp, horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = getTattvaIconRes(subTattva.name)),
                                    contentDescription = subTattva.name,
                                    modifier = Modifier.size(28.dp),
                                    tint = subTattva.color
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = subTattva.name,
                                    fontSize = 24.sp,
                                    color = subTattva.color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = String.format("%02d:%02d", subTattvaMinutes, subTattvaSeconds),
                                fontSize = 24.sp,
                                color = subTattva.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}