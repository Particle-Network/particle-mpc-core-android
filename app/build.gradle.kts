plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.particle.sample"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.particle_mpc_core_android_demo"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["PN_PROJECT_ID"] = "your project id"
        manifestPlaceholders["PN_PROJECT_CLIENT_KEY"] = "your client key"
        manifestPlaceholders["PN_APP_ID"] = "your app id"

        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    val sdkVersion = "1.0.0"
    modules {
        module("org.bouncycastle:bcprov-jdk15to18") {
            replacedBy("org.bouncycastle:bcprov-jdk15on")
        }
    }
    implementation("network.particle:auth-core:$sdkVersion")
    implementation("network.particle:mpc-core:$sdkVersion")
    implementation("network.particle:api-service:$sdkVersion")

    implementation("com.blankj:utilcodex:1.31.0")
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha03")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.google.android.material:material:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}