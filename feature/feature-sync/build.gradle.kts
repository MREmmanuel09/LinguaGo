plugins {
    id("linguago.android.feature")
}

android {
    namespace = "dev.linguago.feature.sync"
}

dependencies {
    implementation(platform(libs.compose.bom))
}
