plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.17-SNAPSHOT" apply false
    id("architectury-plugin") version "3.5-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.3.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.8.4" apply false
}
stonecutter active "26.2-fabric" /* [SC] DO NOT EDIT */
stonecutter.automaticPlatformConstants = true

// Builds every version into `build/libs/{mod.version}/{loader}`
stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}
stonecutter registerChiseled tasks.register("chiseledPublishMods", stonecutter.chiseled) {
    group = "project"
    ofTask("publishMods")
}
stonecutter registerChiseled tasks.register("chiseledRunAllClients", stonecutter.chiseled) {
    group = "project"
    ofTask("runClient")
}

// Builds only the currently active version (stonecutter active "…"). Unlike
// chiseledBuild (all versions) this chisels + builds just one version, which is
// what you want when iterating on a single MC version. The versions filter runs
// lazily, so it doesn't need stonecutter.tree to be populated at registration time.
stonecutter registerChiseled tasks.register("chiseledBuildActive", stonecutter.chiseled) {
    group = "project"
    versions { _, v -> v.isActive }
    ofTask("buildAndCollect")
}



// Builds loader-specific versions into `build/libs/{mod.version}/{loader}`
for (it in stonecutter.tree.branches) {
    if (it.id.isEmpty()) continue
    val loader = it.id.upperCaseFirst()
    stonecutter registerChiseled tasks.register("chiseledBuild$loader", stonecutter.chiseled) {
        group = "project"
        versions { branch, _ -> branch == it.id }
        ofTask("buildAndCollect")
    }
}

// Runs active versions for each loader
for (it in stonecutter.tree.nodes) {
    if (it.metadata != stonecutter.current || it.branch.id.isEmpty()) continue
    val types = listOf("Client", "Server")
    val loader = it.branch.id.upperCaseFirst()
    for (type in types) it.tasks.register("runActive$type$loader") {
        group = "project"
        dependsOn("run$type")
    }
}
