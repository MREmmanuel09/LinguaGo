plugins {
    id("linguago.android.library")
    id("linguago.hilt")
}

android {
    namespace = "dev.linguago.core.data"
    defaultConfig {
        consumerProguardFiles()
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))

    api(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
