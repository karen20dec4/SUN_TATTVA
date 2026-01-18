package com.android.sun.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import android.util.Log
import com.android.sun.data.model.LocationData
import com.android.sun.viewmodel.LocationViewModel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import com.android.sun.data.model.PredefinedCity
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.TopAppBarDefaults
import com.android.sun.ui.components.GradientNavigationBar


private const val TAG = "LocationScreen"

/**
 * Ecran pentru selectarea si gestionarea locatiilor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    mainViewModel: com.android.sun.viewmodel.MainViewModel,
    isDarkTheme: Boolean = false,
    onLocationSelected: (LocationData) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val locations by viewModel.locations.collectAsState()
    val currentGPSLocation by viewModel.currentGPSLocation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showClearAllDialog by rememberSaveable { mutableStateOf(false) }
    val currentSelectedLocation by mainViewModel.currentLocation.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            viewModel.searchPredefinedCities(searchQuery)
        } else {
            viewModel.clearSearchResults()
        }
    }
    
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showSearchDialog by rememberSaveable { mutableStateOf(false) }
    var locationToDelete by remember { mutableStateOf<LocationData?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?:  false
        
        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.loadGPSLocation()
        }
    }
    
    fun requestGPSLocation() {
		Log.d(TAG, "🔵 requestGPSLocation() called")
        val hasFinePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        Log.d(TAG, "Current permissions: fine=$hasFinePermission, coarse=$hasCoarsePermission")
		if (hasFinePermission || hasCoarsePermission) {
            Log.d(TAG, "✅ Already has permission, loading GPS...")
			viewModel.loadGPSLocation()
        } else {
            Log.d(TAG, "🔵 Requesting permissions from user...")
			permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Location") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                error?.let { errorMessage ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
						shape = RoundedCornerShape(7.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("OK")
                            }
                        }
                    }
                }

                GPSLocationSection(
                    currentGPSLocation = currentGPSLocation,
                    isLoading = isLoading,
                    onUseGPS = { onLocationSelected(it) },
                    onLoadGPS = { requestGPSLocation() },
                    onRefresh = { requestGPSLocation() }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showSearchDialog = true },
						shape = RoundedCornerShape(7.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Search location")
                    }
                    
                    FilledTonalIconButton(
                        onClick = { showAddDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Location Manually"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Saved locations (${locations.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                when {
                    isLoading && locations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                    locations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No saved locations", style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Use GPS or search to add locations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = locations,
                                key = { it.id }
                            ) { location ->
                                LocationItemCompact(
                                    location = location,
                                    isSelected = currentSelectedLocation?.name == location.name,
                                    onSelect = { 
                                        Log.d(TAG, "═══════════════════════════════════════")
										Log.d(TAG, "🟢 SELECT BUTTON CLICKED")
										Log.d(TAG, "🟢 Selected location: ${location.name}")
										Log.d(TAG, "🟢   Lat: ${location.latitude}, Lon: ${location.longitude}")
										onLocationSelected(location)
										// mainViewModel.refresh()
										Log.d(TAG, "═══════════════════════════════════════")
                                    },
                                    onDelete = { 
										Log.d(TAG, "❌ DELETE clicked for:  ${location.name}")
                                        locationToDelete = location
                                    }
                                )
                            }
                            
                            item(key = "clear_locations") {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        Log.d(TAG, "🗑️ Clear saved locations clicked")
										showClearAllDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
									shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("Clear saved locations")
                                }
                                
                                // Spatiu pentru gradient
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // Gradient navigation bar
        GradientNavigationBar(
            isDarkTheme = isDarkTheme,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    if (showAddDialog) {
        AddLocationDialog(
            onDismiss = { showAddDialog = false },
            onSave = { locationData ->
                viewModel.saveLocation(locationData)
                showAddDialog = false
            }
        )
    }
    
    if (showSearchDialog) {
        SearchLocationDialog(
            onDismiss = { showSearchDialog = false },
            onCitySelected = { city ->
                viewModel.addPredefinedCity(city)
            },
            viewModel = viewModel,
            isDarkTheme = isDarkTheme
        )
    }
    
    locationToDelete?.let { location ->
        AlertDialog(
            onDismissRequest = { locationToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { 
                Text("Delete location? ") 
            },
            text = { 
                Text("Are you sure you want to delete \"${location.name}\"?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLocation(location)
                        locationToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { locationToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { 
                Text("Clear all locations?") 
            },
            text = { 
                Text("This will delete all saved locations except București.This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSavedLocations()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


/**
 * Dialog fullscreen pentru cautarea si adaugarea locatiilor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchLocationDialog(
    onDismiss:  () -> Unit,
    onCitySelected: (PredefinedCity) -> Unit,
    viewModel: LocationViewModel,
    isDarkTheme: Boolean = false
) {
    val searchResults by viewModel.searchResults.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            viewModel.searchPredefinedCities(searchQuery)
        } else {
            viewModel.clearSearchResults()
        }
    }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Dialog(
        onDismissRequest = {
            viewModel.clearSearchResults()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Search Location") },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.clearSearchResults()
                                onDismiss()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Close"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("City name") },
                        placeholder = { Text("Type to search...(e.g.London)") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    viewModel.clearSearchResults()
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    when {
                        searchQuery.length < 2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "Type at least 2 characters to search",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Search works without diacritics\n(e.g.\"Malmo\" finds \"Malmö\")",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        searchResults.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "No cities found for \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Try a different search term\nor use (+) button to add manually",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "${searchResults.size} cities found",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = searchResults,
                                    key = { "${it.name}_${it.country}" }
                                ) { city ->
                                    SearchResultItemLarge(
                                        city = city,
                                        onAdd = {
                                            onCitySelected(city)
                                            searchQuery = ""
                                            viewModel.clearSearchResults()
                                            onDismiss()
                                        }
                                    )
                                }
                                
                                // Spatiu pentru gradient
                                item {
                                    Spacer(modifier = Modifier.height(100.dp))
                                }
                            }
                        }
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
}

/**
 * Item pentru rezultatele cautarii
 */
