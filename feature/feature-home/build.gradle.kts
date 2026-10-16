plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.home"
}

dependencies {
    implementation(platform(libs.compose.bom))
}
