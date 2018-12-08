import kotlin.String

/**
 * Find which updates are available by running
 *     `$ ./gradlew syncLibs`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val com_google_dagger: String = "2.19" 

    const val disruptor: String = "3.4.2" 

    const val dbsetup_kotlin: String = "2.1.0" 

    const val hikaricp: String = "3.2.0" 

    const val mockk: String = "1.8.13.kotlin13" //available: "1.8.13" 

    const val jmfayard_github_io_gradle_kotlin_dsl_libs_gradle_plugin: String = "0.2.6" 

    const val org_apache_logging_log4j: String = "2.11.1" 

    const val assertj_core: String = "3.11.1" 

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.11" 

    const val org_jetbrains_kotlin_kapt_gradle_plugin: String = "1.3.11" 

    const val kotlin_annotation_processing_gradle: String =
            "1.3.11" // exceed the version found: 1.3.10

    const val kotlin_scripting_compiler_embeddable: String =
            "1.3.11" // exceed the version found: 1.3.10

    const val kotlin_stdlib_jdk8: String = "1.3.10" 

    const val org_junit_jupiter: String = "5.3.2" 

    const val org_liquibase_gradle_gradle_plugin: String = "2.0.1" 

    const val liquibase_core: String = "3.6.2" 

    const val mybatis_typehandlers_jsr310: String = "1.0.2" 

    const val mybatis: String = "3.4.6" 

    const val postgresql: String = "42.2.5" 

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "5.0"

        const val currentVersion: String = "5.0"

        const val nightlyVersion: String = "5.2-20181208000047+0000"

        const val releaseCandidate: String = ""
    }
}
