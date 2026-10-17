plugins {
    id("linguago.android.library")
    id("linguago.hilt")
}

android {
    namespace = "dev.linguago.core.audio"
    defaultConfig {
        consumerProguardFiles()
    }
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))
    implementation(libs.kotlinx.coroutines.android)
    // Runtime TTS/STT offline (Piper VITS, ADR-003). AAR ~47 MB.
    implementation(libs.sherpa.onnx)

    testImplementation(libs.junit4)
}
