package com.android.sun.domain.calculator

import android.content.Context
import swisseph.SweDate
import swisseph.SwissEph
import swisseph.DblObj
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent. withLock

/**
 * Wrapper pentru Swiss Ephemeris - THREAD-SAFE
 * ✅ Folosește ReentrantLock pentru a preveni accesul concurent
 * ✅ AUTO-RECOVERY:  Reinițializează automat fișierele corupte
 */
class SwissEphWrapper(private val context: Context) {

    private var swissEph:  SwissEph?  = null
    private var ephePath: String = ""
    
    @Volatile
    private var isInitialized = false
    
    @Volatile
    private var isInitializing = false
    
    private var retryCount = 0
    private val maxRetries = 2
    
    @Volatile
    private var filesNeedCopy = true

    // ✅ Lock pentru thread-safety
    private val lock = ReentrantLock()
    private val initLock = ReentrantLock()
    
    // ✅ Lock global pentru file operations (static)
    companion object {
        private val fileLock = ReentrantLock()
        
        const val SEFLG_SWIEPH = 2
        
        const val SE_SUN = 0
        const val SE_MOON = 1
        const val SE_MERCURY = 2
        const val SE_VENUS = 3
        const val SE_MARS = 4
        const val SE_JUPITER = 5
        const val SE_SATURN = 6
    }

    init {
        initializeSwissEph()
    }

    /**
     * ✅ Inițializează Swiss Ephemeris - THREAD-SAFE
     */
    private fun initializeSwissEph() {
        initLock.withLock {
            if (isInitialized) {
                android.util.Log.d("SwissEphWrapper", "✅ Already initialized, skipping")
                return
            }
            
            if (isInitializing) {
                android.util.Log.d("SwissEphWrapper", "⏳ Already initializing, waiting...")
                return
            }
            
            isInitializing = true
            
            try {
                ephePath = copyEphemerisFiles()
                swissEph = SwissEph()
                swissEph?. swe_set_ephe_path(ephePath)
                isInitialized = true
                retryCount = 0
                android.util.Log.d("SwissEphWrapper", "✅ Swiss Ephemeris initialized at:  $ephePath")
            } catch (e: Exception) {
                android.util.Log.e("SwissEphWrapper", "❌ Failed to initialize Swiss Ephemeris", e)
                isInitialized = false
                throw e
            } finally {
                isInitializing = false
            }
        }
    }

