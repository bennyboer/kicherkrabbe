package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UploadAssetTest extends AssetsModuleTest {

    @Test
    void shouldUploadAsset() {
        // given: a file to upload
        var content = "Hello, World!".getBytes(UTF_8);

        // and: the user is allowed to upload assets
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: uploading the file as asset
        var assetId = uploadAsset("image/jpeg", content, agent);

        // then: the asset is uploaded
        assertThat(assetId).isNotNull();

        // and: the asset content can be loaded
        var result = new String(getAssetContent(assetId, agent), UTF_8);
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void shouldRaiseErrorWhenPayloadIsTooLarge() {
        // given: a file to upload that is too large
        var content = new byte[1024 * 1024 * 17];

        // and: the user is allowed to upload assets
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: uploading the file as asset; then: an error is raised
        assertThatThrownBy(() -> uploadAsset("image/jpeg", content, agent))
                .matches(e -> e.getCause() instanceof AssetTooLargeError);
    }

}
