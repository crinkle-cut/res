package me.ht9.hdtextures.mixin;

import me.ht9.hdtextures.mixinterface.IFontRenderer;
import me.ht9.hdtextures.mixin.accessor.TexturePackListAccessor;
import me.ht9.hdtextures.util.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GameSettings;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TexturePackBase;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

@Mixin(FontRenderer.class)
public class MixinFontRenderer implements IFontRenderer {
    @Shadow
    private int[] charWidth;
    @Shadow
    public int fontTextureName;
    @Shadow
    private int fontDisplayLists;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(GameSettings gameSettings, String fontPath, RenderEngine renderEngine, CallbackInfo ci) {
        doReload(gameSettings, fontPath, renderEngine);
    }

    @Override
    public void reload() {
        if (Globals.mc.fontRenderer != null) {
            doReload(Globals.mc.gameSettings, "/font/default.png", Globals.mc.renderEngine);
        }
    }

    @Unique
    private void doReload(GameSettings gameSettings, String fontPath, RenderEngine renderEngine) {
        BufferedImage fontImage = null;
        try {
            if (Globals.mc.texturePackList == null || Globals.mc.texturePackList.selectedTexturePack == null) {
                // Fallback if texture pack list isn't ready (shouldn't happen in game usually)
                InputStream is = FontRenderer.class.getResourceAsStream(fontPath);
                if (is != null)
                    fontImage = ImageIO.read(is);
            } else {
                InputStream is = Globals.mc.texturePackList.selectedTexturePack.getResourceAsStream(fontPath);
                if (is == null) {
                    // Fallback to default pack if selected pack is missing font
                    TexturePackBase defaultPack = ((TexturePackListAccessor) Globals.mc.texturePackList)
                            .getDefaultTexturePack();
                    if (defaultPack != null) {
                        is = defaultPack.getResourceAsStream(fontPath);
                    }
                }

                if (is != null) {
                    fontImage = ImageIO.read(is);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fontImage == null) {
            return;
        }

        int width = fontImage.getWidth();
        int height = fontImage.getHeight();
        int[] rgb = new int[width * height];
        fontImage.getRGB(0, 0, width, height, rgb, 0, width);

        int charSize = width / 16;

        for (int k = 0; k < 256; ++k) {
            int col = k % 16;
            int row = k / 16;

            int measuredWidth = -1;
            for (int x = charSize - 1; x >= 0; x--) {
                int xOffset = col * charSize + x;
                boolean columnEmpty = true;
                for (int y = 0; y < charSize; y++) {
                    int yOffset = (row * charSize + y) * width;
                    if ((rgb[xOffset + yOffset] & 0xFF) > 0) {
                        columnEmpty = false;
                        break;
                    }
                }
                if (!columnEmpty) {
                    measuredWidth = x;
                    break;
                }
            }

            if (k == 32) {
                measuredWidth = width / 64 - 1;
            }

            this.charWidth[k] = (128 * measuredWidth + 256) / width;
            if (k == 32) {
                this.charWidth[k] = (128 * (width / 64) + 256) / width;
            }
        }

        this.fontTextureName = renderEngine.allocateAndSetupTexture(fontImage);

        Tessellator tessellator = Tessellator.instance;
        for (int i = 0; i < 256; ++i) {
            GL11.glNewList(this.fontDisplayLists + i, GL11.GL_COMPILE);
            tessellator.startDrawingQuads();
            int l = (i % 16) * 8;
            int m = (i / 16) * 8;
            float f = 7.99f;
            float n = 0.0f;
            float o = 0.0f;

            tessellator.addVertexWithUV(0.0D, (double) (0.0F + f), 0.0D, (double) ((float) l / 128.0F + n),
                    (double) (((float) m + f) / 128.0F + o));
            tessellator.addVertexWithUV((double) (0.0F + f), (double) (0.0F + f), 0.0D,
                    (double) (((float) l + f) / 128.0F + n), (double) (((float) m + f) / 128.0F + o));
            tessellator.addVertexWithUV((double) (0.0F + f), 0.0D, 0.0D, (double) (((float) l + f) / 128.0F + n),
                    (double) ((float) m / 128.0F + o));
            tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, (double) ((float) l / 128.0F + n),
                    (double) ((float) m / 128.0F + o));
            tessellator.draw();
            GL11.glTranslatef((float) this.charWidth[i], 0.0F, 0.0F);
            GL11.glEndList();
        }
    }
}
