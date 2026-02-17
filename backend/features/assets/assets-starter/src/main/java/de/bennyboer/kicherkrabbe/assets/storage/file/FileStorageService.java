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
import java.util.List;
import java.util.stream.Stream;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
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
        Path path = resolveSafePath(fileName);

        return Mono.just(path)
                .flatMap(p -> DataBufferUtils.write(content, path, CREATE))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Flux<DataBuffer> load(AssetId assetId, Location location) {
        FileName fileName = location.getFileName().orElseThrow();
        Path path = resolveSafePath(fileName);

        return Mono.fromCallable(() -> Files.exists(path))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(exists -> exists)
                .flatMapMany(ignored -> DataBufferUtils.read(path, DefaultDataBufferFactory.sharedInstance, 1024));
    }

    @Override
    public Mono<Void> remove(AssetId assetId, Location location) {
        FileName fileName = location.getFileName().orElseThrow();
        Path path = resolveSafePath(fileName);

        return Mono.fromCallable(() -> {
                    Files.deleteIfExists(path);
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Boolean> exists(AssetId assetId, Location location) {
        FileName fileName = location.getFileName().orElseThrow();
        Path path = resolveSafePath(fileName);

        return Mono.fromCallable(() -> Files.exists(path))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<List<String>> listByPrefix(String prefix) {
        return Mono.fromCallable(() -> {
                    try (Stream<Path> paths = Files.list(rootPath)) {
                        return paths
                                .map(path -> path.getFileName().toString())
                                .filter(name -> name.startsWith(prefix))
                                .toList();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Long> getSize(AssetId assetId, Location location) {
        FileName fileName = location.getFileName().orElseThrow();
        Path path = resolveSafePath(fileName);

        return Mono.fromCallable(() -> Files.size(path))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Path resolveSafePath(FileName fileName) {
        Path absoluteRootPath = rootPath.toAbsolutePath();
        Path path = absoluteRootPath.resolve(fileName.getValue()).normalize();

        check(path.startsWith(absoluteRootPath), "Invalid file location");

        return path;
    }

}
