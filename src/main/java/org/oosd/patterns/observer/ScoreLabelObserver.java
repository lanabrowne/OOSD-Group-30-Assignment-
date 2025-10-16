package org.oosd.patterns.observer;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class ScoreLabelObserver implements ScoreObserver {
    private final Label label;
    private final String prefix;

    public ScoreLabelObserver(Label label, String prefix) {
        this.label = label;
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public void onScoreChanged(ScoreEvent event) {
        if (label == null) return;
        Platform.runLater(() -> label.setText(prefix + event.total));
    }
}
