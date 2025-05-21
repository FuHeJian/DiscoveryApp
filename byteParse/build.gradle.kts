
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply(from = file("../base.gradle"))

android {
    namespace = "com.fhj.byteparse"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    configurations.size

}

dependencies {

    fun getKotlinVersion() = libs.versions.kotlin.get()
    fun getKSPVersion() : String {
        return when (getKotlinVersion()) {
            "1.6.21" -> "1.6.21-1.0.6"
            "1.7.20" -> "1.7.20-1.0.7"
            "1.8.21" -> "1.8.21-1.0.11"
            "1.9.22" -> "1.9.22-1.0.16"
            "2.0.21" -> "2.0.21-1.0.27"
            else -> "${getKotlinVersion()}-1.0.7" // 默认版本
        }
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("com.google.devtools.ksp:symbol-processing-api:${getKSPVersion()}")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}