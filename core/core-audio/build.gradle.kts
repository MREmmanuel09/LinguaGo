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
    // Runtime TTS/STT offline. sherpa-onnx (Piper, ADR-003) ~47 MB;
    // vosk-android (STT, Fase 2.3) ~26 MB + jna.
    implementation(libs.sherpa.onnx)
    implementation(libs.vosk.android)

    testImplementation(libs.junit4)
}
