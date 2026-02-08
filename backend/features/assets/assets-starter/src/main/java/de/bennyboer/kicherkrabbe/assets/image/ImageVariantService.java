package de.bennyboer.kicherkrabbe.assets.image;

import de.bennyboer.kicherkrabbe.assets.*;
import de.bennyboer.kicherkrabbe.assets.storage.StorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class ImageVariantService {

    private static final String WEBP_CONTENT_TYPE = "image/webp";

    private final StorageService storageService;

    public Mono<Boolean> variantsExist(AssetId assetId, Location location) {
        Location metadataLocation = metadataLocation(assetId);
        return storageService.exists(assetId, metadataLocation);
    }

    public Mono<Void> generateVariants(AssetId assetId, Location location, ContentType contentType) {
        if (!ImageProcessor.isImageContentType(contentType.getValue())) {
            return Mono.empty();
        }

        return loadOriginalBytes(assetId, location)
                .flatMap(originalBytes -> Mono.fromCallable(() -> {
                            ImageDimensions dimensions = ImageProcessor.readDimensions(originalBytes);
                            return new OriginalImageData(originalBytes, dimensions);
                        })
                        .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(data -> generateAllVariants(assetId, data))
                .onErrorResume(IOException.class, e -> {
                    log.warn("Failed to generate variants for asset {}: {}", assetId, e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<AssetContent> loadBestMatchingVariant(AssetId assetId, Location location, int requestedWidth) {
        return findExistingVariantWidths(assetId)
                .map(widths -> selectBestVariant(widths, requestedWidth))
                .flatMap(selectedWidth -> loadVariant(assetId, selectedWidth));
    }

    public Mono<Void> removeVariants(AssetId assetId, Location location) {
        return loadOriginalWidth(assetId)
                .map(this::computeApplicableWidths)
                .onErrorResume(e -> {
                    log.debug("Could not read variant metadata for removal, trying standard widths: {}", e.getMessage());
                    return Mono.just(VariantWidths.ALL);
                })
                .defaultIfEmpty(VariantWidths.ALL)
                .flatMapMany(Flux::fromIterable)
                .flatMap(width -> {
                    Location variantLoc = variantLocation(assetId, width);
                    return storageService.remove(assetId, variantLoc);
                })
                .then()
                .then(removeMetadata(assetId));
    }

    private Mono<Void> removeMetadata(AssetId assetId) {
        Location metadataLoc = metadataLocation(assetId);
        return storageService.remove(assetId, metadataLoc);
    }

    private Mono<byte[]> loadOriginalBytes(AssetId assetId, Location location) {
        return DataBufferUtils.join(storageService.load(assetId, location))
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    return bytes;
                });
    }

    private Mono<Void> generateAllVariants(AssetId assetId, OriginalImageData data) {
        List<Integer> applicableWidths = computeApplicableWidths(data.dimensions.getWidth());

        return Flux.fromIterable(applicableWidths)
                .flatMap(targetWidth -> generateAndStoreVariant(assetId, data.bytes, targetWidth))
                .then()
                .then(storeMetadata(assetId, data.dimensions.getWidth()));
    }

    private Mono<Void> storeMetadata(AssetId assetId, int originalWidth) {
        Location metadataLoc = metadataLocation(assetId);
        byte[] metadataBytes = String.valueOf(originalWidth).getBytes();
        Flux<DataBuffer> buffers = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(metadataBytes));
        return storageService.store(assetId, metadataLoc, buffers);
    }

    private Mono<Integer> loadOriginalWidth(AssetId assetId) {
        Location metadataLoc = metadataLocation(assetId);
        return DataBufferUtils.join(storageService.load(assetId, metadataLoc))
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    return Integer.parseInt(new String(bytes).trim());
                });
    }

    private Location metadataLocation(AssetId assetId) {
        return Location.file(FileName.of(assetId.getValue() + "_meta"));
    }

    private List<Integer> computeApplicableWidths(int originalWidth) {
        List<Integer> widths = new ArrayList<>();

        for (int standardWidth : VariantWidths.ALL) {
            if (originalWidth > standardWidth) {
                widths.add(standardWidth);
            }
        }

        widths.add(originalWidth);
        return widths;
    }

    private Mono<Void> generateAndStoreVariant(AssetId assetId, byte[] originalBytes, int targetWidth) {
        return Mono.fromCallable(() -> {
                    try {
                        return ImageProcessor.resizeToWebP(originalBytes, targetWidth);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to generate variant for asset " + assetId.getValue() + " with width " + targetWidth, e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(variantBytes -> {
                    Location variantLoc = variantLocation(assetId, targetWidth);
                    Flux<DataBuffer> buffers = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(variantBytes));
                    return storageService.store(assetId, variantLoc, buffers);
                });
    }

    private Mono<List<Integer>> findExistingVariantWidths(AssetId assetId) {
        return loadOriginalWidth(assetId)
                .map(this::computeApplicableWidths);
    }

    private int selectBestVariant(List<Integer> availableWidths, int requestedWidth) {
        Optional<Integer> smallestAboveOrEqual = availableWidths.stream()
                .filter(w -> w >= requestedWidth)
                .min(Comparator.naturalOrder());

        return smallestAboveOrEqual.orElseGet(() -> availableWidths.stream()
                .max(Comparator.naturalOrder())
                .orElse(requestedWidth));
    }

    private Mono<AssetContent> loadVariant(AssetId assetId, int width) {
        Location variantLoc = variantLocation(assetId, width);
        Flux<DataBuffer> buffers = storageService.load(assetId, variantLoc);
        return Mono.just(AssetContent.of(ContentType.of(WEBP_CONTENT_TYPE), buffers));
    }

    private Location variantLocation(AssetId assetId, int width) {
        String variantFileName = assetId.getValue() + "_" + width;
        return Location.file(FileName.of(variantFileName));
    }

    private record OriginalImageData(byte[] bytes, ImageDimensions dimensions) {
    }

}
