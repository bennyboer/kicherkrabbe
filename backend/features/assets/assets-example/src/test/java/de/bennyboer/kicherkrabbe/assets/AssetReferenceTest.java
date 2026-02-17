package de.bennyboer.kicherkrabbe.assets;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetReferenceTest extends AssetsModuleTest {

    @Test
    void shouldTrackAssetReference() {
        updateAssetReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", Set.of("ASSET_1"));

        var refs = findAssetReferences("ASSET_1");
        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().getResourceType()).isEqualTo(AssetReferenceResourceType.FABRIC);
        assertThat(refs.getFirst().getResourceId()).isEqualTo(AssetResourceId.of("FABRIC_1"));
    }

    @Test
    void shouldUpdateReferencesForResource() {
        updateAssetReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", Set.of("ASSET_1"));

        updateAssetReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", Set.of("ASSET_2"));

        var oldRefs = findAssetReferences("ASSET_1");
        assertThat(oldRefs).isEmpty();

        var newRefs = findAssetReferences("ASSET_2");
        assertThat(newRefs).hasSize(1);
        assertThat(newRefs.getFirst().getResourceType()).isEqualTo(AssetReferenceResourceType.FABRIC);
    }

    @Test
    void shouldRemoveReferencesOnResourceDeletion() {
        updateAssetReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", Set.of("ASSET_1", "ASSET_2"));

        removeAssetReferencesByResource(AssetReferenceResourceType.FABRIC, "FABRIC_1");

        assertThat(findAssetReferences("ASSET_1")).isEmpty();
        assertThat(findAssetReferences("ASSET_2")).isEmpty();
    }

    @Test
    void shouldTrackMultipleResourcesReferencingSameAsset() {
        updateAssetReferences(AssetReferenceResourceType.FABRIC, "FABRIC_1", Set.of("ASSET_1"));
        updateAssetReferences(AssetReferenceResourceType.PATTERN, "PATTERN_1", Set.of("ASSET_1"));

        var refs = findAssetReferences("ASSET_1");
        assertThat(refs).hasSize(2);
    }

    @Test
    void shouldTrackMultipleAssetsReferencedBySameResource() {
        updateAssetReferences(AssetReferenceResourceType.PATTERN, "PATTERN_1", Set.of("ASSET_1", "ASSET_2", "ASSET_3"));

        var refs1 = findAssetReferences("ASSET_1");
        assertThat(refs1).hasSize(1);

        var refs2 = findAssetReferences("ASSET_2");
        assertThat(refs2).hasSize(1);

        var refs3 = findAssetReferences("ASSET_3");
        assertThat(refs3).hasSize(1);
    }

}
