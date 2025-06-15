import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.vkid.placeholders)
}

//repositories{
//    mavenCentral()
//    maven(url = "http://maven.google.com/")
//}

vkidManifestPlaceholders {
    init(
        clientId = "${properties["VKIDClientID"]}",
        clientSecret = "${properties["VKIDClientSecret"]}"
    )
    vkidRedirectHost = "${properties["VKIDRedirectHost"]}"
    vkidRedirectScheme = "${properties["VKIDRedirectScheme"]}"
}