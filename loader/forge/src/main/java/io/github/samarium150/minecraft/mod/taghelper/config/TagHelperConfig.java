package io.github.samarium150.minecraft.mod.taghelper.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Forge-backed configuration.
 *
 * <p>The static accessors below are the contract the shared command code is
 * written against; every loader layer supplies its own class with this name.
 * See docs/PORTING.md.
 */
public final class TagHelperConfig {
    
    public static final ForgeConfigSpec SPEC;
    
    private static final ForgeConfigSpec.BooleanValue ENABLE_GET;
    private static final ForgeConfigSpec.BooleanValue ENABLE_SET;
    private static final ForgeConfigSpec.BooleanValue ENABLE_REMOVE;
    private static final ForgeConfigSpec.BooleanValue ENABLE_HOTBAR;
    private static final ForgeConfigSpec.BooleanValue ENABLE_INVENTORY;
    private static final ForgeConfigSpec.BooleanValue ENABLE_ENDER_CHEST;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("configs").push("General");
        ENABLE_GET = builder.define("enableGetCommand", true);
        ENABLE_SET = builder.define("enableSetCommand", true);
        ENABLE_REMOVE = builder.define("enableRemoveCommand", true);
        ENABLE_HOTBAR = builder.define("enableHotbarCommands", true);
        ENABLE_INVENTORY = builder.define("enableInventoryCommands", true);
        ENABLE_ENDER_CHEST = builder.define("enableEnderChestCommands", true);
        builder.pop();
        SPEC = builder.build();
    }
    
    private TagHelperConfig() { }
    
    public static boolean getEnabled() { return ENABLE_GET.get(); }
    
    public static boolean setEnabled() { return ENABLE_SET.get(); }
    
    public static boolean removeEnabled() { return ENABLE_REMOVE.get(); }
    
    public static boolean hotbarEnabled() { return ENABLE_HOTBAR.get(); }
    
    public static boolean inventoryEnabled() { return ENABLE_INVENTORY.get(); }
    
    public static boolean enderChestEnabled() { return ENABLE_ENDER_CHEST.get(); }
}
