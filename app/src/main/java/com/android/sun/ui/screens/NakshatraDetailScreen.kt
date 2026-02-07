package com.android.sun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.domain.calculator.NakshatraType

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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                        text = nakshatra.degreeRange,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            // Informații principale
            InfoCard(
                title = "🧝 Zeitate",
                content = nakshatra.deity
            )
            
            InfoCard(
                title = "🔱 Simbol",
                content = nakshatra.symbol
            )
            
            InfoCard(
                title = "🐾 Animal",
                content = nakshatra.animal
            )
            
            InfoCard(
                title = "🪐 Planetă",
                content = nakshatra.planet
            )
            
            InfoCard(
                title = "🌿 Natură",
                content = nakshatra.nature
            )
            
            // Card cu descriere generică
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
                        text = "📖 Descriere",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = getGenericDescription(nakshatra),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
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
 * Generic description for each Nakshatra
 * Can be modified later by the user
 * Descriere generică pentru fiecare Nakshatra - poate fi modificată mai târziu de utilizator
 */
private fun getGenericDescription(nakshatra: NakshatraType): String {
    return when (nakshatra) {
        NakshatraType.ASHWINI -> "Prima Nakshatra din zodiac, Ashwini reprezintă începutul, vindecarea și energia vitală. Sub protecția gemenilor divini Ashwini Kumara, vindecătorii cerești, această Nakshatra aduce rapiditate, spontaneitate și putere de vindecare."
        
        NakshatraType.BHARANI -> "Bharani este Nakshatra a nașterii și transformării, guvernată de Yama, zeul morții. Reprezintă ciclul vieții, fertilitatea și responsabilitatea. Energia sa este profundă și transformatoare."
        
        NakshatraType.KRITTIKA -> "Krittika, flacăra purificatoare, este guvernată de Agni, zeul focului. Simbolizează arderea impurităților și claritatea mentală. Aduce energie puternică de transformare și iluminare."
        
        NakshatraType.ROHINI -> "Rohini este considerată cea mai favorabilă Nakshatra pentru creștere și abundență. Sub guvernarea Lunii, ea reprezintă fertilitatea, creativitatea și frumusețea materială."
        
        NakshatraType.MRIGASHIRA -> "Mrigashira, capul de cerb, reprezintă căutarea și explorarea. Guvernată de Marte și asociată cu Soma, aduce curiozitate, sensibilitate și dorința de a descoperi adevărul."
        
        NakshatraType.ARDRA -> "Ardra este Nakshatra furtunii și transformării radicale. Sub protecția lui Rudra (Shiva), ea aduce schimbări intense, purificare prin suferință și renaștere spirituală."
        
        NakshatraType.PUNARVASU -> "Punarvasu înseamnă 'întoarcerea luminii'. Guvernată de Jupiter și protejată de Aditi, mama zeilor, ea aduce optimism, reînnoire și posibilitatea unui nou început."
        
        NakshatraType.PUSHYA -> "Pushya este considerată cea mai norocoasă și mai nutritivă Nakshatra. Guvernată de Saturn și protejată de Brihaspati, ea aduce creștere spirituală, hrană și protecție."
        
        NakshatraType.ASHLESHA -> "Ashlesha, șarpele cosmic, este guvernată de Mercur și asociată cu Naga. Reprezintă puterea kundalini, intuiția profundă și înțelepciunea ascunsă, dar și posibilitatea de manipulare."
        
        NakshatraType.MAGHA -> "Magha reprezintă tronul ancestral și puterea regală. Guvernată de Ketu și protejată de Pitri (strămoși), ea aduce onoare, respect pentru tradiție și conexiune cu rădăcinile spirituale."
        
        NakshatraType.PURVA_PHALGUNI -> "Purva Phalguni este Nakshatra plăcerii, relaxării și creativității artistice. Guvernată de Venus și protejată de Bhaga, zeul bunăstării, ea aduce bucurie, romantism și apreciere pentru frumusețe."
        
        NakshatraType.UTTARA_PHALGUNI -> "Uttara Phalguni reprezintă parteneriatele benefice și sprijinul mutual. Guvernată de Soare și protejată de Aryaman, zeul prieteniei, ea aduce loialitate, generozitate și stabilitate în relații."
        
        NakshatraType.HASTA -> "Hasta, mâna, simbolizează îndemânarea, precizia și capacitatea de a manifesta. Guvernată de Lună și protejată de Savitar, zeul soarelui, ea aduce talent practic și abilitatea de a crea."
        
        NakshatraType.CHITRA -> "Chitra este Nakshatra bijuteriei divine și a frumuseții perfecte. Guvernată de Marte și protejată de Tvashtar, arhitectul cosmic, ea aduce creativitate, estetică și dorința de perfecțiune."
        
        NakshatraType.SWATI -> "Swati, frunza purtată de vânt, reprezintă independența și adaptabilitatea. Guvernată de Rahu și protejată de Vayu, zeul vântului, ea aduce libertate, flexibilitate și dorința de schimbare."
        
        NakshatraType.VISHAKHA -> "Vishakha este Nakshatra determinării și ambiției. Guvernată de Jupiter și protejată de Indra și Agni, ea aduce putere de concentrare, perseverență și capacitatea de a atinge obiective mari."
        
        NakshatraType.ANURADHA -> "Anuradha reprezintă prietenia devotată și colaborarea spirituală. Guvernată de Saturn și protejată de Mitra, zeul prieteniei, ea aduce loialitate, devotament și capacitatea de a lucra în armonie."
        
        NakshatraType.JYESHTHA -> "Jyeshtha este Nakshatra seniorității și puterii. Guvernată de Mercur și protejată de Indra, regele zeilor, ea aduce autoritate, protecție și responsabilitatea conducerii."
        
        NakshatraType.MULA -> "Mula reprezintă rădăcina și investigarea profundă. Guvernată de Ketu și protejată de Nirriti, zeița distrugerii, ea aduce transformare radicală, căutarea adevărului și eliminarea a ceea ce nu mai servește."
        
        NakshatraType.PURVA_ASHADHA -> "Purva Ashadha este Nakshatra invincibilității și optimismului. Guvernată de Venus și protejată de Apah, zeița apelor, ea aduce încredere, energie de învingere și capacitatea de purificare."
        
        NakshatraType.UTTARA_ASHADHA -> "Uttara Ashadha reprezintă victoria finală și realizarea permanentă. Guvernată de Soare și protejată de Vishvadeva, ea aduce succes durabil, recunoaștere și autoritate universală."
        
        NakshatraType.SHRAVANA -> "Shravana este Nakshatra ascultării și învățării. Guvernată de Lună și protejată de Vishnu, păstrătorul, ea aduce înțelepciune prin ascultare, conexiune spirituală și capacitatea de a primi cunoaștere."
        
        NakshatraType.DHANISHTA -> "Dhanishta reprezintă prosperitatea și ritmul cosmic. Guvernată de Marte și protejată de Vasus, zeii abundenței, ea aduce succes material, muzicalitate și capacitatea de a lucra în ritm cu universul."
        
        NakshatraType.SHATABHISHA -> "Shatabhisha, cele o sută de vindecători, este guvernată de Rahu și protejată de Varuna, zeul apelor cosmice. Ea aduce vindecarea misterioasă, intuiția profundă și capacitatea de a descoperi secrete ascunse."
        
        NakshatraType.PURVA_BHADRAPADA -> "Purva Bhadrapada reprezintă arderea karmei și transformarea spirituală intensă. Guvernată de Jupiter și protejată de Aja Ekapada, ea aduce intensitate, pasiune spirituală și dorința de transcendență."
        
        NakshatraType.UTTARA_BHADRAPADA -> "Uttara Bhadrapada este Nakshatra profunzimii și înțelepciunii cosmice. Guvernată de Saturn și protejată de Ahirbudhnya, șarpele adâncurilor, ea aduce stabilitate spirituală, înțelepciune profundă și capacitatea de a susține lumea."
        
        NakshatraType.REVATI -> "Revati, ultima Nakshatra, reprezintă călătoria finală și protecția divină. Guvernată de Mercur și protejată de Pushan, zeul călătoriilor, ea aduce hrănire spirituală, completare și ghidare către destinație."
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
    return when (tattva) {
        "A" -> Color(0xFF4A00D3)   // Akasha - Purple
        "V" -> Color(0xFF009AD3)   // Vayu - Blue
        "T" -> Color(0xFFFF0000)   // Tejas - Red
        "Ap" -> Color(0xFF8A8A8A)  // Apas - Gray
        "P" -> Color(0xFFDFCD00)   // Prithivi - Yellow
        else -> Color.Gray
    }
}
