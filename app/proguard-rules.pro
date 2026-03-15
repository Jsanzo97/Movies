# --- General Android & Kotlin ---
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

# --- Koin ---
# Keep Koin annotations and classes using them
-keep @org.koin.core.annotation.* class * { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}
# Keep constructors for Koin injection to work via reflection/scanning
-keepclassmembers class jsanzo.movies.** {
    public <init>(...);
}

# --- Compose ---
-keep class androidx.compose.runtime.Recomposer { *; }
-keep class androidx.compose.ui.platform.AbstractComposeView { *; }
-keep @androidx.compose.runtime.Composable class * { *; }

# --- Lottie ---
-keep class com.airbnb.lottie.** { *; }

# --- Firebase ---
-keep class com.google.firebase.** { *; }

# --- Preserve line numbers for Crashlytics ---
-keepattributes SourceFile,LineNumberTable
