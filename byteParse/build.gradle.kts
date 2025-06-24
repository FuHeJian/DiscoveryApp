plugins{
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}
apply(from = file("../base.gradle"))
android {
    namespace = "com.fhj.byteparse"
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
    api(libs.flatbuffers.java)//解析通信数据
    implementation(libs.flatbuffers.java.grpc)
    implementation(libs.grpc.stub)
//    implementation(libs.grpc.okhttp)//android客户端需要使用okhttp不能使用netty
    implementation("io.grpc:grpc-cronet:1.73.0")
    implementation(libs.ktor.client.core.jvm)//网络请求工具
    implementation(libs.ktor.client.content.negotiation.jvm)
    implementation(libs.ktor.client.logging.jvm)
    implementation(libs.kotlinpoet)
    implementation(libs.gson)
}