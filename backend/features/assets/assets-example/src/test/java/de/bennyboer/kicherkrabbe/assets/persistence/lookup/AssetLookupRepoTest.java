package de.bennyboer.kicherkrabbe.assets.persistence.lookup;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AssetLookupRepoTest {

    private AssetLookupRepo repo;

    protected abstract AssetLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldFindAssets() {
        var asset1 = LookupAsset.of(AssetId.of("ASSET_1"), Version.of(0), ContentType.of("image/jpeg"), 1024, Instant.parse("2024-01-01T00:00:00Z"));
        var asset2 = LookupAsset.of(AssetId.of("ASSET_2"), Version.of(0), ContentType.of("image/png"), 2048, Instant.parse("2024-01-02T00:00:00Z"));
        var asset3 = LookupAsset.of(AssetId.of("ASSET_3"), Version.of(0), ContentType.of("image/jpeg"), 512, Instant.parse("2024-01-03T00:00:00Z"));

        update(asset1);
        update(asset2);
        update(asset3);

        var page = find(
                List.of(AssetId.of("ASSET_1"), AssetId.of("ASSET_2"), AssetId.of("ASSET_3")),
                Set.of(),
                AssetsSortProperty.CREATED_AT,
                AssetsSortDirection.ASCENDING,
                0,
                30
        );

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getResults()).hasSize(3);
        assertThat(page.getResults().get(0).getId()).isEqualTo(AssetId.of("ASSET_1"));
        assertThat(page.getResults().get(1).getId()).isEqualTo(AssetId.of("ASSET_2"));
        assertThat(page.getResults().get(2).getId()).isEqualTo(AssetId.of("ASSET_3"));
    }

    @Test
    void shouldFilterByContentType() {
        var asset1 = LookupAsset.of(AssetId.of("ASSET_1"), Version.of(0), ContentType.of("image/jpeg"), 1024, Instant.parse("2024-01-01T00:00:00Z"));
        var asset2 = LookupAsset.of(AssetId.of("ASSET_2"), Version.of(0), ContentType.of("image/png"), 2048, Instant.parse("2024-01-02T00:00:00Z"));

        update(asset1);
        update(asset2);

        var page = find(
                List.of(AssetId.of("ASSET_1"), AssetId.of("ASSET_2")),
                Set.of(ContentType.of("image/png")),
                AssetsSortProperty.CREATED_AT,
                AssetsSortDirection.ASCENDING,
                0,
                30
        );

        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults().getFirst().getId()).isEqualTo(AssetId.of("ASSET_2"));
    }

    @Test
    void shouldSortByFileSize() {
        var asset1 = LookupAsset.of(AssetId.of("ASSET_1"), Version.of(0), ContentType.of("image/jpeg"), 2048, Instant.parse("2024-01-01T00:00:00Z"));
        var asset2 = LookupAsset.of(AssetId.of("ASSET_2"), Version.of(0), ContentType.of("image/jpeg"), 512, Instant.parse("2024-01-02T00:00:00Z"));
        var asset3 = LookupAsset.of(AssetId.of("ASSET_3"), Version.of(0), ContentType.of("image/jpeg"), 1024, Instant.parse("2024-01-03T00:00:00Z"));

        update(asset1);
        update(asset2);
        update(asset3);

        var page = find(
                List.of(AssetId.of("ASSET_1"), AssetId.of("ASSET_2"), AssetId.of("ASSET_3")),
                Set.of(),
                AssetsSortProperty.FILE_SIZE,
                AssetsSortDirection.ASCENDING,
                0,
                30
        );

        assertThat(page.getResults().get(0).getId()).isEqualTo(AssetId.of("ASSET_2"));
        assertThat(page.getResults().get(1).getId()).isEqualTo(AssetId.of("ASSET_3"));
        assertThat(page.getResults().get(2).getId()).isEqualTo(AssetId.of("ASSET_1"));
    }

    @Test
    void shouldPaginate() {
        var asset1 = LookupAsset.of(AssetId.of("ASSET_1"), Version.of(0), ContentType.of("image/jpeg"), 1024, Instant.parse("2024-01-01T00:00:00Z"));
        var asset2 = LookupAsset.of(AssetId.of("ASSET_2"), Version.of(0), ContentType.of("image/jpeg"), 2048, Instant.parse("2024-01-02T00:00:00Z"));
        var asset3 = LookupAsset.of(AssetId.of("ASSET_3"), Version.of(0), ContentType.of("image/jpeg"), 512, Instant.parse("2024-01-03T00:00:00Z"));

        update(asset1);
        update(asset2);
        update(asset3);

        var page = find(
                List.of(AssetId.of("ASSET_1"), AssetId.of("ASSET_2"), AssetId.of("ASSET_3")),
                Set.of(),
                AssetsSortProperty.CREATED_AT,
                AssetsSortDirection.ASCENDING,
                1,
                1
        );

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getResults()).hasSize(1);
        assertThat(page.getResults().getFirst().getId()).isEqualTo(AssetId.of("ASSET_2"));
    }

    @Test
    void shouldFindUniqueContentTypes() {
        var asset1 = LookupAsset.of(AssetId.of("ASSET_1"), Version.of(0), ContentType.of("image/jpeg"), 1024, Instant.parse("2024-01-01T00:00:00Z"));
        var asset2 = LookupAsset.of(AssetId.of("ASSET_2"), Version.of(0), ContentType.of("image/png"), 2048, Instant.parse("2024-01-02T00:00:00Z"));
        var asset3 = LookupAsset.of(AssetId.of("ASSET_3"), Version.of(0), ContentType.of("image/jpeg"), 512, Instant.parse("2024-01-03T00:00:00Z"));

        update(asset1);
        update(asset2);
        update(asset3);

        var types = repo.findUniqueContentTypes(
                List.of(AssetId.of("ASSET_1"), AssetId.of("ASSET_2"), AssetId.of("ASSET_3"))
        ).collectList().block();

        assertThat(types).containsExactlyInAnyOrder(ContentType.of("image/jpeg"), ContentType.of("image/png"));
    }

    @Test
    void shouldUpdateExistingAsset() {
        var asset = LookupAsset.of(AssetId.of("ASSET_1"), Version.of(0), ContentType.of("image/jpeg"), 1024, Instant.parse("2024-01-01T00:00:00Z"));
        update(asset);

        var updated = LookupAsset.of(AssetId.of("ASSET_1"), Version.of(1), ContentType.of("image/jpeg"), 2048, Instant.parse("2024-01-01T00:00:00Z"));
        update(updated);

        var page = find(
                List.of(AssetId.of("ASSET_1")),
                Set.of(),
                AssetsSortProperty.CREATED_AT,
                AssetsSortDirection.ASCENDING,
                0,
                30
        );

        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults().getFirst().getFileSize()).isEqualTo(2048);
        assertThat(page.getResults().getFirst().getVersion()).isEqualTo(Version.of(1));
    }

    @Test
    void shouldRemoveAsset() {
        var asset = LookupAsset.of(AssetId.of("ASSET_1"), Version.of(0), ContentType.of("image/jpeg"), 1024, Instant.parse("2024-01-01T00:00:00Z"));
        update(asset);

        repo.remove(AssetId.of("ASSET_1")).block();

        var page = find(
                List.of(AssetId.of("ASSET_1")),
                Set.of(),
                AssetsSortProperty.CREATED_AT,
                AssetsSortDirection.ASCENDING,
                0,
                30
        );

        assertThat(page.getTotal()).isEqualTo(0);
    }

    private void update(LookupAsset asset) {
        repo.update(asset).block();
    }

    private LookupAssetPage find(
            List<AssetId> assetIds,
            Set<ContentType> contentTypes,
            AssetsSortProperty sortProperty,
            AssetsSortDirection sortDirection,
            long skip,
            long limit
    ) {
        return repo.find(assetIds, contentTypes, sortProperty, sortDirection, skip, limit).block();
    }

}
