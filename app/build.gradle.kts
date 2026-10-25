import org.gradle.api.tasks.Sync

plugins {
    id("linguago.android.application")
    id("linguago.hilt")
    id("androidx.baselineprofile")
}

// Distribución por idioma (ADR-017): cada flavor empaqueta SOLO los modelos
// de voz de su idioma; `full` (dev/CI) lleva los cuatro. Sin límite duro por
// flavor: se monitoriza el peso (holgura bajo 2 GB en GitHub Releases) sin
// bloquear el release por diferencias de ±5 MB.
val modelsRoot = rootProject.file("models")
val voiceAssetsByFlavor: Map<String, List<String>> =
    linkedMapOf(
        "en" to
            listOf(
                "tts/en_US-amy-medium",
                "stt/vosk-model-small-en-us-0.15",
            ),
        "pt" to
            listOf(
                "tts/pt_BR-faber-medium",
                "stt/vosk-model-small-pt-0.3",
            ),
        "fr" to
            listOf(
                "tts/fr_FR-siwis-medium",
                "stt/vosk-model-small-fr-0.22",
            ),
        "ja" to listOf("stt/vosk-model-small-ja-0.22"),
        "full" to listOf("tts", "stt"),
    )

// Una tarea Sync por flavor (registrada una sola vez): copia SOLO los
// modelos del idioma a build/generated/voiceAssets/<flavor> con las rutas
// de assets que esperan los motores (tts/<voz>, stt/<modelo>).
val syncVoiceAssetsByFlavor: Map<String, TaskProvider<Sync>> =
    voiceAssetsByFlavor.mapValues { (flavor, paths) ->
        val camel = flavor.replaceFirstChar { it.uppercase() }
        val dest =
            layout.buildDirectory
                .dir("generated/voiceAssets/$flavor")
                .get()
                .asFile
        tasks.register<Sync>("sync${camel}VoiceAssets") {
            into(dest)
            paths.forEach { rel -> into(rel) { from(File(modelsRoot, rel)) } }
            from(File(modelsRoot, "manifest.json"))
        }
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
        versionCode = 2
        versionName = "0.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
        debug {
            // El rediseño del camino se puede probar en debug antes de abrirlo
            // en release (ADR-026). Release queda con el fallback actual.
            buildConfigField("boolean", "LEARNING_PATH_V2", "true")
        }
        release {
            buildConfigField("boolean", "LEARNING_PATH_V2", "true")
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
    flavorDimensions += "language"
    productFlavors {
        create("en") { dimension = "language" }
        create("pt") { dimension = "language" }
        create("fr") { dimension = "language" }
        create("ja") { dimension = "language" }
        create("full") { dimension = "language" }
    }
    // Un APK universal por flavor + su variante arm64-v8a (ADR-017).
    // `-Plinguago.splits=false` empaqueta todas las ABIs de las dependencias:
    // útil para probar en emuladores x86_64; por defecto se mantiene arm64.
    val abiSplitsEnabled =
        providers
            .gradleProperty("linguago.splits")
            .orElse("true")
            .get()
            .toBoolean()
    splits {
        abi {
            isEnable = abiSplitsEnabled
            reset()
            include("arm64-v8a")
            isUniversalApk = true
        }
    }
}

// El perfil se genera para el flavor `en`: el baseline se versiona en
// `app/src/main/baseline-prof.txt` para que todos los flavors lo reutilicen,
// y el startup permanece en `app/src/enRelease/generated/baselineProfiles`.
baselineProfile {
    saveInSrc = true
}

// Los modelos viven fuera del sourceSet main: cada variante genera su
// directorio de assets con Sync (rutas relativas estables a models/) y lo
// registra como directorio generado, de modo que AGP cablea la dependencia
// hacia todos los consumidores (merge de assets, lint, empaquetado).
androidComponents {
    onVariants { variant ->
        val flavorName = variant.flavorName ?: return@onVariants
        val syncTask = syncVoiceAssetsByFlavor[flavorName] ?: return@onVariants
        variant.sources.assets?.addGeneratedSourceDirectory(syncTask) { task -> task.destinationDirectory }
    }
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-data"))
    compileOnly(libs.androidx.room.runtime)
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
    implementation(libs.androidx.profileinstaller)

    baselineProfile(project(":baselineprofile"))

    testImplementation(libs.junit4)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
