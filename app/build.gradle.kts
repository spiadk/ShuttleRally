plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
dependencies { testImplementation("junit:junit:4.13.2") }
android {
    namespace = "com.rally.badminton"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.rally.badminton"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
