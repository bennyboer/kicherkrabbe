package de.bennyboer.kicherkrabbe.assets.storage;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.Location;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class DelegatingStorageService implements StorageService {

    private final StorageService fileStorageService;

    @Override
    public Mono<Void> store(AssetId assetId, Location location, Flux<DataBuffer> content) {
        return switch (location.getType()) {
            case FILE -> fileStorageService.store(assetId, location, content);
        };
    }

    @Override
    public Flux<DataBuffer> load(AssetId assetId, Location location) {
        return switch (location.getType()) {
            case FILE -> fileStorageService.load(assetId, location);
        };
    }

    @Override
    public Mono<Void> remove(AssetId assetId, Location location) {
        return switch (location.getType()) {
            case FILE -> fileStorageService.remove(assetId, location);
        };
    }

}
