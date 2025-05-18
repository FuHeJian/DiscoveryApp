import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtensionInternal
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

apply {
    this.from(file("../base.gradle.kts"))
}

android {
    namespace = "com.fhj.discoveryapp"

    defaultConfig {
        applicationId = "com.fhj.discoveryapp"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding.enable = true
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.navigation.ui)
    implementation(libs.navigation.fragment.ui)
    implementation(project(":Base"))
    implementation(project(":dns"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// 示例共通配置
tasks.register("commonTask") {
    doLast {
        //做一些公共的配置
        (this.extensions["android"] as BaseAppModuleExtensionInternal).apply {
            compileSdk = 36

        }
    }
}