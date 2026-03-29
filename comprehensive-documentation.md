# SUN TATTVA - Comprehensive Documentation

## Overview

**SUN TATTVA** is an Android application built with Kotlin and Jetpack Compose that calculates and displays Vedic astrological information in real-time. The app focuses on Tattva cycles (elemental periods), planetary hours, moon phases, and Nakshatra positions.

- **Package:** `com.android.sun.tattva`
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Ephemeris Engine:** Swiss Ephemeris (swisseph.jar)
- **Current Version:** 2.32 (versionCode 26)

### ⚠️ Version Increment Rule
**IMPORTANT:** The version MUST be incremented by 0.01 with every modification/release.
- Current: **2.32**
- Next versions: **2.33**, **2.34**, **2.35**, ...
- Update both `versionName` and `versionCode` in `app/build.gradle.kts`
- Increment `versionCode` by 1 and `versionName` by 0.01 for each set of changes

---

## Architecture

### Project Structure

```
app/src/main/java/com/android/sun/
├── MainActivity.kt                    # Entry point + NavHost setup
├── data/
│   ├── model/                         # Data classes (AstroData, TattvaInfo, etc.)
│   ├── preferences/                   # SettingsPreferences (DataStore)
│   └── repository/                    # AstroRepository, LocationPreferences
├── domain/calculator/                 # All calculation engines
│   ├── AstroCalculator.kt            # Core Swiss Ephemeris wrapper calls
│   ├── TattvaCalculator.kt           # Tattva cycle calculation (24-min periods)
│   ├── SubTattvaCalculator.kt        # Sub-Tattva calculation (4.8-min periods)
│   ├── PlanetCalculator.kt           # Planetary hour calculation
│   ├── MoonPhaseCalculator.kt        # Moon phase, full/new moon, Tripura Sundari
│   ├── NakshatraCalculator.kt        # Lunar mansion calculation
│   ├── NityaCalculator.kt            # Nitya (lunar day) calculation
│   ├── PolarityCalculator.kt         # Sunrise/sunset polarity
│   ├── SwissEphWrapper.kt            # Swiss Ephemeris JNI wrapper
│   └── Supplement.kt                 # Additional helper calculations
├── notification/                      # Scheduled notifications
│   ├── NotificationScheduler.kt      # WorkManager scheduling
│   ├── NotificationWorker.kt         # Background notification tasks
│   ├── NotificationHelper.kt         # Notification creation helpers
│   └── BootReceiver.kt              # Reschedule after device reboot
├── service/
│   └── TattvaNotificationService.kt  # Foreground service for status bar
├── ui/
│   ├── components/                    # Reusable UI components
│   │   ├── CombinedTattvaCard.kt     # Main Tattva + SubTattva display
│   │   ├── MoonPhaseCard.kt          # Moon phases card (expandable full moon)
│   │   ├── PlanetaryHoursCard.kt     # Planetary hours schedule
│   │   ├── NakshatraCard.kt          # Nakshatra display
│   │   ├── NityaExpandableCard.kt    # Nitya (lunar day) display
│   │   ├── LocalizationHelpers.kt    # i18n helpers for zodiac/planet/nakshatra translation
│   │   ├── GradientNavigationBar.kt  # Bottom gradient navigation bar
│   │   └── ...                        # Other UI components
│   ├── screens/                       # Full-screen composables
│   │   ├── MainScreen.kt             # Main dashboard
│   │   ├── AllDayScreen.kt           # Full-day Tattva schedule
│   │   ├── CalendarPickerScreen.kt   # Date picker for custom day view
│   │   ├── LocationScreen.kt         # Location search/selection
│   │   ├── SettingsScreen.kt         # App settings
│   │   └── NakshatraDetailScreen.kt  # Nakshatra detailed info
│   └── theme/                         # Material 3 theme configuration
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
├── util/
│   ├── AppLog.kt                      # Debug logging wrapper
│   ├── AppDefaults.kt                 # Default values
│   ├── TattvaColors.kt               # Tattva color definitions
│   └── TimeZoneUtils.kt              # Timezone/DST utilities
└── viewmodel/
    ├── MainViewModel.kt               # Main screen state management
    ├── AstroViewModel.kt              # Astro calculations for views
    └── LocationViewModel.kt           # Location management
```

