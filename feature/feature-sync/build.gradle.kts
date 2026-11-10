plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.sync"
}

dependencies {
    implementation(project(":core:core-sync"))
    implementation(project(":core:core-auth"))
    implementation(project(":core:core-common"))
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
}
