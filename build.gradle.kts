import java.util.Properties
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import me.modmuss50.mpp.ReleaseType
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("net.fabricmc.fabric-loom") version "1.18-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.147" apply false
    id("me.modmuss50.mod-publish-plugin")
    id("com.gradleup.shadow") apply false
}

// Exposes this node's ["<mc version>"] section from versions/<mc version>.toml
// as bare mod.*/deps.* properties (the ["<node>"] section is unprefixed by default).
stonecutter.properties.tags(stonecutter.current.version)

val platform = requireNotNull(findProperty("mod.platform") as? String) {
    "Missing 'mod.platform' for ${stonecutter.current.project}"
}
val minecraft_version = stonecutter.current.version
val loader = platform

version = "${mod.version}+${minecraft_version}"
group = mod.group

base {
    archivesName.set("${mod.id}-${loader}")
}

// -----------------------------------------------------------------------------
// Plugins Configuration
// -----------------------------------------------------------------------------
when (platform) {
    "fabric" -> {
        apply(plugin = "net.fabricmc.fabric-loom")
        apply(plugin = "com.gradleup.shadow")
    }
    "neoforge" -> {
        apply(plugin = "net.neoforged.moddev")
    }
}

// -----------------------------------------------------------------------------
// Loader Specific Logic
// -----------------------------------------------------------------------------
when (platform) {
    "fabric" -> {
        configure<LoomGradleExtensionAPI> {
            val ctVersion = (findProperty("mod.ct_version") as String?) ?: "fallback"
            accessWidenerPath.set(rootProject.file("src/main/resources/ct/jjelytraswap.${ctVersion}.classtweaker"))

            decompilers {
                get("vineflower").apply {
                    options.put("mark-corresponding-synthetics", "1")
                }
            }
        }
    }
    "neoforge" -> {
        val atVersion = (findProperty("mod.at_version") as String?) ?: "fallback"
        val accessTransformerName = "jjelytraswap.${atVersion}.accesstransformer"

        configure<NeoForgeExtension> {
            version = mod.dep("neoforge_loader") as String
            accessTransformers.from(rootProject.file("src/main/resources/at/${accessTransformerName}"))

            runs {
                register("client") {
                    client()
                    systemProperty("neoforge.enabledGameTestNamespaces", mod.id)
                }

                register("server") {
                    server()
                    programArgument("--nogui")
                    systemProperty("neoforge.enabledGameTestNamespaces", mod.id)
                }

                register("gameTestServer") {
                    type = "gameTestServer"
                    systemProperty("neoforge.enabledGameTestNamespaces", mod.id)
                }

                register("data") {
                    clientData()
                    programArguments.addAll(
                        "--mod", mod.id,
                        "--all",
                        "--output", file("src/generated/resources/").absolutePath,
                        "--existing", file("src/main/resources/").absolutePath
                    )
                }
                configureEach {
                    systemProperty("forge.logging.markers", "REGISTRIES")
                    logLevel.set(org.slf4j.event.Level.DEBUG)
                }
            }

            mods {
                create(mod.id) {
                    sourceSet(sourceSets.main.get())
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Repositories & Dependencies
// -----------------------------------------------------------------------------
repositories {
    when (platform) {
        "fabric" -> {
            maven("https://maven.neoforged.net/releases/")
            maven("https://maven.terraformersmc.com/")
            maven("https://maven.nucleoid.xyz/")
        }
    }
}

dependencies {
    when (platform) {
        "fabric" -> {
            "minecraft"("com.mojang:minecraft:${minecraft_version}")
            implementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
            implementation("com.terraformersmc:modmenu:${mod.dep("modmenu_version")}")
            implementation("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_version")}")
        }
    }
}

// -----------------------------------------------------------------------------
// Common Java & SourceSets Configuration
// -----------------------------------------------------------------------------
java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

sourceSets.main {
    resources {
        when (platform) {
            "neoforge" -> {
                srcDir("src/generated/resources")
                exclude("**/*.bbmodel")
                exclude("src/generated/**/.cache")
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Platform-Specific Tasks Configuration
// -----------------------------------------------------------------------------
when (platform) {
    "fabric" -> {
        val shadowBundle = configurations.create("shadowBundle") {
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        tasks.named<ShadowJar>("shadowJar") {
            configurations = listOf(shadowBundle)
            archiveClassifier.set("dev-shadow")
        }

        tasks.jar {
            archiveClassifier.set("dev")
        }
    }
    "neoforge" -> {
        val localRuntime = configurations.create("localRuntime")
        configurations.runtimeClasspath.get().extendsFrom(localRuntime)

        val atVersion = (findProperty("mod.at_version") as String?) ?: "fallback"
        val accessTransformerName = "jjelytraswap.${atVersion}.accesstransformer"

        val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
            val replaceProperties = mapOf(
                "minecraft_version" to minecraft_version,
                "neo_version" to mod.dep("neoforge_loader"),
                "mod_id" to mod.id,
                "mod_name" to mod.name,
                "mod_version" to mod.version
            )
            inputs.properties(replaceProperties)
            expand(replaceProperties)
            from(rootProject.file("src/main/templates"))
            into("build/generated/sources/modMetadata")
        }
        sourceSets.main.get().resources.srcDir(generateModMetadata)

        extensions.configure<NeoForgeExtension>("neoForge") {
            ideSyncTask(generateModMetadata)
        }
    }
}

tasks.processResources {
    when (platform) {
        "fabric" -> {
            properties(
                listOf("fabric.mod.json"),
                "id" to mod.id,
                "name" to mod.name,
                "version" to mod.version,
                "minecraft" to mod.prop("mc_dep_fabric")
            )
        }
        "neoforge" -> {
            val atVersion = (findProperty("mod.at_version") as String?) ?: "fallback"
            val accessTransformerName = "jjelytraswap.${atVersion}.accesstransformer"

            properties(
                listOf("META-INF/neoforge.mods.toml", "pack.mcmeta"),
                "id" to mod.id,
                "name" to mod.name,
                "version" to mod.version,
                "minecraft" to mod.prop("mc_dep_forgelike"),
                "file" to mod.prop("mc_dep_forgelike")
            )
            from(rootProject.file("src/main/resources/at/${accessTransformerName}")) {
                rename { "META-INF/accesstransformer.cfg" }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Common Build, Publish & Stonecutter Tasks
// -----------------------------------------------------------------------------
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

publishMods {
    val modrinthToken = localProperties.getProperty("publish.modrinthToken", "")
    val curseforgeToken = localProperties.getProperty("publish.curseforgeToken", "")

    file.set(tasks.jar.get().archiveFile)
    dryRun.set(modrinthToken.isEmpty() || curseforgeToken.isEmpty())

    displayName.set("${mod.name}${loader.replaceFirstChar { it.uppercase() }} ${property("mod.publish_name")}-${mod.version}")
    version.set(mod.version)
    changelog.set(rootProject.file("CHANGELOG.md").readText())
    type.set(ReleaseType.BETA)

    modLoaders.add(loader)

    val targets = property("mod.publish_versions").toString().split(' ')
    modrinth {
        projectId.set(property("publish.modrinth").toString())
        accessToken.set(modrinthToken)
        targets.forEach { minecraftVersions.add(it) }
        if (loader == "fabric") {
            requires("fabric-api")
            optional("modmenu")
        }
    }

    curseforge {
        projectId.set(property("publish.curseforge").toString())
        accessToken.set(curseforgeToken)
        targets.forEach { minecraftVersions.add(it) }
        if (loader == "fabric") {
            requires("fabric-api")
            optional("modmenu")
        }
    }
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.jar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/${loader}"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(buildAndCollect)
    }

    rootProject.tasks.register("runActive") {
        group = "project"
        dependsOn(tasks.named("runClient"))
    }
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}