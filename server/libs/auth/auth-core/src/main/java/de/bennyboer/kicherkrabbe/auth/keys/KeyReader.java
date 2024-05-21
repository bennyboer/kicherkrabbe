package de.bennyboer.kicherkrabbe.auth.keys;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Optional;

public class KeyReader {

    public static Mono<KeyPair> readKeyPairFromClassPath(String path) {
        Security.addProvider(new BouncyCastleProvider());

        return loadKeyPairFromClassPath(path)
                .flatMap(KeyReader::convertKeyPair)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public static Mono<KeyPair> readKeyPairFromFile(String path) {
        Security.addProvider(new BouncyCastleProvider());

        return loadKeyPairFromFile(path)
                .flatMap(KeyReader::convertKeyPair)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static Mono<KeyPair> convertKeyPair(PEMKeyPair pemKeyPair) {
        return Mono.fromCallable(() -> {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            java.security.KeyPair javaKeyPair = converter.getKeyPair(pemKeyPair);

            return KeyPair.of(
                    (ECPublicKey) javaKeyPair.getPublic(),
                    (ECPrivateKey) javaKeyPair.getPrivate()
            );
        });
    }

    private static Mono<PEMKeyPair> loadKeyPairFromClassPath(String path) {
        return Mono.fromCallable(() -> {
            try (
                    var parser = new PEMParser(new InputStreamReader(
                            Optional.ofNullable(KeyReader.class.getResourceAsStream(path)).orElseThrow()
                    ))
            ) {
                return (PEMKeyPair) parser.readObject();
            }
        });
    }

    private static Mono<PEMKeyPair> loadKeyPairFromFile(String path) {
        return Mono.fromCallable(() -> {
            try (var parser = new PEMParser(new FileReader(path))) {
                return (PEMKeyPair) parser.readObject();
            }
        });
    }

}
