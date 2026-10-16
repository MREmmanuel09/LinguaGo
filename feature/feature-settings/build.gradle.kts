plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.settings"
}

dependencies {
    implementation(platform(libs.compose.bom))
}
