package io.github.samarium150.minecraft.mod.taghelper.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.samarium150.minecraft.mod.taghelper.config.TagHelperConfig;
import io.github.samarium150.minecraft.mod.taghelper.platform.ItemData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/** Dumps the data attached to the stacks in a scope. */
public final class Get {
    
    private Get() { }
    
    static int run(@Nonnull CommandContext<CommandSourceStack> context, @Nonnull Scope scope)
            throws CommandSyntaxException {
        if (!TagHelperConfig.getEnabled() || !scope.isEnabled())
            return Feedback.disabled(context.getSource(), "get");
        
        List<ItemStack> items = scope.select(context);
        if (items.isEmpty()) return Feedback.empty(context, scope);
        
        Feedback.report(context, scope, items, ItemData.label() + ": ");
        return Feedback.SUCCESS;
    }
}
