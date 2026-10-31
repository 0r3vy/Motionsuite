package dev.r3vy.motionsuite.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

public final class ConfigManager {
    private final ConfigFile file;
    private final ExecutorService writer;
    private final AtomicBoolean dirty = new AtomicBoolean();
    private final AtomicLong epoch = new AtomicLong();

    private volatile ConfigSnapshot snapshot = ConfigSnapshot.defaults();

    public ConfigManager() {
        this(ConfigFile.defaultLocation());
    }

    ConfigManager(ConfigFile file) {
        this.file = file;
        this.writer = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "motionsuite-config-writer");
            thread.setDaemon(true);
            return thread;
        });
    }

    public ConfigSnapshot snapshot() {
        return snapshot;
    }

    public long epoch() {
        return epoch.get();
    }

    public synchronized FilterState filterState() {
        return new FilterState(snapshot, epoch.get());
    }

    public record FilterState(ConfigSnapshot snapshot, long epoch) {
    }

    public void load() {
        if (file.exists()) {
            publish(file.load(snapshot));
            return;
        }
        saveCurrent();
    }

    public void reload() {
        flushNow();
        load();
    }

    public void reset() {
        publish(ConfigSnapshot.defaults());
        saveCurrent();
    }

    public void update(UnaryOperator<ConfigSnapshot> change) {
        publish(change.apply(snapshot));
        saveCurrent();
    }

    public void updateQuiet(UnaryOperator<ConfigSnapshot> change) {
        publish(change.apply(snapshot));
    }

    public void saveCurrent() {
        dirty.set(true);
        writer.execute(this::flushNow);
    }

    public void flushNow() {
        if (dirty.compareAndSet(true, false) && !file.write(snapshot)) {
            dirty.set(true);
        }
    }

    private void publish(ConfigSnapshot next) {
        snapshot = next;
        epoch.incrementAndGet();
    }
}
