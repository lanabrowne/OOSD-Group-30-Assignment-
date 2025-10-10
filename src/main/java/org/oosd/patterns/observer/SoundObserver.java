package org.oosd.patterns.observer;

import org.oosd.sound.SoundEffects;

public class SoundObserver implements ScoreObserver {
    @Override
    public void onScoreChanged(ScoreEvent event) {
        // if (event.delta >= 300) {
        //    try { SoundEffects.lineClear(); } catch (Throwable ignored) {}
        // }
    }
}