package me.ht9.hdtextures.mixin.accessor;

import net.minecraft.src.TexturePackList;
import net.minecraft.src.TexturePackBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TexturePackList.class)
public interface TexturePackListAccessor {
    @Accessor("defaultTexturePack")
    TexturePackBase getDefaultTexturePack();
}
