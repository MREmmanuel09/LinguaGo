plugins {
    id("linguago.android.library")
    id("linguago.hilt")
}

android {
    namespace = "dev.linguago.core.sync"
    defaultConfig {
        consumerProguardFiles()
    }
}

dependencies {
    // El API de core-sync expone tipos de dominio (Result<StreakState> del
    // SyncEngine): api para visibilidad en features.
    api(project(":core:core-domain"))
    implementation(project(":core:core-common"))
    implementation(libs.androidx.work.runtime.ktx)
    // Codec del snapshot v1 (ADR-013); misma lib que el contenido (ADR-009).
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
