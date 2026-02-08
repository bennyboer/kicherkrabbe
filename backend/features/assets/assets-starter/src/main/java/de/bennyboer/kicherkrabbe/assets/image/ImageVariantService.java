package de.bennyboer.kicherkrabbe.assets.image;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.json.JsonMapper;
import de.bennyboer.kicherkrabbe.assets.*;
import de.bennyboer.kicherkrabbe.assets.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ImageVariantService {

    private static final String WEBP_CONTENT_TYPE = "image/webp";
    private static final int METADATA_VERSION = 1;
    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private final StorageService storageService;
    private final ConcurrentHashMap<String, Mono<Void>> inProgressGenerations = new ConcurrentHashMap<>();

    public ImageVariantService(StorageService storageService) {
        this.storageService = storageService;
    }

    @JsonSerialize
    @JsonDeserialize
    record VariantMetadata(int version, int originalWidth, List<Integer> generatedWidths) {
        static VariantMetadata of(int originalWidth, List<Integer> generatedWidths) {
            return new VariantMetadata(METADATA_VERSION, originalWidth, generatedWidths);
        }
    }

    public Mono<Boolean> variantsExist(AssetId assetId, Location location) {
        Location metadataLocation = metadataLocation(assetId);
        return storageService.exists(assetId, metadataLocation);
    }

    public Mono<Void> generateVariants(AssetId assetId, Location location, ContentType contentType) {
        if (!ImageProcessor.isImageContentType(contentType.getValue())) {
            return Mono.empty();
        }

        return variantsExist(assetId, location)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.empty();
                    }
                    return doGenerateVariants(assetId, location);
                });
    }

    private Mono<Void> doGenerateVariants(AssetId assetId, Location location) {
        String assetKey = assetId.getValue();

        Mono<Void> generationMono = loadOriginalBytes(assetId, location)
                .flatMap(originalBytes -> Mono.fromCallable(() -> {
                            ImageDimensions dimensions = ImageProcessor.readDimensions(originalBytes);
                            return new OriginalImageData(originalBytes, dimensions);
                        })
                        .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(data -> generateAllVariantsAndReturnWidths(assetId, data))
                .then()
                .doFinally(signal -> inProgressGenerations.remove(assetKey))
                .onErrorResume(IOException.class, e -> {
                    log.warn("Failed to generate variants for asset {}: {}", assetId, e.getMessage());
                    return Mono.empty();
                })
                .cache();

        Mono<Void> previous = inProgressGenerations.putIfAbsent(assetKey, generationMono);
        if (previous != null) {
            return previous;
        }

        return generationMono;
    }

    public Mono<AssetContent> loadBestMatchingVariant(AssetId assetId, Location location, int requestedWidth) {
        return loadMetadata(assetId)
                .map(metadata -> selectBestVariant(metadata.generatedWidths(), requestedWidth))
                .flatMap(selectedWidth -> loadVariant(assetId, selectedWidth));
    }

    public Mono<AssetContent> getOrGenerateBestMatchingVariant(
            AssetId assetId,
            Location location,
            ContentType contentType,
            int requestedWidth
    ) {
        return loadMetadata(assetId)
                .map(metadata -> selectBestVariant(metadata.generatedWidths(), requestedWidth))
                .flatMap(selectedWidth -> loadVariant(assetId, selectedWidth))
                .switchIfEmpty(Mono.defer(() -> generateVariantsAndLoad(assetId, location, contentType, requestedWidth)));
    }

    private Mono<AssetContent> generateVariantsAndLoad(
            AssetId assetId,
            Location location,
            ContentType contentType,
            int requestedWidth
    ) {
        if (!ImageProcessor.isImageContentType(contentType.getValue())) {
            return Mono.empty();
        }

        return doGenerateVariants(assetId, location)
                .then(loadBestMatchingVariant(assetId, location, requestedWidth));
    }

    private Mono<List<Integer>> generateAllVariantsAndReturnWidths(AssetId assetId, OriginalImageData data) {
        List<Integer> applicableWidths = computeApplicableWidths(data.dimensions.getWidth());

        return Flux.fromIterable(applicableWidths)
                .flatMap(targetWidth -> generateAndStoreVariant(assetId, data.bytes, targetWidth))
                .then(storeMetadata(assetId, data.dimensions.getWidth(), applicableWidths))
                .thenReturn(applicableWidths);
    }

    public Mono<Void> removeVariants(AssetId assetId, Location location) {
        return loadMetadata(assetId)
                .map(VariantMetadata::generatedWidths)
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

    private Mono<Void> storeMetadata(AssetId assetId, int originalWidth, List<Integer> generatedWidths) {
        Location metadataLoc = metadataLocation(assetId);
        VariantMetadata metadata = VariantMetadata.of(originalWidth, generatedWidths);
        try {
            byte[] metadataBytes = JSON_MAPPER.writeValueAsBytes(metadata);
            Flux<DataBuffer> buffers = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(metadataBytes));
            return storageService.store(assetId, metadataLoc, buffers);
        } catch (JacksonException e) {
            return Mono.error(new RuntimeException("Failed to serialize variant metadata", e));
        }
    }

    private Mono<VariantMetadata> loadMetadata(AssetId assetId) {
        Location metadataLoc = metadataLocation(assetId);
        return DataBufferUtils.join(storageService.load(assetId, metadataLoc))
                .flatMap(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    return parseMetadata(bytes);
                });
    }

    private Mono<VariantMetadata> parseMetadata(byte[] bytes) {
        String content = new String(bytes, StandardCharsets.UTF_8).trim();

        if (content.startsWith("{")) {
            try {
                return Mono.just(JSON_MAPPER.readValue(content, VariantMetadata.class));
            } catch (JacksonException e) {
                return Mono.error(new RuntimeException("Failed to parse variant metadata JSON", e));
            }
        }

        try {
            int originalWidth = Integer.parseInt(content);
            List<Integer> widths = computeApplicableWidths(originalWidth);
            return Mono.just(VariantMetadata.of(originalWidth, widths));
        } catch (NumberFormatException e) {
            return Mono.error(new RuntimeException("Failed to parse legacy variant metadata", e));
        }
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
