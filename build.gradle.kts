// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
        classpath("com.google.gms:google-services:4.4.2")
    }
}