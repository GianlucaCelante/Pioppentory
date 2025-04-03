import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "it.pioppi"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.pioppi"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }

        packaging {
            resources {
                excludes.add("META-INF/INDEX.LIST")
                excludes.add("META-INF/DEPENDENCIES")
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

}



dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation(libs.androidx.room.guava)

    // optional - Test helpers
    testImplementation(libs.androidx.room.testing)

    // optional - Paging 3 Integration
    implementation(libs.androidx.room.paging)
    implementation (libs.material)
    implementation (libs.koin.android)
    implementation(libs.androidx.recyclerview)
    // For control over item selection of both touch and mouse driven selection
    implementation(libs.androidx.recyclerview.selection)

    // Java language implementation
    implementation (libs.androidx.navigation.fragment)
    implementation (libs.androidx.navigation.ui)


    // Feature module Support
    implementation (libs.androidx.navigation.dynamic.features.fragment)

    // Testing Navigation
    androidTestImplementation (libs.androidx.navigation.testing)

    // Jetpack Compose Integration
    implementation (libs.androidx.navigation.compose)
    implementation (libs.android.logging.log4j)

    // Required -- JUnit 4 framework
    testImplementation (libs.junit)
    // Optional -- Mockito framework
    testImplementation (libs.mockito.core)
    // Optional -- Mockk framework
    testImplementation (libs.mockk)
    implementation (libs.asset.delivery)

    implementation (libs.glide)

    // Google Drive integration
    implementation(libs.play.services.auth)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)

}