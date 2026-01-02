package me.ht9.hdtextures.mixin;

import me.ht9.hdtextures.mixinterface.ITextureFX;
import net.minecraft.src.TextureFX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextureFX.class)
public class MixinTextureFX implements ITextureFX {
    @Unique
    public int textureRes = 16;

    @Override
    public int getTextureRes() {
        return textureRes;
    }

    @Override
    public void setTextureRes(int res) {
        this.textureRes = res;
    }
}