@Composable
private fun SearchResultItemLarge(
    city: PredefinedCity,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(7.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = city.country,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Lat: ${String.format("%.2f", city.latitude)}° Lon: ${String.format("%.2f", city.longitude)}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            }
            
            FilledTonalButton(
                onClick = { 
					Log. d(TAG, "➕ ADD clicked for: ${city.name}, ${city.country}")
					onAdd() 
				}
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
}


/**
 * Dialog pentru adaugarea unei locatii noi
 */
@Composable
private fun AddLocationDialog(
    onDismiss: () -> Unit,
    onSave: (LocationData) -> Unit
) {
    var cityName by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    var altitude by rememberSaveable { mutableStateOf("0") }
    
    val defaultTimeZone = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (1000.0 * 60.0 * 60.0)
    var timeZone by rememberSaveable { mutableStateOf(String.format("%.1f", defaultTimeZone)) }
    
    val isValid = cityName.isNotBlank() && 
                  latitude.toDoubleOrNull() != null && 
                  longitude.toDoubleOrNull() != null &&
                  altitude.toDoubleOrNull() != null &&
                  timeZone.toDoubleOrNull() != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
				shape = RoundedCornerShape(7.dp)
            //shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add New Location",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text("City Name") },
                    placeholder = { Text("e.g.Bucharest") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    placeholder = { Text("e.g.44.4268") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = latitude.isNotEmpty() && latitude.toDoubleOrNull() == null,
                    supportingText = {
                        if (latitude.isNotEmpty() && latitude.toDoubleOrNull() == null) {
                            Text("Enter a valid number")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    placeholder = { Text("e.g.26.1025") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = longitude.isNotEmpty() && longitude.toDoubleOrNull() == null,
                    supportingText = {
                        if (longitude.isNotEmpty() && longitude.toDoubleOrNull() == null) {
                            Text("Enter a valid number")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = altitude,
                        onValueChange = { altitude = it },
                        label = { Text("Alt (m)") },
                        placeholder = { Text("85") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = altitude.isNotEmpty() && altitude.toDoubleOrNull() == null,
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = timeZone,
                        onValueChange = { timeZone = it },
                        label = { Text("UTC±") },
                        placeholder = { Text("2.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = timeZone.isNotEmpty() && timeZone.toDoubleOrNull() == null,
                        supportingText = {
                            if (timeZone.isNotEmpty() && timeZone.toDoubleOrNull() == null) {
                                Text("Invalid")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newLocation = LocationData(
                                id = 0,
                                name = cityName.trim(),
                                latitude = latitude.toDouble(),
                                longitude = longitude.toDouble(),
                                altitude = altitude.toDoubleOrNull() ?: 0.0,
                                timeZone = timeZone.toDoubleOrNull() ?: 2.0,
                                isCurrentLocation = false
                            )
                            onSave(newLocation)
                        },
                        enabled = isValid
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}


/**
 * Sectiunea pentru locatia GPS
 */
@Composable
private fun GPSLocationSection(
    currentGPSLocation: LocationData?,
    isLoading: Boolean,
    onUseGPS: (LocationData) -> Unit,
    onLoadGPS: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(7.dp),
		colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
		elevation = CardDefaults.cardElevation(
        defaultElevation = 2.dp // Modifică valoarea după preferință (ex: 2.dp, 6.dp)
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
                Text(
                    text = "GPS Location",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    
                    if (currentGPSLocation != null && ! isLoading) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh GPS location",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentGPSLocation != null) {
                Column(Modifier.fillMaxWidth()) {
                    InfoRow(label = "Lat:", value = String.format("%.4f°", currentGPSLocation.latitude))
                    Spacer(Modifier.height(4.dp))
                    InfoRow(label = "Lon:", value = String.format("%.4f°", currentGPSLocation.longitude))
                    Spacer(Modifier.height(4.dp))
                    InfoRow(label = "Alt:", value = "${currentGPSLocation.altitude.toInt()}m")

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { onUseGPS(currentGPSLocation) },
                        modifier = Modifier.fillMaxWidth(),
						shape = RoundedCornerShape(7.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Use this location")
                    }
                }
            } else {
                Button(
                    onClick = { onLoadGPS() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Get GPS Location")
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Tap to request your current location",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value:  String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}



			/**
			 * Item compact cu expand/collapse intern
			 * București nu poate fi șters (nu afișăm butonul Delete)
			 * Marchează vizual locația selectată curent
			 */
			@Composable
			private fun LocationItemCompact(
				location: LocationData,
				isSelected: Boolean = false,
				onSelect: () -> Unit,
				onDelete: () -> Unit
			) {
				var expanded by rememberSaveable(location. id) { mutableStateOf(false) }
				
				// București nu poate fi șters
				val canDelete = location. name != "București"

				Log.d(TAG, "LocationItemCompact rendering:  ${location.name}, canDelete=$canDelete, isSelected=$isSelected")

				Card(
					modifier = Modifier
						.fillMaxWidth()
						.animateContentSize(),
					shape = RoundedCornerShape(7.dp),
					colors = CardDefaults. cardColors(
						containerColor = if (isSelected) 
							MaterialTheme.colorScheme. primaryContainer 
						else 
							MaterialTheme.colorScheme.surfaceVariant
					),
					border = if (isSelected) 
						BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
					else 
						null
				) {
					Column(Modifier.fillMaxWidth()) {
						// Bara compactă
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 16.dp, vertical = 12.dp),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically
						) {
							// Nume oraș (click → expand/collapse)
							Text(
								text = location.name,
								style = MaterialTheme.typography.titleMedium,
								fontWeight = if (isSelected) FontWeight.Bold else FontWeight. Normal,
								color = if (isSelected) 
									MaterialTheme.colorScheme.onPrimaryContainer 
								else 
									MaterialTheme.colorScheme. onSurfaceVariant,
								modifier = Modifier
									.weight(1f)
									.padding(end = 8.dp)
									.clickable { 
										Log.d(TAG, "City name clicked: ${location. name}")
										expanded = !expanded 
									}
							)

							// Expand / Collapse
							IconButton(onClick = { 
								Log.d(TAG, "Expand/Collapse clicked:  ${location.name}")
								expanded = !expanded 
							}) {
								Icon(
									imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
									contentDescription = if (expanded) "Collapse" else "Expand",
									tint = if (isSelected) 
										MaterialTheme. colorScheme.onPrimaryContainer 
									else 
										MaterialTheme.colorScheme. onSurfaceVariant
								)
							}

							// Delete Button - doar dacă NU este București
							if (canDelete) {
								IconButton(
									onClick = {
										Log. d(TAG, "DELETE BUTTON CLICKED for: ${location.name}")
										onDelete()
									},
									colors = IconButtonDefaults.iconButtonColors(
										contentColor = MaterialTheme.colorScheme. error
									)
								) {
									Icon(imageVector = Icons. Filled.Delete, contentDescription = "Delete location")
								}
							} else {
								// Placeholder pentru a păstra alinierea
								Spacer(modifier = Modifier.size(48.dp))
							}

							// Select Button - arată diferit dacă e deja selectat
							if (isSelected) {
								TextButton(
									onClick = { },
									enabled = false
								) {
									Text("Active", color = MaterialTheme. colorScheme.primary)
								}
							} else {
								TextButton(
									onClick = {
										Log. d(TAG, "SELECT BUTTON CLICKED for: ${location. name}")
										onSelect()
									},
									colors = ButtonDefaults.textButtonColors(
										contentColor = MaterialTheme.colorScheme.primary
									)
								) {
									Text("Select")
								}
							}
						}

						// Detalii expandate
						if (expanded) {
							Column(
								modifier = Modifier
									.fillMaxWidth()
									. padding(horizontal = 16.dp, vertical = 8.dp)
							) {
								InfoRow(label = "Lat:", value = String.format("%.4f°", location.latitude))
								Spacer(Modifier.height(4.dp))
								InfoRow(label = "Lon:", value = String.format("%.4f°", location.longitude))
								Spacer(Modifier. height(4.dp))
								InfoRow(label = "Alt:", value = location.getFormattedAltitude())
								Spacer(Modifier.height(8.dp))
							}
						}
					}
				}
			}