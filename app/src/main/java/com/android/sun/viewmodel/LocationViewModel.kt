package com.android.sun.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.sun.data.model.LocationData
import com.android.sun.data.repository.LocationPreferences
import com.android.sun.data.repository.LocationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.android.sun.data.model.PredefinedCity

/**
 * ViewModel pentru gestionarea locațiilor
 */
class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application)
    private val locationPreferences = LocationPreferences(application)
    private val context = application.applicationContext

    // State pentru lista de locații
    val locations:  StateFlow<List<LocationData>> = locationRepository
        .getAllLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // State pentru locația curentă GPS
    private val _currentGPSLocation = MutableStateFlow<LocationData?>(null)
    val currentGPSLocation: StateFlow<LocationData?> = _currentGPSLocation.asStateFlow()

    // State pentru loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading:  StateFlow<Boolean> = _isLoading.asStateFlow()

    // State pentru erori
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
	
	// ✅ State pentru rezultatele căutării în lista predefinită
    private val _searchResults = MutableStateFlow<List<PredefinedCity>>(emptyList())
    val searchResults: StateFlow<List<PredefinedCity>> = _searchResults.asStateFlow()
	
	
	
    init {
        // ✅ La inițializare, încarcă ultima locație GPS salvată
        loadSavedGPSLocation()
        loadGPSLocation()
    }

    /**
     * Încarcă locația GPS curentă
     */
    fun loadGPSLocation() {
        com.android.sun.util.AppLog.d("LocationViewModel", "🔵 loadGPSLocation() CALLED")
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            com.android.sun.util.AppLog.d("LocationViewModel", "🔵 Starting GPS location request...")
            
            try {
                val location = locationRepository.getCurrentLocation()
                com.android.sun.util.AppLog.d("LocationViewModel", "🔵 GPS result: $location")
                
                if (location != null) {
                    _currentGPSLocation.value = location
                    // ✅ Salvează locația GPS pentru utilizare ulterioară
                    saveGPSLocationToPrefs(location)
                    com.android.sun.util.AppLog.d("LocationViewModel", "✅ GPS Location set and saved:  ${location.latitude}, ${location.longitude}")
                } else {
                    // ✅ GPS nu e disponibil - încercăm să folosim ultima locație salvată
                    if (_currentGPSLocation.value == null) {
                        loadSavedGPSLocation()
                        if (_currentGPSLocation.value != null) {
                            com.android.sun.util.AppLog.d("LocationViewModel", "✅ Using saved GPS location")
                        } else {
                            _error.value = "Could not get GPS location. Check permissions."
                            com.android.sun.util.AppLog.e("LocationViewModel", "❌ GPS returned null and no saved location")
                        }
                    }
                }
            } catch (e: Exception) {
                // ✅ La eroare, încercăm să folosim ultima locație salvată
                if (_currentGPSLocation.value == null) {
                    loadSavedGPSLocation()
                    if (_currentGPSLocation.value != null) {
                        com.android.sun.util.AppLog.d("LocationViewModel", "✅ GPS failed, using saved location")
                    } else {
                        _error.value = "GPS Error: ${e.message}"
                        com.android.sun.util.AppLog.e("LocationViewModel", "❌ GPS Exception: ${e.message}")
                    }
                }
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                com.android.sun.util.AppLog.d("LocationViewModel", "🔵 GPS request finished")
            }
        }
    }

    /**
     * ✅ Salvează locația GPS în SharedPreferences
     */
    private fun saveGPSLocationToPrefs(location: LocationData) {
        try {
            val prefs = context.getSharedPreferences("sun_gps_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putFloat("gps_latitude", location.latitude.toFloat())
                .putFloat("gps_longitude", location.longitude.toFloat())
                .putFloat("gps_altitude", location.altitude.toFloat())
                .putFloat("gps_timezone", location.timeZone.toFloat())
                .putLong("gps_timestamp", System.currentTimeMillis())
                .apply()
            
            com.android.sun.util.AppLog.d("LocationViewModel", "✅ GPS location saved to prefs:  ${location.latitude}, ${location.longitude}")
        } catch (e: Exception) {
            com.android.sun.util.AppLog.e("LocationViewModel", "❌ Error saving GPS to prefs: ${e.message}")
        }
    }

    /**
     * ✅ Încarcă ultima locație GPS din SharedPreferences
     */
    private fun loadSavedGPSLocation() {
        try {
            val prefs = context.getSharedPreferences("sun_gps_prefs", Context.MODE_PRIVATE)
            val latitude = prefs.getFloat("gps_latitude", 0f)
            val longitude = prefs.getFloat("gps_longitude", 0f)
            
            if (latitude != 0f && longitude != 0f) {
                val savedLocation = LocationData(
                    id = -1,
                    name = "GPS",
                    latitude = latitude.toDouble(),
                    longitude = longitude.toDouble(),
                    altitude = prefs.getFloat("gps_altitude", 0f).toDouble(),
                    timeZone = prefs.getFloat("gps_timezone", 2f).toDouble(),
                    isCurrentLocation = true
                )
                _currentGPSLocation.value = savedLocation
                
                val timestamp = prefs.getLong("gps_timestamp", 0)
                val ageMinutes = (System.currentTimeMillis() - timestamp) / 60000
                com.android.sun.util.AppLog.d("LocationViewModel", "✅ Loaded saved GPS location (${ageMinutes} min old): ${savedLocation.latitude}, ${savedLocation.longitude}")
            } else {
                com.android.sun.util.AppLog.d("LocationViewModel", "ℹ️ No saved GPS location found")
            }
        } catch (e: Exception) {
            com.android.sun.util.AppLog.e("LocationViewModel", "❌ Error loading saved GPS:  ${e.message}")
        }
    }

    /**
     * Salvează o locație nouă
     */
    fun saveLocation(location:  LocationData) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                locationRepository.saveLocation(location)
            } catch (e:  Exception) {
                _error.value = "Error saving location: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ Șterge o locație
     * Dacă locația ștearsă este cea curent selectată, resetează la București
     */
    fun deleteLocation(location: LocationData) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // ✅ Verifică dacă locația ștearsă este cea curent selectată
                val currentSavedName = locationPreferences.getSavedLocationName()
                val isCurrentLocation = currentSavedName == location.name
                
                // Șterge locația din DB
                locationRepository.deleteLocation(location)
                
                // ✅ Dacă am șters locația curentă, resetează la București
                if (isCurrentLocation) {
                    com.android.sun.util.AppLog.d("LocationViewModel", "⚠️ Deleted current location '${location.name}', resetting to București")
                    
                    // Salvează București ca locație curentă
                    locationPreferences.saveSelectedLocation(
                        id = 0,
                        name = com.android.sun.util.AppDefaults.LOCATION_NAME,
                        latitude = com.android.sun.util.AppDefaults.LATITUDE,
                        longitude = com.android.sun.util.AppDefaults.LONGITUDE,
                        altitude = com.android.sun.util.AppDefaults.ALTITUDE,
                        timeZone = com.android.sun.util.AppDefaults.TIME_ZONE,
                        isGPS = false
                    )
                }
                
            } catch (e: Exception) {
                _error.value = "Error deleting location:  ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualizează o locație
     */
    fun updateLocation(location: LocationData) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                locationRepository.updateLocation(location)
            } catch (e: Exception) {
                _error.value = "Error updating location: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ Încarcă locațiile predefinite (toate orașele din România)
     */
    fun loadDefaultLocations() {
        com.android.sun.util.AppLog.d("LocationViewModel", "🔵 loadDefaultLocations() CALLED")
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                locationRepository.loadDefaultLocations()
                com.android.sun.util.AppLog.d("LocationViewModel", "✅ Default locations loaded")
            } catch (e: Exception) {
                _error.value = "Error loading defaults: ${e.message}"
                com.android.sun.util.AppLog.e("LocationViewModel", "❌ Error:  ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    
	
	 /**
     * ✅ Șterge toate locațiile salvate, păstrând doar București
     */
    fun clearSavedLocations() {
        com.android.sun.util.AppLog.d("LocationViewModel", "🗑️ clearSavedLocations() CALLED")
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                locationRepository.clearSavedLocations()
                com.android.sun.util.AppLog.d("LocationViewModel", "✅ Saved locations cleared")
            } catch (e: Exception) {
                _error.value = "Error clearing locations: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
	
	
	
	
	    /**
		 * ✅ Caută în lista de orașe predefinite
		 * Apelat când utilizatorul scrie în câmpul de căutare
		 */
		fun searchPredefinedCities(query: String) {
			val results = locationRepository.searchPredefinedCities(query)
			_searchResults.value = results
			com.android.sun.util.AppLog.d("LocationViewModel", "🔍 Search '$query' → ${results.size} results")
		}
		
		/**
		 * ✅ Golește rezultatele căutării
		 */
		fun clearSearchResults() {
			_searchResults.value = emptyList()
		}
		
		/**
		 * ✅ Adaugă un oraș predefinit în locațiile salvate
		 */
		fun addPredefinedCity(city: PredefinedCity) {
			com.android.sun.util.AppLog.d("LocationViewModel", "➕ addPredefinedCity: ${city.name}, ${city.country}")
			
			viewModelScope.launch {
				_isLoading.value = true
				_error. value = null
				
				try {
					locationRepository.addPredefinedCity(city)
					// Golește rezultatele căutării după adăugare
					_searchResults.value = emptyList()
					com.android.sun.util.AppLog.d("LocationViewModel", "✅ City added successfully")
				} catch (e: Exception) {
					_error.value = "Error adding city: ${e.message}"
					com.android.sun.util.AppLog.e("LocationViewModel", "❌ Error:  ${e.message}")
					e.printStackTrace()
				} finally {
					_isLoading.value = false
				}
			}
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
     * Verifică dacă are permisiuni GPS
     */
    fun hasLocationPermission(): Boolean {
        return try {
            _currentGPSLocation.value != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Curăță eroarea
     */
    fun clearError() {
        _error.value = null
    }
}