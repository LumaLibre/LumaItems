import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    kotlin("jvm")
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version("8.3.5")
    id("io.papermc.paperweight.userdev") version("1.7.5") // PaperWeight
}


group = "dev.jsinco.lumaitems"
version = "ver/1.21"


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
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.Zrips:jobs:v4.17.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")

    implementation("com.iridium:IridiumColorAPI:1.0.9")
    implementation(kotlin("stdlib-jdk8"))

    // PaperWeight
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
}

tasks {

    // PaperWeight
    assemble {
        dependsOn(reobfJar)
    }

    processResources {
        outputs.upToDateWhen { false }
        filter<ReplaceTokens>(mapOf(
            "tokens" to mapOf("version" to project.version),
            "beginToken" to "\${",
            "endToken" to "}"
        )).filteringCharset = "UTF-8"
    }

    shadowJar {
        dependencies {
            include(dependency("com.iridium:IridiumColorAPI"))
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        }
        archiveClassifier.set("")
    }

    jar {
        version = ""
        enabled = false
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    kotlin {
        jvmToolchain(21)
    }

    reobfJar {
        outputJar.set(layout.buildDirectory.file("${projectDir}${File.separator}build${File.separator}libs${File.separator}${project.name}.jar"))
    }

    build {
        dependsOn(shadowJar)
    }

//    register("legacyToMM") {
//        println("HERE:")
//        var newString = legacyToMMConverter()
//        if (newString.contains("<b>")) {
//            newString = "<b>" + newString.replace("<b>", "") + "</b>"
//        }
//        println(newString)
//    }

}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}


//val legacyString = "&#f498f6&lI&#c990f9&ln&#9d88fc&lf&#7280ff&lo &r&8»&#E2E2E2"


fun legacyToMMConverter(): String {
    val legacyString = File("convert_me.txt").readText()

    if (legacyString.isEmpty()) {
        return legacyString
    }
    val texts = legacyString.split(String.format("((?<=%1\$s)|(?=%1\$s))", "&").toRegex()).dropLastWhile { it.isEmpty() }
        .toTypedArray()
    val finalText = StringBuilder()
    var i = 0
    while (i < texts.size) {
        if (texts[i].equals("&", ignoreCase = true)) {
            //get the next string
            i++
            if (texts[i][0] == '#') {
                finalText.append('<').append(texts[i].substring(0, 7)).append(texts[i].substring(7)).append('>')
            } else {
                finalText.append(getMiniMessageNamedColor("&" + texts[i].substring(0, 1))).append(texts[i].substring(1))
            }
        } else {
            finalText.append(texts[i])
        }
        i++
    }
    return finalText.toString()
}

fun getMiniMessageNamedColor(namedColor: String): String {
    return when (namedColor) {
        "&0" -> "<black>"
        "&1" -> "<dark_blue>"
        "&2" -> "<dark_green>"
        "&3" -> "<dark_aqua>"
        "&4" -> "<dark_red>"
        "&5" -> "<dark_purple>"
        "&6" -> "<gold>"
        "&7" -> "<gray>"
        "&8" -> "<dark_gray>"
        "&9" -> "<blue>"
        "&a" -> "<green>"
        "&b" -> "<aqua>"
        "&c" -> "<red>"
        "&d" -> "<light_purple>"
        "&e" -> "<yellow>"
        "&f" -> "<white>"
        "&k" -> "<obf>"
        "&l" -> "<b>"
        "&m" -> "<st>"
        "&n" -> "<u>"
        "&o" -> "<i>"
        "&r" -> "<reset>"
        else -> throw IllegalStateException("Unexpected value: $namedColor")
    }
}
