plugins {
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    group = providers.gradleProperty("GROUP").orNull ?: "app.resolvekit"
    version = providers.gradleProperty("VERSION_NAME").orNull ?: "1.0.0"
}
