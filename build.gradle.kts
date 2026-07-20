// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    id("com.android.test") version "9.2.1" apply false
    id("androidx.baselineprofile") version "1.5.0-alpha07" apply false
}
