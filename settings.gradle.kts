pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        val loomVersion = settings.providers.gradleProperty("loom_version").get()
        id("fabric-loom") version loomVersion
    }
}

rootProject.name = "motionsuite"
