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
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static java.nio.file.StandardOpenOption.CREATE;

public class FileStorageService implements StorageService {

    private static final String META_FILE_NAME = ".storage-meta";
    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private final Path rootPath;
    private final Path metaFilePath;
    private final AtomicLong totalSize;

    @JsonSerialize
    @JsonDeserialize
    record StorageMeta(long totalSizeBytes) {
    }

    public FileStorageService(Path rootPath) {
        this.rootPath = rootPath;
        this.metaFilePath = rootPath.resolve(META_FILE_NAME);
        this.totalSize = new AtomicLong(0);

        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create root directory for assets storage", e);
        }

        initializeTotalSize();
    }

    @Override
    public Mono<Void> store(AssetId assetId, Location location, Flux<DataBuffer> content) {
        FileName fileName = location.getFileName().orElseThrow();
        Path path = resolveSafePath(fileName);

        return Mono.just(path)
                .flatMap(p -> DataBufferUtils.write(content, path, CREATE))
                .then(Mono.fromCallable(() -> {
                    long size = Files.size(path);
                    addToTotalSize(size);
                    return size;
                }))
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
                    long size = Files.exists(path) ? Files.size(path) : 0;
                    Files.deleteIfExists(path);
                    if (size > 0) {
                        subtractFromTotalSize(size);
                    }
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

    @Override
    public Mono<Long> getTotalStorageSize() {
        return Mono.just(totalSize.get());
    }

    private void initializeTotalSize() {
        try {
            if (Files.exists(metaFilePath)) {
                var meta = JSON_MAPPER.readValue(Files.readString(metaFilePath), StorageMeta.class);
                totalSize.set(meta.totalSizeBytes());
            } else {
                long size = calculateDirectorySize();
                totalSize.set(size);
                persistMeta();
            }
        } catch (Exception e) {
            try {
                long size = calculateDirectorySize();
                totalSize.set(size);
                persistMeta();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to initialize total storage size", ex);
            }
        }
    }

    private long calculateDirectorySize() throws IOException {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().equals(META_FILE_NAME))
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
        }
    }

    private void addToTotalSize(long size) {
        totalSize.addAndGet(size);
        persistMeta();
    }

    private void subtractFromTotalSize(long size) {
        totalSize.addAndGet(-size);
        persistMeta();
    }

    private void persistMeta() {
        try {
            var meta = new StorageMeta(totalSize.get());
            Files.writeString(metaFilePath, new String(JSON_MAPPER.writeValueAsBytes(meta)));
        } catch (Exception e) {
            // Best-effort persistence; the in-memory value is still correct
        }
    }

    private Path resolveSafePath(FileName fileName) {
        Path absoluteRootPath = rootPath.toAbsolutePath();
        Path path = absoluteRootPath.resolve(fileName.getValue()).normalize();

        check(path.startsWith(absoluteRootPath), "Invalid file location");

        return path;
    }

}
