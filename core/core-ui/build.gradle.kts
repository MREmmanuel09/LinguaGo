plugins {
    id("linguago.android.library.compose")
}

android {
    namespace = "dev.linguago.core.ui"
}

dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.material3)
    api(libs.compose.foundation)
    api(libs.compose.ui.tooling.preview)
    debugApi(libs.compose.ui.tooling)

    implementation(project(":core:core-domain"))

    testImplementation(libs.junit4)
}
