import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlinx.parcelize)
    alias(libs.plugins.kotlinx.kover)
}
val secretProperties = Properties()
secretProperties.load(FileInputStream(rootProject.file("secrets.properties")))
android {
    namespace = "com.pay2.exhangeapp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.pay2.exhangeapp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "BASE_URL",
                "\"${secretProperties["BASE_URL_DEBUG"]}\""
            )
            buildConfigField(
                "String",
                "OPEN_EXCHANGE_APP_ID",
                "\"${secretProperties["OPEN_EXCHANGE_APP_ID"]}\""
            )

        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", secretProperties["BASE_URL"].toString())
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "String",
                "BASE_URL",
                "\"${secretProperties["BASE_URL"]}\""
            )
            buildConfigField(
                "String",
                "OPEN_EXCHANGE_APP_ID",
                "\"${secretProperties["OPEN_EXCHANGE_APP_ID"]}\""
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
    kapt {
        correctErrorTypes = true
    }
}

dependencies {

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ktx)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.room)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.collection)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.paging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter)
    implementation(platform(libs.compose.bom))
    implementation(platform(libs.okhttp.bom))
    kapt(libs.hilt.compiler)
    kapt(libs.room.compiler)
    testImplementation(libs.androidx.test)
    testImplementation(libs.core.test)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.compose.test.junit)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(platform(libs.compose.bom))
    debugImplementation(libs.compose.test.manifest)
    debugImplementation(libs.compose.tooling)
}