package io.github.samarium150.minecraft.mod.taghelper.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.samarium150.minecraft.mod.taghelper.config.TagHelperConfig;
import io.github.samarium150.minecraft.mod.taghelper.platform.ItemData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/** Writes data onto the stacks in a scope. */
public final class Set {
    
    private Set() { }
    
    /** {@code set <key> <value>} -- writes a single entry, leaving the rest alone. */
    static int entry(@Nonnull CommandContext<CommandSourceStack> context, @Nonnull Scope scope)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!TagHelperConfig.setEnabled() || !scope.isEnabled())
            return Feedback.disabled(source, "set");
        
        List<ItemStack> items = scope.select(context);
        if (items.isEmpty()) return Feedback.empty(context, scope);
        
        String key = ItemData.key(context);
        Tag value = NbtTagArgument.getNbtTag(context, "value");
        // Copy per stack so several stacks never end up aliasing one tag.
        for (ItemStack item : items) ItemData.set(source, item, key, value.copy());
        
        return Feedback.reportChange(context, scope, items, items.size(), "Set '" + key + "' on");
    }
    
    /** {@code set <data>} -- replaces everything on the stack with the given compound. */
    static int all(@Nonnull CommandContext<CommandSourceStack> context, @Nonnull Scope scope)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!TagHelperConfig.setEnabled() || !scope.isEnabled())
            return Feedback.disabled(source, "set");
        
        List<ItemStack> items = scope.select(context);
        if (items.isEmpty()) return Feedback.empty(context, scope);
        
        CompoundTag data = CompoundTagArgument.getCompoundTag(context, "data");
        // Copy per stack so the stacks do not end up sharing one mutable tag.
        for (ItemStack item : items) ItemData.setAll(source, item, data.copy());
        
        return Feedback.reportChange(context, scope, items, items.size(),
                "Set " + ItemData.label() + " on");
    }
}
