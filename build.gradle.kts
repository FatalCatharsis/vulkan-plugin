import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "github.fatalcatharsis"
version = "1.0-SNAPSHOT"


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    jcenter()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    compile(kotlin("stdlib-jdk8"))

    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.12")
    testImplementation("org.mockito:mockito-core:2.25.0")
}

gradlePlugin {
    plugins {
        create("vulkan") {
            id = "github.fatalcatharsis.vulkan"
            implementationClass = "github.fatalcatharsis.VulkanPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/FatalCatharsis/vulkan-plugin"
    vcsUrl = "https://github.com/FatalCatharsis/vulkan-plugin"
    description = "A plugin that attaches the vulkan source and static libraries to a cpp-application project."

    (plugins) {
        "vulkan" {
            displayName = "Vulkan Plugin"
            tags = listOf("vulkan", "c++")
            version = project.version as String
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "github.fatalcatharsis"
            artifactId = "vulkan"
            version = project.version as String
        }
    }
}
