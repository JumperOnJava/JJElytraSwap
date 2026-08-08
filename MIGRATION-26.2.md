# Migrating JJElytraSwap to Minecraft 26.2

## Status: build-blocking config issues fixed. Source migration still outstanding.

This branch adds `26.2` as a target in the Stonecutter tree (`settings.gradle.kts`),
creates `versions/26.2-fabric/` and `versions/26.2-neoforge/` with dependency
versions researched as of August 2026, and fixes two build-blocking issues found
while testing:

- Fabric Loader 0.19.3, Fabric API 0.152.2+26.2
- NeoForge 26.2.0.50-beta (still beta at time of writing - pin exact build, re-check
  the NeoForged Maven before publishing)

### Fix 1: Yarn mappings no longer resolve for 26.x

Minecraft 26.1+ ships unobfuscated with Mojang's official names built into the game
jar - Yarn was discontinued as of that version, so `net.fabricmc:yarn:26.2+build.0`
doesn't exist and never will. `build.gradle.kts` unconditionally tried to resolve it
for every loader; the `mappings(...)` calls are now skipped for any target whose
Minecraft version starts with `26.`.

### Fix 2: Architectury Loom was too old to configure a 26.x project at all

Separately from mappings, Architectury Loom itself couldn't configure *any* 26.1+
project - it would crash trying to prepare a project without mappings, tracked at
[architectury-loom#328](https://github.com/architectury/architectury-loom/issues/328)
(filed Feb 2026, thread never closed). The actual fix shipped in **Architectury Loom
1.17** (July 8, 2026), whose changelog lists *"Support for Forge 1.21.x and 26.x"*
(#343, #349) - the issue thread just never got updated to reflect it.

This repo was pinned to `1.13-SNAPSHOT` in `stonecutter.gradle.kts`, several releases
behind. Bumped to `1.17`.

**⚠️ This version is declared once for the whole project, not per Minecraft target** -
the bump affects every existing version folder (`1.21.3` through `1.21.11`, both
loaders), not just `26.2`. Jumping four+ minor releases could plausibly change
behavior for the already-working targets too. **Before relying on this, rebuild at
least one known-good existing target (e.g. `1.21.11-fabric`) to confirm nothing
regressed**, in addition to trying `26.2-fabric` again.

## What's still outstanding: source-level mapping migration

Neither fix above touches the actual Java source, which still uses Yarn-mapped
class/method names. This is the remaining, and riskiest, part of the migration:

- `src/main/java/.../JJElytraSwapInit.java` - imports and uses `MinecraftClient`,
  `ItemStack`, `EquipmentSlot`, `RegistryKeys`, etc. (Yarn names)
- `src/main/java/.../mixin/SwapCheckMixin.java` - **mixin injection target is a
  string-matched method signature**: `@Mixin(ClientPlayerEntity.class)` injecting
  into `tickMovement` at the `checkGliding()` invocation. If these names don't match
  the Mojang-mapped equivalents exactly, the mixin silently fails to apply (or the
  build fails loudly, depending on mixin config strictness) - this is the
  highest-risk part of the whole migration.
- Fabric's porting guidance also calls for switching `modImplementation`/
  `modCompileOnly` usages tied to the Minecraft dependency itself (not mod
  dependencies like Fabric API) over to plain `implementation`/`compileOnly` on
  unobfuscated versions - worth re-checking against a current Fabric example mod.
- NeoForge's mapping layering (`yarn-mappings-patch-neoforge`) is also Yarn-dependent;
  now skipped alongside the Yarn fix above, but worth confirming NeoForge doesn't need
  an equivalent replacement for 26.x.

### Recommended path

Fabric Loom ships a `migrateMappings` Gradle task built for exactly this transition
(Yarn -> Mojang mappings), and there's also a Ravel IntelliJ plugin alternative that
additionally handles Kotlin and reportedly performs better on more complex projects.
Both need real network access to the Fabric/Mojang Maven repositories to run, which
isn't available in the sandbox this scaffolding was prepared in - this part needs to
happen on a machine with normal internet access, with the result compiled and
smoke-tested before merging.
