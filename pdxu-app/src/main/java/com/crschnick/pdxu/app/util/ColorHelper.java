package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.io.node.TaggedNode;
import com.crschnick.pdxu.io.node.ValueNode;
import com.crschnick.pdxu.model.GameColor;
import javafx.scene.paint.Color;

import java.util.List;

public class ColorHelper {

    public static int pickClosestColor(int input, int... colors) {
        int minDist = Integer.MAX_VALUE;
        int cMin = -1;
        int counter = 0;
        for (int c : colors) {
            if (Math.abs(input - c) < minDist) {
                minDist = Math.abs(input - c);
                cMin = counter;
            }
            counter++;
        }
        return cMin;
    }

    public static Color withAlpha(Color c, double alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static int getRed(int color) {
        return (color & 0x00FF0000) >>> 16;
    }

    public static int getGreen(int color) {
        return (color & 0x0000FF00) >>> 8;
    }

    public static int getBlue(int color) {
        return (color & 0x000000FF);
    }

    public static int getAlpha(int color) {
        return (color & 0xFF000000) >>> 24;
    }

    public static java.awt.Color toAwtColor(Color fx) {
        return new java.awt.Color((float) fx.getRed(),
                (float) fx.getGreen(),
                (float) fx.getBlue(),
                (float) fx.getOpacity());
    }

    public static int intFromColor(Color c) {
        return (0xFF << 24) + ((int) (c.getRed() * 0xFF) << 16) + ((int) (c.getGreen() * 0xFF) << 8) + ((int) (c.getBlue() * 0xFF));
    }

    public static TaggedNode toColorNode(Color c) {
        return new TaggedNode(TaggedNode.TagType.RGB, List.of(
                new ValueNode(String.valueOf((int) (c.getRed() * 255)), false),
                new ValueNode(String.valueOf((int) (c.getGreen() * 255)), false),
                new ValueNode(String.valueOf((int) (c.getBlue() * 255)), false)));
    }

    public static Color fromGameColor(GameColor color) {
        var c = color.getValues();
        return switch (color.getType()) {
            case HSV -> Color.hsb(
                    Double.parseDouble(c.get(0)) * 360,
                    Double.parseDouble(c.get(1)),
                    Double.parseDouble(c.get(2)));
            case HSV360 -> Color.hsb(
                    Double.parseDouble(c.get(0)),
                    Double.parseDouble(c.get(1)) / 360.0,
                    Double.parseDouble(c.get(2)) / 360.0);
            case RGB -> Color.color(
                    Double.parseDouble(c.get(0)) / 255.0,
                    Double.parseDouble(c.get(1)) / 255.0,
                    Double.parseDouble(c.get(2)) / 255.0);
            case HEX -> Color.valueOf("#" + color.getValues().get(0));
            default -> throw new IllegalStateException("Unexpected value: " + color.getType());
        };
    }
}