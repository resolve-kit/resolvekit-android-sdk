# ResolveKit Consumer ProGuard Rules
# Add these to your app's proguard-rules.pro

# ResolveKit
-keep class app.resolvekit.** { *; }
-keep class app.resolvekit.core.** { *; }
-keep class app.resolvekit.ui.** { *; }
-keep class app.resolvekit.networking.** { *; }

# KSP-generated adapters
-keep class **ResolveKitAdapter { *; }
-keep class **_ResolveKitAdapter { *; }

# JSON serialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep function names for tool dispatch
-keepnames class app.resolvekit.authoring.** { *; }
