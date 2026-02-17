package de.bennyboer.kicherkrabbe.assets.persistence.references;

import de.bennyboer.kicherkrabbe.assets.persistence.references.inmemory.InMemoryAssetReferenceRepo;

public class InMemoryAssetReferenceRepoTest extends AssetReferenceRepoTest {

    @Override
    protected AssetReferenceRepo createRepo() {
        return new InMemoryAssetReferenceRepo();
    }

}
