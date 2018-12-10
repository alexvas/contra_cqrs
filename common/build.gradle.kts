import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    kotlin("jvm")
}

apply<JarTest>()

dependencies {
    implementation(Libs.kotlin_stdlib_jdk8)

    implementation(Libs.owner_java8)

    implementation(Libs.log4j_api)
}
