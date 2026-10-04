# Building

The build is the same framework as the other FRS-Projects mods:
[Stonecutter](https://stonecutter.kikugie.dev/) with Architectury Loom, one source tree, and one
node per Minecraft version and loader.

## Requirements

* A **JDK 25** where Gradle can find it: Gradle 9.7 runs its daemon on Java 25
  (`gradle/gradle-daemon-jvm.properties`). Each node still compiles to its own Java level
  (17 for 1.20.1, 21 for 1.21.1); Gradle downloads those toolchains itself.
* Nothing else. The Gradle wrapper pins the Gradle version.

## Commands

| Command | What it does |
| --- | --- |
| `./gradlew buildAll` | Builds every node |
| `./gradlew checkAll` | Runs every node's checks, including `verifyModMetadata` |
| `./gradlew collectJars` | Copies every node's jar into `build/libs` |
| `./gradlew :1.20.1-forge:build` | Builds one node; its jar lands in `versions/1.20.1-forge/build/libs/` |
| `./gradlew :1.20.1-forge:runClient` | Launches a dev client for one node |
| `./gradlew "Set active project to 1.20.1-forge"` | Switches the working tree to another node |

`verifyModMetadata` opens each built jar and fails the build if its loader metadata or
`pack.mcmeta` is missing, so a jar that would load as an empty mod never ships. Pushing a `v*`
tag that matches `mod.version` publishes a GitHub Release with every node's jar.

## Jar names

Each node produces `taghelper-<mod version>+<node>.jar`:

```
build/libs/
  taghelper-1.2.0+1.20.1-forge.jar
  taghelper-1.2.0+1.21.1-neoforge.jar
```

The mod version is `mod.version` in `gradle.properties` and is shared by every node; bumping it
once re-versions the whole matrix.

## Layout

```
src/main/java/.../command/   the command tree, scopes and feedback; every node.
src/main/java/.../platform/  ItemData, branched on `//? if >=1.20.5` (NBT vs data components).
src/main/java/.../config/    TagHelperConfig, the switches the commands read.
src/main/java/.../forge/     Forge entry point and config spec (`//? if forge`).
src/main/java/.../neoforge/  The same, NeoForge spelling (`//? if neoforge`).
src/main/resources/          mods.toml / neoforge.mods.toml / pack.mcmeta, templated from
                             gradle.properties and versions/<node>/gradle.properties.
build-logic/                 Convention plugin shared by every node (metadata, checks).
versions/<node>/             That node's dependency versions.
```

Git holds the tree as the `1.21.1-neoforge` node, so the Forge file and the NBT branch of
`ItemData` are committed commented out.

## Adding a Minecraft version

1. Add a `match("<minecraft>", "<loader>")` line in `settings.gradle.kts`.
2. Add `versions/<minecraft>-<loader>/gradle.properties` (copy the nearest one and adjust
   `deps.*`, `java.version` and `deps.pack_format`).
3. Add the node to the matrix in `.github/workflows/build.yml`.

That is the whole change if the Minecraft APIs the mod touches did not move. If they did, see
[PORTING.md](PORTING.md).

### pack_format

| Minecraft | pack_format |
| --- | --- |
| 1.19.4 | 13 |
| 1.20.1 | 15 |
| 1.20.4 | 22 |
| 1.20.6 | 32 |
| 1.21.1 | 34 |
| 1.21.4 | 46 |

### 1.19.4

1.19.4-forge was a target under the previous ModDevGradle build. Architectury Loom 1.17 leaves
part of Forge 1.19.4's Minecraft jar unmapped (`net.minecraft.network.chat.Component` and ~230
other classes are missing), so the node was dropped. The NBT branch of `ItemData` covers it;
re-adding it is the procedure above once Loom maps it correctly.
