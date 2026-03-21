plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

apply(from = rootProject.file("gradle/publish.gradle.kts"))

dependencies {
    implementation(libs.ksp.api)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
