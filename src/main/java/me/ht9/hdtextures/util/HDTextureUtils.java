package me.ht9.hdtextures.util;

import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class HDTextureUtils {
    public static int terrainTexSize = 16;
    public static int itemsTexSize = 16;
    public static HashMap<String, BufferedImage> path2bufImg = new HashMap<>();

    public static void initTextures() {
        Minecraft mc = Globals.mc;
        if (mc == null || mc.texturePackList == null || mc.texturePackList.selectedTexturePack == null)
            return;

        path2bufImg.clear();
        try {
            BufferedImage items = getResource("/gui/items.png");
            BufferedImage terrain = getResource("/terrain.png");

            if (items != null) {
                itemsTexSize = items.getWidth() / 16;
            }
            if (terrain != null) {
                terrainTexSize = terrain.getWidth() / 16;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage getResource(String src) throws IOException {
        if (path2bufImg.containsKey(src)) {
            return path2bufImg.get(src);
        }
        Minecraft mc = Globals.mc;
        InputStream res = mc.texturePackList.selectedTexturePack.getResourceAsStream(src);
        if (res == null)
            return null;

        BufferedImage img = ImageIO.read(res);
        res.close();
        path2bufImg.put(src, img);
        return img;
    }

    public static BufferedImage getRescaledResource(String src, int size) throws IOException {
        BufferedImage img = getResource(src);
        if (img == null)
            return null;

        if (img.getWidth() == size)
            return img;
        return rescale(img, size);
    }

    public static void registerCustomAnimations(net.minecraft.src.RenderEngine renderEngine) {
        initTextures();
        java.util.List textureList = ((me.ht9.hdtextures.mixin.accessor.RenderEngineAccessor) renderEngine)
                .getTextureList();

        if (textureList != null) {
            textureList.removeIf(o -> o instanceof net.minecraft.src.TextureWaterFX ||
                    o instanceof net.minecraft.src.TextureWaterFlowFX ||
                    o instanceof net.minecraft.src.TextureLavaFX ||
                    o instanceof net.minecraft.src.TextureLavaFlowFX ||
                    o instanceof net.minecraft.src.TexturePortalFX ||
                    o instanceof net.minecraft.src.TextureFlamesFX ||
                    o instanceof CustomAnimation);
        }

        renderEngine
                .registerTextureFX(new CustomAnimation(net.minecraft.src.Block.waterMoving.blockIndexInTexture + 1, 0,
                        2, "water_flowing", 0, 0));
        renderEngine.registerTextureFX(new CustomAnimation(net.minecraft.src.Block.waterMoving.blockIndexInTexture, 0,
                1, "water_still", -1, -1));
        renderEngine
                .registerTextureFX(new CustomAnimation(net.minecraft.src.Block.lavaMoving.blockIndexInTexture + 1, 0,
                        2, "lava_flowing", 3, 6));
        renderEngine.registerTextureFX(new CustomAnimation(net.minecraft.src.Block.lavaMoving.blockIndexInTexture, 0, 1,
                "lava_still", -1, -1));
        renderEngine.registerTextureFX(
                new CustomAnimation(net.minecraft.src.Block.portal.blockIndexInTexture, 0, 2, "portal", -1, -1));
        renderEngine.registerTextureFX(
                new CustomAnimation(net.minecraft.src.Block.fire.blockIndexInTexture, 0, 1, "fire_e_w", 2, 4));
        renderEngine.registerTextureFX(
                new CustomAnimation(net.minecraft.src.Block.fire.blockIndexInTexture + 16, 0, 1, "fire_n_s", 2, 4));
    }

    public static BufferedImage repeat(BufferedImage img, int count) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage newImage = new BufferedImage(w * count, h * count, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        for (int x = 0; x < count; x++) {
            for (int y = 0; y < count; y++) {
                g.drawImage(img, x * w, y * h, (ImageObserver) null);
            }
        }
        g.dispose();
        return newImage;
    }

    public static BufferedImage rescale(BufferedImage img, int width) {
        if (img.getWidth() == width)
            return img;
        int height = (img.getHeight() * width) / img.getWidth();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = newImage.createGraphics();
        graphics2D.drawImage(img, 0, 0, width, height, (ImageObserver) null);
        graphics2D.dispose();
        return newImage;
    }
}
