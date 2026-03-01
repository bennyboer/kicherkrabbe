package de.bennyboer.kicherkrabbe.patching;

public class PatchingInProgressException extends RuntimeException {

    public PatchingInProgressException() {
        super("Patching is currently in progress by another instance");
    }

}
