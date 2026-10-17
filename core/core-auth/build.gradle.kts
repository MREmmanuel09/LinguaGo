plugins {
    id("linguago.android.library")
    id("linguago.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.linguago.core.auth"
    defaultConfig {
        consumerProguardFiles()
    }
}

dependencies {
    // La API de auth expone tipos del dominio (AuthRepository produce
    // AuthState/SyncSnapshot vía CloudProvider): api para visibilidad.
    api(project(":core:core-domain"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-sync"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