---

## Core Concepts

### Tattva System

The 5 Tattvas (elements) cycle in a fixed 24-minute period starting from sunrise:

| Index | Tattva    | Element | Duration |
|-------|-----------|---------|----------|
| 0     | Akasha    | Ether   | 24 min   |
| 1     | Vayu      | Air     | 24 min   |
| 2     | Tejas     | Fire    | 24 min   |
| 3     | Apas      | Water   | 24 min   |
| 4     | Prithivi  | Earth   | 24 min   |

- **Full cycle:** 120 minutes (2 hours)
- **Reference point:** Sunrise (not midnight!)
- **Sub-Tattva:** Each Tattva is divided into 5 sub-periods of 4.8 minutes each

**Calculation:**
```
elapsed_ms = current_time - sunrise_time
tattva_index = (elapsed_ms / 24_minutes) % 5
sub_tattva_index = ((elapsed_ms % 24_minutes) / 4.8_minutes) % 5
```

### Planetary Hours

Each day (sunrise to sunset, sunset to next sunrise) is divided into 12 planetary hours, each ruled by a planet in Chaldean order: Sun, Venus, Mercury, Moon, Saturn, Jupiter, Mars.

### Moon Phases

The app tracks:
- **Tripura Sundari:** Phase angle ~154.28° (Shukla Dasami)
- **Full Moon:** Phase angle 180° (with 18-hour influence period: ±18h from peak)
- **Shivaratri:** Krishna Chaturdashi (phase angle ~342°) - includes yearly list with next date highlighted
- **New Moon:** Phase angle 0°/360°

The 18h influence period is calculated based on the Moon-Sun relative angular speed (~12.2°/day), where 18 hours ≈ 9.15° of relative motion (using 9.5° threshold for safety margin).

**MoonPhaseCard UI:**
- Rows display in order: Tripura Sundari → Full Moon → Shivaratri → New Moon
- Full Moon row is expandable: shows 18h influence period (peak ± 18h) + next 12 full moon dates with cyan highlight for nearest
- (v2.23+) Full Moon influence period always has a visible background (dark blue-grey when inactive, deep purple when active/highlighted) for prominence
- Shivaratri row is expandable: shows next 12 Shivaratri periods (across years if needed) with cyan highlight for next date. Past dates (after 6:00 AM on morning date) are filtered out.
- All rows use uniform 16sp bold titleMedium font

### Nakshatra

Lunar mansions (27 divisions of the ecliptic, each ~13.33°), calculated from the Moon's sidereal longitude using Swiss Ephemeris with ayanamsa correction.

**Future Nakshatra Calculation (v2.18+):** All 27 future Nakshatra time intervals are calculated using actual ephemeris data for each boundary crossing (Newton-Raphson refinement), instead of extrapolating with a single constant moon speed. This eliminates the large cumulative errors that occurred because the Moon's speed varies from ~11.8° to ~15.2° per day. See `nakshatra-fix-resolution.md` for details.

**Extended Descriptions (v2.20+):** Each Nakshatra has a detailed description including symbolism, **Ce se face:** (What to do) and **Ce nu se face:** (What NOT to do) — practical guidance for each lunar mansion. The descriptions are rendered with bold markers and paragraph spacing. See `Nakshatra-descriere-extinsa.txt` for the source and `nakshatra-calculation.md` for the full technical documentation.

**NakshatraCard UI (v2.21+):**
- **Countdown fix:** The remaining time countdown now uses the ephemeris-refined `endTime` from `futureNakshatras[0]` instead of the simple extrapolation `endTime` from `calculateNakshatra()`. This fixes cases where the countdown showed incorrect values (e.g. 28h instead of ~12h).
- **Layout fix:** "Nakshatra:" label moved to card title row (smaller, above the name). The Nakshatra name now has the full row width, preventing truncation of long names like "Purva Bhadrapada" or "Uttara Bhadrapada" on smaller screens.

---

## Navigation Structure

