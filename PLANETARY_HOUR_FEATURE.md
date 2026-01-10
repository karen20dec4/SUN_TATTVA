# Planetary Hour in Status Bar - Implementation Guide

## Overview
This feature adds planetary hour display to the status bar notification, working independently or together with the Tattva notification.

## Changes Made

### 1. Settings Preferences (`SettingsPreferences.kt`)
Added new preference for planetary hour notification:
```kotlin
// Key: KEY_PLANETARY_HOUR_NOTIFICATION = "planetary_hour_notification"
fun getPlanetaryHourNotification(): Boolean
fun setPlanetaryHourNotification(enabled: Boolean)
```

### 2. Settings Screen (`SettingsScreen.kt`)
Added new toggle switch in the Notifications section:
- **Title**: "Planetary Hour in Status Bar"
- **Subtitle**: "Show current planetary hour as persistent notification"
- Icon: Bell notification icon

### 3. Main Activity (`MainActivity.kt`)
- Added state flow for `isPlanetaryHourNotification`
- Updated service start/stop logic to consider both toggles
- Service starts if EITHER Tattva OR Planetary Hour is enabled
- Service stops only when BOTH are disabled

### 4. Notification Service (`TattvaNotificationService.kt`)
Major updates to support planetary hours:

#### Data Collection
- Reads both Tattva and Planetary Hour settings
- Calculates both from `AstroRepository`
- Uses existing planetary hour calculation (no duplication)

#### Display Logic
Three display modes based on settings:

**Mode 1: Only Tattva Enabled**
- Collapsed: Shows tattva emoji (e.g., 🔵)
- Expanded: "🔵 Vayu - ends at 01:08 (GMT+2)"

**Mode 2: Only Planetary Hour Enabled**
- Collapsed: Shows planet symbol (e.g., ☉)
- Expanded: "☉ Soare - ends at 01:35 (GMT+2)"

**Mode 3: Both Enabled**
- Collapsed: Shows both symbols (e.g., 🔵☉)
- Expanded:
  ```
  SUN TATTVA - Paris
  🔵 Vayu - ends at 01:08 (GMT+2)
  ☉ Soare - ends at 01:35 (GMT+2)
  ```

Note: The collapsed notification title shows ONLY the symbols. When expanded, it shows the full format with location name in the first line, followed by detailed timing information for each element.

## Features

### Independent Operation
- ✅ Tattva and Planetary Hour can be enabled/disabled independently
- ✅ Service automatically starts/stops based on settings
- ✅ No code duplication - reuses existing calculation logic

### Status Bar Display
- ✅ Collapsed view shows only symbols for minimal space usage
- ✅ Expanded view shows detailed information with timing
- ✅ Location name displayed in title

### Data Accuracy
- ✅ Uses existing AstroRepository calculations
- ✅ Respects location timezone settings
- ✅ Updates every 30 seconds
- ✅ Responds to location changes immediately

## Testing Instructions

### Test Case 1: Only Tattva
1. Go to Settings
2. Enable "Tattva in Status Bar"
3. Disable "Planetary Hour in Status Bar"
4. Check notification shows only tattva symbol
5. Expand notification - should show only tattva info

### Test Case 2: Only Planetary Hour
1. Go to Settings
2. Disable "Tattva in Status Bar"
3. Enable "Planetary Hour in Status Bar"
4. Check notification shows only planet symbol
5. Expand notification - should show only planet info

### Test Case 3: Both Enabled
1. Go to Settings
2. Enable both "Tattva in Status Bar" and "Planetary Hour in Status Bar"
3. Check notification shows both symbols (emoji first, then planet)
4. Expand notification - should show:
   - Title with location
   - Tattva line with emoji, name, and end time
   - Planet line with symbol, name, and end time

### Test Case 4: Both Disabled
1. Go to Settings
2. Disable both toggles
3. Service should stop automatically
4. No notification should be visible

### Test Case 5: Toggle While Running
1. Start with only Tattva enabled
2. Enable Planetary Hour while service is running
3. Notification should update to show both
4. Disable Tattva - should switch to only planet
5. Disable Planetary Hour - service should stop

## Technical Details

### Planetary Hour Calculation
The planetary hour is calculated by `AstroRepository.calculateAstroData()` which:
1. Determines current sunrise/sunset
2. Calculates previous sunset and next sunrise
3. Calls `PlanetCalculator.calculatePlanetaryHour()`
4. Returns `PlanetResult` with planet type and timing

### Planet Symbols
```kotlin
SUN      -> ☉ (Soare)
MOON     -> ☽ (Lună)
MERCURY  -> ☿ (Mercur)
VENUS    -> ♀ (Venus)
MARS     -> ♂ (Marte)
JUPITER  -> ♃ (Jupiter)
SATURN   -> ♄ (Saturn)
```

### Tattva Symbols
```kotlin
AKASHA   -> 🟣 (Akasha)
VAYU     -> 🔵 (Vayu)
TEJAS    -> 🔺 (Tejas)
APAS     -> 🌙 (Apas)
PRITHIVI -> 🟨 (Prithivi)
```

## File Changes Summary
1. `SettingsPreferences.kt` - Added planetary hour preference storage
2. `SettingsScreen.kt` - Added UI toggle for planetary hour
3. `MainActivity.kt` - Updated service lifecycle based on both settings
4. `TattvaNotificationService.kt` - Core notification logic with dual display

## Notes for User
- The implementation reuses existing planetary hour calculations from `PlanetaryHoursCard`
- No additional computation overhead - data is already calculated
- Settings are persisted across app restarts
- Notification updates automatically based on location changes
- All files are complete and ready for download/testing
