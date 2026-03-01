package de.bennyboer.kicherkrabbe.patching;

public class PatchingTimeoutException extends RuntimeException {

    public PatchingTimeoutException() {
        super("Timed out waiting for another instance to finish patching");
    }

}
