# Reguli pentru SUN Tattva

# Keep Swiss Ephemeris
-keep class swisseph.** { *; }
-dontwarn swisseph.**

# Keep ViewModels (important for AndroidViewModel instantiation)
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# Keep Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep data classes (pentru serializare)
-keepclassmembers class com.android.sun.data.** {
    <fields>;
    <init>(...);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Optimizări generale
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Keep line numbers pentru debugging crash-uri
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile