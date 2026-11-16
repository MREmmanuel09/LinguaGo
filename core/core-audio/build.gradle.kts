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
    // Runtime TTS/STT offline. sherpa-onnx 2.0.0 (Piper, ADR-003/ADR-057)
    // sin espeak-ng embebido; AAR local para builds reproducibles en este repo.
    implementation(files(rootProject.file("libs/sherpa-onnx-v2.0.0.aar")))
    implementation(libs.vosk.android)

    testImplementation(libs.junit4)
}
