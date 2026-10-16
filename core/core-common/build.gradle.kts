plugins {
    id("linguago.kotlin.library")
}

kotlin {
    explicitApi()
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
