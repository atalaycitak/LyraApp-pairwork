# ── Kotlin / Coroutines ──
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── Gson (Retrofit converter) ──
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.turkcell.lyraapp.data.** { *; }
-keepclassmembers class com.turkcell.lyraapp.data.** {
    <fields>;
}
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── Retrofit ──
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ── OkHttp ──
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Room ──
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { <fields>; }
-dontwarn androidx.room.paging.**

# ── Hilt / Dagger ──
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── Media3 / ExoPlayer ──
-dontwarn androidx.media3.**

# ── EncryptedSharedPreferences ──
-keep class androidx.security.crypto.** { *; }

# ── Stack trace readability ──
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
