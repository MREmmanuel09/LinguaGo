import org.gradle.api.JavaVersion

plugins {
    id("com.android.test")
    id("linguago.quality")
}

android {
    namespace = "dev.linguago.qa"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["linguago.releasegate.durationMinutes"] = "20"
        testInstrumentationRunnerArguments["linguago.releasegate.minLessons"] = "1"
    }

    buildTypes {
        create("release") {
            // Variante instalable sobre el emulador (firma debug como la app).
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    // El gate corre contra el flavor `en`: es el que trae el contenido v2 B2
    // y los motores Piper/Vosk completos (mismos nativos en los 4 flavors).
    // Se declara la dimensión explícita (como en baselineprofile) para que
    // existan las tareas connected<En><Debug|Release>AndroidTest sobre la app
    // minimizada.
    flavorDimensions += "language"
    productFlavors {
        create("en") { dimension = "language" }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        animationsDisabled = true
    }
}

dependencies {
    implementation(libs.junit4)
    implementation(libs.androidx.test.core)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    // Self-instrumenting: los assets del curso se leen desde `targetContext`
    // (el paquete de este módulo), no del APK de la app. Sin esta dependencia
    // `courses/es-en/course.json` no existe para el instrumentation.
    implementation(project(":content:content-es-en"))
}
