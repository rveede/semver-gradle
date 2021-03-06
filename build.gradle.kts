import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    jacoco
    id("com.github.ben-manes.versions").version("0.21.0")
    id("com.gradle.plugin-publish").version("0.10.1")
    id("io.gitlab.arturbosch.detekt").version("1.0.0-RC14")
    id("org.jlleitschuh.gradle.ktlint").version("7.2.1")
    id("org.sonarqube") version "2.7"
}

version = "1.0.0"
group = "net.thauvin.erik.gradle"

var github = "https://github.com/ethauvin/semver-gradle"
var packageName = "net.thauvin.erik.gradle.semver"

var spek_version = "2.0.1"

repositories {
    jcenter()
}

dependencies {
    implementation(gradleApi())

    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spek_version")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spek_version")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        // Gradle 4.6
        kotlinOptions.apiVersion = "1.2"
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }

    withType<JacocoReport> {
        reports {
            html.isEnabled = true
            xml.isEnabled = true
        }
    }

    "check" {
        dependsOn("ktlintCheck")
    }

    "sonarqube" {
        dependsOn("jacocoTestReport")
    }
}

detekt {
    input = files("src/main/kotlin", "src/test/kotlin")
    filters = ".*/resources/.*,.*/build/.*"
    baseline = project.rootDir.resolve("detekt-baseline.xml")
}

sonarqube {
    properties {
        property("sonar.projectName", "semver-gradle")
        property("sonar.projectKey", "ethauvin_semver-gradle")
        property("sonar.sourceEncoding", "UTF-8")
    }
}

gradlePlugin {
    plugins {
        create(project.name) {
            id = packageName
            displayName = "SemVer Plugin"
            description = "Semantic Version Plugin for Gradle"
            implementationClass = "$packageName.SemverPlugin"
        }
    }
}

pluginBundle {
    website = github
    vcsUrl = github
    tags = listOf("semver", "semantic", "version", "versioning", "auto-increment", "kotlin", "java")
    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
    }
}
