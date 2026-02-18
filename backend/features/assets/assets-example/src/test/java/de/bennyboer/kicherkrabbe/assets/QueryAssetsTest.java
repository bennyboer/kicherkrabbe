package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryAssetsTest extends AssetsModuleTest {

    @Test
    void shouldQueryAssets() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var assetId1 = uploadAsset("image/jpeg", "content1".getBytes(UTF_8), agent);
        var assetId2 = uploadAsset("image/png", "content2".getBytes(UTF_8), agent);
        var assetId3 = uploadAsset("image/jpeg", "content3".getBytes(UTF_8), agent);

        var page = getAssets(0, 30, agent);

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getResults()).hasSize(3);
    }

    @Test
    void shouldQueryAssetsWithPagination() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        uploadAsset("image/jpeg", "content1".getBytes(UTF_8), agent);
        uploadAsset("image/jpeg", "content2".getBytes(UTF_8), agent);
        uploadAsset("image/jpeg", "content3".getBytes(UTF_8), agent);

        var page1 = getAssets(0, 2, agent);
        assertThat(page1.getTotal()).isEqualTo(3);
        assertThat(page1.getResults()).hasSize(2);

        var page2 = getAssets(2, 2, agent);
        assertThat(page2.getTotal()).isEqualTo(3);
        assertThat(page2.getResults()).hasSize(1);
    }

    @Test
    void shouldFilterByContentType() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        uploadAsset("image/jpeg", "content1".getBytes(UTF_8), agent);
        uploadAsset("image/png", "content2".getBytes(UTF_8), agent);
        uploadAsset("image/jpeg", "content3".getBytes(UTF_8), agent);

        var page = getAssets(null, Set.of("image/png"), null, null, 0, 30, agent);

        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults()).hasSize(1);
        assertThat(page.getResults().getFirst().getContentType().getValue()).isEqualTo("image/png");
    }

    @Test
    void shouldSortByFileSize() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        uploadAsset("image/jpeg", "a".getBytes(UTF_8), agent);
        uploadAsset("image/jpeg", "longer content".getBytes(UTF_8), agent);
        uploadAsset("image/jpeg", "bb".getBytes(UTF_8), agent);

        var pageAsc = getAssets(null, Set.of(), "FILE_SIZE", "ASCENDING", 0, 30, agent);
        assertThat(pageAsc.getResults().get(0).getFileSize()).isLessThanOrEqualTo(pageAsc.getResults().get(1).getFileSize());
        assertThat(pageAsc.getResults().get(1).getFileSize()).isLessThanOrEqualTo(pageAsc.getResults().get(2).getFileSize());

        var pageDesc = getAssets(null, Set.of(), "FILE_SIZE", "DESCENDING", 0, 30, agent);
        assertThat(pageDesc.getResults().get(0).getFileSize()).isGreaterThanOrEqualTo(pageDesc.getResults().get(1).getFileSize());
        assertThat(pageDesc.getResults().get(1).getFileSize()).isGreaterThanOrEqualTo(pageDesc.getResults().get(2).getFileSize());
    }

    @Test
    void shouldSearchByResourceName() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var assetId1 = uploadAsset("image/jpeg", "content1".getBytes(UTF_8), agent);
        var assetId2 = uploadAsset("image/jpeg", "content2".getBytes(UTF_8), agent);

        updateAssetReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", Set.of(assetId1), "Blumentraum");
        updateAssetReferences(AssetReferenceResourceType.PATTERN, "PATTERN_1", Set.of(assetId2), "Babyshirt");

        var page = getAssets("Blumen", Set.of(), null, null, 0, 30, agent);

        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults()).hasSize(1);
        assertThat(page.getResults().getFirst().getId().getValue()).isEqualTo(assetId1);
    }

    @Test
    void shouldReturnReferencesWithAssets() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var assetId = uploadAsset("image/jpeg", "content".getBytes(UTF_8), agent);

        updateAssetReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", Set.of(assetId), "Blumentraum");

        var page = getAssets(0, 30, agent);

        assertThat(page.getResults()).hasSize(1);
        var asset = page.getResults().getFirst();
        assertThat(asset.getReferences()).hasSize(1);
        assertThat(asset.getReferences().getFirst().getResourceType()).isEqualTo(AssetReferenceResourceType.FABRIC);
        assertThat(asset.getReferences().getFirst().getResourceName()).isEqualTo("Blumentraum");
    }

    @Test
    void shouldQueryContentTypes() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        uploadAsset("image/jpeg", "content1".getBytes(UTF_8), agent);
        uploadAsset("image/png", "content2".getBytes(UTF_8), agent);
        uploadAsset("image/jpeg", "content3".getBytes(UTF_8), agent);

        var contentTypes = getContentTypes(agent);

        assertThat(contentTypes).containsExactlyInAnyOrder("image/jpeg", "image/png");
    }

    @Test
    void shouldReturnEmptyPageWhenNoAssetsExist() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var page = getAssets(0, 30, agent);

        assertThat(page.getTotal()).isEqualTo(0);
        assertThat(page.getResults()).isEmpty();
    }

    @Test
    void shouldUpdateResourceNameOnRename() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var assetId = uploadAsset("image/jpeg", "content".getBytes(UTF_8), agent);
        updateAssetReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", Set.of(assetId), "Old Name");

        updateResourceNameInReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", "New Name");

        var page = getAssets(0, 30, agent);
        var asset = page.getResults().getFirst();
        assertThat(asset.getReferences().getFirst().getResourceName()).isEqualTo("New Name");
    }

    @Test
    void shouldSearchByUpdatedResourceName() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var assetId = uploadAsset("image/jpeg", "content".getBytes(UTF_8), agent);
        updateAssetReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", Set.of(assetId), "Old Name");
        updateResourceNameInReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", "New Name");

        var page = getAssets("New", Set.of(), null, null, 0, 30, agent);
        assertThat(page.getTotal()).isEqualTo(1);

        var oldPage = getAssets("Old", Set.of(), null, null, 0, 30, agent);
        assertThat(oldPage.getTotal()).isEqualTo(0);
    }

    @Test
    void shouldNotReturnAssetsOfOtherUser() {
        allowUserToCreateAssets("USER_A");
        allowUserToCreateAssets("USER_B");
        var agentA = Agent.user(AgentId.of("USER_A"));
        var agentB = Agent.user(AgentId.of("USER_B"));

        uploadAsset("image/jpeg", "contentA".getBytes(UTF_8), agentA);
        uploadAsset("image/png", "contentB".getBytes(UTF_8), agentB);

        var pageA = getAssets(0, 30, agentA);
        assertThat(pageA.getTotal()).isEqualTo(1);
        assertThat(pageA.getResults()).hasSize(1);
        assertThat(pageA.getResults().getFirst().getContentType().getValue()).isEqualTo("image/jpeg");

        var pageB = getAssets(0, 30, agentB);
        assertThat(pageB.getTotal()).isEqualTo(1);
        assertThat(pageB.getResults()).hasSize(1);
        assertThat(pageB.getResults().getFirst().getContentType().getValue()).isEqualTo("image/png");
    }

    @Test
    void shouldNotShowDeletedAssetsInQuery() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var assetId = uploadAsset("image/jpeg", "content".getBytes(UTF_8), agent);

        var page1 = getAssets(0, 30, agent);
        assertThat(page1.getTotal()).isEqualTo(1);

        deleteAsset(assetId, 0, agent);

        var page2 = getAssets(0, 30, agent);
        assertThat(page2.getTotal()).isEqualTo(0);
    }

}
