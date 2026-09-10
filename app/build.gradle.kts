import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.github.anders81fin.nullplate"
    compileSdk = 36
    defaultConfig {
        applicationId = "io.github.anders81fin.nullplate"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }

    // Credentials live outside the repository. Without them the release build
    // still succeeds, unsigned — which is what F-Droid wants, since it builds
    // from source and signs with its own key.
    val keystoreProperties = Properties()
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val hasKeystore = keystorePropertiesFile.exists()
    if (hasKeystore) {
        FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
    }

    signingConfigs {
        create("release") {
            if (hasKeystore) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = if (hasKeystore) signingConfigs.getByName("release") else null
            isMinifyEnabled = false
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
}

// A jvmTarget, not a jvmToolchain: only the bytecode level matters, and
// pinning a toolchain would demand a JDK 17 that the F-Droid build server does
// not have and is not allowed to download.
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore)
  implementation(libs.androidx.work.runtime.ktx)
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

}
