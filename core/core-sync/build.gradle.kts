plugins {
    id("linguago.android.library")
}

android {
    namespace = "dev.linguago.core.sync"
    defaultConfig {
        consumerProguardFiles()
    }
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit4)
}
