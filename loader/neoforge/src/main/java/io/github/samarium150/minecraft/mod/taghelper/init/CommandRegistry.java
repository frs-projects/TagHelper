package io.github.samarium150.minecraft.mod.taghelper.init;

import io.github.samarium150.minecraft.mod.taghelper.command.TagHelperCommands;
import io.github.samarium150.minecraft.mod.taghelper.util.GeneralUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = GeneralUtil.MOD_ID)
public final class CommandRegistry {
    
    private CommandRegistry() { }
    
    @SubscribeEvent
    public static void register(@Nonnull final RegisterCommandsEvent event) {
        TagHelperCommands.register(event.getDispatcher());
    }
}
