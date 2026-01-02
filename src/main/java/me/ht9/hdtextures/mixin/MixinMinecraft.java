package me.ht9.hdtextures.mixin;

import me.ht9.hdtextures.util.HDTextureUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.src.RenderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow
    public RenderEngine renderEngine;

    @Inject(method = "startGame", at = @At("RETURN"))
    public void glContextCreated(CallbackInfo ci) {
        HDTextureUtils.registerCustomAnimations(this.renderEngine);
    }
}
