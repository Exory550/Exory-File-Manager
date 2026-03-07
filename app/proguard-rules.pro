# ProGuard configuration for Exory File Manager
# Professional rules for Play Store / F-Droid release

# Keep AndroidX classes
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep Google Material classes
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep Kotlin classes
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
    public static final ** $CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keep class **.R
-keep class **.R$*

# Keep Application class
-keep class com.exory550.filemanager.ExoryApplication { *; }

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keepclassmembers class ** {
    @dagger.hilt.android.internal.lifecycle.HiltViewModelMap <methods>;
    @dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories <methods>;
}
-keep @com.google.dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.HiltAndroidApp class *

# Keep Room classes
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Retrofit classes
-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature,Exceptions

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okio.** { *; }

# Keep Glide classes
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.** { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keepclassmembers class * {
    @com.bumptech.glide.annotation.GlideOption <methods>;
}
-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
-dontwarn com.bumptech.glide.**

# Keep Coil classes
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }
-keep interface kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep Gson
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep ExoPlayer
-keep class com.google.android.exoplayer2.** { *; }
-keep interface com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# Keep PhotoView
-keep class com.github.chrisbanes.photoview.** { *; }
-dontwarn com.github.chrisbanes.photoview.**

# Keep PDF Viewer
-keep class com.github.barteksc.pdfviewer.** { *; }
-dontwarn com.github.barteksc.pdfviewer.**

# Keep Lottie
-keep class com.airbnb.lottie.** { *; }
-keep interface com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Keep Biometric
-keep class androidx.biometric.** { *; }
-keep interface androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Keep Security Crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Keep WorkManager
-keep class androidx.work.** { *; }
-keep interface androidx.work.** { *; }
-dontwarn androidx.work.**
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(...);
}

# Keep Paging
-keep class androidx.paging.** { *; }
-dontwarn androidx.paging.**

# Keep Navigation
-keep class androidx.navigation.** { *; }
-keep interface androidx.navigation.** { *; }
-keepclassmembers class ** {
    @androidx.navigation.NavigationDestination <fields>;
}
-dontwarn androidx.navigation.**

# Keep Lifecycle
-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent <methods>;
}
-keepclassmembers class ** {
    @androidx.lifecycle.OnLifecycleEvent <methods>;
}
-dontwarn androidx.lifecycle.**

# Keep Material Dialogs
-keep class com.afollestad.materialdialogs.** { *; }
-dontwarn com.afollestad.materialdialogs.**

# Keep SimpleStorage
-keep class com.anggrayudi.storage.** { *; }
-dontwarn com.anggrayudi.storage.**

# Keep Apache Commons
-keep class org.apache.commons.** { *; }
-dontwarn org.apache.commons.**

# Keep ThreeTenABP
-keep class org.threeten.bp.** { *; }
-keep class org.threeten.bp.zone.** { *; }
-dontwarn org.threeten.bp.**

# Keep File viewing libraries
-keep class com.davemorrissey.labs.subscaleview.** { *; }
-keep class com.github.chrisbanes.photoview.** { *; }
-dontwarn com.davemorrissey.labs.subscaleview.**
-dontwarn com.github.chrisbanes.photoview.**

# Data models (adjust package names as needed)
-keep class com.exory550.filemanager.data.models.** { *; }
-keep class com.exory550.filemanager.data.entities.** { *; }
-keep class com.exory550.filemanager.data.responses.** { *; }
-keep class com.exory550.filemanager.data.requests.** { *; }
-keep class com.exory550.filemanager.data.local.** { *; }
-keep class com.exory550.filemanager.data.remote.** { *; }

# UI models
-keep class com.exory550.filemanager.ui.models.** { *; }
-keep class com.exory550.filemanager.ui.state.** { *; }

# Utils classes
-keep class com.exory550.filemanager.utils.** { *; }

# Extensions
-keep class com.exory550.filemanager.extensions.** { *; }

# Keep custom views
-keep class com.exory550.filemanager.views.** { *; }
-keepclasseswithmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
    void set*(***);
    *** get*();
}

# Keep custom drawables
-keep class com.exory550.filemanager.drawables.** { *; }

# Remove logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
    public static *** log(...);
}

-assumenosideeffects class kotlin.io.** {
    public static *** println(...);
    public static *** print(...);
}

# Keep attributes
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations

# Keep source file names and line numbers for crash reporting
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# Keep custom exceptions
-keep public class * extends java.lang.Exception
-keep public class * extends java.lang.RuntimeException

# Keep Javascript interfaces for WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep ML Kit (if used)
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Keep Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep Performance Monitoring
-keep class com.google.firebase.perf.** { *; }
-dontwarn com.google.firebase.perf.**

# Keep OS License classes
-keep class com.google.android.gms.oss.licenses.** { *; }

# Ensure JavaScript is preserved for WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve annotations
-keepattributes *Annotation*

# Preserve generic signatures
-keepattributes Signature

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable

# Preserve stack traces
-keepattributes Exceptions

# Remove debug logs in release
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** i(...);
    public static *** v(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
    public static *** log(...);
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** i(...);
    public static *** v(...);
    public static *** w(...);
    public static *** e(...);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep JavaScript interface methods for WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep test classes
-dontwarn junit.**
-dontwarn org.junit.**
-dontwarn org.mockito.**
-dontwarn org.robolectric.**
-dontwarn androidx.test.**
-dontwarn org.assertj.**

# Keep for debug builds only (optional)
# -dontobfuscate
# -dontoptimize
