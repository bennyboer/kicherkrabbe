package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.assets.storage.StorageService;
import de.bennyboer.kicherkrabbe.assets.storage.file.FileStorageService;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;

public class AssetsModuleTest {

    private final AssetService assetService = new AssetService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher()
    );

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private StorageService storageService;

    {
        try {
            storageService = new FileStorageService(
                    Files.createTempDirectory("kicherkrabbe-test-assets")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final AssetsModuleConfig config = new AssetsModuleConfig();

    private final AssetsModule module = config.assetsModule(
            assetService,
            permissionsService,
            storageService
    );

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

    public void deleteAsset(String assetId, long version, Agent agent) {
        module.deleteAsset(assetId, version, agent).block();
    }

    public byte[] getAssetContent(String assetId, Agent agent) {
        Flux<DataBuffer> buffers$ = module.getAssetContent(assetId, agent).flatMapMany(AssetContent::getBuffers);
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

    public void allowAnonymousUsersToReadAsset(String assetId) {
        module.allowAnonymousUsersToReadAsset(assetId).block();
    }

    private void allowUserToManageAsset(String assetId, String userId) {
        module.allowUserToManageAsset(assetId, userId).block();
    }

}
