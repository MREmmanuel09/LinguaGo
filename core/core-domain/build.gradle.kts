plugins {
    id("linguago.kotlin.library")
}

kotlin {
    explicitApi()
}

dependencies {
    api(project(":core:core-common"))

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
