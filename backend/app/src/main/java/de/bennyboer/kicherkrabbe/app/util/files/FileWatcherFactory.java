package de.bennyboer.kicherkrabbe.app.util.files;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileWatcherFactory {

    private final List<FileWatcher> watchers = new ArrayList<>();

    private static class InstanceHolder {

        private static final FileWatcherFactory INSTANCE = new FileWatcherFactory();

    }

    public static FileWatcherFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public FileWatcher createWatcher(Path filePath, Runnable callback) {
        var watcher = new ModifyFileWatcher(filePath, callback);

        synchronized (watchers) {
            watchers.add(watcher);
        }

        return watcher;
    }

    public void stopAll() {
        synchronized (watchers) {
            watchers.forEach(FileWatcher::stop);
            watchers.clear();
        }
    }

}
