package com.android.sun.ui.screens

import java.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.R
import com.android.sun.domain.calculator.NakshatraType
import com.android.sun.ui.components.getLocalizedNakshatraSymbol
import com.android.sun.ui.components.getLocalizedNakshatraAnimal
import com.android.sun.ui.components.getLocalizedNakshatraPlanet
import com.android.sun.ui.components.getLocalizedNakshatraNature
import com.android.sun.ui.components.getLocalizedNakshatraDegreeRange

/**
 * Detailed screen for a specific Nakshatra
 * Ecran detaliat pentru o Nakshatra specifică
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NakshatraDetailScreen(
    nakshatra: NakshatraType,
    startTime: Calendar? = null,
    endTime: Calendar? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = nakshatra.displayName,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = getNakshatraColor(nakshatra).copy(alpha = 0.3f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card cu gradient
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    getNakshatraColor(nakshatra).copy(alpha = 0.7f),
                                    getNakshatraColor(nakshatra).copy(alpha = 0.5f)
                                )
                            )
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${nakshatra.number}. ${nakshatra.displayName}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show time interval if available
                    if (startTime != null && endTime != null) {
                        val dateTimeFormat = java.text.SimpleDateFormat("d-MMM HH:mm", java.util.Locale.ENGLISH)
                        val timeInterval = "${dateTimeFormat.format(startTime.time)}   -  ${dateTimeFormat.format(endTime.time)}"
                        Text(
                            text = timeInterval,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Text(
                        text = getLocalizedNakshatraDegreeRange(nakshatra),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            // Informații principale
            InfoCard(
                title = stringResource(R.string.deity_title),
                content = nakshatra.deity
            )
            
            InfoCard(
                title = stringResource(R.string.symbol_title),
                content = getLocalizedNakshatraSymbol(nakshatra)
            )
            
            InfoCard(
                title = stringResource(R.string.animal_title),
                content = getLocalizedNakshatraAnimal(nakshatra)
            )
            
            InfoCard(
                title = stringResource(R.string.planet_title),
                content = getLocalizedNakshatraPlanet(nakshatra)
            )
            
            InfoCard(
                title = stringResource(R.string.nature_title),
                content = getLocalizedNakshatraNature(nakshatra)
            )
            
            // Card cu descriere detaliată
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.description_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Render description with bold "Ce se face:" / "Ce nu se face:" markers
                    val descriptionText = getLocalizedDescription(nakshatra)
                    val formattedDescription = formatNakshatraDescription(descriptionText)
                    
                    Text(
                        text = formattedDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Card pentru o singură informație
 */
@Composable
private fun InfoCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Localized description for each Nakshatra using string resources
 * Descriere localizată pentru fiecare Nakshatra folosind resurse de string
 */
@Composable
private fun getLocalizedDescription(nakshatra: NakshatraType): String {
    return when (nakshatra) {
        NakshatraType.ASHWINI -> stringResource(R.string.nakshatra_desc_ashwini)
        NakshatraType.BHARANI -> stringResource(R.string.nakshatra_desc_bharani)
        NakshatraType.KRITTIKA -> stringResource(R.string.nakshatra_desc_krittika)
        NakshatraType.ROHINI -> stringResource(R.string.nakshatra_desc_rohini)
        NakshatraType.MRIGASHIRA -> stringResource(R.string.nakshatra_desc_mrigashira)
        NakshatraType.ARDRA -> stringResource(R.string.nakshatra_desc_ardra)
        NakshatraType.PUNARVASU -> stringResource(R.string.nakshatra_desc_punarvasu)
        NakshatraType.PUSHYA -> stringResource(R.string.nakshatra_desc_pushya)
        NakshatraType.ASHLESHA -> stringResource(R.string.nakshatra_desc_ashlesha)
        NakshatraType.MAGHA -> stringResource(R.string.nakshatra_desc_magha)
        NakshatraType.PURVA_PHALGUNI -> stringResource(R.string.nakshatra_desc_purva_phalguni)
        NakshatraType.UTTARA_PHALGUNI -> stringResource(R.string.nakshatra_desc_uttara_phalguni)
        NakshatraType.HASTA -> stringResource(R.string.nakshatra_desc_hasta)
        NakshatraType.CHITRA -> stringResource(R.string.nakshatra_desc_chitra)
        NakshatraType.SWATI -> stringResource(R.string.nakshatra_desc_swati)
        NakshatraType.VISHAKHA -> stringResource(R.string.nakshatra_desc_vishakha)
        NakshatraType.ANURADHA -> stringResource(R.string.nakshatra_desc_anuradha)
        NakshatraType.JYESHTHA -> stringResource(R.string.nakshatra_desc_jyeshtha)
        NakshatraType.MULA -> stringResource(R.string.nakshatra_desc_mula)
        NakshatraType.PURVA_ASHADHA -> stringResource(R.string.nakshatra_desc_purva_ashadha)
        NakshatraType.UTTARA_ASHADHA -> stringResource(R.string.nakshatra_desc_uttara_ashadha)
        NakshatraType.SHRAVANA -> stringResource(R.string.nakshatra_desc_shravana)
        NakshatraType.DHANISHTA -> stringResource(R.string.nakshatra_desc_dhanishta)
        NakshatraType.SHATABHISHA -> stringResource(R.string.nakshatra_desc_shatabhisha)
        NakshatraType.PURVA_BHADRAPADA -> stringResource(R.string.nakshatra_desc_purva_bhadrapada)
        NakshatraType.UTTARA_BHADRAPADA -> stringResource(R.string.nakshatra_desc_uttara_bhadrapada)
        NakshatraType.REVATI -> stringResource(R.string.nakshatra_desc_revati)
    }
}

