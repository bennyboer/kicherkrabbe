package de.bennyboer.kicherkrabbe.assets.image;

import de.bennyboer.kicherkrabbe.assets.*;
import de.bennyboer.kicherkrabbe.assets.samples.SampleImage;
import de.bennyboer.kicherkrabbe.assets.storage.StorageService;
import de.bennyboer.kicherkrabbe.assets.storage.file.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageVariantServiceTest {

    @TempDir
    Path tempDir;

    private StorageService storageService;

    private ImageVariantService imageVariantService;

    @BeforeEach
    void setUp() {
        storageService = new FileStorageService(tempDir);
        imageVariantService = new ImageVariantService(storageService);
    }

    @Test
    void shouldDetectWhenVariantsDoNotExist() {
        AssetId assetId = AssetId.of("test-asset");
        Location location = Location.file(FileName.of("test-asset"));

        Boolean exists = imageVariantService.variantsExist(assetId, location).block();

        assertThat(exists).isFalse();
    }

    @Test
    void shouldDetectWhenVariantsExist() {
        AssetId assetId = AssetId.of("test-asset-2");
        Location location = Location.file(FileName.of("test-asset-2"));
        byte[] imageBytes = SampleImage.createJpeg(1000, 800);

        storeImage(assetId, location, imageBytes);
        imageVariantService.generateVariants(assetId, location, ContentType.of("image/jpeg")).block();

        Boolean exists = imageVariantService.variantsExist(assetId, location).block();
        assertThat(exists).isTrue();
    }

    @Test
    void shouldGenerateApplicableVariants() {
        AssetId assetId = AssetId.of("test-asset-3");
        Location location = Location.file(FileName.of("test-asset-3"));
        byte[] imageBytes = SampleImage.createJpeg(2000, 1500);

        storeImage(assetId, location, imageBytes);
        imageVariantService.generateVariants(assetId, location, ContentType.of("image/jpeg")).block();

        assertVariantExists(assetId, 384);
        assertVariantExists(assetId, 768);
        assertVariantExists(assetId, 1536);
        assertVariantExists(assetId, 2000);
    }

    @Test
    void shouldNotGenerateVariantsLargerThanOriginal() {
        AssetId assetId = AssetId.of("test-asset-4");
        Location location = Location.file(FileName.of("test-asset-4"));
        byte[] imageBytes = SampleImage.createJpeg(500, 400);

        storeImage(assetId, location, imageBytes);
        imageVariantService.generateVariants(assetId, location, ContentType.of("image/jpeg")).block();

        assertVariantExists(assetId, 384);
        assertVariantExists(assetId, 500);
        assertVariantDoesNotExist(assetId, 768);
        assertVariantDoesNotExist(assetId, 1536);
    }

    @Test
    void shouldSelectBestMatchingVariant() {
        AssetId assetId = AssetId.of("test-asset-5");
        Location location = Location.file(FileName.of("test-asset-5"));
        byte[] imageBytes = SampleImage.createJpeg(2000, 1500);

        storeImage(assetId, location, imageBytes);
        imageVariantService.generateVariants(assetId, location, ContentType.of("image/jpeg")).block();

        AssetContent content = imageVariantService.loadBestMatchingVariant(assetId, location, 500).block();

        assertThat(content).isNotNull();
        assertThat(content.getContentType().getValue()).isEqualTo("image/webp");
        byte[] variantBytes = readBytes(content);
        ImageDimensions dims = readDimensions(variantBytes);
        assertThat(dims.getWidth()).isEqualTo(768);
    }

    @Test
    void shouldSelectLargestVariantWhenRequestedWidthExceedsAll() {
        AssetId assetId = AssetId.of("test-asset-6");
        Location location = Location.file(FileName.of("test-asset-6"));
        byte[] imageBytes = SampleImage.createJpeg(1200, 900);

        storeImage(assetId, location, imageBytes);
        imageVariantService.generateVariants(assetId, location, ContentType.of("image/jpeg")).block();

        AssetContent content = imageVariantService.loadBestMatchingVariant(assetId, location, 2000).block();

        assertThat(content).isNotNull();
        byte[] variantBytes = readBytes(content);
        ImageDimensions dims = readDimensions(variantBytes);
        assertThat(dims.getWidth()).isEqualTo(1200);
    }

    @Test
    void shouldRemoveAllVariantsOnDelete() {
        AssetId assetId = AssetId.of("test-asset-7");
        Location location = Location.file(FileName.of("test-asset-7"));
        byte[] imageBytes = SampleImage.createJpeg(1000, 800);

        storeImage(assetId, location, imageBytes);
        imageVariantService.generateVariants(assetId, location, ContentType.of("image/jpeg")).block();
        assertVariantExists(assetId, 384);
        assertVariantExists(assetId, 768);

        imageVariantService.removeVariants(assetId, location).block();

        assertVariantDoesNotExist(assetId, 384);
        assertVariantDoesNotExist(assetId, 768);
        assertVariantDoesNotExist(assetId, 1000);
    }

    private void storeImage(AssetId assetId, Location location, byte[] bytes) {
        Flux<DataBuffer> buffers = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(bytes));
        storageService.store(assetId, location, buffers).block();
    }

    private void assertVariantExists(AssetId assetId, int width) {
        Location variantLocation = Location.file(FileName.of(assetId.getValue() + "_" + width));
        Boolean exists = storageService.exists(assetId, variantLocation).block();
        assertThat(exists).as("Variant with width %d should exist", width).isTrue();
    }

    private void assertVariantDoesNotExist(AssetId assetId, int width) {
        Location variantLocation = Location.file(FileName.of(assetId.getValue() + "_" + width));
        Boolean exists = storageService.exists(assetId, variantLocation).block();
        assertThat(exists).as("Variant with width %d should not exist", width).isFalse();
    }

    private byte[] readBytes(AssetContent content) {
        DataBuffer buffer = DataBufferUtils.join(content.getBuffers()).block();
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        DataBufferUtils.release(buffer);
        return bytes;
    }

    private ImageDimensions readDimensions(byte[] bytes) {
        try {
            return ImageProcessor.readDimensions(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