```
NavHost (startDestination = "main")
├── "main"                              → MainScreen (dashboard)
├── "location"                          → LocationScreen (city search)
├── "settings"                          → SettingsScreen
├── "allday"                            → AllDayScreen (today's Tattva schedule)
├── "calendar"                          → CalendarPickerScreen
├── "customday/{year}/{month}/{day}"    → AllDayScreen (custom date schedule)
└── "nakshatra/{number}"                → NakshatraDetailScreen
```

### Main Screen Interaction Areas

- **Date/Time row (top):** Click → navigates to AllDayScreen (full day Tattva schedule)
- **Location row:** Click → navigates to LocationScreen
- **Settings icon:** Click → navigates to SettingsScreen
- **Tattva Card:** Click → expands to show Sub-Tattva; "SHOW DAY" button → AllDayScreen
- **Moon Phase Card:** Full Moon row is clickable → expands to show 18h influence period
- **Nakshatra Card:** Click → navigates to NakshatraDetailScreen

---

## Notification System

### 1. Foreground Service (Persistent Status Bar)

**File:** `TattvaNotificationService.kt`

- Shows persistent notification with current Tattva emoji (🔺🟨🌙🔵🟣) and planetary hour
- Uses `AlarmManager.setExactAndAllowWhileIdle()` to update at exact Tattva/Planet transitions
- Includes 5-minute safety net loop as fallback for Doze mode / OEM restrictions
- Responds to location/settings changes via BroadcastReceiver
- (v2.23+) Uses `TimeZoneUtils.getLocationTimeZone()` for DST-aware time display and offset suffix
- (v2.23+) Uses localized "until"/"până la" text from string resources (`R.string.notification_until`)

**Notification Channels:**
- `tattva_persistent_channel` - Tattva element updates
- `planet_persistent_channel` - Planetary hour updates

### 2. Scheduled Notifications (WorkManager)

**Files:** `NotificationScheduler.kt`, `NotificationWorker.kt`, `NotificationHelper.kt`

Scheduled events:
- Full Moon start (18h before peak)
- Full Moon end (18h after peak)
- Tripura Sundari (24h before)
- New Moon (24h before)

### 3. Boot Receiver

**File:** `BootReceiver.kt`
- Reschedules notifications after device reboot

---

## Settings

| Setting                    | Description                                    |
|----------------------------|------------------------------------------------|
| Dark Theme                 | Toggle dark/light mode                         |
| Language                   | Toggle Romanian (OFF) / English (ON)           |
| Tattva Notification        | Persistent status bar Tattva notification       |
| Planetary Hour Notification| Persistent status bar planetary hour notification|
| Full Moon Notification     | Alert before/during full moon                  |
| Tripura Sundari Notification| Alert before Tripura Sundari                  |
| New Moon Notification      | Alert before new moon                          |
| Tattva Sound (per element) | Individual sound alerts for each Tattva change |
| Tattva Sound Volume        | Slider (0–100%) to control Tattva sound playback volume. Stored in SharedPrefs. Checking a sound checkbox also previews the sound at the current volume. |
| Tattva Sound Mute Warning  | (v2.22+) When phone is on silent or vibrate mode, a Snackbar popup warns the user that they won't hear the sound preview. Uses AudioManager.ringerMode detection. |
| Tattva Custom Sound        | Tap the tattva symbol icon to open a full-screen picker (gradient header in tattva color). Select any audio file (MP3, OGG, WAV, AAC, FLAC, M4A) from device. Persistent URI permission is taken. Reset button reverts to built-in default. Custom URI stored in SharedPrefs per tattva code. A colored dot on the icon indicates a custom sound is active. |
| 🐛 Debug Date Override    | (v2.19+) Set a custom date to test Nakshatra calculations without waiting days. Opens a DatePicker, overrides `Calendar.getInstance()` in all calculations. Orange debug banner shown on MainScreen when active. Disable to return to real-time mode. Stored in SharedPrefs as `debug_date_enabled` (Boolean) and `debug_date_millis` (Long). |

---

## Key Data Models

