// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("vkid.manifest.placeholders") version "1.1.0" apply true
}

vkidManifestPlaceholders {
    init(
        clientId = "53306543",
        clientSecret = properties["vkIdClientSecret"] as String
    )
    vkidRedirectHost = "vk.com"
    vkidRedirectScheme = "vk53306543"
}