package me.ht9.hdtextures.mixin.accessor;

import net.minecraft.src.RenderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;

@Mixin(RenderEngine.class)
public interface RenderEngineAccessor {
    @Accessor("textureList")
    List getTextureList();
}
