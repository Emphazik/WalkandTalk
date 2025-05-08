plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.vkid.placeholders)
}

android {
    namespace = "ru.walkAndTalk"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.walkAndTalk"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "SUPABASE_URL", "\"${properties["supabaseUrl"]}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${properties["supabaseKey"]}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        addManifestPlaceholders(mapOf(
            "VKIDRedirectHost" to "${properties["VKIDRedirectHost"]}",
            "VKIDRedirectScheme" to "${properties["VKIDRedirectScheme"]}",
            "VKIDClientID" to "${properties["VKIDClientID"]}",
            "VKIDClientSecret" to "${properties["VKIDClientSecret"]}"
        ))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "${JavaVersion.VERSION_11}"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "2.1.0"
    }
}

dependencies {
    /** Core **/
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.work.manager)
    /** Compose **/
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.lottie.compose)
    //implementation("com.google.accompanist:accompanist-navigation-animation:0.31.2-alpha")
    /** Coroutines **/
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    /** Network **/
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    /** Coil **/
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.compose)
    /** DI **/
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.workmanager)
    /** Room **/
    implementation(libs.room)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)
    ksp(libs.room.compiler)
    /** ORBIT MVI **/
    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    /** Supabase **/
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.postgrest)
    /** Test **/
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    /** VK TEST **/
    implementation(libs.android.sdk.core)
    implementation(libs.android.sdk.api)
    implementation(libs.android.sdk.support)
    implementation(libs.onetap.compose)
    /** Desugaring for VK **/
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}