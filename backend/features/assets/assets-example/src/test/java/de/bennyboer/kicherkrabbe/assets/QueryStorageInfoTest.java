package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryStorageInfoTest extends AssetsModuleTest {

    @Test
    void shouldQueryStorageInfo() {
        setUp(1024 * 1024);

        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var content = "Hello, World!".getBytes(UTF_8);
        uploadAsset("image/jpeg", content, agent);

        var info = getStorageInfo(agent);
        assertThat(info.getUsedBytes()).isGreaterThan(0);
        assertThat(info.getLimitBytes()).isEqualTo(1024 * 1024);
    }

    @Test
    void shouldQueryStorageInfoWithNoLimit() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var info = getStorageInfo(agent);
        assertThat(info.getUsedBytes()).isEqualTo(0);
        assertThat(info.getLimitBytes()).isEqualTo(0);
    }

}
