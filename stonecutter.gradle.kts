plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.17-SNAPSHOT" apply false
    id("dev.architectury.loom-no-remap") version "1.17-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.3.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.8.4" apply false
}
stonecutter active "26.2-neoforge" /* [SC] DO NOT EDIT */

tasks.register("chiseledBuild") {
    group = "project"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("buildAndCollect") })
}

tasks.register("chiseledPublishMods") {
    group = "project"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("publishMods") })
}

tasks.register("chiseledRunAllClients") {
    group = "project"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("runClient") })
}
