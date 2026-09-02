package io.github.samarium150.minecraft.mod.taghelper.init;

import io.github.samarium150.minecraft.mod.taghelper.command.TagHelperCommands;
import io.github.samarium150.minecraft.mod.taghelper.util.GeneralUtil;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = GeneralUtil.MOD_ID)
public final class CommandRegistry {
    
    private CommandRegistry() { }
    
    @SubscribeEvent
    public static void register(@Nonnull final RegisterCommandsEvent event) {
        TagHelperCommands.register(event.getDispatcher());
    }
}
