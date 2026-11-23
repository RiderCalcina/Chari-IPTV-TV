plugins {
  alias(libs.plugins.android.application)
  id("org.jetbrains.kotlin.android")
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.ksp)
  id("com.google.dagger.hilt.android") version "2.51.1"
}

android {
    namespace = "com.example.chariiptv"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.qwertyaserty.chariiptv"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.2"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = project.rootProject.file("release.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
                keyAlias = System.getenv("KEY_ALIAS") ?: "chariiptv"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            val releaseKeystore = project.rootProject.file("release.keystore")
            signingConfig = if (releaseKeystore.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            if (variant.buildType.name == "release") {
                output?.outputFileName = "Chari-IPTV_1.2.apk"
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Custom TVDROID Dependencies
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")
  val media3Version = "1.9.0"
  implementation("androidx.media3:media3-exoplayer:$media3Version")
  implementation("androidx.media3:media3-exoplayer-hls:$media3Version")
  implementation("androidx.media3:media3-exoplayer-dash:$media3Version")
  implementation("androidx.media3:media3-datasource-okhttp:$media3Version")
  implementation("androidx.media3:media3-ui:$media3Version")
  implementation("org.jellyfin.media3:media3-ffmpeg-decoder:1.9.0+1")
  implementation("io.coil-kt:coil-compose:2.6.0")
  implementation("io.coil-kt:coil-svg:2.6.0")

  // TV Compose
  implementation("androidx.tv:tv-foundation:1.0.0-alpha11")
  implementation("androidx.tv:tv-material:1.0.0")
  implementation("androidx.tvprovider:tvprovider:1.0.0")

  // Room
  val room_version = "2.6.1"
  implementation("androidx.room:room-runtime:$room_version")
  implementation("androidx.room:room-ktx:$room_version")
  ksp("androidx.room:room-compiler:$room_version")

  // Hilt
  implementation("com.google.dagger:hilt-android:2.51.1")
  ksp("com.google.dagger:hilt-compiler:2.51.1")
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
}
