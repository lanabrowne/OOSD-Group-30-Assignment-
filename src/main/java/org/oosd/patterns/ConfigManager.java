package org.oosd.patterns;

import java.util.concurrent.atomic.AtomicBoolean;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfig;

public final class ConfigManager {
    private static volatile ConfigManager INSTANCE;
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    private ConfigManager() {}

    public static ConfigManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ConfigManager.class) {
                if (INSTANCE == null) INSTANCE = new ConfigManager();
            }
        }
        return INSTANCE;
    }

    public TetrisConfig get() {
        if (loaded.compareAndSet(false, true)) {
            ConfigService.load();
        }
        return ConfigService.get();
    }
}