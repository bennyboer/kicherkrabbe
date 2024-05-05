package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ColorsModule {

    private final ColorService colorService;

    private final PermissionsService permissionsService;

}
