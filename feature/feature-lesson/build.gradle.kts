plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.lesson"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))
    implementation(platform(libs.compose.bom))
}
