package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final AssetService assetService = new AssetService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldCreateAsset() {
        // given: a content type + location to create an asset for
        var contentType = ContentType.of("image/jpeg");
        var location = Location.file(FileName.of("image.jpg"));

        // when: creating the asset
        var id = create(contentType, location);

        // then: the asset is created
        var asset = get(id);
        assertThat(asset.getId()).isEqualTo(id);
        assertThat(asset.getVersion()).isEqualTo(Version.zero());
        assertThat(asset.getContentType()).isEqualTo(contentType);
        assertThat(asset.getLocation()).isEqualTo(location);
        assertThat(asset.isNotDeleted()).isTrue();
    }

    @Test
    void shouldDeleteAsset() {
        // given: an asset
        var contentType = ContentType.of("image/jpeg");
        var location = Location.file(FileName.of("image.jpg"));
        var id = create(contentType, location);

        // when: deleting the asset
        delete(id, Version.zero());

        // then: the asset is deleted
        assertThat(get(id)).isNull();
    }

    private Asset get(AssetId id) {
        return assetService.get(id).block();
    }

    private AssetId create(ContentType contentType, Location location) {
        return assetService.create(AssetId.create(), contentType, location, Agent.system()).block().getId();
    }

    private void delete(AssetId id, Version version) {
        assetService.delete(id, version, Agent.system()).block();
    }

}
