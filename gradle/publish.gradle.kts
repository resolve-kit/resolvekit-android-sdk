import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension
import java.io.File
import java.util.Base64

apply(plugin = "maven-publish")
apply(plugin = "signing")

fun Project.loadDotEnv(): Map<String, String> {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return emptyMap()

    return envFile.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
        .associate { line ->
            val index = line.indexOf('=')
            val key = line.substring(0, index).trim()
            val rawValue = line.substring(index + 1).trim()
            val value = rawValue
                .removeSurrounding("\"")
                .removeSurrounding("'")
            key to value
        }
}

val dotEnv = loadDotEnv()

fun Project.requiredProperty(name: String): String =
    providers.gradleProperty(name).orNull
        ?: error("Missing Gradle property: $name")

fun Project.optionalSecret(vararg names: String): String? {
    for (name in names) {
        providers.gradleProperty(name).orNull?.let { return it }
        System.getenv(name)?.let { return it }
        dotEnv[name]?.let { return it }
    }
    return null
}

group = requiredProperty("GROUP")
version = requiredProperty("VERSION_NAME")

plugins.withId("org.jetbrains.kotlin.jvm") {
    extensions.configure(JavaPluginExtension::class.java) {
        withSourcesJar()
        withJavadocJar()
    }
}

val androidJavadocJar = tasks.register("androidJavadocJar", Jar::class.java) {
    archiveClassifier.set("javadoc")
    description = "Assembles an empty javadoc jar for Android publications."
}

afterEvaluate {
    val component = components.findByName("release") ?: components.findByName("java") ?: return@afterEvaluate

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                from(component)
                artifactId = project.name

                if (component.name == "release") {
                    artifact(androidJavadocJar)
                }

                pom {
                    name.set("${requiredProperty("POM_NAME")} - ${project.name}")
                    description.set(requiredProperty("POM_DESCRIPTION"))
                    url.set(requiredProperty("POM_URL"))

                    licenses {
                        license {
                            name.set(requiredProperty("POM_LICENSE_NAME"))
                            url.set(requiredProperty("POM_LICENSE_URL"))
                        }
                    }

                    developers {
                        developer {
                            id.set(requiredProperty("POM_DEVELOPER_ID"))
                            name.set(requiredProperty("POM_DEVELOPER_NAME"))
                            email.set(requiredProperty("POM_DEVELOPER_EMAIL"))
                        }
                    }

                    scm {
                        connection.set(requiredProperty("POM_SCM_CONNECTION"))
                        developerConnection.set(requiredProperty("POM_SCM_DEV_CONNECTION"))
                        url.set(requiredProperty("POM_SCM_URL"))
                    }
                }
            }
        }

        repositories {
            maven {
                name = "MavenCentral"
                url = uri(
                    if (version.toString().endsWith("SNAPSHOT"))
                        requiredProperty("SONATYPE_SNAPSHOT_URL")
                    else
                        requiredProperty("SONATYPE_RELEASE_URL")
                )
                credentials {
                    username = optionalSecret("MAVEN_CENTRAL_USERNAME", "OSSRH_USERNAME")
                    password = optionalSecret("MAVEN_CENTRAL_PASSWORD", "OSSRH_PASSWORD")
                }
            }

            maven {
                name = "GitHubPackages"
                url = uri(requiredProperty("GITHUB_PACKAGES_URL"))
                credentials {
                    username = optionalSecret("GITHUB_PACKAGES_USERNAME", "GITHUB_ACTOR")
                    password = optionalSecret("GITHUB_PACKAGES_PASSWORD", "GITHUB_TOKEN")
                }
            }
        }
    }

    extensions.configure<SigningExtension> {
        val signingKey = optionalSecret("SIGNING_KEY_BASE64")?.let { encoded ->
            String(Base64.getDecoder().decode(encoded))
        } ?: optionalSecret("SIGNING_KEY")
        val signingPassword = optionalSecret("SIGNING_PASSWORD")
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            val publishing = extensions.getByType<PublishingExtension>()
            sign(publishing.publications)
        }
    }
}
