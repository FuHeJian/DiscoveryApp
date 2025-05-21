plugins{
    kotlin("jvm")
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

    implementation("com.google.devtools.ksp:symbol-processing-api:${getKSPVersion()}")
}