package de.bennyboer.kicherkrabbe.assets.storage.file;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.FileName;
import de.bennyboer.kicherkrabbe.assets.Location;
import de.bennyboer.kicherkrabbe.assets.storage.StorageService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Service storing assets on the file system.
 */
public class FileStorageService implements StorageService {

    private final Path rootPath;

    public FileStorageService(Path rootPath) {
        this.rootPath = rootPath;

        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create root directory for assets storage", e);
        }
    }

    @Override
    public Mono<Void> store(AssetId assetId, Location location, Flux<DataBuffer> content) {
        FileName fileName = location.getFileName().orElseThrow();
        Path path = rootPath.resolve(fileName.getValue())
                .normalize()
                .toAbsolutePath();

        return Mono.just(path)
                .flatMap(p -> DataBufferUtils.write(content, path, CREATE))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Flux<DataBuffer> load(AssetId assetId, Location location) {
        FileName fileName = location.getFileName().orElseThrow();
        Path path = rootPath.resolve(fileName.getValue())
                .normalize()
                .toAbsolutePath();

        if (!Files.exists(path)) {
            return Flux.empty();
        }

        return Mono.just(path)
                .flatMapMany(p -> DataBufferUtils.read(p, DefaultDataBufferFactory.sharedInstance, 1024));
    }

    @Override
    public Mono<Void> remove(AssetId assetId, Location location) {
        FileName fileName = location.getFileName().orElseThrow();
        Path path = rootPath.resolve(fileName.getValue())
                .normalize()
                .toAbsolutePath();

        return Mono.just(path)
                .flatMap(p -> {
                    try {
                        Files.deleteIfExists(p);

                        return Mono.empty();
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                });
    }

}
