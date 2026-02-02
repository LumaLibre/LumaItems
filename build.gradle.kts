import java.nio.charset.Charset
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java")
    id("maven-publish")
    kotlin("jvm") version "2.0.21"
    id("com.gradleup.shadow") version "9.2.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("dev.jsinco.pterodactyldeploy") version "1.15-SNAPSHOT"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}


group = "dev.lumas.lumaitems"
version = getGitCommitHashShort()

val jdkVersion = 21
val charset = "UTF-8"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://nexus.iridiumdevelopment.net/repository/maven-releases/")
    maven("https://jitpack.io")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://nexus.neetgames.com/repository/maven-releases/")
    maven("https://repo.jsinco.dev/releases")
    maven("https://maven.playpro.com/")
    maven("https://repo.okaeri.cloud/releases")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.glaremasters.me/repository/towny/")
}

dependencies {
    compileOnly("com.github.Zrips:Jobs:v5.2.6.2") {
        isTransitive = false
    }
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("dev.lumas.lumacore:LumaCore:d774bc6")
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.2.047-CUSTOM") {
        isTransitive = false
    }
    compileOnly("net.coreprotect:coreprotect:23.0") {
        isTransitive = false
    }
    compileOnly("dev.lumas.glowapi:LumaGlowAPI:c57567c")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9-beta1")
    compileOnly("com.palmergames.bukkit.towny:towny:0.102.0.0")

    implementation("com.iridium:IridiumColorAPI:1.0.9")
    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:6.1.0-beta.1")

    // PaperWeight
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

tasks {

    processResources {
        outputs.upToDateWhen { false }
        filter<ReplaceTokens>(mapOf(
            "tokens" to mapOf("version" to project.version.toString()),
            "beginToken" to "\${",
            "endToken" to "}"
        )).filteringCharset = charset
    }

    shadowJar {
        val pack = "dev.lumas.lumaitems.shaded"
        relocate("com.iridium.iridiumcolorapi", "$pack.iridiumcolorapi")
        relocate("eu.okaeri", "$pack.okaeri")
        exclude("kotlin/**")
        minimize()
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    jar {
        enabled = false
    }

    withType<JavaCompile>().configureEach {
        options.encoding = charset
    }


    build {
        dependsOn(shadowJar)
    }

    pterodactylDeploy {
        url = System.getenv("PTERO_URL") ?: return@pterodactylDeploy
        apiKey = System.getenv("PTERO_TOKEN")
        serverId = System.getenv("PTERO_SERVER")

        dropIn {
            deployDirectory = "plugins"
            uploadFiles = mutableListOf(file("build/libs/LumaItems.jar"))
            deployCommands = mutableListOf("plugman reload LumaItems")
        }
    }

    runServer {
        minecraftVersion("1.21.11")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(jdkVersion)
    withSourcesJar()
}

kotlin {
    jvmToolchain(jdkVersion)
}

publishing {
    repositories {
        maven {
            name = "jsinco-repo"
            url = uri("https://repo.jsinco.dev/releases")
            credentials(PasswordCredentials::class) {
                // get from environment
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks.shadowJar.get().archiveFile) {
                builtBy(tasks.shadowJar)
            }
        }
    }
}

fun getGitCommitHashShort(): String {
    return try {
        val process = ProcessBuilder("git", "log", "-1", "--format=%h")
            .redirectErrorStream(true)
            .start()
        val result = process.inputStream.bufferedReader(Charset.defaultCharset()).readText().trim()
        result.ifBlank { "none" }
    } catch (e: Exception) {
        println("Failed to get git commit hash! (No git repository found?)")
        "none"
    }
}