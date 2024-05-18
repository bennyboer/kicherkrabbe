package de.bennyboer.kicherkrabbe.assets.storage;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.Location;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StorageService {

    Mono<Void> store(AssetId assetId, Location location, Flux<DataBuffer> content);

    Flux<DataBuffer> load(AssetId assetId, Location location);

    Mono<Void> remove(AssetId assetId, Location location);

}
