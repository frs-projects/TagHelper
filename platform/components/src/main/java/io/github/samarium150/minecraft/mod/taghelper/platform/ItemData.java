package io.github.samarium150.minecraft.mod.taghelper.platform;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Item data access for Minecraft 1.20.5 and later, where item NBT was replaced
 * by a {@link DataComponentMap}.
 *
 * <p>Keys are data component ids such as {@code minecraft:custom_data} or
 * {@code minecraft:damage}, and values are the component's own NBT
 * representation -- the same form {@code /give} accepts.
 *
 * <p>This is one of two competing implementations of the same class; the build
 * puts exactly one of them on each target's source path. See docs/PORTING.md.
 */
public final class ItemData {
    
    private static final DynamicCommandExceptionType UNKNOWN_COMPONENT =
            new DynamicCommandExceptionType(id -> Component.literal("unknown data component '" + id + "'"));
    
    private static final DynamicCommandExceptionType NOT_SERIALIZABLE =
            new DynamicCommandExceptionType(id -> Component.literal("data component '" + id + "' cannot be edited"));
    
    private static final DynamicCommandExceptionType BAD_VALUE =
            new DynamicCommandExceptionType(error -> Component.literal("invalid component value: " + error));
    
    private ItemData() { }
    
    /** What this version calls the data being edited. */
    @Nonnull
    public static String label() {
        return "components";
    }
    
    /** Component ids are resource locations, so an unqualified key means {@code minecraft:}. */
    @Nonnull
    public static ArgumentType<?> keyArgument() {
        return ResourceLocationArgument.id();
    }
    
    @Nonnull
    public static String key(@Nonnull CommandContext<CommandSourceStack> context) {
        return ResourceLocationArgument.getId(context, "key").toString();
    }
    
    /** Suggests every registered data component id. */
    public static CompletableFuture<Suggestions> suggestKeys(@Nonnull CommandContext<CommandSourceStack> context,
                                                             @Nonnull SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(BuiltInRegistries.DATA_COMPONENT_TYPE.keySet(), builder);
    }
    
    /**
     * Dumps the stack's full effective component map, including the components
     * the item type contributes by default.
     *
     * <p>Components whose type carries no codec are transient and cannot be
     * represented as NBT, so they are skipped rather than failing the dump.
     */
    @Nullable
    public static String describe(@Nonnull CommandSourceStack source, @Nonnull ItemStack item) {
        DynamicOps<Tag> ops = ops(source);
        CompoundTag out = new CompoundTag();
        for (TypedDataComponent<?> component : item.getComponents()) {
            ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
            if (id == null) continue;
            encode(component, ops).ifPresent(tag -> out.put(id.toString(), tag));
        }
        return out.isEmpty() ? null : out.toString();
    }
    
    public static void set(@Nonnull CommandSourceStack source, @Nonnull ItemStack item,
                           @Nonnull String key, @Nonnull Tag value) throws CommandSyntaxException {
        apply(item, lookup(key), value, ops(source));
    }
    
    /**
     * Replaces the stack's components wholesale: the stack is first reset to the
     * item type's defaults, then every entry in {@code data} is applied.
     */
    public static void setAll(@Nonnull CommandSourceStack source, @Nonnull ItemStack item,
                              @Nonnull CompoundTag data) throws CommandSyntaxException {
        DynamicOps<Tag> ops = ops(source);
        removeAll(source, item);
        for (String key : data.getAllKeys()) {
            Tag value = data.get(key);
            if (value == null) continue;
            apply(item, lookup(key), value, ops);
        }
    }
    
    /**
     * Deletes a component outright, so an item can be stripped of a component
     * its type would otherwise supply.
     *
     * @return whether the stack actually carried that component
     */
    public static boolean remove(@Nonnull CommandSourceStack source, @Nonnull ItemStack item, @Nonnull String key) {
        ResourceLocation id = ResourceLocation.tryParse(key);
        if (id == null) return false;
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
        if (type == null || !item.has(type)) return false;
        item.remove(type);
        return true;
    }
    
    /**
     * Resets the stack to the components its item type declares, which is the
     * component-era equivalent of clearing an item's NBT.
     */
    public static void removeAll(@Nonnull CommandSourceStack source, @Nonnull ItemStack item) {
        DataComponentPatch patch = item.getComponentsPatch();
        if (patch.isEmpty()) return;
        
        // Snapshot the patched types first; the loop mutates the stack.
        List<DataComponentType<?>> patched = new ArrayList<>();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            patched.add(entry.getKey());
        }
        
        DataComponentMap defaults = item.getItem().components();
        for (DataComponentType<?> type : patched) {
            Object fallback = defaults.get(type);
            // Removing a type that the item type supplies would leave it absent
            // rather than default, so put the default value back instead.
            if (fallback == null) item.remove(type);
            else restore(item, type, fallback);
        }
    }
    
    // -- internals ----------------------------------------------------------
    
    /**
     * Serialization context carrying the registries, which components such as
     * enchantments need in order to encode and decode.
     */
    @Nonnull
    private static DynamicOps<Tag> ops(@Nonnull CommandSourceStack source) {
        return source.registryAccess().createSerializationContext(NbtOps.INSTANCE);
    }
    
    @Nonnull
    private static DataComponentType<?> lookup(@Nonnull String key) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocation.tryParse(key);
        if (id == null) throw UNKNOWN_COMPONENT.create(key);
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
        if (type == null) throw UNKNOWN_COMPONENT.create(key);
        return type;
    }
    
    private static <T> Optional<Tag> encode(@Nonnull TypedDataComponent<T> component, @Nonnull DynamicOps<Tag> ops) {
        Codec<T> codec = component.type().codec();
        if (codec == null) return Optional.empty();
        return codec.encodeStart(ops, component.value()).result();
    }
    
    private static <T> void apply(@Nonnull ItemStack item, @Nonnull DataComponentType<T> type,
                                  @Nonnull Tag value, @Nonnull DynamicOps<Tag> ops) throws CommandSyntaxException {
        Codec<T> codec = type.codec();
        if (codec == null) {
            throw NOT_SERIALIZABLE.create(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type));
        }
        DataResult<T> result = codec.parse(ops, value);
        Optional<T> parsed = result.result();
        if (!parsed.isPresent()) {
            throw BAD_VALUE.create(result.error().map(DataResult.Error::message).orElse("could not be read"));
        }
        item.set(type, parsed.get());
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void restore(@Nonnull ItemStack item, @Nonnull DataComponentType<?> type, @Nonnull Object value) {
        item.set((DataComponentType) type, value);
    }
}
