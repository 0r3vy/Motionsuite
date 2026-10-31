plugins {
    id("fabric-loom")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName = project.property("archives_base_name") as String
}

val vulkanModJar = layout.projectDirectory.file("libs/vulkanmod.jar")
val vkEnabled = vulkanModJar.asFile.exists()
val vkRequired = providers.gradleProperty("motionsuite.requireVk").getOrElse("false").toBoolean()

if (vkRequired && !vkEnabled) {
    throw GradleException(
        "MotionSuite: -Pmotionsuite.requireVk=true was set but ${vulkanModJar.asFile.path} is missing. " +
                "Drop the VulkanMod jar into libs/ or drop the property to build without VulkanMod support."
    )
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Terraformers"
                url = uri("https://maven.terraformersmc.com/releases/")
            }
        }
        filter {
            includeGroup("com.terraformersmc")
        }
    }
    mavenCentral()
}

sourceSets {
    val vk by creating {
        java.setSrcDirs(if (vkEnabled) listOf("src/vk/java") else emptyList<String>())
        compileClasspath += sourceSets["main"].compileClasspath + sourceSets["main"].output
    }
}

val mixinextras = "io.github.llamalad7:mixinextras-fabric:${project.property("mixinextras_version")}"

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modCompileOnly("com.terraformersmc:modmenu:${project.property("modmenu_version")}")

    implementation(mixinextras)
    annotationProcessor(mixinextras)
    include(mixinextras)

    if (vkEnabled) {
        "vkCompileOnly"(sourceSets.main.get().output)
        "vkCompileOnly"(files(vulkanModJar))
        "vkCompileOnly"("org.lwjgl:lwjgl-vulkan:3.3.3")
    } else {
        logger.lifecycle("MotionSuite: VulkanMod support disabled - place the VulkanMod jar at libs/vulkanmod.jar to enable it")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:${project.property("junit_version")}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    from(sourceSets["vk"].output)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
