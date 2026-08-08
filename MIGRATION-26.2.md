# Migrating JJElytraSwap to Minecraft 26.2

## Status: paused. Baseline (1.13-SNAPSHOT / Gradle 9.2.1) restored and confirmed working.

`26.2` is scaffolded (version tree, dependency versions in `versions/26.2-fabric/`
and `versions/26.2-neoforge/`) but does not build. Multiple approaches were tried
and are documented below for whoever picks this back up. None succeeded cleanly.

## Attempt 1: Architectury Loom directly (bumped 1.13-SNAPSHOT -> 1.17.x)

Architectury Loom's `1.17.x` line self-reports as beta for 26.x and crashes on its
own plugin bootstrap (`NoSuchMethodError` on `Configuration.extendsFrom`).
Confirmed across 3 Gradle versions (8.14, 9.0/9.1, 9.2.1), 2 JDKs (21, 25), and 3
separate Loom builds (`1.17.477`, `1.17.488`, `1.17.491`) - identical crash every
time, before this project's `build.gradle.kts` is even evaluated. Not fixable from
this repo's side.

## Attempt 2: bypass Architectury Loom, use plain net.fabricmc.fabric-loom for the fabric target only

Plain upstream Fabric Loom has real, non-beta 26.x support (confirmed via Fabric's
own actively-maintained example mod, tag `26.1.2`). Wired a conditional plugin
application (`useFabricLoomDirect`) so only the `26.2-fabric` target used plain
Fabric Loom while everything else kept Architectury Loom. This ran into a chain of
further issues, each real and fixable individually, but which stacked into
diminishing returns:

- `Configuration 'mappings' has no dependencies` even with zero `mappings()` calls
  (matching Fabric's own example exactly) - cause never fully isolated. Ruled out:
  `architectury-plugin` interference (removed it, same error), Loom snapshot
  version flakiness (pinned to concrete `1.17.12`, same error).
- Gradle wrapper had to bump to 9.5.1 for Loom `1.17.12`'s `plugin.api-version`
  requirement (9.5.0+) - straightforward once diagnosed via the actual Gradle
  variant-mismatch error message.
- After that bump, the **entire** `build.gradle.kts` script failed to compile -
  not just Loom-specific DSL, but core Gradle accessors like `base { }` and
  `archivesName` that have nothing to do with Loom. This suggests something about
  Gradle 9.5.1's plugin classpath/accessor generation broke more broadly for this
  project's setup (9.5.1's own release notes highlight changes to "type-safe
  accessors for precompiled Kotlin Settings plugins," which may be related) -
  not investigated further.

**This branch's `build.gradle.kts` and `stonecutter.gradle.kts` have been reverted
to the original, known-working state** (Architectury Loom `1.13-SNAPSHOT`, Gradle
9.2.1). All existing targets (`1.21.3`-`1.21.11`, both loaders) build normally.

## Recommended path forward, next time this is picked up

Given how many independent toolchain issues stacked up in one evening, next
attempt should probably:

1. Start completely fresh in an isolated, throwaway project (not this multi-version
   Stonecutter repo) using **exactly** what Fabric's own `fabric-example-mod`
   (tag matching whatever the latest 26.x release is by then) declares - same
   Loom version, same Gradle version, same plugin ID - and confirm that builds
   clean in total isolation before touching this project at all.
2. Only once that isolated baseline works, bring the working version numbers back
   into this project's `versions/26.2-fabric/gradle.properties` and retry the
   `useFabricLoomDirect` carve-out approach (the branch history on
   `mc-26.2-mappings-fix` still has that implementation if useful as a reference,
   even though it didn't fully work).
3. Watch [architectury-loom releases](https://github.com/architectury/architectury-loom/releases)
   for a stable, non-beta 26.x release, which would let this go back to the far
   simpler single-Loom-plugin setup (and would also unblock `26.2-neoforge`, which
   has no alternative path - plain Fabric Loom doesn't support NeoForge at all).

## What's still outstanding once a build works at all: source-level mapping migration

Untouched all evening, and still the real remaining work once tooling is sorted:

- `src/main/java/.../JJElytraSwapInit.java` - imports and uses `MinecraftClient`,
  `ItemStack`, `EquipmentSlot`, `RegistryKeys`, etc. (Yarn names, need renaming to
  Mojang's official equivalents)
- `src/main/java/.../mixin/SwapCheckMixin.java` - **mixin injection target is a
  string-matched method signature**: `@Mixin(ClientPlayerEntity.class)` injecting
  into `tickMovement` at the `checkGliding()` invocation. Highest-risk part of the
  whole migration - a wrong name here fails silently rather than at compile time.
- `ModPlatform.java`, `ConfigScreen.java`, `platforms/fabric/*` - not yet audited.

Fabric Loom ships a `migrateMappings` Gradle task for this transition; Ravel
(IntelliJ plugin) is an alternative that also handles Kotlin.
