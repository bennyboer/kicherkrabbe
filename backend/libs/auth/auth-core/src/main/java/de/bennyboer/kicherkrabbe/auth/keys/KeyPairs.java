package de.bennyboer.kicherkrabbe.auth.keys;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

public class KeyPairs {

    public static KeyPair readFromClassPath(String path) {
        return KeyReader.readKeyPairFromClassPath(path).block();
    }

    public static KeyPair readFromFile(String path) {
        return KeyReader.readKeyPairFromFile(path).block();
    }

    public static KeyPair generate() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(new ECGenParameterSpec("secp521r1"), new SecureRandom());
            java.security.KeyPair keyPair = kpg.generateKeyPair();

            return KeyPair.of(
                    (ECPublicKey) keyPair.getPublic(),
                    (ECPrivateKey) keyPair.getPrivate()
            );
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

}
