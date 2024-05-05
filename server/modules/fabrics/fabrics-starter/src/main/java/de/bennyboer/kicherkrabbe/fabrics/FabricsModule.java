package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.fabrics.FabricService;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FabricsModule {

    private final FabricService fabricService;

    private final PermissionsService permissionsService;

}
