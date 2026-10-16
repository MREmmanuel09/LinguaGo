plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.onboarding"
}

dependencies {
    implementation(platform(libs.compose.bom))
}
