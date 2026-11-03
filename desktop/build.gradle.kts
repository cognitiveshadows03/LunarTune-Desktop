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

// The Windows CI builds x64 and arm64 separately. Each architecture gets its own
// MSI upgrade code so installing one never upgrades/uninstalls the other.
val lunarTuneArch: String =
    providers.gradleProperty("lunarTuneArch").orNull
        ?.takeIf { it == "x64" || it == "arm64" }
        ?: if (System.getProperty("os.arch").orEmpty().lowercase() in setOf("aarch64", "arm64")) "arm64" else "x64"

configurations.configureEach {
    // compose.desktop.currentOs already adds the Skiko runtime jar for the exact
    // OS/arch we build on. The "-all" jar carries every native for every platform
    // (~42 MB of mostly incompressible DLLs) and would be copied into the installer.
    exclude(group = "org.jetbrains.skiko", module = "skiko-awt-runtime-all")
    // Optional lavaplayer modules we never register or use.
    exclude(group = "com.github.walkyst.lavaplayer-fork", module = "lavaplayer-test-samples")
    exclude(group = "com.github.walkyst.lavaplayer-fork", module = "lavaplayer-ext-format-xm")
    exclude(group = "com.github.walkyst.lavaplayer-fork", module = "lavaplayer-stream-merger")
    exclude(group = "com.github.walkyst.lavaplayer-fork", module = "lavaplayer-ext-youtube-rotator")
    // Coil ships a separate GIF decoder we don't need - static artwork only.
    exclude(group = "io.coil-kt.coil3", module = "coil-gif")
}

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

                // Album-art loading. Coil3 is multiplatform; the OkHttp network
                // fetcher reuses the HTTP stack already on the classpath.
                implementation(libs.coil)
                implementation(libs.coil.network.okhttp)

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
            // createDistributable builds the unpacked app image independently of
            // this list; CI zips it as the portable download alongside EXE/MSI.
            targetFormats(TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "LunarTune"
            packageVersion = "5.2.0"
            description = "LunarTune desktop music client"
            vendor = "LunarTune contributors"

            // Curated jlink module list. Compose cannot infer which JDK modules an
            // app needs, and anything missing only shows up at runtime as the
            // launcher's "Failed to launch JVM". The previous build shipped every
            // module (~210 MB uncompressed runtime); this set covers exactly what
            // the app touches — AWT/Swing (java.desktop), HTTP (java.net.http),
            // TLS (jdk.crypto.cryptoki + jdk.crypto.ec — without these, no stream
            // resolves), XML for TTML/QRC (java.xml), the lyrics libraries
            // (java.sql, java.naming), Skiko (jdk.unsupported), and so on — and
            // CI's --launch-check validates the set on every build.
            modules(
                "java.base",
                "java.datatransfer",
                "java.desktop",
                "java.logging",
                "java.management",
                "java.naming",
                "java.net.http",
                "java.prefs",
                "java.scripting",
                "java.security.sasl",
                "java.sql",
                "java.transaction.xa",
                "java.xml",
                "jdk.accessibility",
                "jdk.charsets",
                "jdk.crypto.cryptoki",
                "jdk.crypto.ec",
                "jdk.unsupported",
            )

            // Runtime tuning only - no behaviour change. String deduplication trims
            // heap pressure from the many short-lived strings the lyrics parser and
            // InnerTube JSON decoder produce; capping the heap lets the launcher sit
            // comfortably alongside other desktop apps.
            jvmArgs(
                "-XX:+UseG1GC",
                "-XX:MaxRAMPercentage=45",
                "-XX:+UseStringDeduplication",
                "-Dawt.useSystemAAFontSettings=on",
                "-Dswing.aatext=true",
            )

            windows {
                upgradeUuid =
                    if (lunarTuneArch == "arm64") {
                        "B2E6D3F1-7E4C-4F8B-AD29-3C6E9A1B5D72"
                    } else {
                        "A1F5C2E0-6D3B-4E7A-9C18-2B5D8F0A4C61"
                    }
                perUserInstall = true
                shortcut = true
                menu = true
            }
        }
    }
}
