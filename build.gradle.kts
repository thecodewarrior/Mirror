import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.51"
    id("org.jetbrains.dokka")
}

group = "com.teamwizardry.mirror"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.3.0-M1")
    testCompile("com.nhaarman", "mockito-kotlin-kt1.1", "1.5.0")
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
}

tasks.withType<DokkaTask> {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
    jdkVersion = 8
}