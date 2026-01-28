package de.bennyboer.kicherkrabbe.assets.storage;

import de.bennyboer.kicherkrabbe.assets.storage.file.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStorageServiceTest extends StorageServiceTest {

    @Override
    protected StorageService createRepo() {
        try {
            Path rootPath = Files.createTempDirectory("kicherkrabbe-test-assets");
            return new FileStorageService(rootPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
