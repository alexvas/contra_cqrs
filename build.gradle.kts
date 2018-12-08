
plugins {
    kotlin("jvm") version Versions.org_jetbrains_kotlin_jvm_gradle_plugin apply false
    kotlin("kapt") version Versions.org_jetbrains_kotlin_kapt_gradle_plugin apply false
    id("org.liquibase.gradle") version Versions.org_liquibase_gradle_gradle_plugin apply false
    id("jmfayard.github.io.gradle-kotlin-dsl-libs") version Versions.jmfayard_github_io_gradle_kotlin_dsl_libs_gradle_plugin
}

allprojects {
    repositories {
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlinx/")
    }
}

