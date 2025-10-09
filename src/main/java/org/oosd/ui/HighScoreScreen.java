package org.oosd.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.oosd.Main;
import org.oosd.controller.GameScoreController;

import java.net.URL;

public class HighScoreScreen implements Screen {
    private final Main main;
    public HighScoreScreen(Main main) { this.main = main; }

    @Override
    public Parent getScreen() {
        try {
            URL url = HighScoreScreen.class.getResource("/org/oosd/HighScore/GameScoreScreen.fxml");
            if (url == null) {
                throw new IllegalStateException("FXML not found: /org/oosd/HighScore/GameScoreScreen.fxml " +
                        "(check: src/main/resources/org/oosd/HighScore/GameScoreScreen.fxml)");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            GameScoreController controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("Controller is null. " +
                        "Make sure fx:controller=\"org.oosd.controller.GameScoreController\" is set in FXML.");
            }
            controller.setMain(main);
            return root;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load HighScore FXML", e);
        }
    }

    @Override
    public void setRoute(String path, Screen screen) { /* not used */ }
}