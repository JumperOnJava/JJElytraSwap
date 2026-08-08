# Migrating JJElytraSwap to Minecraft 26.2

## Status: 26.2-fabric should now configure. 26.2-neoforge still blocked. Source migration untouched.

## The fix: plain Fabric Loom for the fabric target, bypassing Architectury Loom entirely

Architectury Loom's `1.17.x` line has beta-only 26.x support that crashes on its
own plugin bootstrap (`NoSuchMethodError` on `Configuration.extendsFrom`),
confirmed across 3 Gradle versions, 2 JDKs, and 3 separate Loom builds - see the
"Ruled out" section below for the full diagnostic trail.

Instead, `build.gradle.kts` now conditionally applies **plain upstream
`net.fabricmc.fabric-loom`** (not the Architectury fork) for any target where
`loader == "fabric"` and the Minecraft version starts with `"26."`. This is a
completely separate, non-beta codebase from Architectury Loom, confirmed via
Fabric's own actively-maintained example mod (tag `26.1.2`):

```
loom_version=1.17-SNAPSHOT      (plain Fabric Loom, NOT Architectury)
loader_version=0.19.3
fabric_api_version=0.155.2+26.1.2  (26.2 equivalent: 0.155.2+26.2, verified via Modrinth)
```

Fabric's porting guidance for unobfuscated versions also calls for dropping the
mod-prefixed dependency configs (`modImplementation`, `modCompileOnly`) in favor
of plain `implementation`/`compileOnly`, since there's nothing left to remap -
this is now done for `useFabricLoomDirect` targets specifically, leaving the
obfuscated pre-26.x fabric path (Architectury Loom, mod-prefixed configs)
completely unchanged.

**`stonecutter.gradle.kts`** now registers both `dev.architectury.loom` (existing
targets) and `net.fabricmc.fabric-loom` (new, 26.x fabric only) with `apply
false`, and `build.gradle.kts` picks between them at the top of the script via
`useFabricLoomDirect`, before any Loom-specific extension is touched.

## Critical limitation: NeoForge has no equivalent escape hatch

Plain Fabric Loom only builds Fabric - it has no NeoForge support at all.
Architectury Loom exists specifically to bridge Fabric and Forge/NeoForge from
one shared codebase, so **`26.2-neoforge` stays blocked** until Architectury
Loom ships a stable, non-beta release with working 26.x support. Watch
https://github.com/architectury/architectury-loom/releases.

## What's still outstanding for 26.2-fabric: source-level mapping migration

Getting past the Loom bootstrap crash does not touch the actual Java source,
which still uses Yarn-mapped class/method names. This is genuinely untested and
is the next expected failure:

- `src/main/java/.../JJElytraSwapInit.java` - imports and uses `MinecraftClient`,
  `ItemStack`, `EquipmentSlot`, `RegistryKeys`, etc. (Yarn names, need renaming
  to Mojang's official equivalents)
- `src/main/java/.../mixin/SwapCheckMixin.java` - **mixin injection target is a
  string-matched method signature**: `@Mixin(ClientPlayerEntity.class)` injecting
  into `tickMovement` at the `checkGliding()` invocation. If these names don't
  match the Mojang-mapped equivalents exactly, the mixin silently fails to apply
  (or the build fails loudly, depending on mixin config strictness) - this is the
  highest-risk part of the whole migration.
- `ModPlatform.java`, `ConfigScreen.java`, `platforms/fabric/*` - not yet audited
  for Yarn-mapped names.

Fabric Loom ships a `migrateMappings` Gradle task for this transition, and
there's a Ravel IntelliJ plugin alternative that also handles Kotlin. Both
require actually getting a working build first, which is what today's fix
addresses - this is genuinely the next step now, not a hypothetical one.

## Ruled out (full diagnostic trail, for reference)

Before landing on the plain-Fabric-Loom carve-out, the following were tested and
ruled out as the cause of Architectury Loom's crash:

- Yarn mappings resolution - fixed early on, confirmed not the root blocker
- Gradle/JDK version mismatch - tested Gradle 8.14, 9.0/9.1, 9.2.1, across JDK 21
  and JDK 25 - identical crash every time, thrown from inside Architectury Loom's
  own plugin bootstrap (`LoomConfigurations.java`) before this project's
  `build.gradle.kts` is even evaluated
- Bad specific build artifact - tested `1.17.477`, `1.17.488`, `1.17.491` - all
  three fail identically, including `.477`, the very first build after the
  GitHub 1.17 release
- The project itself - confirmed healthy; Loom `1.13-SNAPSHOT` builds every
  existing target (`1.21.3`-`1.21.11`, both loaders) cleanly

Every Architectury Loom `1.17.x` build prints on startup: **"This version of
Architectury Loom is in beta! Please report any issues you encounter."** - beta
software failing at its own plugin initialization, unrelated to anything in this
repository.
