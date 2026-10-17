plugins {
    id("linguago.android.library")
    id("linguago.hilt")
}

android {
    namespace = "dev.linguago.core.content"
    defaultConfig {
        consumerProguardFiles()
    }
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))
    api(libs.kotlinx.serialization.json)

    testImplementation(libs.junit4)
}
