import groovy.lang.Closure
import org.gradle.kotlin.dsl.provider.kotlinScriptClassPathProviderOf
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.concurrent.Callable

plugins {
    java
    kotlin("jvm") version "1.3.11"
    id("org.jetbrains.dokka")
}

group = "com.teamwizardry.mirror"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("io.leangen.geantyref", "geantyref", "1.3.6")
    compile("net.bytebuddy", "byte-buddy", "1.9.6")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.3.0-M1")
    testCompile("com.nhaarman", "mockito-kotlin-kt1.1", "1.5.0")
    testCompile(files("noParamNames/out"))
}

java.sourceSets {
    getByName("main").java.srcDirs("src/samples/kotlin")
    getByName("main").java.srcDirs("src/samples/java")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.javaParameters = true
}

tasks.withType<DokkaTask> {
    val out = "$projectDir/docs"
    outputFormat = "html"
    outputDirectory = out
    jdkVersion = 8
    doFirst {
        println("Cleaning doc directory $out...")
        project.delete(fileTree(out))
    }

    kotlinTasks(Any().dokkaDelegateClosureOf<Any?> { emptyList<Any?>() })

    sourceDirs = listOf("src/main/kotlin").map { projectDir.resolve(it) }
    samples = listOf("src/samples/java", "src/samples/kotlin")
    includes = projectDir.resolve("src/main/docs").walkTopDown()
            .filter { it.isFile }
            .toList()
}

fun <T> Any.dokkaDelegateClosureOf(action: T.() -> Unit) = object : Closure<Any?>(this, this) {
    @Suppress("unused") // to be called dynamically by Groovy
    fun doCall() = org.gradle.internal.Cast.uncheckedCast<T>(delegate).action()
}