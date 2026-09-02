# Porting to a new Minecraft version

## How the source layers work

There is no per-version branch and no per-version copy of the mod. Each target
is compiled from a stack of source directories declared in
`gradle/targets.gradle`:

```
common/                  every target
platform/nbt/            targets below 1.20.5
platform/components/     targets from 1.20.5 up
loader/forge/            Forge targets
loader/neoforge/         NeoForge targets
versions/<target>/       optional, that one target only
```

`common/` holds the mod: the command tree, the scopes, the feedback messages.
Code written there lands in every jar.

The version- and loader-specific classes live at the **same fully-qualified
name** in competing layers, and exactly one of those layers is on any given
target's source path. `platform/nbt/` and `platform/components/` both define
`platform.ItemData`; `loader/forge/` and `loader/neoforge/` both define
`config.TagHelperConfig`. Common code calls those classes directly, with no
interfaces, no reflection and no runtime dispatch — the choice is made by the
build.

The practical consequence: if you add a method to one implementation and forget
the other, that target fails to compile. The contract is enforced by javac.

## The contract

### `platform.ItemData`

Everything that differs between item NBT (up to 1.20.4) and data components
(1.20.5 and later). Both implementations must provide:

| Member | Purpose |
| --- | --- |
| `String label()` | What this version calls the data: `"NBT"` or `"components"`. Used in all user-facing messages. |
| `ArgumentType<?> keyArgument()` | Brigadier type for the `<key>` argument. |
| `String key(CommandContext)` | Reads the `"key"` argument back out as a string. |
| `CompletableFuture<Suggestions> suggestKeys(CommandContext, SuggestionsBuilder)` | Completions for `<key>`. |
| `String describe(CommandSourceStack, ItemStack)` | Dump of the stack's data, or `null` when it has none. |
| `void set(CommandSourceStack, ItemStack, String, Tag)` | Writes one entry. |
| `void setAll(CommandSourceStack, ItemStack, CompoundTag)` | Replaces all data. |
| `boolean remove(CommandSourceStack, ItemStack, String)` | Drops one entry; returns whether anything changed. |
| `void removeAll(CommandSourceStack, ItemStack)` | Resets the stack to a pristine item. |

### `config.TagHelperConfig`

Each loader layer wires the mod's options to that loader's config system and
exposes them as `getEnabled()`, `setEnabled()`, `removeEnabled()`,
`hotbarEnabled()`, `inventoryEnabled()`, `enderChestEnabled()`, plus a `SPEC`
field the loader's entrypoint registers.

Option names are identical across loaders, so a `taghelper-common.toml` written
by the Forge build is read unchanged by the NeoForge build.

### Loader entrypoint

`TagHelper` and `init.CommandRegistry` are wholly loader-specific. Both call
into `command.TagHelperCommands.register(dispatcher)`, which builds the entire
command tree.

## What actually changes between versions

Most Minecraft API drift lands in `common/` and just works, because the classes
the mod touches — `CommandSourceStack`, `ItemStack`, `Inventory`, `Component`,
`Commands` — have been stable. Two things have historically broken:

**1.20.5 replaced item NBT with data components.** `ItemStack.getTag()` and
`setTag()` were deleted. This is why `platform/` exists at all. On a components
target, keys are data component ids (`minecraft:custom_data`,
`minecraft:damage`) and values are that component's own NBT form — the same
syntax `/give` takes.

**Loader package renames.** Forge's `net.minecraftforge.*` became NeoForge's
`net.neoforged.*`, `ForgeConfigSpec` became `ModConfigSpec`,
`@Mod.EventBusSubscriber` became `@EventBusSubscriber`, and
`META-INF/mods.toml` became `META-INF/neoforge.mods.toml`. All of that is
contained in `loader/`.

## Procedure

1. Add the target to `gradle/targets.gradle`. See [BUILDING.md](BUILDING.md).
2. `./gradlew :<target>:compileJava`.
3. If it compiles, you are done.
4. If a `common/` class fails to compile because Minecraft moved an API:
   * If the old and new forms can be expressed identically, fix it in `common/`
     and both versions keep working.
   * If they cannot, move the affected code behind a new method on `ItemData`
     (or a new class following the same pattern), implement it in both
     `platform/` layers, and document it in the contract above.
5. If only one target needs a class that no shared layer can express, drop it in
   `versions/<target>/src/main/java/`. That directory is on that target's source
   path alone. To *replace* a layer's class rather than add to it, give the
   target an explicit `layers` list in the matrix that swaps the layer out.
6. `./gradlew collectJars` and check the jar names.

## Worked example: what 1.21.4 would take

Adding

```groovy
[minecraft: '1.21.4', loader: 'neoforge', loaderVersion: '21.4.157', java: 21, packFormat: 46],
```

to the matrix is enough to make Gradle set the target up in full -- toolchain,
Minecraft artifacts, metadata, jar name. `./gradlew :1.21.4-neoforge:compileJava`
then fails on exactly two lines:

```
platform/components/.../ItemData.java:130: error: incompatible types:
    Optional<Reference<DataComponentType<?>>> cannot be converted to DataComponentType<?>
platform/components/.../ItemData.java:175: error: ...same...
```

1.21.2 changed `Registry.get(ResourceLocation)` from returning a nullable value
to returning `Optional<Holder.Reference<T>>`. No single Java source form
compiles against both signatures, so `platform/components/` cannot span that
boundary. The fix is step 5 above: split the layer.

1. Copy `platform/components/` to `platform/components-1212/`.
2. Change those two lookups in the copy to
   `BuiltInRegistries.DATA_COMPONENT_TYPE.get(id).map(Holder::value).orElse(null)`.
3. Give the new targets `itemData: 'components-1212'` in the matrix.

Targets on 1.20.5-1.21.1 keep the old layer; targets from 1.21.2 up get the new
one; `common/` is untouched and both keep building. That is the pattern for
every future break: contain it in a layer, leave the mod alone.

## Deliberate behaviour differences

On components targets:

* `get` dumps the stack's **full effective component map**, including the
  components the item type supplies by default, not just the ones that were
  edited. Components whose type carries no codec are transient and are skipped.
* `remove <key>` deletes a component outright, so an item can be stripped of a
  component its type would otherwise supply.
* `remove` with no key resets the stack to the item type's defaults, which is
  the component-era equivalent of clearing an item's NBT.
* `set <data>` resets the stack to defaults first, then applies every entry in
  the compound.
