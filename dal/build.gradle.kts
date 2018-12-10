@file:Suppress("UnstableApiUsage")

import org.liquibase.gradle.Activity

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.liquibase.gradle")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    implementation(project(":common"))
    implementation(Libs.kotlin_stdlib_jdk8)

    implementation(Libs.log4j_api)

    liquibaseRuntime(Libs.liquibase_core)
    liquibaseRuntime(Libs.postgresql)

    implementation(Libs.postgresql)
    implementation(Libs.mybatis)
    implementation(Libs.mybatis_typehandlers_jsr310)
    implementation(Libs.hikaricp)

    testImplementation(Libs.dbsetup_kotlin)
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

val changelog = "changelog.xml"

tasks.register("targetDb") {
    liquibase {
        activities.register("main") {
            val dbUrl by project.extra.properties
            val dbOwnerUser by project.extra.properties
            val dbOwnerPass by project.extra.properties
            this.arguments = mapOf(
                    "classpath" to "src/main/db",
                    "logLevel" to "info",
                    "changeLogFile" to changelog,
                    "url" to dbUrl,
                    "username" to dbOwnerUser,
                    "password" to dbOwnerPass
            )
        }
        runList = "main"
    }
}
