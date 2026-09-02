package io.github.samarium150.minecraft.mod.taghelper;

import io.github.samarium150.minecraft.mod.taghelper.config.TagHelperConfig;
import io.github.samarium150.minecraft.mod.taghelper.util.GeneralUtil;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(GeneralUtil.MOD_ID)
public final class TagHelper {
    
    public TagHelper(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, TagHelperConfig.SPEC);
    }
}
