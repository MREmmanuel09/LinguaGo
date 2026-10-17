plugins {
    id("linguago.android.application")
    id("linguago.hilt")
}

android {
    namespace = "dev.linguago.app"
    defaultConfig {
        applicationId = "dev.linguago.app"
        versionCode = 1
        versionName = "0.1.0-alpha"
    }
    sourceSets {
        getByName("main") {
            // Voz tts/… y manifest.json del tool de descarga (ADR-006).
            assets.srcDir(rootProject.file("models"))
        }
    }
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-content"))
    implementation(project(":core:core-audio"))
    implementation(project(":core:core-sync"))

    implementation(project(":feature:feature-onboarding"))
    implementation(project(":feature:feature-auth"))
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-lesson"))
    implementation(project(":feature:feature-profile"))
    implementation(project(":feature:feature-sync"))
    implementation(project(":feature:feature-settings"))

    implementation(project(":content:content-es-en"))
    implementation(project(":content:content-es-pt"))
    implementation(project(":content:content-es-fr"))
    implementation(project(":content:content-es-ja"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit4)
}
