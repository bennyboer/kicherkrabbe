package de.bennyboer.kicherkrabbe.assets.samples;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class SampleImage {

    private SampleImage() {
    }

    public static byte[] createJpeg(int width, int height) {
        return createImage(width, height, "jpg");
    }

    public static byte[] createPng(int width, int height) {
        return createImage(width, height, "png");
    }

    private static byte[] createImage(int width, int height, String format) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);
        graphics.drawLine(0, 0, width, height);
        graphics.dispose();

        try (var output = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create sample image", e);
        }
    }

}
