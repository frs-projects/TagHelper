# Building

## Requirements

* JDK 21 to run Gradle. Individual targets compile against their own Java
  release (17 or 21); Gradle provisions those toolchains itself.
* Nothing else. The Gradle wrapper pins the Gradle version.

## Commands

| Command | What it does |
| --- | --- |
| `./gradlew buildAll` | Builds every target in the matrix. |
| `./gradlew collectJars` | Builds everything and gathers the shippable jars into `build/dist/`. |
| `./gradlew :1.21.1-neoforge:build` | Builds one target. |
| `./gradlew :1.20.1-forge:runClient` | Launches a dev client for one target. |
| `./gradlew printTargets` | Prints the matrix as JSON. CI uses this for its job matrix. |

Set `CI=true` to skip Minecraft decompilation. Builds get much faster at the
cost of not being able to step into Minecraft sources in a debugger.

## Jar names

Each target produces one jar named `taghelper-<minecraft>-<loader>-<mod>.jar`:

```
build/dist/
  taghelper-1.19.4-forge-1.1.0.jar
  taghelper-1.20.1-forge-1.1.0.jar
  taghelper-1.21.1-neoforge-1.1.0.jar
```

The mod version is `mod_version` in `gradle.properties` and is shared by every
target; bumping it once re-versions the whole matrix. Each jar's manifest also
carries `MC-Version` and `Mod-Loader` attributes.

For legacy Forge targets, `build/libs/` holds the reobfuscated production jar
and `build/devlibs/` holds the un-reobfuscated one. `collectJars` only takes
from `build/libs/`, so it always gathers the shippable artifact.

## Adding a Minecraft version

Add one line to `gradle/targets.gradle`:

```groovy
[minecraft: '1.21.4', loader: 'neoforge', loaderVersion: '21.4.154', java: 21, packFormat: 46],
```

That is the whole change if the Minecraft APIs the mod touches did not move. If
they did, see [PORTING.md](PORTING.md).

Fields:

| Field | Meaning |
| --- | --- |
| `minecraft` | Minecraft version. Also becomes the Gradle project name and part of the jar name. |
| `loader` | `forge` or `neoforge`. Selects `loader/<loader>/` as a source layer. |
| `loaderVersion` | Loader version **without** the Minecraft prefix. |
| `java` | Java release. 17 up to 1.20.4, 21 from 1.20.5. |
| `packFormat` | Resource pack format, see the table below. |

Optional overrides: `itemData` (`nbt` or `components`, derived from the
Minecraft version), `mcRange` and `loaderRange` (dependency ranges in the mod
metadata), and `layers` (the source layer list).

### pack_format

| Minecraft | pack_format |
| --- | --- |
| 1.19.4 | 13 |
| 1.20.1 | 15 |
| 1.20.4 | 22 |
| 1.20.6 | 32 |
| 1.21.1 | 34 |
| 1.21.4 | 46 |

### Loader toolchains

Forge targets build with ModDevGradle's legacy plugin, which supports
MinecraftForge **1.17 through 1.20.1 only**. NeoForge targets build with the
regular ModDevGradle plugin, which supports NeoForge 21.0 and later.

There is therefore no toolchain wired up for MinecraftForge on 1.20.2+. Adding
such a target needs ForgeGradle 6 alongside ModDevGradle, which the matrix does
not currently model.
