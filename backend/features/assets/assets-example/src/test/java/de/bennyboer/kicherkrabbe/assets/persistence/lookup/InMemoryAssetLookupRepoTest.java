package de.bennyboer.kicherkrabbe.assets.persistence.lookup;

import de.bennyboer.kicherkrabbe.assets.persistence.lookup.inmemory.InMemoryAssetLookupRepo;

public class InMemoryAssetLookupRepoTest extends AssetLookupRepoTest {

    @Override
    protected AssetLookupRepo createRepo() {
        return new InMemoryAssetLookupRepo();
    }

}
