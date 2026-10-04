//? if neoforge {
package io.github.samarium150.minecraft.mod.taghelper.neoforge;

import io.github.samarium150.minecraft.mod.taghelper.command.TagHelperCommands;
import io.github.samarium150.minecraft.mod.taghelper.config.TagHelperConfig;
import io.github.samarium150.minecraft.mod.taghelper.util.GeneralUtil;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// NeoForge entry point: declares the config spec, binds it to TagHelperConfig and registers the
// commands. Everything else is shared code.
//
// Only line comments here, and in every other loader-gated file: Stonecutter comments an
// inactive file out with one block comment, which a Javadoc block inside would end early.
@Mod(GeneralUtil.MOD_ID)
public final class TagHelper {
    
    private static final ModConfigSpec SPEC;
    
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("configs").push("General");
        ModConfigSpec.BooleanValue get = builder.define("enableGetCommand", true);
        ModConfigSpec.BooleanValue set = builder.define("enableSetCommand", true);
        ModConfigSpec.BooleanValue remove = builder.define("enableRemoveCommand", true);
        ModConfigSpec.BooleanValue hotbar = builder.define("enableHotbarCommands", true);
        ModConfigSpec.BooleanValue inventory = builder.define("enableInventoryCommands", true);
        ModConfigSpec.BooleanValue enderChest = builder.define("enableEnderChestCommands", true);
        builder.pop();
        SPEC = builder.build();
        TagHelperConfig.bind(get, set, remove, hotbar, inventory, enderChest);
    }
    
    public TagHelper(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
        // The game event bus, not the mod bus: commands are registered per server start.
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }
    
    private void registerCommands(RegisterCommandsEvent event) {
        TagHelperCommands.register(event.getDispatcher());
    }
}
//?}
