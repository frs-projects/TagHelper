package io.github.samarium150.minecraft.mod.taghelper.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.samarium150.minecraft.mod.taghelper.config.TagHelperConfig;
import io.github.samarium150.minecraft.mod.taghelper.util.CommandUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * The set of item stacks a subcommand operates on.
 *
 * <p>Adding a new scope means adding a constant here and one line in
 * {@link TagHelperCommands}; the get/set/remove implementations pick it up
 * without changes.
 */
public enum Scope {
    
    /** The stack in the player's main hand. */
    HOLDING("the main hand", false),
    /** The stack in an explicitly numbered slot, 0-40. */
    SLOT("slot", false),
    /** Every non-empty stack in the hotbar, slots 0-8. */
    HOTBAR("hotbar", true),
    /** Every non-empty stack in the main inventory, slots 9-35. */
    INVENTORY("inventory", true),
    /** Every non-empty stack in the player's ender chest. */
    ECHEST("ender chest", true);
    
    private final String label;
    private final boolean bulk;
    
    Scope(String label, boolean bulk) {
        this.label = label;
        this.bulk = bulk;
    }
    
    /**
     * Whether this scope can touch more than one stack. Bulk scopes report a
     * count; single scopes report the resulting data.
     */
    public boolean isBulk() {
        return bulk;
    }
    
    /** Human-readable name of this scope, used in feedback and failure messages. */
    public String label(@Nonnull CommandContext<CommandSourceStack> context) {
        return this == SLOT ? "slot " + IntegerArgumentType.getInteger(context, "slot") : label;
    }
    
    /** Whether the config allows commands against this scope at all. */
    public boolean isEnabled() {
        switch (this) {
            case HOTBAR:    return TagHelperConfig.hotbarEnabled();
            case INVENTORY: return TagHelperConfig.inventoryEnabled();
            case ECHEST:    return TagHelperConfig.enderChestEnabled();
            default:        return true;
        }
    }
    
    /**
     * Resolves the stacks this scope addresses. The returned stacks are the live
     * inventory objects, so mutating them edits the player's items in place.
     *
     * @return the non-empty stacks in this scope, possibly empty
     */
    @Nonnull
    public List<ItemStack> select(@Nonnull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        switch (this) {
            case HOLDING: {
                ItemStack item = player.getMainHandItem();
                return item.isEmpty() ? Collections.emptyList() : Collections.singletonList(item);
            }
            case SLOT: {
                ItemStack item = CommandUtil.getItemFromSlot(player, IntegerArgumentType.getInteger(context, "slot"));
                return item == null ? Collections.emptyList() : Collections.singletonList(item);
            }
            case HOTBAR:
                return CommandUtil.range(player.getInventory(), CommandUtil.HOTBAR_START, CommandUtil.HOTBAR_END);
            case INVENTORY:
                return CommandUtil.range(player.getInventory(), CommandUtil.INVENTORY_START, CommandUtil.INVENTORY_END);
            case ECHEST:
                return CommandUtil.all(player.getEnderChestInventory());
            default:
                return Collections.emptyList();
        }
    }
    
    /** Message shown when this scope resolves to nothing. */
    public String emptyMessage(@Nonnull CommandContext<CommandSourceStack> context) {
        return bulk ? "no items found in " + label(context) : "no item in " + label(context);
    }
}
