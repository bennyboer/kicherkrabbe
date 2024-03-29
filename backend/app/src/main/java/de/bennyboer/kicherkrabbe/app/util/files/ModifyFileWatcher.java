package de.bennyboer.kicherkrabbe.app.util.files;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@Slf4j
public class ModifyFileWatcher implements FileWatcher {

    private final Path filePath;

    private final Runnable callback;

    @Nullable
    private WatchService watchService;

    @Nullable
    private Thread thread;

    public ModifyFileWatcher(Path filePath, Runnable callback) {
        this.filePath = filePath;
        this.callback = callback;
    }

    @Override
    public void start() {
        if (isStarted()) {
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            filePath.getParent().register(watchService, ENTRY_MODIFY);

            thread = startWatchThread();

            log.info("Started file watcher for file '{}'", filePath);
        } catch (IOException e) {
            log.error("Failed to create watch service for file '{}'", filePath, e);
        }
    }

    @Override
    public void stop() {
        getWatchService().ifPresent(watchService -> {
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("Failed to close watch service for file '{}'", filePath, e);
            }
        });

        getThread().ifPresent(Thread::interrupt);
    }

    private Thread startWatchThread() {
        var thread = new Thread(() -> {
            try {
                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path eventPath = (Path) event.context();
                        Path changedPath = filePath.getParent().resolve(eventPath);
                        if (Files.isSameFile(filePath, changedPath)) {
                            log.info(
                                    "Watched file '{}' changed with event kind '{}' and context '{}'",
                                    filePath,
                                    event.kind(),
                                    event.context()
                            );

                            callback.run();
                        }
                    }
                    key.reset();
                }
            } catch (ClosedWatchServiceException e) {
                log.info("Stopped file watcher thread for file '{}' gracefully", filePath, e);
            } catch (InterruptedException e) {
                log.info("Watch service thread for file '{}' interrupted", filePath, e);
            } catch (IOException e) {
                log.error("Failed to watch file '{}'", filePath, e);
            }
        });

        thread.start();

        return thread;
    }

    private Optional<WatchService> getWatchService() {
        return Optional.ofNullable(watchService);
    }

    private Optional<Thread> getThread() {
        return Optional.ofNullable(thread);
    }

    private boolean isStarted() {
        return getThread().isPresent();
    }

}
