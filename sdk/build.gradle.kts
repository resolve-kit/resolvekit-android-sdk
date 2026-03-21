plugins {
    alias(libs.plugins.android.library)
}

apply(from = rootProject.file("gradle/publish.gradle.kts"))

android {
    namespace = "app.resolvekit.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":ui"))
}
