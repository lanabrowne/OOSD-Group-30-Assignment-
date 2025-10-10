package org.oosd.patterns.observer;

import java.util.Objects;

public final class ScoreEvent {
    public final String playerId;
    public final int delta;
    public final int total;

    public ScoreEvent(String playerId, int delta, int total) {
        this.playerId = Objects.requireNonNull(playerId);
        this.delta = delta;
        this.total = total;
    }
}

