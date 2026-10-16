plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.profile"
}

dependencies {
    implementation(platform(libs.compose.bom))
}
