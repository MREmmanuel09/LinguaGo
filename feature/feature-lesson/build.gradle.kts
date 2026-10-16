plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.lesson"
}

dependencies {
    implementation(platform(libs.compose.bom))
}
