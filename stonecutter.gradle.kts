plugins {
    id("dev.kikugie.stonecutter")
    id("com.gradleup.shadow") version "9.3.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply false
}
stonecutter active "26.1-fabric" /* [SC] DO NOT EDIT */

stonecutter parameters {
    constants{
        val loader = node.metadata.project.substringAfterLast('-')
        match(loader, "fabric", "neoforge", "forge")
    }
}

tasks.register("jjelytraswap_buildAll") {
    group = "build"
    dependsOn(sc.tree.nodes.map { "${it.hierarchy}:build" })

    doLast {
        val rootBuildDir = layout.buildDirectory.asFile.get().resolve("libs");
        sc.tree.nodes.forEach { node ->
            val subprojectBuildDir = project(node.hierarchy.toString()).layout.buildDirectory.asFile.get()
            copy {
                from(fileTree("$subprojectBuildDir/libs").matching { include("*.jar") })
                into(rootBuildDir.resolve(node.name.split(":").last()))
            }
        }
    }
}
