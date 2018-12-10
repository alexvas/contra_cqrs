import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*

const val kapt = "kapt"
const val implementation = "implementation"

const val kaptTest = "kaptTest"
const val testImplementation = "testImplementation"
const val testRuntimeOnly = "testRuntimeOnly"

class JarTest : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val testArchives by configurations.creating {
            extendsFrom(configurations["testCompile"])
        }

        val kotlinCompile = tasks.getByName("compileTestKotlin")

        val jarTest = tasks.register<Jar>("jarTest") {
            dependsOn(kotlinCompile)
            from(kotlinCompile.outputs)
            classifier = "test"
        }

        artifacts {
            add(configurationName, jarTest)
        }
    }

    companion object {
        const val configurationName = "testArchives"
    }

}
