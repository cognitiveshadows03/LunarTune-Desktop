@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        exclusiveContent {
            forRepository {
                maven {
                    name = "JitPack"
                    setUrl("https://jitpack.io")
                }
            }
            filter {
                includeGroup("com.github.therealbush")
                includeGroup("com.github.TeamNewPipe")
                includeGroup("com.github.walkyst")
            }
        }
    }
}

rootProject.name = "LunarTune"

// Desktop-focused build. Only the Compose Multiplatform app and the pure-JVM
// libraries it shares with the Android app are configured here; the Android app
// sources kept in this repository are reference material and deliberately stay
// out of the Gradle build so a desktop compile needs no Android SDK.
include(":desktop")
include(":core")
include(":morideobfuscator")
include(":lyrics:kugou")
include(":lyrics:lrclib")
include(":lyrics:simpmusic")
include(":lyrics:paxsenix")
include(":lyrics:betterlyrics")
include(":lyrics:unison")
include(":lyrics:youlyplus")