/**
 * Formats a Nakshatra description text with bold markers for
 * "Ce se face:" / "Ce nu se face:" (RO) and "What to do:" / "What NOT to do:" (EN).
 * Adds paragraph spacing between sections for readability.
 */
@Composable
private fun formatNakshatraDescription(text: String): androidx.compose.ui.text.AnnotatedString {
    // Bold markers for Romanian and English
    val boldMarkers = listOf(
        "Ce se face:",
        "Ce nu se face:",
        "What to do:",
        "What NOT to do:"
    )
    
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            // Find the next bold marker
            var nearestIdx = Int.MAX_VALUE
            var nearestMarker = ""
            for (marker in boldMarkers) {
                val idx = remaining.indexOf(marker)
                if (idx in 0 until nearestIdx) {
                    nearestIdx = idx
                    nearestMarker = marker
                }
            }
            
            if (nearestMarker.isNotEmpty() && nearestIdx != Int.MAX_VALUE) {
                // Append text before the marker
                val before = remaining.substring(0, nearestIdx)
                append(before)
                
                // Append the bold marker
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(nearestMarker)
                }
                
                remaining = remaining.substring(nearestIdx + nearestMarker.length)
            } else {
                // No more markers, append the rest
                append(remaining)
                remaining = ""
            }
        }
    }
}

/**
 * Returns Tattva type for each Nakshatra based on Vedic correspondence
 */
private fun getNakshatraTattva(nakshatra: NakshatraType): String {
    return when (nakshatra) {
        // PRITHIVI (Earth)
        NakshatraType.DHANISHTA -> "P"
        NakshatraType.ROHINI -> "P"
        NakshatraType.JYESHTHA -> "P"
        NakshatraType.ANURADHA -> "P"
        NakshatraType.SHRAVANA -> "P"
        NakshatraType.UTTARA_ASHADHA -> "P"
        
        // APAS (Water)
        NakshatraType.PURVA_ASHADHA -> "Ap"
        NakshatraType.ASHLESHA -> "Ap"
        NakshatraType.MULA -> "Ap"
        NakshatraType.ARDRA -> "Ap"
        NakshatraType.REVATI -> "Ap"
        NakshatraType.UTTARA_BHADRAPADA -> "Ap"
        NakshatraType.SHATABHISHA -> "Ap"
        
        // TEJAS (Fire)
        NakshatraType.BHARANI -> "T"
        NakshatraType.KRITTIKA -> "T"
        NakshatraType.PUSHYA -> "T"
        NakshatraType.MAGHA -> "T"
        NakshatraType.PURVA_PHALGUNI -> "T"
        NakshatraType.PURVA_BHADRAPADA -> "T"
        NakshatraType.SWATI -> "T"
        
        // VAYU (Air)
        NakshatraType.VISHAKHA -> "V"
        NakshatraType.UTTARA_PHALGUNI -> "V"
        NakshatraType.HASTA -> "V"
        NakshatraType.CHITRA -> "V"
        NakshatraType.PUNARVASU -> "V"
        NakshatraType.ASHWINI -> "V"
        NakshatraType.MRIGASHIRA -> "V"
    }
}

/**
 * Culori pentru Nakshatra (bazate pe Tattva)
 */
private fun getNakshatraColor(nakshatra: NakshatraType): Color {
    val tattva = getNakshatraTattva(nakshatra)
    return com.android.sun.util.TattvaColors.getByCode(tattva)
}
