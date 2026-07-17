import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val keystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
val keystorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("ANDROID_KEY_ALIAS")
val releaseKeyPassword = System.getenv("ANDROID_KEY_PASSWORD")
val hasReleaseSigning = !keystorePath.isNullOrBlank() &&
    !keystorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank() &&
    keystorePath?.let { File(it).isFile } == true

android {
    namespace = "com.quenan.duji"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.quenan.duji"
        minSdk = 33
        targetSdk = 36
        versionCode = 18
        versionName = "0.10.13"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(requireNotNull(keystorePath))
                storePassword = requireNotNull(keystorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.lunar)
    debugImplementation(libs.compose.ui.tooling)

    // Miuix —— 全量导入
    implementation(libs.miuix.ui)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.blur)
    implementation(libs.miuix.squircle)
    implementation(libs.miuix.navigation3)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
