package de.bennyboer.kicherkrabbe.assets.image;

import de.bennyboer.kicherkrabbe.assets.samples.SampleImage;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageProcessorTest {

    @Test
    void shouldReadImageDimensions() throws IOException {
        byte[] imageData = SampleImage.createJpeg(800, 600);

        ImageDimensions dimensions = ImageProcessor.readDimensions(imageData);

        assertThat(dimensions.getWidth()).isEqualTo(800);
        assertThat(dimensions.getHeight()).isEqualTo(600);
    }

    @Test
    void shouldResizeImagePreservingAspectRatio() throws IOException {
        byte[] originalImage = SampleImage.createJpeg(1000, 500);

        byte[] resizedImage = ImageProcessor.resizeToWebP(originalImage, 500);

        ImageDimensions dimensions = ImageProcessor.readDimensions(resizedImage);
        assertThat(dimensions.getWidth()).isEqualTo(500);
        assertThat(dimensions.getHeight()).isEqualTo(250);
    }

    @Test
    void shouldConvertToWebP() throws IOException {
        byte[] jpegImage = SampleImage.createJpeg(200, 200);

        byte[] webpImage = ImageProcessor.convertToWebP(jpegImage);

        assertThat(webpImage).isNotEmpty();
        assertThat(webpImage).isNotEqualTo(jpegImage);

        ImageDimensions dimensions = ImageProcessor.readDimensions(webpImage);
        assertThat(dimensions.getWidth()).isEqualTo(200);
        assertThat(dimensions.getHeight()).isEqualTo(200);
    }

    @Test
    void shouldIdentifyImageContentTypes() {
        assertThat(ImageProcessor.isImageContentType("image/jpeg")).isTrue();
        assertThat(ImageProcessor.isImageContentType("image/png")).isTrue();
        assertThat(ImageProcessor.isImageContentType("image/gif")).isTrue();
        assertThat(ImageProcessor.isImageContentType("image/webp")).isTrue();
        assertThat(ImageProcessor.isImageContentType("IMAGE/JPEG")).isTrue();
    }

    @Test
    void shouldNotIdentifyNonImageContentTypes() {
        assertThat(ImageProcessor.isImageContentType("text/plain")).isFalse();
        assertThat(ImageProcessor.isImageContentType("application/json")).isFalse();
        assertThat(ImageProcessor.isImageContentType("application/pdf")).isFalse();
        assertThat(ImageProcessor.isImageContentType(null)).isFalse();
    }

    @Test
    void shouldNotUpscaleWhenTargetIsLargerThanOriginal() throws IOException {
        byte[] originalImage = SampleImage.createJpeg(300, 200);

        byte[] result = ImageProcessor.resizeToWebP(originalImage, 500);

        ImageDimensions dimensions = ImageProcessor.readDimensions(result);
        assertThat(dimensions.getWidth()).isEqualTo(300);
        assertThat(dimensions.getHeight()).isEqualTo(200);
    }

}
