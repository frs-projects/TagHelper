package io.github.samarium150.minecraft.mod.taghelper.config;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * The switches the shared command code is written against.
 *
 * <p>Each loader declares the options in its own config spec ({@code forge/TagHelper},
 * {@code neoforge/TagHelper}) and binds the resulting values here; both specs' boolean values
 * are {@code Supplier<Boolean>}. The option names are the same on every loader so an existing
 * taghelper-common.toml carries over unchanged. See docs/PORTING.md.
 */
public final class TagHelperConfig {
    
    private static Supplier<Boolean> get = () -> true;
    private static Supplier<Boolean> set = () -> true;
    private static Supplier<Boolean> remove = () -> true;
    private static Supplier<Boolean> hotbar = () -> true;
    private static Supplier<Boolean> inventory = () -> true;
    private static Supplier<Boolean> enderChest = () -> true;
    
    private TagHelperConfig() { }
    
    /** Called once from the loader's mod constructor with that loader's config values. */
    public static void bind(@Nonnull Supplier<Boolean> get, @Nonnull Supplier<Boolean> set,
                            @Nonnull Supplier<Boolean> remove, @Nonnull Supplier<Boolean> hotbar,
                            @Nonnull Supplier<Boolean> inventory, @Nonnull Supplier<Boolean> enderChest) {
        TagHelperConfig.get = get;
        TagHelperConfig.set = set;
        TagHelperConfig.remove = remove;
        TagHelperConfig.hotbar = hotbar;
        TagHelperConfig.inventory = inventory;
        TagHelperConfig.enderChest = enderChest;
    }
    
    public static boolean getEnabled() { return get.get(); }
    
    public static boolean setEnabled() { return set.get(); }
    
    public static boolean removeEnabled() { return remove.get(); }
    
    public static boolean hotbarEnabled() { return hotbar.get(); }
    
    public static boolean inventoryEnabled() { return inventory.get(); }
    
    public static boolean enderChestEnabled() { return enderChest.get(); }
}
