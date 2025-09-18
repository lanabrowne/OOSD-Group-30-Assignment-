package org.oosd.ui;

import javafx.scene.Parent;
import org.oosd.config.TetrisConfigView;

public class ConfigScreen implements Screen {
    private final Parent root;

    public ConfigScreen(
            Runnable singlePlayer,
            Runnable twoPlayer,
            Runnable back
    ) {
        // hand the callbacks straight to TetrisConfigView
        root = TetrisConfigView.buildConfigRoot(back, singlePlayer, twoPlayer);
    }

    @Override
    public Parent getScreen() { return root; }

    @Override
    public void setRoute(String name, Screen screen) { /* no-op */ }
}
