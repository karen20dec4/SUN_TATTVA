package com.android.sun.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferințe pentru Settings
 * - Dark Theme
 * - Notificări Luna Plină
 * - Notificări Tripura Sundari
 * - Notificări Luna Nouă
 * - Notificări Tattva Persistentă
 */
class SettingsPreferences(context: Context) {
    
    private val prefs:  SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    // ═══════════════════════════════════════════════════════════════════
    // DARK THEME
    // ═══════════════════════════════════════════════════════════════════
    
    private val _isDarkTheme = MutableStateFlow(getDarkTheme())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    fun getDarkTheme(): Boolean {
        return prefs.getBoolean(KEY_DARK_THEME, false)
    }
    
    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _isDarkTheme.value = enabled
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // NOTIFICĂRI LUNA PLINĂ (18h înainte și după)
    // ═══════════════════════════════════════════════════════════════════
    
    private val _fullMoonNotification = MutableStateFlow(getFullMoonNotification())
    val fullMoonNotification:  StateFlow<Boolean> = _fullMoonNotification.asStateFlow()
    
    fun getFullMoonNotification(): Boolean {
        return prefs.getBoolean(KEY_FULL_MOON_NOTIFICATION, false)
    }
    
    fun setFullMoonNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FULL_MOON_NOTIFICATION, enabled).apply()
        _fullMoonNotification.value = enabled
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // NOTIFICĂRI TRIPURA SUNDARI (24h înainte)
    // ═══════════════════════════════════════════════════════════════════
    
    private val _tripuraSundariNotification = MutableStateFlow(getTripuraSundariNotification())
    val tripuraSundariNotification:  StateFlow<Boolean> = _tripuraSundariNotification.asStateFlow()
    
    fun getTripuraSundariNotification(): Boolean {
        return prefs.getBoolean(KEY_TRIPURA_SUNDARI_NOTIFICATION, false)
    }
    
    fun setTripuraSundariNotification(enabled:  Boolean) {
        prefs.edit().putBoolean(KEY_TRIPURA_SUNDARI_NOTIFICATION, enabled).apply()
        _tripuraSundariNotification.value = enabled
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // NOTIFICĂRI LUNA NOUĂ (24h înainte)
    // ═══════════════════════════════════════════════════════════════════
    
    private val _newMoonNotification = MutableStateFlow(getNewMoonNotification())
    val newMoonNotification: StateFlow<Boolean> = _newMoonNotification.asStateFlow()
    
    fun getNewMoonNotification(): Boolean {
        return prefs.getBoolean(KEY_NEW_MOON_NOTIFICATION, false)
    }
    
    fun setNewMoonNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NEW_MOON_NOTIFICATION, enabled).apply()
        _newMoonNotification.value = enabled
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // NOTIFICĂRI TATTVA PERSISTENTĂ (în status bar)
    // ═══════════════════════════════════════════════════════════════════
    
    private val _tattvaNotification = MutableStateFlow(getTattvaNotification())
    val tattvaNotification: StateFlow<Boolean> = _tattvaNotification.asStateFlow()
    
    fun getTattvaNotification(): Boolean {
        return prefs.getBoolean(KEY_TATTVA_NOTIFICATION, false)
    }
    
    fun setTattvaNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TATTVA_NOTIFICATION, enabled).apply()
        _tattvaNotification.value = enabled
    }
    
	// ═══════════════════════════════════════════════════════════════════
    // NOTIFICĂRI ORĂ PLANETARĂ (în status bar)
    // ═══════════════════════════════════════════════════════════════════
    
    private val _planetaryHourNotification = MutableStateFlow(getPlanetaryHourNotification())
    val planetaryHourNotification: StateFlow<Boolean> = _planetaryHourNotification.asStateFlow()
    
    fun getPlanetaryHourNotification(): Boolean {
        return prefs.getBoolean(KEY_PLANETARY_HOUR_NOTIFICATION, false)
    }
    
    fun setPlanetaryHourNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PLANETARY_HOUR_NOTIFICATION, enabled).apply()
        _planetaryHourNotification.value = enabled
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // SUNETE TATTVA (per tattva individuale)
    // ═══════════════════════════════════════════════════════════════════
    
    private val _tattvaSoundAkasha = MutableStateFlow(getTattvaSoundAkasha())
    val tattvaSoundAkasha: StateFlow<Boolean> = _tattvaSoundAkasha.asStateFlow()
    
    private val _tattvaSoundVayu = MutableStateFlow(getTattvaSoundVayu())
    val tattvaSoundVayu: StateFlow<Boolean> = _tattvaSoundVayu.asStateFlow()
    
    private val _tattvaSoundTejas = MutableStateFlow(getTattvaSoundTejas())
    val tattvaSoundTejas: StateFlow<Boolean> = _tattvaSoundTejas.asStateFlow()
    
    private val _tattvaSoundApas = MutableStateFlow(getTattvaSoundApas())
    val tattvaSoundApas: StateFlow<Boolean> = _tattvaSoundApas.asStateFlow()
    
    private val _tattvaSoundPrithivi = MutableStateFlow(getTattvaSoundPrithivi())
    val tattvaSoundPrithivi: StateFlow<Boolean> = _tattvaSoundPrithivi.asStateFlow()
    
    fun getTattvaSoundAkasha(): Boolean = prefs.getBoolean(KEY_TATTVA_SOUND_AKASHA, false)
    fun getTattvaSoundVayu(): Boolean = prefs.getBoolean(KEY_TATTVA_SOUND_VAYU, false)
    fun getTattvaSoundTejas(): Boolean = prefs.getBoolean(KEY_TATTVA_SOUND_TEJAS, false)
    fun getTattvaSoundApas(): Boolean = prefs.getBoolean(KEY_TATTVA_SOUND_APAS, false)
    fun getTattvaSoundPrithivi(): Boolean = prefs.getBoolean(KEY_TATTVA_SOUND_PRITHIVI, false)
    
    fun setTattvaSoundAkasha(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TATTVA_SOUND_AKASHA, enabled).apply()
        _tattvaSoundAkasha.value = enabled
    }
    fun setTattvaSoundVayu(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TATTVA_SOUND_VAYU, enabled).apply()
        _tattvaSoundVayu.value = enabled
    }
    fun setTattvaSoundTejas(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TATTVA_SOUND_TEJAS, enabled).apply()
        _tattvaSoundTejas.value = enabled
    }
    fun setTattvaSoundApas(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TATTVA_SOUND_APAS, enabled).apply()
        _tattvaSoundApas.value = enabled
    }
    fun setTattvaSoundPrithivi(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TATTVA_SOUND_PRITHIVI, enabled).apply()
        _tattvaSoundPrithivi.value = enabled
    }
    
    /**
     * Verifică dacă sunetul este activat pentru o tattva specifică (după cod)
     */
    fun isTattvaSoundEnabled(tattvaCode: String): Boolean {
        return when (tattvaCode) {
            "A"  -> getTattvaSoundAkasha()
            "V"  -> getTattvaSoundVayu()
            "T"  -> getTattvaSoundTejas()
            "Ap" -> getTattvaSoundApas()
            "P"  -> getTattvaSoundPrithivi()
            else -> false
        }
    }
    	
    companion object {
        private const val PREFS_NAME = "sun_settings_prefs"
        
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_FULL_MOON_NOTIFICATION = "full_moon_notification"
        private const val KEY_TRIPURA_SUNDARI_NOTIFICATION = "tripura_sundari_notification"
        private const val KEY_NEW_MOON_NOTIFICATION = "new_moon_notification"
        private const val KEY_TATTVA_NOTIFICATION = "tattva_notification"
		private const val KEY_PLANETARY_HOUR_NOTIFICATION = "planetary_hour_notification"
        
        // Tattva sound keys
        private const val KEY_TATTVA_SOUND_AKASHA = "tattva_sound_akasha"
        private const val KEY_TATTVA_SOUND_VAYU = "tattva_sound_vayu"
        private const val KEY_TATTVA_SOUND_TEJAS = "tattva_sound_tejas"
        private const val KEY_TATTVA_SOUND_APAS = "tattva_sound_apas"
        private const val KEY_TATTVA_SOUND_PRITHIVI = "tattva_sound_prithivi"
    }
}