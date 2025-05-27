plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply(from = file("../base.gradle"))

android {
    namespace = "com.fhj.logger"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {

}