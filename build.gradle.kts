import java.nio.charset.Charset
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.2.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.run-paper") version "3.0.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.7.1"
    kotlin("jvm") version "2.3.21"
}


group = "dev.lumas.lumaitems"
version = getGitCommitHashShort()

val jdk = 25

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://nexus.neetgames.com/repository/maven-releases/")
    maven("https://repo.jsinco.dev/releases")
    maven("https://maven.playpro.com/")
    maven("https://repo.okaeri.cloud/releases")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.glaremasters.me/repository/towny/")
    maven("https://repo.codemc.io/repository/EvenMoreFish/")
    maven("https://maven.canvasmc.io/releases")
}

dependencies {
    compileOnly("com.github.Zrips:Jobs:v5.2.6.2") {
        isTransitive = false
    }
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("dev.lumas.core:LumaCore:f25f237")
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.2.047-CUSTOM") {
        isTransitive = false
    }
    compileOnly("net.coreprotect:coreprotect:23.0") {
        isTransitive = false
    }
    compileOnly("dev.lumas.glowapi:LumaGlowAPI:325d91d")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9-beta1")
    compileOnly("com.palmergames.bukkit.towny:towny:0.102.0.0")
    compileOnly("com.oheers.evenmorefish:even-more-fish-plugin:2.1.14") {
        isTransitive = false
    }
    compileOnly("com.oheers.evenmorefish:even-more-fish-api:2.1.14") {
        isTransitive = false
    }

    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:6.1.0-beta.1")

    paperweight.devBundle("io.canvasmc.canvas", "26.1.2.build.+")
}

tasks {

    shadowJar {
        val pack = "dev.lumas.lumaitems.shaded"
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
        options.encoding = "UTF-8"
    }


    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.1.2")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(jdk)
}

kotlin {
    jvmToolchain(jdk)
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

bukkit {
    name = "LumaItems"
    main = "dev.lumas.lumaitems.LumaItems"
    version = project.version.toString()
    apiVersion = "1.21"
    author = "Jsinco"
    foliaSupported = true
    depend = listOf("LumaCore")
    softDepend = listOf(
        "ProtocolLib",
        "EvenMoreFish", // Soft depend on EMF to register listeners after it
        "Jobs",
        "mcMMO"
    )
    permissions {
        register("lumaitems.disassemblergui") {
            description = "Permission to open the disassembler GUI"
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }
}