package org.oosd.patterns.observer;

public interface ScoreSubject {
    void addObserver(ScoreObserver obs);
    void removeObserver(ScoreObserver obs);
    void notifyObservers(ScoreEvent event);
}

