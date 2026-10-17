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

    testImplementation(libs.junit4)
}
