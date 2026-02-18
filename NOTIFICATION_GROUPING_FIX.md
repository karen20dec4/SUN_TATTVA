# Fix: Notification Grouping Causing Generic Icon

## 🐛 Problem

When both Tattva and Planetary Hour status bar notifications were enabled:
1. Both icons appeared correctly for 3-4 seconds
2. Then Android grouped them and displayed a generic app icon
3. The specific Tattva/Planet icons disappeared

## 🔍 Analysis from Logs

From the logcat provided by the user:

```
2026-02-18 19:43:43.747 TattvaNoti...ionService: Creating Tattva notification: 🔵 VAYU - until 19:59 (+2.0)
2026-02-18 19:43:43.747 TattvaNoti...ionService: Tattva icon resource: 2131034151
2026-02-18 19:43:43.747 TattvaNoti...ionService: Notification created with group key: com.android.sun.STATUS_BAR_NOTIFICATIONS
2026-02-18 19:43:43.747 TattvaNoti...ionService: Calling startForeground with TATTVA_NOTIF_ID=1001

2026-02-18 19:43:43.752 TattvaNoti...ionService: Creating Planet notification: ♀Venus - until 20:01 (+2.0)
2026-02-18 19:43:43.753 TattvaNoti...ionService: Planet icon resource: 2131034146
2026-02-18 19:43:43.754 TattvaNoti...ionService: Notification created with group key: com.android.sun.STATUS_BAR_NOTIFICATIONS
2026-02-18 19:43:43.754 TattvaNoti...ionService: Both enabled - calling notify() for PLANET_NOTIF_ID=1002
```

**Key observations:**
- Both notifications were being created with correct icons (2131034151 for Tattva, 2131034146 for Planet)
- Both were assigned to the same group key: `com.android.sun.STATUS_BAR_NOTIFICATIONS`
- The code was working correctly, but Android's notification grouping behavior was the issue

## 📚 Android Notification Grouping Behavior

When multiple notifications share the same group key:

1. **Initial display:** Android shows each notification individually with its own icon
2. **Auto-summary creation:** After a few seconds, Android automatically creates a summary notification
3. **Grouping:** Android collapses the individual notifications under the summary
4. **Generic icon:** The auto-generated summary uses the app's default icon, not the individual notification icons

This is documented Android behavior for grouped notifications. When the system creates an automatic summary notification, it uses the app icon unless we explicitly provide a custom summary notification.

## ✅ Solution

**Remove notification grouping entirely.**

Instead of trying to fix the auto-generated summary (which would require creating and managing a custom summary notification), we simply let each notification display independently.

### Changes Made

**Before:**
```kotlin
private const val GROUP_KEY = "com.android.sun.STATUS_BAR_NOTIFICATIONS"

val notification = NotificationCompat.Builder(this, channelId)
    .setSmallIcon(iconRes)
    .setContentTitle(title)
    // ... other settings ...
    .setGroup(GROUP_KEY)  // Grouped notifications
    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
    .build()
```

**After:**
```kotlin
// No GROUP_KEY constant needed

val notification = NotificationCompat.Builder(this, channelId)
    .setSmallIcon(iconRes)
    .setContentTitle(title)
    // ... other settings ...
    // No .setGroup() call - notifications display independently
    .build()
```

## 🎯 Result

Now when both notifications are enabled:
- ✅ Tattva notification displays with its specific icon (🔺, 🟨, 🌙, 🔵, or 🟣)
- ✅ Planetary Hour notification displays with its specific icon (☀️, 🌒, ☿, ♀, ♂, ♃, or ♄)
- ✅ Icons remain visible and don't get replaced by generic icon
- ✅ Each notification is independent in the status bar
- ✅ No automatic grouping or summary notification

## 📱 Visual Comparison

**Before (with grouping):**
```
Status Bar:
First 3-4 seconds: 🔵 ♀
After grouping:    📱 (generic app icon)
```

**After (no grouping):**
```
Status Bar:
Always:            🔵 ♀ (specific icons persist)
```

## 🔧 Technical Details

### Why Grouping Was Added Initially

The grouping was added with good intentions:
- To organize related notifications together
- To allow them to be dismissed as a group
- To follow Android notification best practices

However, the automatic summary notification behavior caused the unintended side effect of hiding the specific icons.

### Alternative Solutions (Not Used)

**Option 1: Custom Summary Notification**
We could create a custom summary notification with a specific icon:
```kotlin
val summaryNotification = NotificationCompat.Builder(this, channelId)
    .setSmallIcon(R.drawable.app_icon)
    .setGroup(GROUP_KEY)
    .setGroupSummary(true)
    .build()
```

**Why not used:** 
- More complex code
- Need to manage summary notification separately
- Would show 3 icons in status bar (Tattva, Planet, Summary)
- Unclear what icon to use for summary

**Option 2: Different Group Keys**
Give each notification a different group key.

**Why not used:**
- Defeats the purpose of grouping
- Same result as having no groups

### Chosen Solution: No Grouping

**Why this is best:**
- ✅ Simplest implementation
- ✅ Each notification always shows its specific icon
- ✅ No auto-generated summary to cause issues
- ✅ Clear visual indication of what's active
- ✅ Minimal code changes
- ✅ No ongoing maintenance

## 📝 Testing

To verify the fix works:

1. **Enable both notifications** (Tattva + Planetary Hour)
   - Check status bar immediately
   - Wait 5-10 seconds
   - Both specific icons should remain visible

2. **Enable only Tattva**
   - Should show Tattva icon (🔺, 🟨, 🌙, 🔵, or 🟣)
   - Should NOT show generic icon

3. **Enable only Planetary Hour**
   - Should show Planet icon (☀️, 🌒, ☿, ♀, ♂, ♃, or ♄)
   - Should NOT show generic icon

4. **Toggle between states**
   - Icons should update immediately (thanks to ACTION_SETTINGS_CHANGED broadcast)
   - Should always show correct icon, never generic

## 📊 Impact

- ✅ **User experience:** Improved - always see the correct, meaningful icons
- ✅ **Code complexity:** Reduced - removed grouping logic
- ✅ **Maintainability:** Better - less code to maintain
- ✅ **Performance:** No impact - same number of notification calls

## 🎓 Lessons Learned

1. **Android's automatic behavior** can sometimes work against our intentions
2. **Simpler is often better** - removing grouping was better than trying to fix it
3. **User testing reveals real-world issues** - logging helped identify the exact problem
4. **Notifications are tricky** - what seems like a good practice (grouping) can have unintended consequences

---

**Date Fixed:** February 18, 2026
**Issue:** Notification grouping causing generic icon after 3-4 seconds
**Solution:** Removed notification grouping
**Files Changed:** `app/src/main/java/com/android/sun/service/TattvaNotificationService.kt`
**Lines Changed:** -4 (removed GROUP_KEY and grouping code)
