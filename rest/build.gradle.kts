plugins {
    kotlin("jvm")
    application
}

repositories {
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":dal"))

    implementation(Libs.kotlin_stdlib_jdk8)
    implementation(Libs.kotlinx_coroutines_core)

    implementation(Libs.ktor_server_core)
    implementation(Libs.ktor_server_netty)
    implementation(Libs.ktor_jackson)
    implementation(Libs.jackson_datatype_jsr310)

    implementation(Libs.owner_java8)

    implementation(Libs.log4j_api)
    implementation(Libs.log4j_core)
    implementation(Libs.log4j_slf4j_impl)
    implementation(Libs.log4j_jul)
    implementation(Libs.log4j_jcl)
    implementation(Libs.disruptor)

    implementation(project(":dal", JarTest.configurationName))
    implementation(Libs.dbsetup_kotlin)

    testImplementation(project(":common", JarTest.configurationName))
    testImplementation(project(":dal", JarTest.configurationName))

    testImplementation(Libs.dbsetup_kotlin)
    testImplementation(Libs.ktor_client_apache)
    testImplementation(Libs.ktor_client_jackson)
    testImplementation(Libs.assertj_core)
    testImplementation(Libs.junit_jupiter_api)
    testImplementation(Libs.mockk)

    testRuntimeOnly(Libs.junit_jupiter_engine)
    testRuntimeOnly(Libs.log4j_core)
    testRuntimeOnly(Libs.log4j_jcl)
    testRuntimeOnly(Libs.log4j_jul)
    testRuntimeOnly(Libs.log4j_slf4j_impl)
    testRuntimeOnly(Libs.disruptor)
}

application {
    mainClassName = "contra.rest.Main"
}
