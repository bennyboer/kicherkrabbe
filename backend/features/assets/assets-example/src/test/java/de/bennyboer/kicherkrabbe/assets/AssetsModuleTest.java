package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.assets.image.ImageVariantService;
import de.bennyboer.kicherkrabbe.assets.samples.SampleAsset;
import de.bennyboer.kicherkrabbe.assets.storage.StorageService;
import de.bennyboer.kicherkrabbe.assets.storage.file.FileStorageService;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;

public class AssetsModuleTest {

    @TempDir
    Path tempDir;

    private AssetService assetService;

    private PermissionsService permissionsService;

    private StorageService storageService;

    private ImageVariantService imageVariantService;

    private AssetsModule module;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(
                new InMemoryEventSourcingRepo(),
                new LoggingEventPublisher(),
                Clock.systemUTC()
        );

        permissionsService = new PermissionsService(
                new InMemoryPermissionsRepo(),
                ignored -> Mono.empty()
        );

        storageService = new FileStorageService(tempDir);
        imageVariantService = new ImageVariantService(storageService);

        var config = new AssetsModuleConfig();
        module = config.assetsModule(
                assetService,
                permissionsService,
                storageService,
                imageVariantService
        );
    }

    public void allowUserToCreateAssets(String userId) {
        module.allowUserToCreateAssets(userId).block();
    }

    public String uploadAsset(String contentType, byte[] content, Agent agent) {
        Flux<DataBuffer> buffer$ = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(content));

        String assetId = module.uploadAsset(contentType, buffer$, agent).block();

        if (agent.getType() == AgentType.USER) {
            allowUserToManageAsset(assetId, agent.getId().getValue());
        }
        allowAnonymousUsersToReadAsset(assetId);

        return assetId;
    }

    public String uploadAsset(SampleAsset sample, Agent agent) {
        return uploadAsset(sample.getContentType(), sample.getContent(), agent);
    }

    public String uploadSampleAsset(Agent agent) {
        return uploadAsset(SampleAsset.builder().build(), agent);
    }

    public void deleteAsset(String assetId, long version, Agent agent) {
        module.deleteAsset(assetId, version, agent).block();
    }

    public byte[] getAssetContent(String assetId, Agent agent) {
        return getAssetContent(assetId, null, agent);
    }

    public byte[] getAssetContent(String assetId, @Nullable Integer width, Agent agent) {
        Flux<DataBuffer> buffers$ = module.getAssetContent(assetId, width, agent).flatMapMany(AssetContent::getBuffers);
        DataBuffer buffer = DataBufferUtils.join(buffers$).block();

        if (buffer == null) {
            return new byte[0];
        }

        try {
            return buffer.asInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AssetContent getAssetContentWithMetadata(String assetId, Integer width, Agent agent) {
        return module.getAssetContent(assetId, width, agent).block();
    }

    public void allowAnonymousUsersToReadAsset(String assetId) {
        module.allowAnonymousUsersToReadAsset(assetId).block();
    }

    private void allowUserToManageAsset(String assetId, String userId) {
        module.allowUserToManageAsset(assetId, userId).block();
    }

}
