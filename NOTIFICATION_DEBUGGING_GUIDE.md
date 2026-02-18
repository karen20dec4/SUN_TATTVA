# Debugging Notification Grouping Issues

## 🐛 Current Problems

1. **Both notifications show for 3-4 seconds, then Android groups them with generic icon**
2. **When disabling one notification (e.g., leaving only Tattva), generic icon shows instead of Tattva icon**
3. **Only after refresh button or ~1 minute does the correct icon appear**

## 🔍 Debugging Approach

### Added Comprehensive Logging

All notification operations now log to Android Logcat with tag `TattvaNotificationService`.

**To view logs:**
```bash
adb logcat -s TattvaNotificationService
```

Or in Android Studio: Filter by "TattvaNotificationService"

### What the Logs Show

1. **Service Lifecycle:**
   - `onCreate()` - When service is created
   - `onStartCommand()` - When service is started
   - `updateNotification()` - Each time notifications are updated

2. **Settings State:**
   - `Settings: showTattva=true, showPlanet=false`
   - Shows which notifications are enabled

3. **Notification Creation:**
   - `Creating Tattva notification: 🔺 TEJAS - until 14:30 (+2.0)`
   - `Tattva icon resource: 2131230875` (the drawable resource ID)
   - Shows what notification is being created and which icon

4. **Display Method:**
   - `Calling startForeground with TATTVA_NOTIF_ID=1001`
   - `Both enabled - calling notify() for PLANET_NOTIF_ID=1002`
   - Shows whether using foreground service or regular notification

5. **Broadcast Reception:**
   - `Received ACTION_SETTINGS_CHANGED broadcast`
   - Shows when settings changes trigger updates

## 📊 Expected Log Flow

### When Both Are Enabled

```
TattvaNotificationService: ═══════════════════════════════════════════════════════
TattvaNotificationService: updateNotification() called
TattvaNotificationService: Settings: showTattva=true, showPlanet=true
TattvaNotificationService: Creating Tattva notification: 🔺 TEJAS - until 14:30 (+2.0)
TattvaNotificationService: Tattva icon resource: 2131230875
TattvaNotificationService: createDetailedNotification(): title='🔺 TEJAS - until 14:30 (+2.0)', iconRes=2131230875, channelId=tattva_persistent_channel
TattvaNotificationService: Notification created with group key: com.android.sun.STATUS_BAR_NOTIFICATIONS
TattvaNotificationService: Calling startForeground with TATTVA_NOTIF_ID=1001
TattvaNotificationService: Creating Planet notification: ☀️Sun - until 15:45 (+2.0)
TattvaNotificationService: Planet icon resource: 2131230880
TattvaNotificationService: createDetailedNotification(): title='☀️Sun - until 15:45 (+2.0)', iconRes=2131230880, channelId=planet_persistent_channel
TattvaNotificationService: Notification created with group key: com.android.sun.STATUS_BAR_NOTIFICATIONS
TattvaNotificationService: Both enabled - calling notify() for PLANET_NOTIF_ID=1002
TattvaNotificationService: updateNotification() completed successfully
TattvaNotificationService: ═══════════════════════════════════════════════════════
```

### When Only Tattva Is Enabled

```
TattvaNotificationService: ═══════════════════════════════════════════════════════
TattvaNotificationService: updateNotification() called
TattvaNotificationService: Settings: showTattva=true, showPlanet=false
TattvaNotificationService: Creating Tattva notification: 🔺 TEJAS - until 14:30 (+2.0)
TattvaNotificationService: Tattva icon resource: 2131230875
TattvaNotificationService: createDetailedNotification(): title='🔺 TEJAS - until 14:30 (+2.0)', iconRes=2131230875, channelId=tattva_persistent_channel
TattvaNotificationService: Notification created with group key: com.android.sun.STATUS_BAR_NOTIFICATIONS
TattvaNotificationService: Calling startForeground with TATTVA_NOTIF_ID=1001
TattvaNotificationService: Planet disabled - canceling notification
TattvaNotificationService: updateNotification() completed successfully
TattvaNotificationService: ═══════════════════════════════════════════════════════
```

