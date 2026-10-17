plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.auth"
}

dependencies {
    implementation(project(":core:core-auth"))
    implementation(project(":core:core-common"))
    implementation(platform(libs.compose.bom))
}
