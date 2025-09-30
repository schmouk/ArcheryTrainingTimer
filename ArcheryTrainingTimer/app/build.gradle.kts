/*
MIT License

Copyright (c) 2025 Philippe Schmouker

This file is part of Android application ArcheryTrainingTimer.

Permission is hereby granted,  free of charge,  to any person obtaining a copy
of this software and associated documentation files (the "Software"),  to deal
in the Software without restriction,  including without limitation the  rights
to use,  copy,  modify,  merge,  publish,  distribute, sublicense, and/or sell
copies of the Software,  and  to  permit  persons  to  whom  the  Software  is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS",  WITHOUT WARRANTY OF ANY  KIND,  EXPRESS  OR
IMPLIED,  INCLUDING  BUT  NOT  LIMITED  TO  THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT  SHALL  THE
AUTHORS  OR  COPYRIGHT  HOLDERS  BE  LIABLE  FOR  ANY CLAIM,  DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM,
OUT  OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.util.Properties

// Plugins block is usually essential
plugins {
    id("com.android.application")
    id("base")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") // OR alias(libs.plugins.kotlin.compose) if using version catalog
    //id("com.android.application") version "8.13.0" apply false
    //id("com.android.library") version "8.13.0" apply false
    //id("org.jetbrains.kotlin.android") version "2.1.20" apply false
}

// Set archivesBaseName
//base.archivesBaseName = "ArcheryTrainingTimer"  // <-----<<< Notice: deprecated!
base.archivesName = "ArcheryTrainingTimer"

// Load properties from keystore.properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists() && keystorePropertiesFile.isFile) {
    try {
        keystorePropertiesFile.inputStream().use { input ->
            keystoreProperties.load(input)
        }
        println("INFO: Keystore properties loaded from ${keystorePropertiesFile.absolutePath}")
    } catch (e: Exception) {
        println("ERROR: Failed to load keystore.properties: ${e.message}")
        // Optionally throw an error if signing is mandatory for this configuration phase
        //throw GradleException("Failed to load keystore.properties", e)
    }
} else {
    println("WARNING: keystore.properties file not found at ${keystorePropertiesFile.absolutePath}. Release signing will be unconfigured.")
}

// Minimal Android block
android {
    namespace = "com.github.schmouk.archerytrainingtimer"
    compileSdk = 36 // Or the current compileSdk

    defaultConfig {
        applicationId = namespace  // i.e. "com.github.schmouk.archerytrainingtimer"
        minSdk = 24 // Or our current minSdk
        targetSdk = 36 // Or our current targetSdk
        versionCode = 10 // To be incremented with each release
        // Using a property for versionName is common, but we can also hardcode it
        // If we want to use a property, we can define it in gradle.properties or
        // pass it as a command line argument, e.g., -PversionName=0.1.0
        // Here, we use a hardcoded value for simplicity, but we can replace it with a property if needed.
        // versionName = project.findProperty("versionName")?.toString() ?: "0.1.0"
        versionName = "0.2.2a" // REPLACE with the actual version or use a property as commented above
    }

    signingConfigs {
        create("release") {
            // Only configure signing if all properties are available
            if (keystoreProperties.containsKey("storeFile") &&
                keystoreProperties.containsKey("storePassword") &&
                keystoreProperties.containsKey("keyAlias") &&
                keystoreProperties.containsKey("keyPassword")) {

                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            } else {
                println("WARNING: Release signing configuration is incomplete due to missing properties in keystore.properties.")
                // You might want to make this an error if release signing is critical
                // For CI/CD environments, you might use environment variables instead of keystore.properties
            }
        }
        // We can also define a debug signingConfig if needed, but usually not necessary
        // as Android Studio uses a default debug keystore automatically.
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        getByName("debug") {
            // Debug builds are usually signed with a default debug keystore
            // No explicit signingConfig needed unless you have specific needs
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // Ensure we have a valid Compose Compiler version.
        // If using BOM, this is often managed by it.
        // If not using BOM or using a version catalog, it might look like:
        kotlinCompilerExtensionVersion = "1.5.3" // REPLACE with the actual/compatible version or libs.versions.compose.compiler.get()
    }
    packaging { // Added from your original, good for excluding duplicate metadata
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


// --- Configure APK Naming with Version ---
// This uses the modern AndroidComponentsExtension API
// Ensure our AGP version supports this (AGP 7.0+ is typical)
androidComponents {
    onVariants { variant -> // 'variant' here is an instance of com.android.build.api.variant.Variant
        variant.outputs.forEach { output ->
            val baseName = project.property("archivesBaseName").toString()
            val version = android.defaultConfig.versionName ?: "" // Using the Elvis operator for null safety
            val variantName = variant.name // i.e., "debug", "release"

            // Ensure outputFileName is settable on the specific output type
            // The type of 'output' can vary. For APKs, it's often related to ApkVariantOutput.
            // Let's try to find a common settable property.
            // In many AGP versions, variant.outputs are of type com.android.build.api.variant.VariantOutput
            // which has a property for outputFileName (or similar) on its concrete implementations.
            // This is a common pattern for AGP 7+
            (output as? com.android.build.api.variant.impl.VariantOutputImpl)?.outputFileName?.set(
                if (version == "") "$baseName-$variantName.apk" else "$baseName-v$version-$variantName.apk"
            )
            // If the above cast fails or outputFileName is not settable,
            // it means the specific AGP version has a slightly different API structure.
            // The exact type of 'output' and how to set its name can be version-dependent.
        }
    }
}

// Minimal dependencies block
dependencies {
    implementation("androidx.activity:activity-compose:1.10.1") // Or the latest stable version

    implementation("androidx.core:core-ktx:1.16.0") // Example minimal dependency

    implementation("androidx.compose.material:material-icons-core:1.7.8") // Or the version from your BOM
    implementation("androidx.compose.material:material-icons-extended:1.7.8") // Or the version from your BOM

    // Compose Bill of Materials (BOM) - Recommended
    // The BOM ensures that versions of different Compose libraries are compatible.
    //implementation(platform("androidx.compose:compose-bom:2024.05.00")) // REPLACE with latest BOM version
    //implementation("androidx.compose.material3:material3:1.2.1")
    //implementation("androidx.compose.material3.adaptive:adaptive:1.0.0-beta01")

    // Essential Compose UI libraries (versions managed by BOM if used)
    implementation("androidx.compose.material3:material3:1.3.2")  // 1.4.0-beta03 and 1.5.0-alpha03 are available also
    //implementation("androidx.compose.material3.adaptive:adaptive:1.1.0") // Or the latest version shown in the docs (e.g., 1.2.0-beta01 if that's current)
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.1.0")  // 1.2.0-beta01 is available also
    //implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.1.0")  // 1.2.0-beta01 is available also
    implementation("androidx.compose.ui:ui:1.9.1")
    //implementation("androidx.compose.ui:ui-graphics:1.9.1")
    //implementation("androidx.compose.ui:ui-unit:1.9.1")


    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    /*
    val lifecycleVersion = "2.9.3"
    val archVersion = "2.2.0"

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Lifecycles only (without ViewModel or LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    // Lifecycle utilities for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")

    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")

    // ViewModel integration with Navigation3
    //implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0-alpha04")

    // Annotation processor
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    // optional - helpers for implementing LifecycleOwner in a Service
    implementation("androidx.lifecycle:lifecycle-service:$lifecycleVersion")
    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    // optional - ReactiveStreams support for LiveData
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycleVersion")
    // optional - Test helpers for LiveData
    testImplementation("androidx.arch.core:core-testing:$archVersion")
    // optional - Test helpers for Lifecycle runtime
    testImplementation ("androidx.lifecycle:lifecycle-runtime-testing:$lifecycleVersion")
    */

    //-- Tooling for Previews (optional but very helpful)
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.ui.unit)
    debugImplementation("androidx.compose.ui:ui-tooling") // For tools like Layout Inspector

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.7") // Or the latest stable version

    // Lifecycle KTX (often useful with Compose)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2") // Or our version

    implementation("com.google.accompanist:accompanist-flowlayout:0.36.0")
}
