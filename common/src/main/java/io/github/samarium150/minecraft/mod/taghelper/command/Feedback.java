package io.github.samarium150.minecraft.mod.taghelper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.samarium150.minecraft.mod.taghelper.platform.ItemData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Shared reporting for the three subcommands, so their output stays consistent
 * across scopes and across Minecraft versions.
 */
final class Feedback {
    
    /** Brigadier result for a command that did not do anything. */
    static final int FAILURE = 0;
    
    /** Brigadier result for a command that did what it was asked. */
    static final int SUCCESS = Command.SINGLE_SUCCESS;
    
    private Feedback() { }
    
    static int disabled(@Nonnull CommandSourceStack source, @Nonnull String what) {
        source.sendFailure(Component.literal(what + " command is disabled in config"));
        return FAILURE;
    }
    
    static int empty(@Nonnull CommandContext<CommandSourceStack> context, @Nonnull Scope scope) {
        context.getSource().sendFailure(Component.literal(scope.emptyMessage(context)));
        return FAILURE;
    }
    
    static void message(@Nonnull CommandSourceStack source, @Nonnull String text) {
        source.sendSystemMessage(Component.literal(text));
    }
    
    /** Renders an item's data, or the literal {@code null} when it has none. */
    @Nonnull
    static String describe(@Nonnull CommandSourceStack source, @Nonnull ItemStack item) {
        String data = ItemData.describe(source, item);
        return data == null ? "null" : data;
    }
    
    /**
     * Reports the state of every stack the command touched.
     *
     * <p>A single-stack scope prints the data itself; a bulk scope prints a
     * header and then one line per stack, since dumping a full inventory's data
     * without labels is unreadable.
     */
    static void report(@Nonnull CommandContext<CommandSourceStack> context,
                       @Nonnull Scope scope,
                       @Nonnull List<ItemStack> items,
                       @Nonnull String prefix) {
        CommandSourceStack source = context.getSource();
        if (!scope.isBulk()) {
            message(source, prefix + describe(source, items.get(0)));
            return;
        }
        message(source, "Processing " + items.size() + " items in " + scope.label(context) + ":");
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            message(source, "Item " + (i + 1) + ": " + item.getDisplayName().getString()
                    + " - " + describe(source, item));
        }
    }
    
    /**
     * Reports a bulk mutation as a count, or a single mutation as the resulting
     * data.
     */
    static int reportChange(@Nonnull CommandContext<CommandSourceStack> context,
                            @Nonnull Scope scope,
                            @Nonnull List<ItemStack> items,
                            int changed,
                            @Nonnull String bulkSummary) {
        CommandSourceStack source = context.getSource();
        if (scope.isBulk()) {
            message(source, bulkSummary + " " + changed + " items in " + scope.label(context));
        } else {
            message(source, "current " + ItemData.label() + ": " + describe(source, items.get(0)));
        }
        return changed == 0 ? FAILURE : SUCCESS;
    }
}
