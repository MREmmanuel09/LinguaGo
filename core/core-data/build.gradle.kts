plugins {
    id("linguago.android.library")
}

android {
    namespace = "dev.linguago.core.data"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))

    api(libs.androidx.datastore.preferences)
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit4)
}
