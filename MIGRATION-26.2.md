# Migrating JJElytraSwap to Minecraft 26.2

## Status: version-tree scaffolding only. NOT build-ready.

This branch adds `26.2` as a target in the Stonecutter tree (`settings.gradle.kts`)
and creates `versions/26.2-fabric/` and `versions/26.2-neoforge/` with dependency
versions researched as of August 2026:

- Fabric Loader 0.19.3, Loom 1.17, Fabric API 0.152.2+26.2
- NeoForge 26.2.0.50-beta (still beta at time of writing - pin exact build, re-check
  the NeoForged Maven before publishing)

## Why this alone will not compile

Minecraft 26.1 removed obfuscation entirely and ships with Mojang's official class/
method names built into the game jar. As a direct result:

1. **Yarn mappings are discontinued** as of 26.1+. The root `build.gradle.kts`
   unconditionally calls `mappings("net.fabricmc:yarn:$minecraft+build...")` for
   every loader - that artifact does not exist for `26.2` and will fail to resolve.
   This needs a conditional (e.g. `stonecutter.eval(minecraft, ">=26.1")`) that skips
   the Yarn mappings block for 26.1+ targets, per Fabric's own porting guide.
2. **All Yarn-mapped class/method names in source need renaming to Mojang's official
   names.** This project's source is small but touches this directly:
   - `src/main/java/.../JJElytraSwapInit.java` - imports and uses `MinecraftClient`,
     `ItemStack`, `EquipmentSlot`, `RegistryKeys`, etc. (Yarn names)
   - `src/main/java/.../mixin/SwapCheckMixin.java` - **mixin injection target is a
     string-matched method signature**: `@Mixin(ClientPlayerEntity.class)` injecting
     into `tickMovement` at the `checkGliding()` invocation. If these names don't
     match the Mojang-mapped equivalents exactly, the mixin silently fails to apply
     (or the build fails loudly, depending on mixin config strictness) - this is the
     highest-risk part of the whole migration.
3. Fabric's own guidance also calls for switching `modImplementation`/`modCompileOnly`
   usages tied to the Minecraft dependency itself (not mod dependencies like Fabric
   API) over to plain `implementation`/`compileOnly`, and `remapJar` semantics changed
   with the new Loom plugin generation - worth re-checking against the current Fabric
   example mod before assuming the existing task wiring still applies as-is.
4. NeoForge's mapping layering (`yarn-mappings-patch-neoforge`) in `build.gradle.kts`
   is also Yarn-dependent and needs the equivalent adjustment for the neoforge target.

## Recommended path

Fabric Loom ships a `migrateMappings` Gradle task built for exactly this transition
(Yarn -> Mojang mappings), and there's also a Ravel IntelliJ plugin alternative that
additionally handles Kotlin. Both need real network access to the Fabric/Mojang Maven
repositories to run, which isn't available in the sandbox this scaffolding was
prepared in - so this part needs to happen on a machine with normal internet access,
ideally with the result compiled and smoke-tested before merging.