    /**
     * ✅ Reinițializează Swiss Ephemeris - THREAD-SAFE
     */
    private fun reinitializeSwissEph() {
        initLock.withLock {
            // Verifică din nou după ce am obținut lock-ul
            if (isInitializing) {
                android.util.Log.d("SwissEphWrapper", "⏳ Another thread is reinitializing, waiting...")
                // Așteaptă puțin și returnează
                Thread.sleep(100)
                return
            }
            
            android.util.Log.w("SwissEphWrapper", "⚠️ Reinitializing Swiss Ephemeris (attempt ${retryCount}/$maxRetries)...")
            
            isInitializing = true
            isInitialized = false
            filesNeedCopy = true // ✅ Force recopy of files
            
            try {
                // Închide instanța curentă
                try {
                    swissEph?.swe_close()
                } catch (e: Exception) {
                    android.util.Log.w("SwissEphWrapper", "Warning closing SwissEph: ${e.message}")
                }
                swissEph = null
                
                // ✅ Use global lock for file deletion
                fileLock.withLock {
                    // Șterge fișierele corupte
                    val epheDir = File(context.filesDir, "sweph/data")
                    if (epheDir.exists()) {
                        epheDir.listFiles()?.forEach { file ->
                            if (file.name.endsWith(".se1")) {
                                try {
                                    val deleted = file.delete()
                                    android.util.Log.d("SwissEphWrapper", "Deleted corrupted file: ${file.name} -> $deleted")
                                } catch (e: Exception) {
                                    android.util.Log.w("SwissEphWrapper", "Failed to delete ${file.name}: ${e.message}")
                                }
                            }
                        }
                    }
                }
                
                // Așteaptă puțin pentru a permite sistemului să elibereze resursele
                Thread.sleep(100)
                
                // Reinițializează
                ephePath = copyEphemerisFiles()
                swissEph = SwissEph()
                swissEph?.swe_set_ephe_path(ephePath)
                isInitialized = true
                filesNeedCopy = false
                
                android.util.Log.d("SwissEphWrapper", "✅ Swiss Ephemeris successfully reinitialized!")
                
            } catch (e: Exception) {
                android.util.Log.e("SwissEphWrapper", "❌ Failed to reinitialize Swiss Ephemeris", e)
                isInitialized = false
                throw RuntimeException("Failed to reinitialize Swiss Ephemeris after corruption", e)
            } finally {
                isInitializing = false
            }
        }
    }

/**
 * ✅ Copiază fișierele ephemeris din assets - THREAD-SAFE cu lock global
 * Șterge întotdeauna fișierele existente pentru a preveni corupția
 */
private fun copyEphemerisFiles(): String {
// ✅ Use global file lock to prevent concurrent file operations across all instances
fileLock.withLock {
val epheDir = File(context.filesDir, "sweph/data")
if (!epheDir.exists()) {
epheDir.mkdirs()
}

val assetManager = context.assets
val files = arrayOf("seas_18.se1", "semo_18.se1", "sepl_18.se1")

// ✅ Check if files already exist and are valid
val allFilesValid = files.all { fileName ->
val file = File(epheDir, fileName)
file.exists() && file.length() > 0
}

if (allFilesValid && !filesNeedCopy) {
android.util.Log.d("SwissEphWrapper", "✅ All ephemeris files already exist and are valid, skipping copy")
return epheDir.absolutePath
}

files.forEach { fileName ->
val outFile = File(epheDir, fileName)

// ✅ ȘTERGE doar dacă fișierul există și trebuie recopiat
if (outFile.exists() && filesNeedCopy) {
android.util.Log.d("SwissEphWrapper", "Deleting existing $fileName to prevent corruption")
outFile.delete()
// Wait a bit to ensure filesystem sync
Thread.sleep(10)
}

// ✅ Skip copy if file exists and is valid
if (outFile.exists() && outFile.length() > 0 && !filesNeedCopy) {
android.util.Log.d("SwissEphWrapper", "✓ $fileName already exists (${outFile.length()} bytes), skipping")
return@forEach
}

try {
// ✅ Copiază fișierul cu buffer mare pentru performanță
assetManager.open("sweph/data/$fileName").use { input ->
FileOutputStream(outFile).use { output ->
val buffer = ByteArray(16384) // Increased buffer size
var bytesRead: Int
while (input.read(buffer).also { bytesRead = it } != -1) {
output.write(buffer, 0, bytesRead)
}
output.flush()
output.fd.sync() // ✅ Force sync to disk
}
}

val size = outFile.length()
android.util.Log.d("SwissEphWrapper", "✓ Copied $fileName ($size bytes)")

if (size == 0L) {
throw RuntimeException("File $fileName is EMPTY after copy!")
}

} catch (e: Exception) {
android.util.Log.e("SwissEphWrapper", "✗ Failed to copy $fileName", e)
throw RuntimeException("Failed to copy ephemeris file: $fileName", e)
}
}

filesNeedCopy = false
return epheDir.absolutePath
}
}

    
	
	
	/**
     * ✅ Verifică dacă eroarea indică fișiere corupte
     */
    private fun isCorruptionError(error: String? ): Boolean {
        if (error == null) return false
        return error.contains("damaged", ignoreCase = true) ||
               error.contains("error in ephemeris", ignoreCase = true) ||
               error.contains("file error", ignoreCase = true) ||
               error.contains("coefficients instead", ignoreCase = true) ||
               error.contains("wrong", ignoreCase = true) ||
               error.contains("rename to", ignoreCase = true) ||
               error.contains("ArrayIndexOutOfBounds", ignoreCase = true) ||
               error.contains("exceeds file length", ignoreCase = true)
    }

    fun getJulianDay(year: Int, month:  Int, day: Int, hour: Double): Double {
        val sweDate = SweDate(year, month, day, hour)
        return sweDate.julDay
    }

