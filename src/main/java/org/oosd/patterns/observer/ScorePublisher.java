package org.oosd.patterns.observer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScorePublisher implements ScoreSubject {
    private final Set<ScoreObserver> observers = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Map<String, Integer> totals = new ConcurrentHashMap<>();

    @Override
    public void addObserver(ScoreObserver obs) { observers.add(obs); }

    @Override
    public void removeObserver(ScoreObserver obs) { observers.remove(obs); }

    @Override
    public void notifyObservers(ScoreEvent event) {
        ScoreObserver[] snapshot;
        synchronized (observers) { snapshot = observers.toArray(ScoreObserver[]::new); }
        for (ScoreObserver o : snapshot) o.onScoreChanged(event);
    }

    public void addPoints(String playerId, int delta) {
        int next = totals.merge(playerId, delta, Integer::sum);
        notifyObservers(new ScoreEvent(playerId, delta, next));
    }

    public int getTotal(String playerId) {
        return totals.getOrDefault(playerId, 0);
    }
}

