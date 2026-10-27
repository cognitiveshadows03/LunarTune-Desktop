plugins {
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

// Keep IDEA's Kotlin build-script model happy on every subproject.
if (tasks.findByName("prepareKotlinBuildScriptModel") == null) {
    tasks.register("prepareKotlinBuildScriptModel") {}
}
subprojects {
    if (tasks.findByName("prepareKotlinBuildScriptModel") == null) {
        tasks.register("prepareKotlinBuildScriptModel") {}
    }
}
