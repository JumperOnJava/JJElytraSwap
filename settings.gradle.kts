pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
    kotlinController = true

    //post-deobfuscation versions - one versions/<version>.toml per entry
    val versions = arrayOf(
        "26.1",
        "26.2",
        "26.3"
    )
    for (v in versions) {
        properties.load(file("versions/$v.toml"))
    }

    shared {
        fun mc(version: String, vararg loaders: String) {
            for (loader in loaders) {
                val versionConfig = version("$version-$loader", version);
                versionConfig.buildscript = "build.gradle.kts";
            }
        }

        //post-deobfuscation versions
        for (version in versions) {
            mc(version, "fabric","neoforge")
        }
    }
    create(rootProject)
}

rootProject.name = "JJElytraSwap"