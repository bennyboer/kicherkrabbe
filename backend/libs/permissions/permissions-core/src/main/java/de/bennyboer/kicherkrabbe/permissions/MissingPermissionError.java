package de.bennyboer.kicherkrabbe.permissions;

import lombok.Getter;

@Getter
public class MissingPermissionError extends Exception {

    private final Permission permission;

    public MissingPermissionError(Permission permission) {
        super("%s missing".formatted(permission));
        this.permission = permission;
    }

}
