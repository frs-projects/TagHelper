package io.github.samarium150.minecraft.mod.taghelper;

import io.github.samarium150.minecraft.mod.taghelper.config.TagHelperConfig;
import io.github.samarium150.minecraft.mod.taghelper.util.GeneralUtil;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(GeneralUtil.MOD_ID)
public final class TagHelper {
    
    public TagHelper() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TagHelperConfig.SPEC);
    }
}
