# Bug Fixes for Planetary Hour Notification Feature

## Issues Reported by @karen20dec4

### Bug 1: Status Bar Icon Not Showing Planet Symbol
**Problem**: When only "Planetary Hour in Status Bar" was enabled (with Tattva disabled), the notification icon in the status bar still showed the tattva icon instead of a planet-related icon.

**Expected Behavior**: 
- Only Tattva enabled: Show tattva icon (🔺, 🟨, 🌙, 🔵, or 🟣 depending on current tattva)
- Only Planet enabled: Show planet/sun icon
- Both enabled: Show tattva icon

**Root Cause**: The small icon selection logic always used the tattva icon regardless of which features were enabled:
```kotlin
val iconRes = when (tattvaType) {
    TattvaType.TEJAS -> R.drawable.ic_tattva_tejas
    // ... always used tattva icon
}
```

**Fix Applied**: Modified icon selection to check which features are enabled:
```kotlin
val iconRes = if (showTattva) {
    // When tattva is enabled (with or without planet), use tattva-specific icon
    when (tattvaType) {
        TattvaType.TEJAS -> R.drawable.ic_tattva_tejas
        TattvaType.PRITHIVI -> R.drawable.ic_tattva_prithivi
        TattvaType.APAS -> R.drawable.ic_tattva_apas
        TattvaType.VAYU -> R.drawable.ic_tattva_vayu
        TattvaType.AKASHA -> R.drawable.ic_tattva_akasha
    }
} else {
    // When only planet is enabled, use generic sun icon
    R.drawable.icon
}
```

**Result**: Status bar now correctly displays:
- `13:56 ☿` - When only planetary hour is enabled (with sun icon in status bar)
- `13:56 🔺` - When only tattva is enabled (with tattva icon)
- `13:56 🔺☿` - When both are enabled (with tattva icon)

---

### Bug 2: Duplicate Planet Symbol in Expanded Notification
**Problem**: When the notification was expanded in the notification shade, the planet symbol appeared twice - once in the title area and once in the content.

**Example of the bug**:
```
☿                           <- Title showing symbol
SUN TATTVA - Paris
☿ Mercur - ends at 14:30    <- Symbol appears again here
```

**Root Cause**: Android notification structure shows both the title and the BigTextStyle content when expanded. The code was setting:
- `setContentTitle(collapsedTitle)` - which contained `☿`
- `bigText("SUN TATTVA...\n☿ Mercur...")` - which also contained `☿`

Both were visible in expanded view, causing duplication.

**Fix Applied**: Used `setBigContentTitle()` to set a different title for expanded view:
```kotlin
builder.setContentTitle(collapsedTitle)  // "☿" for collapsed
builder.setContentText("SUN TATTVA - $locationName")  // Shows in collapsed
builder.setStyle(NotificationCompat.BigTextStyle()
    .setBigContentTitle("SUN TATTVA - $locationName")  // Replaces title when expanded
    .bigText("$planetEmoji $planetName - ends at..."))  // Content without duplication
```

**Result**: Expanded notification now shows correctly:
```
SUN TATTVA - Paris
☿ Mercur - ends at 14:30 (GMT+2)
```

No more duplicate symbols!

---

## Notification Display Summary

### Collapsed Notification (in notification shade)
- **Title**: Emoji symbols only (🔵, ☿, or 🔵☿)
- **Text**: "SUN TATTVA - [Location]"
- **Small Icon**: Appropriate icon for enabled feature(s)

### Expanded Notification (when user expands in shade)
- **Title**: "SUN TATTVA - [Location]"
- **Content**: 
  - Tattva only: "🔵 Vayu - ends at 01:08 (GMT+2)"
  - Planet only: "☿ Mercur - ends at 14:30 (GMT+2)"
  - Both: Two lines with tattva and planet info

### Status Bar (top of screen, near clock)
- **Small Icon**: 
  - Tattva icon when tattva enabled
  - Sun icon when only planet enabled
- **Text**: The emoji symbols from title appear next to clock

---

## Files Modified
- `TattvaNotificationService.kt` - Fixed icon selection and notification structure

## Commit
- **Hash**: 7d1652b
- **Message**: "Fix notification icon and remove duplicate planet symbol in expanded view"

## Testing Checklist
- [x] Only Tattva enabled - shows correct icon and no duplicates
- [x] Only Planet enabled - shows sun icon and no duplicates
- [x] Both enabled - shows tattva icon and both symbols correctly
- [x] Collapsed view shows symbols near clock
- [x] Expanded view shows detailed information without duplication
