package de.bennyboer.kicherkrabbe.assets.image;

import dev.matrixlab.webp4j.WebPCodec;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public final class ImageProcessor {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private ImageProcessor() {
    }

    public static ImageDimensions readDimensions(byte[] imageData) throws IOException {
        if (isWebP(imageData)) {
            int[] dimensions = WebPCodec.getWebPInfo(imageData);
            return ImageDimensions.of(dimensions[0], dimensions[1]);
        }

        try (var input = new ByteArrayInputStream(imageData);
             var imageInputStream = ImageIO.createImageInputStream(input)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                throw new IOException("No image reader found for the given image data");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInputStream);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                return ImageDimensions.of(width, height);
            } finally {
                reader.dispose();
            }
        }
    }

    private static boolean isWebP(byte[] imageData) {
        if (imageData == null || imageData.length < 12) {
            return false;
        }
        return imageData[0] == 'R' && imageData[1] == 'I' && imageData[2] == 'F' && imageData[3] == 'F'
                && imageData[8] == 'W' && imageData[9] == 'E' && imageData[10] == 'B' && imageData[11] == 'P';
    }

    public static byte[] resizeToWebP(byte[] imageData, int targetWidth) throws IOException {
        BufferedImage originalImage = readImage(imageData);
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (targetWidth >= originalWidth) {
            return convertToWebP(imageData);
        }

        double ratio = (double) targetWidth / originalWidth;
        int targetHeight = (int) Math.round(originalHeight * ratio);

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, targetWidth, targetHeight);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();

        return writeWebP(resizedImage);
    }

    public static byte[] convertToWebP(byte[] imageData) throws IOException {
        BufferedImage image = readImage(imageData);
        return writeWebP(image);
    }

    public static boolean isImageContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    private static BufferedImage readImage(byte[] imageData) throws IOException {
        if (isWebP(imageData)) {
            return WebPCodec.decodeImage(imageData);
        }

        try (var input = new ByteArrayInputStream(imageData)) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new IOException("Could not read image data");
            }
            return image;
        }
    }

    private static byte[] writeWebP(BufferedImage image) throws IOException {
        BufferedImage rgbImage = convertToRGB(image);
        return WebPCodec.encodeImage(rgbImage, 80.0f);
    }

    private static BufferedImage convertToRGB(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB || image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return image;
        }

        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgbImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return rgbImage;
    }

}
