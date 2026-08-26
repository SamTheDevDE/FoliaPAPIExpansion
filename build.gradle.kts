import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.10"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.22"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "org.dreeam.expansion.folia"
version = "2.0.0"
description = "A lightweight Kotlin PlaceholderAPI expansion for Folia metrics"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "paper"
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        name = "placeholderapi"
        content {
            includeGroup("me.clip")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.4.10") {
        exclude(group = "org.jetbrains", module = "annotations")
    }

    compileOnly("me.clip:placeholderapi:2.12.3") {
        exclude(group = "org.bstats")
    }
    paperweight.foliaDevBundle("26.2.build.7-beta")

    testImplementation(kotlin("test-junit5", "2.4.10"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.14.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        extraWarnings.set(true)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
}

tasks.processResources {
    from("LICENSE") {
        into("META-INF")
        rename { "LICENSE-Folia-Expansion.txt" }
    }
    from("THIRD-PARTY-NOTICES.md") {
        into("META-INF")
    }
    from("licenses/Apache-2.0.txt") {
        into("META-INF")
        rename { "LICENSE-Kotlin.txt" }
    }
}

tasks.withType<Jar>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "SamTheDevDE",
        )
    }
}

tasks.jar {
    archiveClassifier.set("thin")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("Folia-Expansion")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    relocate("kotlin", "org.dreeam.expansion.folia.libs.kotlin")
}

val stageReleaseArtifact = tasks.register<Copy>("stageReleaseArtifact") {
    group = "build"
    description = "Stages only the runtime-ready expansion JAR for CI and releases."
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.flatMap { it.archiveFile })
    into(layout.buildDirectory.dir("release"))
}

tasks.build {
    dependsOn(stageReleaseArtifact)
}

tasks.register("printVersion") {
    group = "help"
    description = "Prints the project version for release automation."
    doLast {
        println(project.version)
    }
}
