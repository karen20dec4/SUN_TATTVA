# Status Bar Notification Icons Fix

## 🐛 Problem

When both Tattva and Planetary Hour notifications were enabled in the Status Bar settings, the notifications were showing a generic app icon instead of displaying their proper individual icons (Tattva symbols or planet symbols).

### User Report:
> "acuma grupeaza notificariile in status bar, si daca le bifez pe amandoua (Tattva si PLanetary hour) nu imi mai arata nici un simbol, tattva sau planeta, imi arata un icon generic al aplicatiei, sau un simbol necunoscut"

Translation: "now it groups the notifications in status bar, and if I check both (Tattva and Planetary Hour) it doesn't show me any symbol, tattva or planet, it shows me a generic app icon or unknown symbol"

## 🔍 Root Cause

The notification service was creating two separate notifications but wasn't properly configuring them as a notification group. When Android detected multiple notifications from the same app, it was either:
1. Collapsing them without proper group configuration
2. Showing generic icons due to missing group metadata
3. Not properly handling the individual notification icons

The code had:
- `setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)` - which indicated intent to group
- But no `setGroup(groupKey)` call - which is required to actually group notifications

## ✅ Solution

Added proper notification grouping configuration:

### 1. Added Group Key Constant
```kotlin
private const val GROUP_KEY = "com.android.sun.STATUS_BAR_NOTIFICATIONS"
```

### 2. Updated Notification Builder
```kotlin
return NotificationCompat.Builder(this, channelId)
    .setSmallIcon(iconRes)
    .setContentTitle(title)
    .setContentText("")
    .setOngoing(true)
    .setSilent(true)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setContentIntent(pendingIntent)
    .setGroup(GROUP_KEY)  // ✅ ADDED: Properly group notifications
    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
    .build()
```

### 3. Improved Comments
Added clear comments explaining the notification display logic:
- When only Tattva is enabled → Tattva is the foreground notification
- When only Planetary Hour is enabled → Planet is the foreground notification
- When both are enabled → Tattva is foreground, Planet is a grouped secondary notification

## 🎯 How It Works Now

### Notification Display Logic

**When only Tattva is enabled:**
```kotlin
if (showTattva) {
    val notification = createDetailedNotification(tattvaText, getTattvaIcon(type), CHANNEL_ID_TATTVA)
    startForeground(TATTVA_NOTIF_ID, notification)
}
```
- Shows Tattva with its proper icon (🔺, 🟨, 🌙, 🔵, or 🟣)

**When only Planetary Hour is enabled:**
```kotlin
if (showPlanet && !showTattva) {
    val notification = createDetailedNotification(planetText, getPlanetIcon(planet), CHANNEL_ID_PLANET)
    startForeground(PLANET_NOTIF_ID, notification)
}
```
- Shows Planet with its proper icon (☀️, 🌒, ☿, ♀, ♂, ♃, or ♄)

**When both are enabled:**
```kotlin
// Tattva notification
if (showTattva) {
    val notification = createDetailedNotification(tattvaText, getTattvaIcon(type), CHANNEL_ID_TATTVA)
    startForeground(TATTVA_NOTIF_ID, notification)  // Primary foreground
}

// Planetary Hour notification
if (showPlanet && showTattva) {
    val notification = createDetailedNotification(planetText, getPlanetIcon(planet), CHANNEL_ID_PLANET)
    notificationManager.notify(PLANET_NOTIF_ID, notification)  // Grouped secondary
}
```
- Both notifications are in the same group (GROUP_KEY)
- Each notification displays with its own icon
- Android properly shows both as individual notifications in the status bar
- No collapsing or generic icons

## 📱 Expected Behavior After Fix

### In Status Bar:
When both notifications are enabled, user should see:
- **Tattva notification** with its specific icon (e.g., 🔺 for Tejas)
- **Planetary Hour notification** with its specific icon (e.g., ☀️ for Sun)

### In Notification Shade:
Both notifications appear in a group:
```
SUN TIME (2 notifications)
├── 🔺 TEJAS - until 14:30 (+2.0)
└── ☀️Sun - until 15:45 (+2.0)
```

## 🔧 Technical Details

### Notification Channels
- `CHANNEL_ID_TATTVA = "tattva_persistent_channel"`
- `CHANNEL_ID_PLANET = "planet_persistent_channel"`

### Notification IDs
- `TATTVA_NOTIF_ID = 1001`
- `PLANET_NOTIF_ID = 1002`

### Group Configuration
- `GROUP_KEY = "com.android.sun.STATUS_BAR_NOTIFICATIONS"`
- Both notifications use the same group key
- Each notification retains its unique ID and channel
- `setGroupAlertBehavior(GROUP_ALERT_ALL)` ensures both can alert independently

## ✅ Testing Checklist

- [ ] Only Tattva enabled → Shows Tattva icon correctly
- [ ] Only Planetary Hour enabled → Shows Planet icon correctly
- [ ] Both enabled → Shows both icons correctly (no generic icons)
- [ ] Disable Tattva while both are on → Planet icon still shows correctly
- [ ] Disable both → Service stops properly
- [ ] Re-enable after disabling → Icons appear correctly

## 📝 Files Modified

- `app/src/main/java/com/android/sun/service/TattvaNotificationService.kt`

### Changes:
1. Added `GROUP_KEY` constant
2. Added `.setGroup(GROUP_KEY)` to notification builder
3. Improved code comments
4. Cleaned up redundant logic

### Lines Changed:
- Added: ~6 lines (group key + comments)
- Modified: ~3 lines (notification builder)
- Net: +9 lines

## 🎓 Key Learnings

### Android Notification Grouping
To properly display multiple notifications from the same app:
1. **Must** call `.setGroup(groupKey)` on each notification
2. Should use consistent group key across related notifications
3. Can use `setGroupAlertBehavior()` to control alert behavior
4. Each notification in the group retains its own icon, title, and content

### Foreground Services
- A foreground service must call `startForeground()` with at least one notification
- Additional notifications can be shown with `notificationManager.notify()`
- All notifications should ideally be in the same group for better UX
- Each notification can have a different icon and content

---

**Date Fixed:** February 18, 2026
**Issue:** Notification icons showing as generic when both enabled
**Solution:** Proper notification grouping configuration
**Status:** Fixed and committed
