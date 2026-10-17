plugins {
    id("linguago.android.application")
    id("linguago.hilt")
}

// Configuración de distribución (ADR-011): ninguna credencial vive en el
// repo. Todo se pasa por gradle properties / local.properties.
val googleClientId = providers.gradleProperty("googleClientId").orElse("").get()
val oauthRedirectScheme =
    providers.gradleProperty("oauthRedirectScheme").orElse("dev.linguago.oauth").get()
val oauthRedirectUri = providers.gradleProperty("oauthRedirectUri").orElse("").get()
val releaseStoreFile = providers.gradleProperty("linguago.storeFile").orNull

android {
    namespace = "dev.linguago.app"
    defaultConfig {
        applicationId = "dev.linguago.app"
        versionCode = 1
        versionName = "0.1.0-alpha"

        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
        buildConfigField("String", "OAUTH_REDIRECT_SCHEME", "\"$oauthRedirectScheme\"")
        buildConfigField("String", "OAUTH_REDIRECT_URI", "\"$oauthRedirectUri\"")
        manifestPlaceholders["oauthRedirectScheme"] = oauthRedirectScheme
    }
    signingConfigs {
        // Firma de distribución: solo si el keystore se provee por properties.
        // Sin keystore, el build release se firma con la debug key (para
        // smoke tests locales en emulador); NUNCA distribuir ese APK.
        create("release") {
            if (!releaseStoreFile.isNullOrBlank()) {
                storeFile = file(releaseStoreFile)
                storePassword = providers.gradleProperty("linguago.storePassword").orNull
                keyAlias = providers.gradleProperty("linguago.keyAlias").orNull
                keyPassword = providers.gradleProperty("linguago.keyPassword").orNull
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                if (!releaseStoreFile.isNullOrBlank()) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
        }
    }
    buildFeatures {
        buildConfig = true
    }
    sourceSets {
        getByName("main") {
            // Voz tts/stt y manifest.json del tool de descarga (ADR-006); el
            // caché de descargas vive fuera de models/ (tarea manual).
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
    implementation(project(":core:core-auth"))

    implementation(project(":feature:feature-onboarding"))
    implementation(project(":feature:feature-auth"))
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-lesson"))
    implementation(project(":feature:feature-profile"))
    implementation(project(":feature:feature-sync"))
    implementation(project(":feature:feature-settings"))
    implementation(project(":feature:feature-review"))

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
