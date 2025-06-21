import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.vkid.placeholders)
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.0") // Adjust version as needed
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0") // Adjust Kotlin version as needed
    }
}

vkidManifestPlaceholders {
    init(
        clientId = "${properties["VKIDClientID"]}",
        clientSecret = "${properties["VKIDClientSecret"]}"
    )
    vkidRedirectHost = "${properties["VKIDRedirectHost"]}"
    vkidRedirectScheme = "${properties["VKIDRedirectScheme"]}"
}