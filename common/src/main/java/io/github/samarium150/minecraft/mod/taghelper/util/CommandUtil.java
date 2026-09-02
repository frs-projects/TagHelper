package io.github.samarium150.minecraft.mod.taghelper.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class CommandUtil {
    
    private CommandUtil() { }
    
    // Slot ranges
    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 8;
    public static final int INVENTORY_START = 9;
    public static final int INVENTORY_END = 35;
    public static final int SLOT_MIN = 0;
    public static final int SLOT_MAX = 40;
    
    /**
     * Resolves one of the player's 41 addressable slots.
     *
     * @return the stack, or null when the slot id is out of range or the slot is empty
     */
    @Nullable
    public static ItemStack getItemFromSlot(@Nonnull ServerPlayer player, int slotId) {
        if (slotId < SLOT_MIN || slotId > SLOT_MAX) return null;
        
        ItemStack item;
        // Armor and offhand sit above the 36 regular inventory slots.
        switch (slotId) {
            case 36: item = player.getItemBySlot(EquipmentSlot.FEET); break;
            case 37: item = player.getItemBySlot(EquipmentSlot.LEGS); break;
            case 38: item = player.getItemBySlot(EquipmentSlot.CHEST); break;
            case 39: item = player.getItemBySlot(EquipmentSlot.HEAD); break;
            case 40: item = player.getItemBySlot(EquipmentSlot.OFFHAND); break;
            default: item = player.getInventory().getItem(slotId); break;
        }
        
        return item.isEmpty() ? null : item;
    }
    
    /** Collects the non-empty stacks in the inclusive slot range {@code [from, to]}. */
    @Nonnull
    public static List<ItemStack> range(@Nonnull Container container, int from, int to) {
        List<ItemStack> items = new ArrayList<>();
        int last = Math.min(to, container.getContainerSize() - 1);
        for (int slot = Math.max(from, 0); slot <= last; slot++) {
            ItemStack item = container.getItem(slot);
            if (!item.isEmpty()) items.add(item);
        }
        return items;
    }
    
    /** Collects every non-empty stack in the container. */
    @Nonnull
    public static List<ItemStack> all(@Nonnull Container container) {
        return range(container, 0, container.getContainerSize() - 1);
    }
}
