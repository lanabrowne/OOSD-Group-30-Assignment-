package org.oosd.patterns.observer;

import org.oosd.HighScore.ConfigTagUtil;
import org.oosd.HighScore.HighScoreWriter;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfig;

public class HighScoreObserver implements ScoreObserver {
    private final String playerName;
    private final int saveThreshold;

    public HighScoreObserver(String playerName, int saveThreshold) {
        this.playerName = playerName == null ? "Player" : playerName;
        this.saveThreshold = Math.max(0, saveThreshold);
    }

    @Override
    public void onScoreChanged(ScoreEvent event) {
        if (event.total >= saveThreshold) {
            ConfigService.load();
            TetrisConfig cfg = ConfigService.get();
            String tag = ConfigTagUtil.makeTagFrom(cfg);
            HighScoreWriter.append(playerName, event.total, tag);
        }
    }
}
