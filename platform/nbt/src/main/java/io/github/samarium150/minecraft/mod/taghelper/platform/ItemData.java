package io.github.samarium150.minecraft.mod.taghelper.platform;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Item data access for Minecraft versions up to and including 1.20.4, where an
 * item's custom data is a single root {@link CompoundTag}.
 *
 * <p>This is one of two competing implementations of the same class; the build
 * puts exactly one of them on each target's source path. See docs/PORTING.md.
 */
public final class ItemData {
    
    private ItemData() { }
    
    /** What this version calls the data being edited. */
    @Nonnull
    public static String label() {
        return "NBT";
    }
    
    /** NBT keys are arbitrary strings, so they are taken as a quoted string. */
    @Nonnull
    public static ArgumentType<?> keyArgument() {
        return StringArgumentType.string();
    }
    
    @Nonnull
    public static String key(@Nonnull CommandContext<CommandSourceStack> context) {
        return StringArgumentType.getString(context, "key");
    }
    
    /**
     * No suggestions: the set of valid keys is whatever the user wants to invent,
     * and the stack being edited is not known at suggestion time.
     */
    public static CompletableFuture<Suggestions> suggestKeys(@Nonnull CommandContext<CommandSourceStack> context,
                                                             @Nonnull SuggestionsBuilder builder) {
        return builder.buildFuture();
    }
    
    @Nullable
    public static String describe(@Nonnull CommandSourceStack source, @Nonnull ItemStack item) {
        CompoundTag tag = item.getTag();
        return tag == null ? null : tag.toString();
    }
    
    public static void set(@Nonnull CommandSourceStack source, @Nonnull ItemStack item,
                           @Nonnull String key, @Nonnull Tag value) {
        CompoundTag tag = item.getOrCreateTag();
        tag.put(key, value);
        item.setTag(tag);
    }
    
    public static void setAll(@Nonnull CommandSourceStack source, @Nonnull ItemStack item,
                              @Nonnull CompoundTag data) {
        item.setTag(data);
    }
    
    public static boolean remove(@Nonnull CommandSourceStack source, @Nonnull ItemStack item, @Nonnull String key) {
        CompoundTag tag = item.getTag();
        if (tag == null || !tag.contains(key)) return false;
        tag.remove(key);
        item.setTag(tag);
        return true;
    }
    
    public static void removeAll(@Nonnull CommandSourceStack source, @Nonnull ItemStack item) {
        item.setTag(null);
    }
}
