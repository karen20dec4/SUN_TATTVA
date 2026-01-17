package com.android.sun.ui.screens

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.data.model.TattvaDayItem
import com.android.sun.ui.components.GradientNavigationBar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Ecran "ALL DAY" - Harta completa a Tattva-urilor pentru ziua curenta sau selectata
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDayScreen(
    tattvaDaySchedule: List<TattvaDayItem>,
    sunriseDate: Calendar,
    sunriseTime: String,
    sunsetTime: String,
    actualSunriseTime: Calendar,
    timeZone: Double,
    isDarkTheme: Boolean = false,
    onBackClick: () -> Unit,
    onCalendarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    val offsetMillis = (timeZone * 3600 * 1000).toInt()
    val locationTimeZone = SimpleTimeZone(offsetMillis, "Location")
    
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US).apply {
        this.timeZone = locationTimeZone
    }
    val timeFormat = SimpleDateFormat("HH:mm: ss", Locale.getDefault()).apply {
        this.timeZone = locationTimeZone
    }
    
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }
    
    var expandedTattvaIndex by remember { mutableStateOf<Int?>(null) }
    var hasScrolled by remember { mutableStateOf(false) }
    
    LaunchedEffect(tattvaDaySchedule) {
        if (! hasScrolled) {
            val currentIndex = tattvaDaySchedule.indexOfFirst { it.isCurrent }
            if (currentIndex != -1) {
                expandedTattvaIndex = currentIndex
                listState.animateScrollToItem(currentIndex.coerceAtLeast(0))
                hasScrolled = true
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormat.format(sunriseDate.time),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = "↑ $sunriseTime  ↓ $sunsetTime",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            
                            // ✅ DEBUG: Afișează timezone și UTC time pentru verificare DST
                            Spacer(modifier = Modifier.height(1.dp))
                            val utcFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
                                this.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }
                            val sunriseUTC = utcFormat.format(actualSunriseTime.time)
                            
                            // Calculează offset-ul real folosind timezone-ul locației
                            // Folosim SimpleTimeZone cu offset-ul furnizat (nu poate determina DST automat fără location)
                            val offsetMillis = (timeZone * 3600.0 * 1000.0).toInt()
                            val locationTz = java.util.SimpleTimeZone(offsetMillis, "Location")
                            val offsetHours = timeZone.toInt()
                            val offsetSign = if (timeZone >= 0) "+" else ""
                            
                            Text(
                                text = "UTC$offsetSign$offsetHours (sunrise UTC: $sunriseUTC)",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = onCalendarClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(end = 8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CALENDAR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    
                    itemsIndexed(tattvaDaySchedule) { index, tattvaItem ->
                        if (index > 0 && index % 5 == 0) {
                            CycleDelimiter(cycleNumber = tattvaItem.cycleNumber)
                        }
                        
                        TattvaDayItemCard(
                            tattvaItem = tattvaItem,
                            isExpanded = expandedTattvaIndex == index,
                            currentTime = currentTime,
                            locationTimeZone = locationTimeZone,
                            onClick = {
                                expandedTattvaIndex = if (expandedTattvaIndex == index) null else index
                            }
                        )
                    }
                    
                    // Spatiu pentru gradient navigation bar
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
        
        // Gradient navigation bar
        GradientNavigationBar(
            isDarkTheme = isDarkTheme,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Delimiter intre cicluri
 */
@Composable
private fun CycleDelimiter(cycleNumber: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        Text(
            text = "Cycle $cycleNumber",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    }
}

/**
 * Card individual pentru o Tattva
 */
@Composable
private fun TattvaDayItemCard(
    tattvaItem: TattvaDayItem,
    isExpanded:  Boolean,
    currentTime: Calendar,
    locationTimeZone: TimeZone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        this.timeZone = locationTimeZone
    }
    
    val isTattvaCurrentNow = currentTime.timeInMillis >= tattvaItem.startTime.timeInMillis &&
                             currentTime.timeInMillis < tattvaItem.endTime.timeInMillis
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isTattvaCurrentNow) {
                tattvaItem.tattvaColor.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isTattvaCurrentNow) {
            androidx.compose.foundation.BorderStroke(3.dp, tattvaItem.tattvaColor)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTattvaCurrentNow) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(tattvaItem.tattvaColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = timeFormat.format(tattvaItem.startTime.time),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = tattvaItem.tattvaColor
                        )
                        Text(
                            text = tattvaItem.tattvaName.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = tattvaItem.tattvaColor
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isTattvaCurrentNow) {
                        Text(
                            text = "◄ NOW",
                            style = MaterialTheme.typography.labelLarge,
                            color = tattvaItem.tattvaColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = tattvaItem.tattvaColor
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                ) {
                    val tattvaDurationMs = tattvaItem.endTime.timeInMillis - tattvaItem.startTime.timeInMillis
                    val subTattvaDurationMs = tattvaDurationMs / 5
                    
                    val timeFormatCompact = SimpleDateFormat("HH:mm: ss", Locale.getDefault()).apply {
                        this.timeZone = locationTimeZone
                    }
                    
                    tattvaItem.subTattvas.forEachIndexed { index, subTattva ->
                        val subEndTimeMs = subTattva.startTime.timeInMillis + subTattvaDurationMs
                        
                        val isSubCurrentNow = currentTime.timeInMillis >= subTattva.startTime.timeInMillis &&
                                              currentTime.timeInMillis < subEndTimeMs
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(subTattva.color, CircleShape)
                                )
                                
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                Text(
                                    text = "${timeFormatCompact.format(subTattva.startTime.time)} - ${subTattva.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSubCurrentNow) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSubCurrentNow) {
                                        subTattva.color
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    }
                                )
                            }
                            
                            if (isSubCurrentNow) {
                                Text(
                                    text = "◄",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = subTattva.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}