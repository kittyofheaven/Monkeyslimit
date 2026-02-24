# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# 1. Preserve Generic Signatures & Annotations (Crucial for ParameterizedType errors)
# Added RuntimeVisibleParameterAnnotations which is required for Retrofit suspend functions
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault, EnclosingMethod, InnerClasses

# 2. Keep the entire data packages (Room entities & API responses)
-keep class com.menac1ngmonkeys.monkeyslimit.data.local.entity.** { *; }
-keep class com.menac1ngmonkeys.monkeyslimit.data.remote.response.** { *; }

# 3. Protect your API Service Interface
-keep interface com.menac1ngmonkeys.monkeyslimit.data.remote.** { *; }
# Explicitly keep methods with Retrofit annotations to prevent signature stripping
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# 4. Standard Retrofit & OkHttp Protections
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# 5. Prevent GSON from scrambling field names used in JSON
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 6. Kotlin Coroutines specific rules
-keep class kotlin.coroutines.Continuation

# 7. Strip debug/verbose/info log calls in release builds
# Only Log.w and Log.e are preserved for production diagnostics
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}

# 8. TLS/SSL Security Providers (Fixes SSL handshake errors on all devices)
# Conscrypt — Primary TLS provider used by OkHttp on most devices
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# BouncyCastle — Fallback security provider (used by some OEMs like Huawei)
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# OpenJSSE — Alternative Java SSL engine
-keep class org.openjsse.** { *; }
-dontwarn org.openjsse.**

# 9. OkHttp Internal TLS Classes (Prevents R8 from stripping SSL negotiation)
-keep class okhttp3.internal.platform.** { *; }
-keep class okhttp3.internal.tls.** { *; }
-dontwarn okhttp3.internal.platform.**

# 10. Java Security — Prevent stripping of core SSL/TLS infrastructure
-keep class javax.net.ssl.** { *; }
-keep class java.security.** { *; }
-keep class sun.security.ssl.** { *; }
-dontwarn sun.security.ssl.**
