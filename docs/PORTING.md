# Porting to a new Minecraft version

## How one source tree covers every node

There is no per-version branch and no per-version copy of the mod. Stonecutter preprocesses the
single tree in `src/` once per node, commenting code in or out by version and loader:

```java
//? if >=1.20.5 {
...data components...
//?} else {
/*...item NBT...
*///?}
```

and `//? if forge {` / `//? if neoforge {` around whole files. Loader constants come from
`stonecutter.gradle.kts`; version comparisons use the node's Minecraft version.

Inside a gated region use only `//` comments: Stonecutter comments an inactive region out with
one block comment, which a Javadoc block inside would end early.

## The contract

### `platform.ItemData`

Everything that differs between item NBT (up to 1.20.4) and data components (1.20.5 and later).
Both branches must provide:

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

If one branch gains a method the other lacks, that node fails to compile, so javac enforces the
contract.

### `config.TagHelperConfig`

Shared. The commands read `getEnabled()`, `setEnabled()`, `removeEnabled()`, `hotbarEnabled()`,
`inventoryEnabled()`, `enderChestEnabled()`. Each loader's `TagHelper` entry point builds its own
config spec (`ForgeConfigSpec` / `ModConfigSpec`) and passes the values to
`TagHelperConfig.bind(...)`.

Option names are identical across loaders, so a `taghelper-common.toml` written by the Forge
build is read unchanged by the NeoForge build.

### Loader entry point

`forge/TagHelper` and `neoforge/TagHelper` are wholly loader-specific: they register the config
and call `command.TagHelperCommands.register(dispatcher)` on `RegisterCommandsEvent`.

## What actually changes between versions

Most Minecraft API drift lands in shared code and just works, because the classes
the mod touches — `CommandSourceStack`, `ItemStack`, `Inventory`, `Component`,
`Commands` — have been stable. Two things have historically broken:

**1.20.5 replaced item NBT with data components.** `ItemStack.getTag()` and
`setTag()` were deleted. This is why `ItemData` is branched at all. On a components
target, keys are data component ids (`minecraft:custom_data`,
`minecraft:damage`) and values are that component's own NBT form — the same
syntax `/give` takes.

**Loader package renames.** Forge's `net.minecraftforge.*` became NeoForge's
`net.neoforged.*`, `ForgeConfigSpec` became `ModConfigSpec`,
`@Mod.EventBusSubscriber` became `@EventBusSubscriber`, and
`META-INF/mods.toml` became `META-INF/neoforge.mods.toml`. All of that is
contained in the `forge/` and `neoforge/` packages.

## Procedure

1. Add the node. See [BUILDING.md](BUILDING.md).
2. `./gradlew :<node>:compileJava`.
3. If it compiles, you are done.
4. If shared code fails to compile because Minecraft moved an API:
   * If the old and new forms can be expressed identically, fix it once and every node keeps
     working.
   * If they cannot, branch the affected lines with `//? if >=<version> {` ... `//?} else {`.
     Keep the branch as small as possible; for anything item-data related, put it in
     `ItemData` and keep the contract above.
5. `./gradlew checkAll collectJars` and check the jar names.

For example, 1.21.2 changed `Registry.get(ResourceLocation)` from returning a nullable value to
returning `Optional<Holder.Reference<T>>`. The two lookups in `ItemData` would get a
`//? if >=1.21.2` branch using
`BuiltInRegistries.DATA_COMPONENT_TYPE.get(id).map(Holder::value).orElse(null)`.

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
