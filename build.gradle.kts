import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.8.0"
}

//Constants:

val modid = "nes"
group = "be.hize.nes"
version = "0.1.Beta.7.1"
val mcVersion = "1.8.9"
val mixinGroup = "$group.mixin"

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}
// Dependencies:

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.spongepowered.org/maven/")
    // If you don't want to log in with your real minecraft account, remove this line
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://jitpack.io") {
        content {
            includeGroupByRegex("com\\.github\\..*")
        }
    }
    maven("https://repo.nea.moe/releases")
    maven("https://repo.hize.be/releases")
    maven("https://maven.notenoughupdates.org/releases")
}

val shadowImpl by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val shadowModImpl by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

val devenvMod by configurations.creating {
    isTransitive = false
    isVisible = false
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shadowImpl("com.github.ILikePlayingGames:DiscordIPC:f91ed4b") {
        exclude(module = "log4j")
        because("Different version conflicts with Minecraft's Log4J")
        exclude(module = "gson")
        because("Different version conflicts with Minecraft's Log4j")
    }

    // If you don't want mixins, remove these lines
    shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")

    implementation(kotlin("stdlib-jdk8"))
    shadowImpl("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") {
        exclude(group = "org.jetbrains.kotlin")
    }

    // If you don't want to log in with your real minecraft account, remove this line
   // modRuntimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.1.2")

    @Suppress("VulnerableLibrariesLocal")
    implementation("com.github.hannibal002:notenoughupdates:4957f0b:all"){
        exclude(group = "null", module = "unspecified")
    }
    @Suppress("VulnerableLibrariesLocal")
    devenvMod("com.github.hannibal002:notenoughupdates:4957f0b:all"){
        exclude(group = "null", module = "unspecified")
    }

    implementation("at.hannibal2:SkyHanni:0.21:all-dev"){
        exclude(group = "null", module = "unspecified")
    }
    devenvMod("at.hannibal2:SkyHanni:0.21:all-dev"){
        exclude(group = "null", module = "unspecified")
    }

    shadowModImpl("org.notenoughupdates.moulconfig:legacy:2.4.3")
    devenvMod("org.notenoughupdates.moulconfig:legacy:2.4.3:test")


    shadowImpl("moe.nea:libautoupdate:1.0.3")
    shadowImpl("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
}
kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
            enableLanguageFeature("BreakContinueInInlineLambdas")
        }
    }
}

// Minecraft configuration:
loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            // If you don't want mixins, remove these lines
            property("mixin.debug", "true")
            property("asmhelper.verbose", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
            arg("--mixin", "mixins.$modid.json")
            val modFiles = devenvMod.incoming.artifacts.resolvedArtifacts.get()
            arg("--mods", modFiles.joinToString(",") { it.file.relativeTo(file("run")).path })
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        // If you don't want mixins, remove this lines
        mixinConfig("mixins.$modid.json")
    }
    // If you don't want mixins, remove these lines
    mixin {
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }
    runConfigs {
        "server" {
            isIdeConfigGenerated = false
        }
    }
}

// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    archiveBaseName.set("nes")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"

        // If you don't want mixins, remove these lines
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.$modid.json"
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("mixinGroup", mixinGroup)

    filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
        expand(inputs.properties)
    }

    rename("(.+_at.cfg)", "META-INF/$1")
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks.jar {
    archiveClassifier.set("nodeps")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    archiveClassifier.set("all-dev")
    configurations = listOf(shadowImpl, shadowModImpl)
    doLast {
        configurations.forEach {
            println("Config: ${it.files}")
        }
    }
    fun relocate(name: String) = relocate(name, "be.hize.nes.deps.$name")

    relocate("org.notenoughupdates.moulconfig")
    relocate("com.jagrosh.discordipc")
    relocate("moe.nea.libautoupdate")
}

tasks.assemble.get().dependsOn(tasks.remapJar)

tasks.compileJava {
    dependsOn(tasks.processResources)
}

sourceSets.main {
    kotlin.destinationDirectory.set(java.destinationDirectory)
    java.srcDir(file("$projectDir/src/main/kotlin"))
    output.setResourcesDir(java.destinationDirectory)
}