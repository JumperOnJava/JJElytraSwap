# Migrating JJElytraSwap to Minecraft 26.2

## Status: blocked on upstream Architectury Loom. Confirmed, not a project issue.

`26.2` is scaffolded (Stonecutter version tree, `versions/26.2-fabric/` and
`versions/26.2-neoforge/` gradle.properties, Yarn-mappings-skip in
`build.gradle.kts`) but **will not build**. This has been isolated to a real bug
in Architectury Loom's still-beta 26.x support, not this project's configuration.

### What was ruled out

- Yarn mappings resolution - fixed, confirmed not the blocker (see git history)
- Gradle/JDK version mismatch - **ruled out**. Tested Gradle 8.14, 9.0/9.1, 9.2.1,
  across both JDK 21 and JDK 25 - identical crash every time:
  `NoSuchMethodError: Configuration.extendsFrom(Provider[])`, thrown from inside
  `net.fabricmc.loom.configuration.LoomConfigurations` during Loom's own plugin
  bootstrap, before this project's `build.gradle.kts` is even evaluated.
- Bad specific build artifact - **ruled out**. Tested three different published
  builds in the Loom `1.17` line (`1.17.477`, `1.17.488`, `1.17.491`) - all three
  fail identically, including `1.17.477`, the very first build after the GitHub
  1.17 release.
- The project itself - **confirmed healthy**. Reverting the Loom pin back to
  `1.13-SNAPSHOT` (this project's original pin) builds every existing target
  (`1.21.3` through `1.21.11`, both loaders) cleanly with no errors.

### The actual finding

Every Loom `1.17.x` build prints on startup: **"This version of Architectury Loom
is in beta! Please report any issues you encounter."** This is upstream beta
software failing at its own plugin initialization, unrelated to anything in this
repository. Given the crash happens in Loom's own bootstrap code before reading
any project build script, there is nothing fixable from this project's side.

## Current state of this branch

`stonecutter.gradle.kts` is pinned back to `1.13-SNAPSHOT` - the known-working
version. This means:

- All existing targets (`1.21.3`-`1.21.11`) build normally on this branch
- `26.2-fabric`/`26.2-neoforge` are present in the version tree but **cannot
  build** until a non-beta Architectury Loom release ships working 26.x support
- The Yarn-mappings-skip fix in `build.gradle.kts` is harmless and left in place
  for whenever that Loom release arrives

## Recommended path forward

Pause 26.2 work. Watch [architectury-loom releases](https://github.com/architectury/architectury-loom/releases)
and/or [architectury-loom#328](https://github.com/architectury/architectury-loom/issues/328)
for a stable (non-beta) release with confirmed 26.x support, then revisit:

1. Bump `stonecutter.gradle.kts` to that release
2. Confirm `26.2-fabric` at least configures without crashing
3. Only then proceed to the still-untouched source migration below

## What's still outstanding once Loom is unblocked: source-level mapping migration

Neither the mappings fix nor the Loom bump touch the actual Java source, which
still uses Yarn-mapped class/method names:

- `src/main/java/.../JJElytraSwapInit.java` - imports and uses `MinecraftClient`,
  `ItemStack`, `EquipmentSlot`, `RegistryKeys`, etc. (Yarn names)
- `src/main/java/.../mixin/SwapCheckMixin.java` - **mixin injection target is a
  string-matched method signature**: `@Mixin(ClientPlayerEntity.class)` injecting
  into `tickMovement` at the `checkGliding()` invocation. If these names don't
  match the Mojang-mapped equivalents exactly, the mixin silently fails to apply
  (or the build fails loudly, depending on mixin config strictness) - this is the
  highest-risk part of the whole migration.
- Fabric's porting guidance also calls for switching `modImplementation`/
  `modCompileOnly` usages tied to the Minecraft dependency itself over to plain
  `implementation`/`compileOnly` on unobfuscated versions.
- NeoForge's mapping layering (`yarn-mappings-patch-neoforge`) is also
  Yarn-dependent; already skipped alongside the fabric fix, worth confirming
  NeoForge doesn't need an equivalent replacement for 26.x.

Fabric Loom ships a `migrateMappings` Gradle task for this transition, and
there's a Ravel IntelliJ plugin alternative that also handles Kotlin. Both need
Loom to actually run first, which is exactly today's blocker.
