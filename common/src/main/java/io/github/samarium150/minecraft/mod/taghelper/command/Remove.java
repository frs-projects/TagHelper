package io.github.samarium150.minecraft.mod.taghelper.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.samarium150.minecraft.mod.taghelper.config.TagHelperConfig;
import io.github.samarium150.minecraft.mod.taghelper.platform.ItemData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/** Strips data from the stacks in a scope. */
public final class Remove {
    
    private Remove() { }
    
    /** {@code remove <key>} -- drops a single entry. */
    static int entry(@Nonnull CommandContext<CommandSourceStack> context, @Nonnull Scope scope)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!TagHelperConfig.removeEnabled() || !scope.isEnabled())
            return Feedback.disabled(source, "remove");
        
        List<ItemStack> items = scope.select(context);
        if (items.isEmpty()) return Feedback.empty(context, scope);
        
        String key = ItemData.key(context);
        int changed = 0;
        for (ItemStack item : items) {
            if (ItemData.remove(source, item, key)) changed++;
        }
        
        return Feedback.reportChange(context, scope, items, changed, "Removed '" + key + "' from");
    }
    
    /** {@code remove} -- resets the stacks to a pristine item. */
    static int all(@Nonnull CommandContext<CommandSourceStack> context, @Nonnull Scope scope)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!TagHelperConfig.removeEnabled() || !scope.isEnabled())
            return Feedback.disabled(source, "remove");
        
        List<ItemStack> items = scope.select(context);
        if (items.isEmpty()) return Feedback.empty(context, scope);
        
        for (ItemStack item : items) ItemData.removeAll(source, item);
        
        // Reporting the resulting data rather than a bare confirmation: on
        // component targets this does not empty the stack, it resets it to the
        // item type's defaults, and the user should see that.
        return Feedback.reportChange(context, scope, items, items.size(),
                "Removed all " + ItemData.label() + " from");
    }
}
