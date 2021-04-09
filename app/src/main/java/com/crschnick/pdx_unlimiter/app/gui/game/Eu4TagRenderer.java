package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.core.CacheManager;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Eu4TagRenderer {

    private static final int SMALL_IMG_SIZE = 64;
    private static final int IMG_SIZE = 256;

    private static BufferedImage createBasicFlagImage(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        switch (tag.getType()) {
            case OBSERVER -> {
                return ImageLoader.fromFXImage(eu4TagImage(GameImage.getEu4TagPath("REB"), info));
            }
            case NORMAL -> {
                return ImageLoader.fromFXImage(eu4TagImage(info, tag));
            }
            case COLONIAL_FLAG -> {
                var ov = Eu4Tag.getTag(info.getAllTags(), tag.getColonialData().getOverlord());
                BufferedImage flagImage = ImageLoader.fromFXImage(eu4TagImage(info, ov));
                Graphics g = flagImage.getGraphics();

                java.awt.Color awtColor = ColorHelper.toAwtColor(ColorHelper.fromGameColor(tag.getCountryColor()));
                g.setColor(awtColor);
                g.fillRect(flagImage.getWidth() / 2, 0, flagImage.getWidth() / 2, flagImage.getWidth());
                return flagImage;
            }
            case CUSTOM_FLAG -> {
                BufferedImage flagImage = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
                var custom = tag.getCustomData();
                var cache = CacheManager.getInstance().get(Eu4CustomFlagCache.class);
                cache.renderTexture(flagImage, custom.getFlagId(), custom.getFlagColors(), custom.getSymbolId());
                return flagImage;
            }
        }
        throw new AssertionError();
    }

    public static Image smallShieldImage(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        BufferedImage flagImage = createBasicFlagImage(info, tag);
        applyMask(flagImage, GameImage.EU4_SMALL_SHIELD_MASK);

        BufferedImage i = new BufferedImage(SMALL_IMG_SIZE, SMALL_IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        g.drawImage(flagImage,
                8,
                5,
                i.getWidth() - 16,
                i.getHeight() - 13,
                new java.awt.Color(0, 0, 0, 0),
                null);

        g.drawImage(ImageLoader.fromFXImage(GameImage.EU4_SMALL_SHIELD_FRAME),
                -2,
                -4,
                i.getWidth() + 4,
                i.getWidth() + 4,
                new java.awt.Color(0, 0, 0, 0),
                null);

        return ImageLoader.toFXImage(i);
    }

    public static Image shieldImage(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        BufferedImage flagImage = createBasicFlagImage(info, tag);
        applyMask(flagImage, GameImage.EU4_SHIELD_MASK);

        BufferedImage i = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        g.drawImage(flagImage,
                32,
                42,
                i.getWidth() - 64,
                i.getHeight() - 64,
                new java.awt.Color(0, 0, 0, 0),
                null);

        g.drawImage(ImageLoader.fromFXImage(GameImage.EU4_SHIELD_FRAME),
                -25,
                0,
                i.getWidth() + 50,
                i.getHeight() + 16,
                new java.awt.Color(0, 0, 0, 0),
                null);

        return ImageLoader.toFXImage(i);
    }

    private static void applyMask(BufferedImage awtImage, Image mask) {
        double xF = mask.getWidth() / awtImage.getWidth();
        double yF = mask.getHeight() / awtImage.getHeight();
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int argb = awtImage.getRGB(x, y);
                int maskArgb = mask.getPixelReader().getArgb(
                        (int) Math.floor(xF * x), (int) Math.floor(yF * y));
                int maskAlpha = maskArgb & 0xFF000000;

                int color = (argb & 0x00FFFFFF) + maskAlpha;
                awtImage.setRGB(x, y, color);
            }
        }
    }

    private static Image eu4TagImage(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        return CacheManager.getInstance().get(Eu4TagImageCache.class).tagImages.computeIfAbsent(
                tag.getTag(), s -> eu4TagImage(GameImage.getEu4TagPath(s), info));
    }

    private static Image eu4TagImage(Path path, SavegameInfo<Eu4Tag> info) {
        var in = CascadeDirectoryHelper.openFile(path, info);
        return ImageLoader.loadImage(in.orElse(null), null);
    }

    public static class Eu4TagImageCache extends CacheManager.Cache {
        Map<String, Image> tagImages = new ConcurrentHashMap<>();

        public Eu4TagImageCache() {
            super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
        }
    }
}
