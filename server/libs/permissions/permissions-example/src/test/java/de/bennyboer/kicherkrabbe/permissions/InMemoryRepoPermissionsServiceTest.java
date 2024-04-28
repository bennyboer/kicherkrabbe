package de.bennyboer.kicherkrabbe.permissions;

import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;

public class InMemoryRepoPermissionsServiceTest extends PermissionsServiceTest {

    @Override
    protected PermissionsRepo createRepo() {
        return new InMemoryPermissionsRepo();
    }

}