    /**
     * ✅ Calculează rise/transit/set cu AUTO-RECOVERY - THREAD-SAFE
     */
    fun calculateRiseTransSet(
        julianDay:  Double,
        body: Int,
        longitude: Double,
        latitude: Double,
        riseSetFlag: Int
    ): Double {
        lock.withLock {
            if (! isInitialized || swissEph == null) {
                if (! isInitializing) {
                    android.util.Log.w("SwissEphWrapper", "⚠️ Not initialized, attempting to initialize...")
                    try {
                        initializeSwissEph()
                    } catch (e: Exception) {
                        android.util.Log.e("SwissEphWrapper", "❌ Failed to initialize on demand", e)
                        throw RuntimeException("Swiss Ephemeris not initialized", e)
                    }
                } else {
                    // Așteaptă să se termine inițializarea
                    Thread.sleep(100)
                    if (!isInitialized) {
                        throw RuntimeException("Swiss Ephemeris not initialized")
                    }
                }
            }

            try {
                val geopos = DoubleArray(3)
                geopos[0] = longitude
                geopos[1] = latitude
                geopos[2] = 0.0

                val tret = DblObj()
                val serr = StringBuffer()

                val result = swissEph!! .swe_rise_trans(
                    julianDay,
                    body,
                    null,
                    SEFLG_SWIEPH,
                    riseSetFlag,
                    geopos,
                    1013.25,
                    10.0,
                    tret,
                    serr
                )

                if (result < 0) {
                    val errorMsg = serr.toString()
                    android.util.Log.e("SwissEphWrapper", "Rise/Set error: $errorMsg")
                    
                    // ✅ Detectează corupție și reinițializează
                    if (isCorruptionError(errorMsg) && retryCount < maxRetries) {
                        retryCount++
                        reinitializeSwissEph()
                        
                        // Reîncearcă calculul (recursiv dar cu lock-ul ținut)
                        return calculateRiseTransSetInternal(julianDay, body, longitude, latitude, riseSetFlag)
                    }
                    
                    throw RuntimeException("Error calculating rise/set:  $errorMsg")
                }

                // Reset retry counter pe succes
                retryCount = 0
                return tret. `val`
                
            } catch (e: NullPointerException) {
                android. util.Log.e("SwissEphWrapper", "❌ NullPointerException - corrupted ephemeris!", e)
                
                if (retryCount < maxRetries) {
                    retryCount++
                    reinitializeSwissEph()
                    return calculateRiseTransSetInternal(julianDay, body, longitude, latitude, riseSetFlag)
                }
                
                throw RuntimeException("NullPointerException after $maxRetries retries", e)
                
            } catch (e: ArrayIndexOutOfBoundsException) {
                android.util.Log.e("SwissEphWrapper", "❌ ArrayIndexOutOfBoundsException - corrupted ephemeris!", e)
                
                if (retryCount < maxRetries) {
                    retryCount++
                    reinitializeSwissEph()
                    return calculateRiseTransSetInternal(julianDay, body, longitude, latitude, riseSetFlag)
                }
                
                throw RuntimeException("ArrayIndexOutOfBoundsException after $maxRetries retries", e)
                
            } catch (e: Exception) {
                android.util.Log.e("SwissEphWrapper", "❌ Unexpected error in calculateRiseTransSet", e)
                
                if (isCorruptionError(e.message) && retryCount < maxRetries) {
                    retryCount++
                    reinitializeSwissEph()
                    return calculateRiseTransSetInternal(julianDay, body, longitude, latitude, riseSetFlag)
                }
                
                throw RuntimeException("Error calculating rise/set:  ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Versiune internă pentru retry (fără lock - deja avem lock-ul)
     */
    private fun calculateRiseTransSetInternal(
        julianDay:  Double,
        body: Int,
        longitude: Double,
        latitude: Double,
        riseSetFlag: Int
    ): Double {
        val geopos = DoubleArray(3)
        geopos[0] = longitude
        geopos[1] = latitude
        geopos[2] = 0.0

        val tret = DblObj()
        val serr = StringBuffer()

        val result = swissEph!!.swe_rise_trans(
            julianDay,
            body,
            null,
            SEFLG_SWIEPH,
            riseSetFlag,
            geopos,
            1013.25,
            10.0,
            tret,
            serr
        )

        if (result < 0) {
            throw RuntimeException("Error calculating rise/set: ${serr}")
        }

        retryCount = 0
        return tret.`val`
    }

    /**
     * ✅ Calculează poziția corpului ceresc cu AUTO-RECOVERY - THREAD-SAFE
     */
    fun calculateBodyPosition(julianDay: Double, body: Int): Double {
        lock.withLock {
            if (!isInitialized || swissEph == null) {
                if (! isInitializing) {
                    android.util.Log.w("SwissEphWrapper", "⚠️ Not initialized, attempting to initialize...")
                    try {
                        initializeSwissEph()
                    } catch (e: Exception) {
                        android.util.Log.e("SwissEphWrapper", "❌ Failed to initialize on demand", e)
                        throw RuntimeException("Swiss Ephemeris not initialized", e)
                    }
                } else {
                    Thread.sleep(100)
                    if (! isInitialized) {
                        throw RuntimeException("Swiss Ephemeris not initialized")
                    }
                }
            }

            try {
                val xx = DoubleArray(6)
                val serr = StringBuffer()

                val result = swissEph!! .swe_calc_ut(
                    julianDay,
                    body,
                    SEFLG_SWIEPH,
                    xx,
                    serr
                )

                if (result < 0) {
                    val errorMsg = serr.toString()
                    android.util.Log.e("SwissEphWrapper", "Body position error: $errorMsg")
                    
                    if (isCorruptionError(errorMsg) && retryCount < maxRetries) {
                        retryCount++
                        reinitializeSwissEph()
                        return calculateBodyPositionInternal(julianDay, body)
                    }
                    
                    throw RuntimeException("Error calculating body position: $errorMsg")
                }

                retryCount = 0
                return xx[0]
                
            } catch (e: NullPointerException) {
                android.util.Log.e("SwissEphWrapper", "❌ NullPointerException - corrupted ephemeris!", e)
                
                if (retryCount < maxRetries) {
                    retryCount++
                    reinitializeSwissEph()
                    return calculateBodyPositionInternal(julianDay, body)
                }
                
                throw RuntimeException("NullPointerException after $maxRetries retries", e)
                
            } catch (e: ArrayIndexOutOfBoundsException) {
                android.util.Log.e("SwissEphWrapper", "❌ ArrayIndexOutOfBoundsException - corrupted ephemeris!", e)
                
                if (retryCount < maxRetries) {
                    retryCount++
                    reinitializeSwissEph()
                    return calculateBodyPositionInternal(julianDay, body)
                }
                
                throw RuntimeException("ArrayIndexOutOfBoundsException after $maxRetries retries", e)
                
            } catch (e: Exception) {
                android.util.Log.e("SwissEphWrapper", "❌ Unexpected error in calculateBodyPosition", e)
                
                if (isCorruptionError(e.message) && retryCount < maxRetries) {
                    retryCount++
                    reinitializeSwissEph()
                    return calculateBodyPositionInternal(julianDay, body)
                }
                
                throw RuntimeException("Error calculating body position: ${e. message}", e)
            }
        }
    }

    /**
     * ✅ Versiune internă pentru retry (fără lock - deja avem lock-ul)
     */
    private fun calculateBodyPositionInternal(julianDay: Double, body: Int): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        val result = swissEph!!.swe_calc_ut(
            julianDay,
            body,
            SEFLG_SWIEPH,
            xx,
            serr
        )

        if (result < 0) {
            throw RuntimeException("Error calculating body position: ${serr}")
        }

        retryCount = 0
        return xx[0]
    }

    /**
     * ✅ Închide Swiss Ephemeris
     */
    fun close() {
        lock.withLock {
            try {
                swissEph?.swe_close()
            } catch (e:  Exception) {
                android.util.Log.w("SwissEphWrapper", "Warning closing SwissEph:  ${e.message}")
            }
            swissEph = null
            isInitialized = false
            android.util.Log.d("SwissEphWrapper", "Swiss Ephemeris closed")
        }
    }
}