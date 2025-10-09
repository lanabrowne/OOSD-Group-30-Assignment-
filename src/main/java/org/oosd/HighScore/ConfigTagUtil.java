package org.oosd.HighScore;

import org.oosd.config.PlayerType;
import org.oosd.config.TetrisConfig;

/** Builds a compact, human-readable snapshot of the game settings for the score. */
public final class ConfigTagUtil {
    private ConfigTagUtil() {}

    public static String makeTagFrom(TetrisConfig cfg) {
        StringBuilder s = new StringBuilder();
        // Base: WIDTHxHEIGHT-LvN
        s.append(cfg.fieldWidth())
                .append("x")
                .append(cfg.fieldHeight())
                .append("-Lv")
                .append(cfg.gameLevel());

        // Single vs Versus rules:
        if (!cfg.extendMode()) {
            // Single play -> prefer AI Single when auto-play; otherwise left player's type
            if (cfg.aiPlay()) {
                s.append("-AI Single");
            } else {
                s.append("-").append(humanize(cfg.leftPlayer())).append(" Single");
            }
        } else {
            // Two players -> LEFTvsRIGHT
            s.append("-")
                    .append(humanize(cfg.leftPlayer()))
                    .append("vs")
                    .append(humanize(cfg.rightPlayer()));
        }
        return s.toString();
    }

    private static String humanize(PlayerType t) {
        // Upper-case tokens to keep JSON stable
        return switch (t) {
            case HUMAN -> "HUMAN";
            case AI -> "AI";
            case EXTERNAL -> "EXTERNAL";
        };
    }
}
