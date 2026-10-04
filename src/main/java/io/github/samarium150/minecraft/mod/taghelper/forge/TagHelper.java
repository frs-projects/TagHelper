//? if forge {
/*package io.github.samarium150.minecraft.mod.taghelper.forge;

import io.github.samarium150.minecraft.mod.taghelper.command.TagHelperCommands;
import io.github.samarium150.minecraft.mod.taghelper.config.TagHelperConfig;
import io.github.samarium150.minecraft.mod.taghelper.util.GeneralUtil;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

// Forge entry point: declares the config spec, binds it to TagHelperConfig and registers the
// commands. Everything else is shared code.
//
// Only line comments here, and in every other loader-gated file: Stonecutter comments an
// inactive file out with one block comment, which a Javadoc block inside would end early.
@Mod(GeneralUtil.MOD_ID)
public final class TagHelper {
    
    private static final ForgeConfigSpec SPEC;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("configs").push("General");
        ForgeConfigSpec.BooleanValue get = builder.define("enableGetCommand", true);
        ForgeConfigSpec.BooleanValue set = builder.define("enableSetCommand", true);
        ForgeConfigSpec.BooleanValue remove = builder.define("enableRemoveCommand", true);
        ForgeConfigSpec.BooleanValue hotbar = builder.define("enableHotbarCommands", true);
        ForgeConfigSpec.BooleanValue inventory = builder.define("enableInventoryCommands", true);
        ForgeConfigSpec.BooleanValue enderChest = builder.define("enableEnderChestCommands", true);
        builder.pop();
        SPEC = builder.build();
        TagHelperConfig.bind(get, set, remove, hotbar, inventory, enderChest);
    }
    
    public TagHelper() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
        // The game event bus, not the mod bus: commands are registered per server start.
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }
    
    private void registerCommands(RegisterCommandsEvent event) {
        TagHelperCommands.register(event.getDispatcher());
    }
}
*///?}