### When Settings Change

```
TattvaNotificationService: Received ACTION_SETTINGS_CHANGED broadcast
TattvaNotificationService: ═══════════════════════════════════════════════════════
TattvaNotificationService: updateNotification() called
...
```

## 🔧 Immediate Updates

**Changes Made:**
- MainActivity now sends `ACTION_SETTINGS_CHANGED` broadcast when toggling checkboxes
- Service immediately updates notifications when receiving broadcast
- No need to wait for 30-second periodic update

## 🤔 Possible Issues to Investigate

### 1. Android Auto-Grouping Without Summary

**Symptom:** Android groups notifications and shows generic icon

**Possible Cause:** When multiple notifications share the same `GROUP_KEY`, Android may create a summary notification automatically. If we don't provide a custom summary notification with the correct icon, Android uses a generic one.

**Solution to Try:** Add a summary notification

### 2. Notification Icon Resources

**What to Check in Logs:**
- Are the icon resource IDs correct?
- Are they the same as the drawable resource files?
- Do they change between updates?

### 3. Channel Configuration

**Potential Issue:** Different channels might have different display behaviors

**What to Check:**
- Both channels use `IMPORTANCE_DEFAULT`
- Both have no sound
- Both have `setShowBadge(false)`

## 📝 Next Steps Based on Logs

### If logs show correct icon resources:
→ Problem is likely Android auto-grouping behavior
→ Solution: Add summary notification

### If logs show icon resources changing:
→ Problem in icon selection logic
→ Solution: Fix icon mapping

### If logs show updateNotification() not being called:
→ Problem with broadcast or service lifecycle
→ Solution: Fix broadcast registration or service start

### If only one notification shows but wrong icon:
→ Problem with notification replacement
→ Solution: Ensure proper notification IDs and channels

## 🎯 Testing Checklist

After compiling with logging:

1. **Enable both notifications:**
   - [ ] Check logs for both notifications being created
   - [ ] Check icon resource IDs
   - [ ] Note if icons show correctly initially
   - [ ] Note when/if generic icon appears
   - [ ] Check logs for any Android system messages

2. **Disable Planetary Hour (only Tattva):**
   - [ ] Check logs for ACTION_SETTINGS_CHANGED
   - [ ] Check logs for updateNotification() being called
   - [ ] Check if Planet notification is canceled
   - [ ] Check if Tattva icon appears correctly
   - [ ] Note any delay in icon appearance

3. **Disable Tattva (only Planetary Hour):**
   - [ ] Same checks as above
   - [ ] Check if correct startForeground() call is made

4. **Toggle between states multiple times:**
   - [ ] Check if service is properly updating
   - [ ] Check for any error messages
   - [ ] Check for notification ID conflicts

## 📋 Information to Collect

When reporting findings, include:

1. **Full logcat output** with filter `TattvaNotificationService`
2. **Which Android version** you're testing on
3. **Exact sequence** of actions (e.g., "enabled both → waited 5 seconds → disabled Tattva")
4. **What icons appeared** at each step
5. **Any error messages** in the full logcat (not just our filter)

## 🔬 Advanced Debugging

If basic logging doesn't reveal the issue, we can add:

1. **Notification Manager state logging:**
   - Query active notifications
   - Check which notifications are actually posted
   - Verify notification IDs

2. **Timing information:**
   - Log timestamps for each operation
   - Measure time between operations
   - Identify any delays

3. **Icon verification:**
   - Log the actual drawable resources being used
   - Verify they match expected icons
   - Check if drawables exist in resources

---

**Remember:** The goal of this logging is to identify exactly where and why the behavior differs from expectations. Once we understand the timing and state transitions, we can implement the proper fix.
