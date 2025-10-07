package org.oosd.HighScore;

import org.oosd.config.TetrisConfig;

public final class ConfigTagUtil {

    private ConfigTagUtil() {}

    public static String makeTagFrom(TetrisConfig cfg) {
        String base = String.format("%dx%d-Lv%d-%svs%s",
                cfg.fieldWidth(), cfg.fieldHeight(), cfg.gameLevel(),
                cfg.leftPlayer().name(), cfg.rightPlayer().name());
        return cfg.extendMode() ? base + "-EXT" : base;
    }
}

