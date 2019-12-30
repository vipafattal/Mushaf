# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line ayaNumber information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line ayaNumber information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep public class co.jp.smagroup.musahaf.framework.api.** { *; }

-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

-dontwarn retrofit2.**
-keep class javax.annotation.Nullable
-keep interface javax.annotation.Nullable
-keep class retrofit2.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
  @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
  @retrofit2.http.* <methods>;
}

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

#Kotlin serilaztion
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class co.jp.smagroup.musahaf.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class co.jp.smagroup.musahaf.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class co.jp.smagroup.musahaf.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {*;}

-keepclassmembers class co.jp.smagroup.musahaf.model.** { *; }
-keepclassmembers class co.jp.smagroup.musahaf.framework.api.** { *; }