### AstroData
Main data container holding all calculated astrological information for the current location and time:
- Tattva (current element + timing)
- SubTattva (sub-element + timing)
- Planetary hour (current planet + timing)
- Moon phase (illumination, next events, influence period)
- Nakshatra (lunar mansion)
- Nitya (lunar day)
- Sunrise/Sunset (with next-day predictions)
- Location info (name, coordinates, timezone)

### TattvaInfo
Display-friendly wrapper containing: name, color, emoji, start/end times, remaining time.

### MoonPhaseResult
- `phaseAngle: Double` - Moon-Sun relative angle (0-360°)
- `illuminationPercent: Int` - Percentage of moon illumination
- `nextTripuraSundari: Calendar` - Time of next Tripura Sundari
- `nextFullMoon: Calendar` - Time of next full moon
- `nextNewMoon: Calendar` - Time of next new moon
- `isInFullMoonInfluence: Boolean` - Whether currently within 18h of full moon peak
- `nextShivaratri: ShivaratriDate` - Next Shivaratri (eveningDate/morningDate)
- `yearlyShivaratri: List<ShivaratriDate>` - Next 12 Shivaratri periods (past ones filtered after 6 AM on morning date, spans across year boundary)
- `futureFullMoons: List<Calendar>` - Next 12 full moon peak times (computed using ephemeris search)

### NakshatraResult
- `nakshatra: NakshatraType` - Current Nakshatra enum (27 types with deity/symbol/animal/planet/nature)
- `moonLongitude: Double` - Current moon sidereal longitude
- `startTime: Calendar` / `endTime: Calendar` - Current Nakshatra time window
- `moonZodiacPosition: String` - Moon position as degrees/minutes/seconds (e.g., "15° 59' 55\"")
- `moonZodiacSignIndex: Int` - Zodiac sign index (0=Aries .. 11=Pisces) for UI localization
- `moonSpeedDegreesPerDay: Double` - Actual moon speed for time calculations
- `zeroReferenceTime: Calendar` - Reference time for stable multi-Nakshatra timeline
- `futureNakshatras: List<NakshatraTimeSlot>` - Pre-computed time intervals for all 27 Nakshatras using real ephemeris data at each boundary (v2.18+)

---

## Build & Dependencies

### Key Dependencies
- **Jetpack Compose:** UI framework (1.6.2)
- **Material 3:** Design system (1.2.1)
- **Navigation Compose:** Screen navigation (2.7.7)
- **Room Database:** Local data persistence (2.6.1)
- **WorkManager:** Background task scheduling (2.9.0)
- **Play Services Location:** GPS location (21.1.0)
- **Swiss Ephemeris:** Astronomical calculations (swisseph.jar)

### Build Configuration
- **Compile SDK:** 34
- **Java/Kotlin Target:** Java 17
- **Compose Compiler:** 1.5.4
- **R8/ProGuard:** Enabled for release builds
- **AAB Splits:** Language, density, ABI splits enabled

---

## Timezone Handling

The app uses a dual-timezone approach:
1. **Phone timezone:** Used for displaying current time in the header
2. **Location timezone:** Used for all astronomical calculations (sunrise, sunset, Tattva timing)

When the phone and location timezones differ, a "Local time" line is shown in the header.

All Swiss Ephemeris calculations are performed in UTC and then converted to the location's timezone for display. DST (Daylight Saving Time) is handled via `TimeZoneUtils.getLocationTimeZone()`.

---

## Internationalization (i18n)

### Bilingual System (Romanian / English)

The app supports two languages: **Romanian** (default) and **English**.

**String Resources:**
- `app/src/main/res/values/strings.xml` — Romanian (default)
- `app/src/main/res/values-en/strings.xml` — English

**Language Preference:**
- Stored in SharedPreferences via `SettingsPreferences` (`KEY_LANGUAGE`, default `"ro"`)
- Toggle in Settings: Switch OFF = Romanian, Switch ON = English

**Locale Switching Mechanism (MainActivity.kt):**
```kotlin
val localizedContext = context.createConfigurationContext(config)
CompositionLocalProvider(
    LocalContext provides localizedContext,
    LocalActivityResultRegistryOwner provides activity  // ⚠️ CRITICAL: Must provide!
) { ... }
```
> **⚠️ CRITICAL:** When overriding `LocalContext` with `createConfigurationContext()`, you MUST also explicitly provide `LocalActivityResultRegistryOwner` (the Activity), otherwise `rememberLauncherForActivityResult()` crashes.

