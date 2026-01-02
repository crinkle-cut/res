package me.ht9.hdtextures.mixin;

import me.ht9.hdtextures.mixinterface.IFontRenderer;
import me.ht9.hdtextures.mixinterface.ITextureFX;
import me.ht9.hdtextures.util.Globals;
import me.ht9.hdtextures.util.HDTextureUtils;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.TextureFX;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

@Mixin(RenderEngine.class)
public abstract class MixinRenderEngine {
    @Shadow
    public ByteBuffer imageData;
    @Unique
    private TextureFX currentTextureFX;

    @Inject(method = "refreshTextures", at = @At("HEAD"))
    private void refreshTextures$head(CallbackInfo ci) {
        // Init/Refresh HD Textures (metadata)
        HDTextureUtils.initTextures();

        // Reload Font
        if (Globals.mc.fontRenderer != null) {
            ((IFontRenderer) Globals.mc.fontRenderer).reload();
        }

        // Reload Custom Animations
        HDTextureUtils.registerCustomAnimations((RenderEngine) (Object) this);
    }

    @Inject(method = "setupTexture", at = @At("HEAD"))
    private void setupTexture$head(BufferedImage var1, int var2, CallbackInfo ci) {
        int size = var1.getWidth() * var1.getHeight() * 4;
        if (this.imageData.capacity() < size) {
            this.imageData = net.minecraft.src.GLAllocation.createDirectByteBuffer(size);
        }
    }

    @Redirect(method = "updateDynamicTextures", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/TextureFX;onTick()V"))
    private void updateDynamicTextures$onTick(TextureFX instance) {
        this.currentTextureFX = instance;
        instance.onTick();
    }

    @Redirect(method = "updateDynamicTextures", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTexSubImage2D(IIIIIIIILjava/nio/ByteBuffer;)V", remap = false))
    private void updateDynamicTextures$glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, ByteBuffer pixels) {
        TextureFX textureFX = this.currentTextureFX;
        if (textureFX == null) {
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
            return;
        }

        int spriteSize = 16;
        if (textureFX.tileImage == 0)
            spriteSize = HDTextureUtils.terrainTexSize;
        else if (textureFX.tileImage == 1)
            spriteSize = HDTextureUtils.itemsTexSize;

        int res = ((ITextureFX) textureFX).getTextureRes();

        int multiplier = spriteSize / 16;
        int resMultiplier = res / 16;

        GL11.glTexSubImage2D(
                target,
                level,
                xoffset * multiplier,
                yoffset * multiplier,
                width * resMultiplier,
                height * resMultiplier,
                format,
                type,
                pixels);
    }

    @Inject(method = "readTextureImage", at = @At("HEAD"), cancellable = true)
    private void readTextureImage$head(java.io.InputStream stream,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<BufferedImage> cir) {
        if (stream == null) {
            // Return a 1x1 transparent image to prevent crash
            cir.setReturnValue(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        }
    }
}
