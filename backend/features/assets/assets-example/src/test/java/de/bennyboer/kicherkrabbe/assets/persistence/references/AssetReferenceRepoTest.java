package de.bennyboer.kicherkrabbe.assets.persistence.references;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AssetReferenceRepoTest {

    private AssetReferenceRepo repo;

    protected abstract AssetReferenceRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpsertReference() {
        var ref = AssetReference.of(
                AssetId.of("ASSET_1"),
                AssetReferenceResourceType.FABRIC,
                AssetResourceId.of("FABRIC_1")
        );

        upsert(ref);

        var found = findByAssetId(AssetId.of("ASSET_1"));
        assertThat(found).containsExactly(ref);
    }

    @Test
    void shouldFindByAssetId() {
        var ref1 = AssetReference.of(
                AssetId.of("ASSET_1"),
                AssetReferenceResourceType.FABRIC,
                AssetResourceId.of("FABRIC_1")
        );
        var ref2 = AssetReference.of(
                AssetId.of("ASSET_1"),
                AssetReferenceResourceType.PATTERN,
                AssetResourceId.of("PATTERN_1")
        );
        var ref3 = AssetReference.of(
                AssetId.of("ASSET_2"),
                AssetReferenceResourceType.PRODUCT,
                AssetResourceId.of("PRODUCT_1")
        );

        upsert(ref1);
        upsert(ref2);
        upsert(ref3);

        var found = findByAssetId(AssetId.of("ASSET_1"));
        assertThat(found).containsExactlyInAnyOrder(ref1, ref2);
    }

    @Test
    void shouldRemoveByResource() {
        var ref1 = AssetReference.of(
                AssetId.of("ASSET_1"),
                AssetReferenceResourceType.FABRIC,
                AssetResourceId.of("FABRIC_1")
        );
        var ref2 = AssetReference.of(
                AssetId.of("ASSET_2"),
                AssetReferenceResourceType.FABRIC,
                AssetResourceId.of("FABRIC_1")
        );
        var ref3 = AssetReference.of(
                AssetId.of("ASSET_3"),
                AssetReferenceResourceType.PATTERN,
                AssetResourceId.of("PATTERN_1")
        );

        upsert(ref1);
        upsert(ref2);
        upsert(ref3);

        removeByResource(AssetReferenceResourceType.FABRIC, AssetResourceId.of("FABRIC_1"));

        var found1 = findByAssetId(AssetId.of("ASSET_1"));
        assertThat(found1).isEmpty();

        var found2 = findByAssetId(AssetId.of("ASSET_2"));
        assertThat(found2).isEmpty();

        var found3 = findByAssetId(AssetId.of("ASSET_3"));
        assertThat(found3).containsExactly(ref3);
    }

    @Test
    void shouldUpsertSameReferenceTwice() {
        var ref = AssetReference.of(
                AssetId.of("ASSET_1"),
                AssetReferenceResourceType.FABRIC,
                AssetResourceId.of("FABRIC_1")
        );

        upsert(ref);
        upsert(ref);

        var found = findByAssetId(AssetId.of("ASSET_1"));
        assertThat(found).hasSize(1);
    }

    @Test
    void shouldReturnEmptyWhenNoReferences() {
        var found = findByAssetId(AssetId.of("NON_EXISTENT"));
        assertThat(found).isEmpty();
    }

    private void upsert(AssetReference reference) {
        repo.upsert(reference).block();
    }

    private void removeByResource(AssetReferenceResourceType resourceType, AssetResourceId resourceId) {
        repo.removeByResource(resourceType, resourceId).block();
    }

    private List<AssetReference> findByAssetId(AssetId assetId) {
        return repo.findByAssetId(assetId).collectList().block();
    }

}
