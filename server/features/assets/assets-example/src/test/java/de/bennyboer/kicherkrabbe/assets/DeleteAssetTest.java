package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteAssetTest extends AssetsModuleTest {

    @Test
    void shouldDeleteAsset() {
        // given: a user is allowed to create assets
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: an asset is uploaded
        var assetId = uploadAsset("image/jpeg", "Hello, World!".getBytes(UTF_8), agent);

        // when: deleting the asset
        deleteAsset(assetId, 0L, agent);

        // then: the asset is deleted
        var content = getAssetContent(assetId, agent);
        assertThat(content).isEmpty();
    }

}
