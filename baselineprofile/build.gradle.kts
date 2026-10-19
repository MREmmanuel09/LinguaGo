import org.gradle.api.JavaVersion

plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
    id("linguago.quality")
}

android {
    namespace = "dev.linguago.baselineprofile"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Resultados preliminares en emulador; la validación final exige ARM físico.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        animationsDisabled = true
    }

    // El perfil se genera solo con los assets de voz de `en`; los demás
    // flavors comparten código y usan el mismo baseline-prof.txt.
    flavorDimensions += "language"
    productFlavors {
        create("en") { dimension = "language" }
    }
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.junit4)
    implementation(libs.androidx.test.core)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
