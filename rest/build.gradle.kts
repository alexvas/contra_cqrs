plugins {
    kotlin("jvm")
}

repositories {
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(Libs.kotlin_stdlib_jdk8)
    implementation(Libs.kotlinx_coroutines_core)
    implementation(Libs.ktor_client_apache)

    implementation(Libs.ktor_server_core)
    implementation(Libs.ktor_server_netty)
    implementation(Libs.ktor_client_jackson)

    implementation(Libs.log4j_api)
    implementation(Libs.log4j_core)
    implementation(Libs.log4j_slf4j_impl)
    implementation(Libs.log4j_jul)
    implementation(Libs.log4j_jcl)
    implementation(Libs.disruptor)
}