### Localization Helpers

**File:** `ui/components/LocalizationHelpers.kt`

All hardcoded Romanian enum values (zodiac signs, planet names, nakshatra details) are translated via `@Composable` helper functions that use `stringResource()`:

| Function | Purpose |
|----------|---------|
| `getLocalizedZodiacSign(signIndex: Int)` | Zodiac sign names (0=Aries..11=Pisces) |
| `getLocalizedPlanetName(planet: PlanetType)` | Planet names (Sun, Moon, Mars, etc.) |
| `getLocalizedNakshatraPlanet(nakshatra)` | Nakshatra ruling planet (incl. Rahu/Ketu) |
| `getLocalizedNakshatraSymbol(nakshatra)` | Nakshatra symbols (sword, horse head, etc.) |
| `getLocalizedNakshatraAnimal(nakshatra)` | Nakshatra animals (lion, horse, etc.) |
| `getLocalizedNakshatraNature(nakshatra)` | Nakshatra natures (fierce, gentle, etc.) |
| `getLocalizedNakshatraDegreeRange(nakshatra)` | Degree ranges with translated zodiac names |

**String Resource Keys (per category):**
- `zodiac_aries` .. `zodiac_pisces` — 12 zodiac signs
- `planet_sun` .. `planet_ketu` — 9 planet names (7 classical + Rahu + Ketu)
- `nakshatra_symbol_ashwini` .. `nakshatra_symbol_revati` — 27 symbols
- `nakshatra_animal_ashwini` .. `nakshatra_animal_revati` — 27 animals
- `nakshatra_nature_ashwini` .. `nakshatra_nature_revati` — 27 natures
- `nakshatra_range_ashwini` .. `nakshatra_range_revati` — 27 degree ranges
- `nakshatra_desc_ashwini` .. `nakshatra_desc_revati` — 27 extended descriptions (v2.20+: symbolism + "Ce se face:" + "Ce nu se face:" with `\n\n` paragraph separators; rendered with bold markers via `formatNakshatraDescription()` in `NakshatraDetailScreen.kt`)

**Moon Zodiac Position Format:**
- `NakshatraResult.moonZodiacPosition` = degrees/minutes/seconds only (e.g., `"15° 59' 55\""`)
- `NakshatraResult.moonZodiacSignIndex` = index 0-11 for localization
- UI combines them: `stringResource(R.string.moon_zodiac_position, position, getLocalizedZodiacSign(index))`

> **Rule:** Never display raw enum fields (`nakshatra.symbol`, `nakshatra.animal`, `nakshatra.planet`, `nakshatra.nature`, `nakshatra.degreeRange`, `planet.displayName`) directly in UI. Always use the corresponding `getLocalized*()` helper.

---

## UI Text Overflow Prevention

All `Row` composables with side-by-side text must follow these rules to prevent overlapping on narrow screens:
- Use `maxLines = 1` and `softWrap = false` on date/time texts (right side)
- Use `Modifier.weight(1f)` on the left-side text
- This applies to: MoonPhaseCard, NakshatraCard, PlanetaryHoursCard, CombinedTattvaCard

---

## Update Mechanism

### UI Updates
- **MainViewModel:** 1-second loop checking if current Tattva has expired → recalculates when needed
- **CombinedTattvaCard:** 1-second countdown timer for remaining time display. (v2.22+) Responsive layout uses Compose weight-based distribution: icon → centered countdown → right-aligned "SHOW DAY" button, preventing overlap on narrow screens.
- **CompactInfoCard:** 1-second clock update for live time display

### Notification Updates
- **Primary:** Exact alarm (`setExactAndAllowWhileIdle`) at each Tattva/Planet transition
- **Safety net:** 5-minute coroutine loop as fallback
- **Triggers:** Location changes, settings changes → immediate recalculation

---

*Last updated: March 2026 - Version 2.17*
