plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.auth"
}

dependencies {
    implementation(platform(libs.compose.bom))
}
