package me.ht9.hdtextures.util;

import me.ht9.hdtextures.mixinterface.ITextureFX;
import net.minecraft.src.TextureFX;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class CustomAnimation extends TextureFX {
    private Delegate delegate;
    private static Random rand = new Random();

    public CustomAnimation(int iconIndex, int tileImage, int tileSize, String name, int minScrollDelay,
            int maxScrollDelay) {
        super(iconIndex);

        this.iconIndex = iconIndex;
        this.tileImage = tileImage;
        this.tileSize = tileSize;

        BufferedImage custom = null;
        String imageName = (tileImage == 0 ? "/terrain.png" : "/gui/items.png");
        try {
            BufferedImage bi = HDTextureUtils.getResource(imageName);
            int baseRes = 16;
            if (bi != null) {
                baseRes = bi.getWidth() / 16;
            }
            ((ITextureFX) this).setTextureRes(baseRes);

            String customSrc = "/custom_" + name + ".png";
            custom = HDTextureUtils.getResource(customSrc);
            if (custom != null) {
                if (custom.getWidth() == baseRes && tileSize > 1) {
                    custom = HDTextureUtils.repeat(custom, tileSize);
                } else if (custom.getWidth() != baseRes * tileSize) {
                    custom = HDTextureUtils.rescale(custom, baseRes * tileSize);
                }
                ((ITextureFX) this).setTextureRes(custom.getWidth());
                imageName = customSrc;
                if (this.tileSize > 1) {
                    this.tileSize = 1;
                }
            } else if (this.tileSize > 1) {
                ((ITextureFX) this).setTextureRes(baseRes * this.tileSize);
                this.tileSize = 1;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        int res = ((ITextureFX) this).getTextureRes();
        this.imageData = new byte[res * res * 4];
        if (custom == null) {
            delegate = new Tile(imageName, iconIndex, minScrollDelay, maxScrollDelay);
        } else {
            delegate = new Strip(custom);
        }
    }

    private static void ARGBtoRGBA(int[] src, byte[] dest) {
        for (int i = 0; i < src.length; ++i) {
            int v = src[i];
            dest[(i * 4) + 3] = (byte) ((v >> 24) & 0xff);
            dest[(i * 4) + 0] = (byte) ((v >> 16) & 0xff);
            dest[(i * 4) + 1] = (byte) ((v >> 8) & 0xff);
            dest[(i * 4) + 2] = (byte) ((v >> 0) & 0xff);
        }
    }

    @Override
    public void onTick() {
        if (delegate != null) {
            delegate.onTick();
        }
    }

    private interface Delegate {
        void onTick();
    }

    private class Tile implements Delegate {
        private final int allButOneRow;
        private final int oneRow;
        private final int minScrollDelay;
        private final int maxScrollDelay;
        private final boolean isScrolling;
        private final byte[] temp;

        private int timer;

        Tile(String imageName, int iconIndex, int minScrollDelay, int maxScrollDelay) {
            int res = ((ITextureFX) CustomAnimation.this).getTextureRes();
            oneRow = res * 4;
            allButOneRow = (res - 1) * oneRow;
            this.minScrollDelay = minScrollDelay;
            this.maxScrollDelay = maxScrollDelay;
            isScrolling = (this.minScrollDelay >= 0);
            if (isScrolling) {
                temp = new byte[oneRow];
            } else {
                temp = null;
            }

            BufferedImage tiles;
            try {
                tiles = HDTextureUtils.getResource(imageName);
                if (tiles == null)
                    return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            int resInImage = tiles.getWidth() / 16;
            int tileX = (iconIndex % 16) * resInImage;
            int tileY = (iconIndex / 16) * resInImage;
            int[] imageBuf = new int[res * res];
            tiles.getRGB(tileX, tileY, res, res, imageBuf, 0, res);
            ARGBtoRGBA(imageBuf, imageData);
        }

        public void onTick() {
            if (isScrolling && (maxScrollDelay <= 0 || --timer <= 0)) {
                if (maxScrollDelay > 0) {
                    timer = rand.nextInt(maxScrollDelay - minScrollDelay + 1) + minScrollDelay;
                }
                System.arraycopy(imageData, allButOneRow, temp, 0, oneRow);
                System.arraycopy(imageData, 0, imageData, oneRow, allButOneRow);
                System.arraycopy(temp, 0, imageData, 0, oneRow);
            }
        }
    }

    private class Strip implements Delegate {
        private final int oneFrame;
        private final byte[] src;
        private final int numFrames;

        private int currentFrame;

        Strip(BufferedImage custom) {
            int res = ((ITextureFX) CustomAnimation.this).getTextureRes();
            oneFrame = res * res * 4;
            numFrames = custom.getHeight() / custom.getWidth();
            int[] imageBuf = new int[custom.getWidth() * custom.getHeight()];
            custom.getRGB(0, 0, custom.getWidth(), custom.getHeight(), imageBuf, 0, custom.getWidth());
            src = new byte[imageBuf.length * 4];
            ARGBtoRGBA(imageBuf, src);
        }

        public void onTick() {
            if (numFrames == 0)
                return;
            if (++currentFrame >= numFrames) {
                currentFrame = 0;
            }
            System.arraycopy(src, currentFrame * oneFrame, imageData, 0, oneFrame);
        }
    }
}
