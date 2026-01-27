package de.bennyboer.kicherkrabbe.assets.storage;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.FileName;
import de.bennyboer.kicherkrabbe.assets.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class StorageServiceTest {

    private StorageService service;

    protected abstract StorageService createRepo();

    @BeforeEach
    public void setUp() {
        service = createRepo();
    }

    @Test
    void storeAndRetrieveAsset() {
        // when: storing a resource
        var assetId = AssetId.create();
        var location = Location.file(FileName.of(assetId.getValue()));
        store(assetId, location, "Hello, World!");

        // then: the resource can be retrieved
        var result = load(assetId, location);
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void shouldDeleteAsset() {
        // given: an asset
        var assetId = AssetId.create();
        var location = Location.file(FileName.of(assetId.getValue()));
        store(assetId, location, "Hello, World!");

        // when: deleting the asset
        remove(assetId, location);

        // then: the asset is deleted
        var result = load(assetId, location);
        assertThat(result).isEmpty();
    }

    private void store(AssetId assetId, Location location, String content) {
        byte[] bytes = content.getBytes(UTF_8);
        Flux<DataBuffer> buffer$ = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(bytes));

        service.store(assetId, location, buffer$).block();
    }

    private String load(AssetId assetId, Location location) {
        Flux<DataBuffer> buffers$ = service.load(assetId, location);

        DataBuffer buffer = DataBufferUtils.join(buffers$).block();
        if (buffer == null) {
            return "";
        }

        return buffer.toString(UTF_8);
    }

    private void remove(AssetId assetId, Location location) {
        service.remove(assetId, location).block();
    }

}
