# SUN TATTVA - Comprehensive Documentation

## Overview

**SUN TATTVA** is an Android application built with Kotlin and Jetpack Compose that calculates and displays Vedic astrological information in real-time. The app focuses on Tattva cycles (elemental periods), planetary hours, moon phases, and Nakshatra positions.

- **Package:** `com.android.sun.tattva`
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Ephemeris Engine:** Swiss Ephemeris (swisseph.jar)

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
- **New Moon:** Phase angle 0°/360°

The 18h influence period is calculated based on the Moon-Sun relative angular speed (~12.2°/day), where 18 hours ≈ 9.15° of relative motion (using 9.5° threshold for safety margin).

### Nakshatra

Lunar mansions (27 divisions of the ecliptic, each ~13.33°), calculated from the Moon's sidereal longitude using Swiss Ephemeris with ayanamsa correction.

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
| Tattva Notification        | Persistent status bar Tattva notification       |
| Planetary Hour Notification| Persistent status bar planetary hour notification|
| Full Moon Notification     | Alert before/during full moon                  |
| Tripura Sundari Notification| Alert before Tripura Sundari                  |
| New Moon Notification      | Alert before new moon                          |
| Tattva Sound (per element) | Individual sound alerts for each Tattva change |

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

## Update Mechanism

### UI Updates
- **MainViewModel:** 1-second loop checking if current Tattva has expired → recalculates when needed
- **CombinedTattvaCard:** 1-second countdown timer for remaining time display
- **CompactInfoCard:** 1-second clock update for live time display

### Notification Updates
- **Primary:** Exact alarm (`setExactAndAllowWhileIdle`) at each Tattva/Planet transition
- **Safety net:** 5-minute coroutine loop as fallback
- **Triggers:** Location changes, settings changes → immediate recalculation

---

*Last updated: March 2026 - Version 2.10*
