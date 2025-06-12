import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.tools.ant.filters.ReplaceTokens
import java.nio.charset.Charset

plugins {
    id("java")
    id("maven-publish")
    kotlin("jvm") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.5"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("dev.jsinco.pterodactyldeploy") version "1.15-SNAPSHOT"
}


group = "dev.jsinco.luma.lumaitems"
version = getGitCommitHashShort()

val jdkVersion = 21
val charset = "UTF-8"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.iridiumdevelopment.net/repository/maven-releases/")
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://nexus.neetgames.com/repository/maven-releases/")
    maven("https://repo.jsinco.dev/releases")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.Zrips:jobs:v4.17.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    implementation("dev.jsinco.luma.lumacore:LumaCore:279f090")
    implementation("fr.skytasul:glowingentities:1.4.4")
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.2.030")
    
    implementation("com.iridium:IridiumColorAPI:1.0.9")

    // PaperWeight
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
}

tasks {

    // PaperWeight
    assemble {
        dependsOn(reobfJar)
    }

    processResources {
        outputs.upToDateWhen { false }
        filter<ReplaceTokens>(mapOf(
            "tokens" to mapOf("version" to project.version.toString()),
            "beginToken" to "\${",
            "endToken" to "}"
        )).filteringCharset = charset
    }

    shadowJar {
        val pack = "dev.jsinco.luma.lumaitems.shaded"
        relocate("com.iridium.iridiumcolorapi", "$pack.iridiumcolorapi")
        relocate("fr.skytasul", "$pack.glowingentities")
        archiveClassifier.set("")
    }

    jar {
        enabled = false
    }

    withType<JavaCompile>().configureEach {
        options.encoding = charset
    }



    reobfJar {
        outputJar.set(layout.buildDirectory.file("${projectDir}${File.separator}build${File.separator}libs${File.separator}${project.name}.jar"))
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
                username = System.getenv("repo_username")
                password = System.getenv("repo_secret")
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

fun getGitCommitHashShort(): String = ByteArrayOutputStream().use { stream ->
    var branch = "none"
    try {
        project.exec {
            commandLine = listOf("git", "log", "-1", "--format=%h")
            standardOutput = stream
        }
    } catch (e: Exception) {
        println("Failed to get git commit hash! (No git repository found?)")
        return branch
    }

    if (stream.size() > 0) branch = stream.toString(Charset.defaultCharset().name()).trim()
    return branch
}
