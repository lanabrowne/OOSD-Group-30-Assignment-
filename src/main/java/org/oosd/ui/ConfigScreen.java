package org.oosd.ui;

import javafx.scene.Parent;
import org.oosd.config.TetrisConfigView;

public class ConfigScreen implements Screen {
    private final Runnable singlePlayer;
    private final Runnable twoPlayer;
    private final Runnable twoPlayerAI; // NEW
    private final Runnable back;

    public ConfigScreen(Runnable singlePlayer, Runnable twoPlayer, Runnable twoPlayerAI, Runnable back) {
        this.singlePlayer = singlePlayer;
        this.twoPlayer = twoPlayer;
        this.twoPlayerAI = twoPlayerAI; // NEW
        this.back = back;
    }

    @Override
    public Parent getScreen() {
        return TetrisConfigView.buildConfigRoot(back, singlePlayer, twoPlayer, twoPlayerAI); // pass it along
    }


    @Override
    public void setRoute(String name, Screen screen) { /* no-op */ }
}
