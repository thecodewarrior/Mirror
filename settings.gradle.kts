pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-dev") }
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.dokka" -> useModule("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")
            }
        }
    }
}

rootProject.name = "Mirror"
