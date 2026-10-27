import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

group = "dev.citali.lunartune"
version = "5.2.0-desktop-alpha"

kotlin {
    jvm("desktop") {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    sourceSets {
        // Explicit coordinates: the shorthand compose.* accessors are deprecated
        // with ERROR level in recent Compose Multiplatform releases.
        val commonMain by getting {
            dependencies {
                implementation(libs.cmp.runtime)
                implementation(libs.cmp.foundation)
                implementation(libs.cmp.material3)
                implementation(libs.cmp.icons)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.lavaplayer.fork)
                runtimeOnly(libs.slf4j.nop)

                // Exactly the same pure-JVM libraries the Android app uses: the
                // InnerTube/YouTube client and every lyrics provider.
                implementation(project(":core"))
                implementation(project(":morideobfuscator"))
                implementation(project(":lyrics:betterlyrics"))
                implementation(project(":lyrics:kugou"))
                implementation(project(":lyrics:lrclib"))
                implementation(project(":lyrics:simpmusic"))
                implementation(project(":lyrics:paxsenix"))
                implementation(project(":lyrics:unison"))
                implementation(project(":lyrics:youlyplus"))
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.citali.lunartune.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "LunarTune"
            packageVersion = "5.2.0"
            description = "LunarTune desktop music client"
            vendor = "LunarTune contributors"
        }
    }
}
