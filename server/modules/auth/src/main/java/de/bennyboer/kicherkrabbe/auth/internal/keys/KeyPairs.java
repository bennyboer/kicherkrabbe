package de.bennyboer.kicherkrabbe.auth.internal.keys;

public class KeyPairs {

    public static KeyPair read(String path) {
        return KeyReader.readKeyPair(path).block();
    }

}
