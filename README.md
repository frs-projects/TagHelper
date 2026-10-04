# Tag Helper Plus

A Minecraft mod for reading and editing item data from commands.

Fork of [Samarium150/TagHelper](https://github.com/Samarium150/TagHelper), extended
with slot, hotbar, inventory and ender chest targeting, and with a build that
produces every supported Minecraft version from one source tree.

## Supported versions

| Minecraft | Loader | Jar |
| --- | --- | --- |
| 1.20.1 | Forge | `taghelper-<version>+1.20.1-forge.jar` |
| 1.21.1 | NeoForge | `taghelper-<version>+1.21.1-neoforge.jar` |

## Commands

`/taghelper` — aliased to `/th`. Requires permission level 2.

    /th <scope> get
    /th <scope> set <key> <value>
    /th <scope> set <data>
    /th <scope> remove <key>
    /th <scope> remove

Scopes:

| Scope | Items affected |
| --- | --- |
| `holding` | The stack in your main hand. |
| `slot <0-40>` | One slot: 0-8 hotbar, 9-35 inventory, 36-39 armor, 40 offhand. |
| `hotbar` | Every non-empty stack in slots 0-8. |
| `inventory` | Every non-empty stack in slots 9-35. |
| `echest` | Every non-empty stack in your ender chest. |

Each of `get`, `set` and `remove`, and each of the bulk scopes, can be switched
off in the config.

### On 1.20.1 and earlier

`<key>` is an item NBT key and `<value>` is any NBT tag.

    /th holding set "myKey" 1
    /th holding get
    NBT: {myKey:1}

### On 1.21.1

1.20.5 replaced item NBT with data components, so `<key>` is a data component id
and `<value>` is that component's NBT form — the same syntax `/give` takes. The
key argument tab-completes from the component registry, and an unqualified key
means `minecraft:`.

    /th holding set custom_data {myKey:1}
    /th holding set damage 10

`get` dumps the stack's full effective component map, including the components
the item type supplies by default:

    /th holding get
    components: {"minecraft:custom_data":{myKey:1},"minecraft:damage":10,...}

`remove <key>` deletes a component outright; `remove` with no key resets the
stack to the item type's defaults, which is the component-era equivalent of
clearing an item's NBT.

## Building

    ./gradlew collectJars

puts every jar in `build/libs/`. See [docs/BUILDING.md](docs/BUILDING.md) for the
rest, and [docs/PORTING.md](docs/PORTING.md) for how one source tree produces all
of them and what to do when Minecraft moves an API.

Adding a Minecraft version is one `match(...)` line in `settings.gradle.kts` plus a
`versions/<node>/gradle.properties`.

## License

GNU General Public License v3.
