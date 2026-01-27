package de.bennyboer.kicherkrabbe.assets.storage;

import de.bennyboer.kicherkrabbe.assets.storage.file.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DelegatingStorageServiceTest extends StorageServiceTest {

    @Override
    protected StorageService createRepo() {
        try {
            Path rootPath = Files.createTempDirectory("kicherkrabbe-test-assets");
            StorageService fileStorageService = new FileStorageService(rootPath);
            return new DelegatingStorageService(fileStorageService);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
