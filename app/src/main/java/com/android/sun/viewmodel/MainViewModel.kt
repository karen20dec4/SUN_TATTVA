package com.android.sun.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.sun.data.model.AstroData
import com.android.sun.data.model. LocationData
import com.android.sun.data.repository.AstroRepository
import com.android.sun.data.repository.LocationPreferences
import com.android.sun.data.repository.LocationRepository
import com.android.sun.data.preferences.SettingsPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel pentru ecranul principal
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val astroRepository = AstroRepository(application)
    private val locationRepository = LocationRepository(application)
    private val locationPreferences = LocationPreferences(application)
    private val settingsPrefs = SettingsPreferences(application)

    // State pentru date astrologice
    private val _astroData = MutableStateFlow<AstroData?>(null)
    val astroData: StateFlow<AstroData?> = _astroData.asStateFlow()

    // State pentru loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State pentru erori
    private val _error = MutableStateFlow<String? >(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // State pentru locația curentă
    private val _currentLocation = MutableStateFlow(getDefaultLocation())
    val currentLocation: StateFlow<LocationData> = _currentLocation. asStateFlow()

    // State pentru code mode
    private val _codeMode = MutableStateFlow(false)
    val codeMode: StateFlow<Boolean> = _codeMode.asStateFlow()

    // Job pentru actualizare automată
    private var updateJob: Job?  = null

    init {
        com.android.sun.util.AppLog.d("MainViewModel", "🔵 init called")
        
        // ✅ Încarcă locația salvată SINCRON (fără coroutine)
        loadSavedLocationSync()
        
        // ✅ Calculează datele astro
        calculateAstroData()
        
        // ✅ Pornește actualizările
        startRealtimeUpdates()
    }

    /**
     * ✅ Încarcă locația salvată SINCRON din SharedPreferences
     * Nu verifică DB-ul aici - verificarea se face în reloadSavedLocation()
     */
    private fun loadSavedLocationSync() {
        com.android.sun.util.AppLog.d("MainViewModel", "🔵 loadSavedLocationSync() called")
        
        if (locationPreferences. hasSavedLocation()) {
            val savedLocation = LocationData(
                id = locationPreferences. getSavedLocationId(),
                name = locationPreferences.getSavedLocationName(),
                latitude = locationPreferences.getSavedLatitude(),
                longitude = locationPreferences. getSavedLongitude(),
                altitude = locationPreferences. getSavedAltitude(),
                timeZone = locationPreferences.getSavedTimeZone(),
                isCurrentLocation = locationPreferences.isSavedLocationGPS()
            )
            
            com.android.sun.util.AppLog.d("MainViewModel", "✅ Loaded saved location: ${savedLocation.name} (${savedLocation.latitude}, ${savedLocation.longitude})")
            _currentLocation.value = savedLocation
        } else {
            com.android.sun.util.AppLog.d("MainViewModel", "⚠️ No saved location, using București")
            _currentLocation.value = getDefaultLocation()
        }
    }

    
	
		/**
		 * ✅ Reîncarcă locația salvată (apelat când revii la ecranul principal)
		 */
		fun reloadSavedLocation() {
			com.android.sun.util.AppLog.d("MainViewModel", "🔵 reloadSavedLocation() called")
			
			viewModelScope. launch {
				val oldLocation = _currentLocation. value
				
				if (locationPreferences. hasSavedLocation()) {
					val savedName = locationPreferences.getSavedLocationName()
					val savedIsGPS = locationPreferences. isSavedLocationGPS()
					
					if (savedIsGPS) {
						// GPS location
						_currentLocation.value = LocationData(
							id = locationPreferences.getSavedLocationId(),
							name = savedName,
							latitude = locationPreferences. getSavedLatitude(),
							longitude = locationPreferences. getSavedLongitude(),
							altitude = locationPreferences.getSavedAltitude(),
							timeZone = locationPreferences.getSavedTimeZone(),
							isCurrentLocation = true
						)
					} else {
						// Verifică DB
						val existsInDB = locationRepository.locationExistsByName(savedName)
						
						if (existsInDB) {
							_currentLocation.value = LocationData(
								id = locationPreferences.getSavedLocationId(),
								name = savedName,
								latitude = locationPreferences. getSavedLatitude(),
								longitude = locationPreferences. getSavedLongitude(),
								altitude = locationPreferences.getSavedAltitude(),
								timeZone = locationPreferences.getSavedTimeZone(),
								isCurrentLocation = false
							)
						} else {
							com.android.sun.util.AppLog.w("MainViewModel", "⚠️ '$savedName' not in DB, resetting")
							resetToDefaultLocation()
						}
					}
				} else {
					_currentLocation.value = getDefaultLocation()
				}
				
				// ✅ Recalculează dacă locația s-a schimbat
				val newLocation = _currentLocation.value
				if (oldLocation. name != newLocation. name || 
					oldLocation. latitude != newLocation. latitude ||
					oldLocation. longitude != newLocation. longitude) {
					com.android.sun.util.AppLog.d("MainViewModel", "🔄 Location changed, recalculating...")
					calculateAstroData()
				}
			}
		}


	/**
	 * ✅ Resetează la București și salvează în preferences
	 */
	private fun resetToDefaultLocation() {
		val defaultLocation = getDefaultLocation()
		_currentLocation.value = defaultLocation
		
		locationPreferences.saveSelectedLocation(
			id = 0,
			name = defaultLocation.name,
			latitude = defaultLocation.latitude,
			longitude = defaultLocation. longitude,
			altitude = defaultLocation.altitude,
			timeZone = defaultLocation. timeZone,
			isGPS = false
		)
		
		com.android.sun.util.AppLog.d("MainViewModel", "✅ Reset to București and saved to preferences")
	}




	/**
	 * Calculează datele astrologice pentru locația curentă
	 */
	fun calculateAstroData() {
		viewModelScope.launch {
			_isLoading.value = true
			_error.value = null
			
			try {
				val location = _currentLocation.value
				
				// ✅ DST: Aplică ora de vară (+1h) dacă e activat
				val dstOffset = if (settingsPrefs.getDstEnabled()) 1.0 else 0.0
				val adjustedTimeZone = location.timeZone + dstOffset
				
				com.android.sun.util.AppLog.d("MainViewModel", "───────────────────────────────────────")
				com.android.sun.util.AppLog.d("MainViewModel", "🔵 calculateAstroData() START")
				com.android.sun.util.AppLog.d("MainViewModel", "🔵 Using location: ${location.name}")
				com.android.sun.util.AppLog.d("MainViewModel", "🔵   Lat: ${location.latitude}, Lon: ${location.longitude}")
				com.android.sun.util.AppLog.d("MainViewModel", "🔵   TimeZone: ${location.timeZone} (base) + DST: $dstOffset = $adjustedTimeZone")
				
				val data = astroRepository.calculateAstroData(
					latitude = location.latitude,
					longitude = location.longitude,
					timeZone = adjustedTimeZone,
					locationName = location.name,
					isGPSLocation = location.isCurrentLocation
				)
				_astroData.value = data
				
				com.android.sun.util.AppLog.d("MainViewModel", "✅ Calculation completed!")
				com.android.sun.util.AppLog.d("MainViewModel", "✅ Result: sunrise=${data.sunriseFormatted}")
				com.android.sun.util.AppLog.d("MainViewModel", "✅ Location in result: ${data.locationName}")
				
				// ✅ ADAUGĂ: Trimite broadcast DUPĂ calculul complet
				try {
					val intent = android. content.Intent("com.android. sun.LOCATION_CHANGED")
					getApplication<Application>().sendBroadcast(intent)
					com.android.sun.util.AppLog.d("MainViewModel", "📍 Broadcast sent after calculation complete")
				} catch (e:  Exception) {
					com.android.sun.util.AppLog.e("MainViewModel", "Error sending broadcast", e)
				}
				
				com.android.sun.util.AppLog.d("MainViewModel", "───────────────────────────────────────")
			} catch (e: Exception) {
				_error.value = "Error:  ${e.message}"
				com.android.sun.util.AppLog.e("MainViewModel", "❌ Error:  ${e.message}")
				e.printStackTrace()
			}
			
			_isLoading.value = false
			com.android.sun.util.AppLog.d("MainViewModel", "🔵 isLoading set to false")
		}
	}







    /**
     * Pornește actualizările în timp real.
     * Recalculează DOAR când tattva principală se schimbă (~24 min).
     * SubTattva și Planetary Hour sunt actualizate local în UI (LaunchedEffect).
     */
    fun startRealtimeUpdates() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            var previousTattvaCode: String? = null
            
            com.android.sun.util.AppLog.d("MainViewModel", "▶️ startRealtimeUpdates() started")
            
            while (true) {
                delay(1000)
                
                val currentData = _astroData.value
                
                if (currentData != null) {
                    val currentTime = Calendar.getInstance()
                    
                    // Recalculează doar când tattva principală expiră
                    val tattvaExpired = currentTime.timeInMillis >= currentData.tattva.endTime.timeInMillis
                    
                    if (tattvaExpired) {
                        com.android.sun.util.AppLog.d("MainViewModel", "⏰ Tattva expired, recalculating...")
                        calculateAstroData()
                    }
                    
                    // Detectează schimbarea de tattva DUPĂ ce recalcularea a actualizat _astroData
                    val latestData = _astroData.value
                    if (latestData != null) {
                        val currentTattvaCode = latestData.tattva.tattva.code
                        if (previousTattvaCode == null) {
                            com.android.sun.util.AppLog.d("MainViewModel", "🔵 Initial tattva code: $currentTattvaCode")
                        } else if (currentTattvaCode != previousTattvaCode) {
                            com.android.sun.util.AppLog.d("MainViewModel", "🔄 Tattva code changed: $previousTattvaCode → $currentTattvaCode")
                            onTattvaChanged(currentTattvaCode)
                        }
                        previousTattvaCode = currentTattvaCode
                    }
                }
            }
        }
    }
    
    /**
     * Apelat când tattva principală se schimbă.
     * Redă sunetul corespunzător dacă este activat în setări.
     */
    private fun onTattvaChanged(newTattvaCode: String) {
        com.android.sun.util.AppLog.d("MainViewModel", "🔔 Tattva changed to: $newTattvaCode")
        
        val context = getApplication<Application>()
        val settingsPreferences = com.android.sun.data.preferences.SettingsPreferences(context)
        
        val soundEnabled = settingsPreferences.isTattvaSoundEnabled(newTattvaCode)
        com.android.sun.util.AppLog.d("MainViewModel", "🔔 Sound enabled for $newTattvaCode: $soundEnabled")
        
        if (soundEnabled) {
            playTattvaSound(context, newTattvaCode)
        } else {
            com.android.sun.util.AppLog.d("MainViewModel", "🔇 Sound DISABLED for tattva: $newTattvaCode (check Settings)")
        }
    }
    
    /**
     * Redă sunetul asociat unei tattva specifice
     */
    private fun playTattvaSound(context: android.content.Context, tattvaCode: String) {
        try {
            val settingsPreferences = com.android.sun.data.preferences.SettingsPreferences(context)
            val volume = settingsPreferences.getTattvaSoundVolume()
            val customUri = settingsPreferences.getCustomSoundUri(tattvaCode)

            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val mediaPlayer: android.media.MediaPlayer
            if (customUri != null) {
                com.android.sun.util.AppLog.d("MainViewModel", "🔊 Using custom URI for tattva: $tattvaCode")
                mediaPlayer = android.media.MediaPlayer()
                mediaPlayer.setAudioAttributes(audioAttributes)
                mediaPlayer.setDataSource(context, android.net.Uri.parse(customUri))
                mediaPlayer.prepare()
            } else {
                val resId = when (tattvaCode) {
                    "A"  -> com.android.sun.R.raw.sound_akasha
                    "V"  -> com.android.sun.R.raw.sound_vayu
                    "T"  -> com.android.sun.R.raw.sound_tejas
                    "Ap" -> com.android.sun.R.raw.sound_apas
                    "P"  -> com.android.sun.R.raw.sound_prithivi
                    else -> {
                        com.android.sun.util.AppLog.e("MainViewModel", "❌ Unknown tattva code: $tattvaCode")
                        return
                    }
                }
                com.android.sun.util.AppLog.d("MainViewModel", "🔊 Creating MediaPlayer for tattva: $tattvaCode (resId=$resId)")
                mediaPlayer = android.media.MediaPlayer.create(
                    context, resId, audioAttributes,
                    android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
                ) ?: run {
                    com.android.sun.util.AppLog.e("MainViewModel", "❌ Failed to create MediaPlayer for tattva: $tattvaCode")
                    return
                }
            }

            mediaPlayer.setOnCompletionListener { mp ->
                com.android.sun.util.AppLog.d("MainViewModel", "🔊 Sound playback completed for tattva: $tattvaCode")
                mp.release()
            }
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                com.android.sun.util.AppLog.e("MainViewModel", "❌ MediaPlayer error for tattva $tattvaCode: what=$what extra=$extra")
                mp.release()
                true
            }
            mediaPlayer.setVolume(volume, volume)
            mediaPlayer.start()
            val durationMs = try { mediaPlayer.duration } catch (_: Exception) { -1 }
            com.android.sun.util.AppLog.d("MainViewModel", "🔊 ▶️ Sound STARTED for tattva: $tattvaCode (volume=$volume, duration=${durationMs}ms)")
        } catch (e: Exception) {
            com.android.sun.util.AppLog.e("MainViewModel", "❌ Exception playing tattva sound: ${e.message}", e)
        }
    }

    /**
     * Oprește actualizările în timp real
     */
    fun stopRealtimeUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    /**
     * Schimbă modul de afișare (cod/nume)
     */
    fun toggleCodeMode() {
        _codeMode.value = !_codeMode.value
    }

    
	
	
	
	/**
     * Setează o nouă locație și o salvează în SharedPreferences
     */
	fun setLocation(location: LocationData) {
		com.android.sun.util.AppLog.d("MainViewModel", "═══════════════════════════════════════")
		com.android.sun.util.AppLog.d("MainViewModel", "🟢 setLocation() called")
		com.android.sun.util.AppLog.d("MainViewModel", "🟢 NEW Location: ${location.name}")
		com.android.sun.util.AppLog.d("MainViewModel", "🟢   Lat: ${location.latitude}, Lon: ${location.longitude}")
		com.android.sun.util.AppLog.d("MainViewModel", "🟢   TimeZone: ${location.timeZone}")
		com.android.sun.util.AppLog.d("MainViewModel", "🟢   IsGPS: ${location.isCurrentLocation}")
		
		val oldLocation = _currentLocation.value
		com.android.sun.util.AppLog.d("MainViewModel", "🟡 OLD Location: ${oldLocation. name}")
		
		_currentLocation.value = location
		
		com.android.sun.util.AppLog.d("MainViewModel", "✅ _currentLocation updated to: ${_currentLocation.value.name}")
		
		// Salvează în SharedPreferences
		locationPreferences.saveSelectedLocation(
			id = location.id,
			name = location.name,
			latitude = location.latitude,
			longitude = location.longitude,
			altitude = location.altitude,
			timeZone = location.timeZone,
			isGPS = location.isCurrentLocation
		)
		
		com.android.sun.util.AppLog.d("MainViewModel", "✅ Location saved to SharedPreferences")
		com.android.sun.util.AppLog.d("MainViewModel", "🟢 Calling calculateAstroData()...")
		calculateAstroData()
		com.android.sun.util.AppLog.d("MainViewModel", "═══════════════════════════════════════")
	}
	
	
	
	
	
	
	
    /**
	 * ✅ Actualizează manual datele
	 * ✅ FIX: Verifică dacă locația curentă încă există în DB
	 */
	fun refresh() {
		com.android.sun.util.AppLog.d("MainViewModel", "═══════════════════════════════════════")
		com.android.sun.util.AppLog.d("MainViewModel", "🔵 refresh() called")
		
		val currentLoc = _currentLocation.value
		com.android.sun.util.AppLog.d("MainViewModel", "🔵 Current location in memory: ${currentLoc.name}")
		com.android.sun.util.AppLog.d("MainViewModel", "🔵   Lat: ${currentLoc. latitude}, Lon: ${currentLoc.longitude}")
		com.android.sun.util.AppLog.d("MainViewModel", "🔵   TimeZone: ${currentLoc.timeZone}")
		com.android.sun.util.AppLog.d("MainViewModel", "🔵   IsGPS: ${currentLoc.isCurrentLocation}")
		
		viewModelScope.launch {
			// ✅ Dacă e locație GPS, folosește-o direct
			if (currentLoc. isCurrentLocation) {
				com.android.sun.util.AppLog.d("MainViewModel", "✅ GPS location, using directly")
				calculateAstroData()
				com.android.sun.util.AppLog.d("MainViewModel", "═══════════════════════════════════════")
				return@launch
			}
			
			// ✅ Verifică dacă locația încă există în DB
			com.android.sun.util.AppLog.d("MainViewModel", "🔍 Checking if location exists in DB...")
			val existsInDB = locationRepository.locationExistsByName(currentLoc.name)
			com.android.sun.util.AppLog.d("MainViewModel", "🔍 Location '${currentLoc.name}' exists in DB: $existsInDB")
			
			if (existsInDB) {
				com.android.sun.util.AppLog.d("MainViewModel", "✅ Location exists, calculating astro data...")
				calculateAstroData()
			} else {
				com.android.sun.util.AppLog.w("MainViewModel", "⚠️ Location '${currentLoc.name}' not found in DB!")
				com.android.sun.util.AppLog.w("MainViewModel", "⚠️ Resetting to București...")
				resetToDefaultLocation()
				calculateAstroData()
			}
			
			com.android.sun.util.AppLog.d("MainViewModel", "═══════════════════════════════════════")
		}
	}






    /**
     * Obține locația default (București)
     */
    private fun getDefaultLocation(): LocationData {
        return com.android.sun.util.AppDefaults.getDefaultLocationData()
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeUpdates()
    }
}