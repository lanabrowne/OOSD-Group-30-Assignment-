package org.oosd.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.oosd.Main;
import org.oosd.controller.GameScoreController;

import java.io.IOException;

public class HighScoreScreen implements Screen {
        private Main main;

        public HighScoreScreen(Main main) {
            this.main = main;
        }

        @Override
        public Parent getScreen() {
            try {
                // Load the FXML for the high score screen
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("/org.oosd/fxml/GameScoreScreen.fxml"));
                Parent highScoreRoot = loader.load();

                //Get controller instance
                GameScoreController controller = loader.getController();
                // pass main instance so controller can navigate
                controller.setMain(main);

                // Optionally, you can pass a callback or controller reference if needed
                // e.g., back button: controller.setBackAction(() -> main.showScreen(main.getMainScreen()));

                return highScoreRoot;
            } catch (IOException ex) {
                ex.printStackTrace();
                // Return empty Parent if FXML fails
                return new Parent() {};
            }
        }

        @Override
        public void setRoute(String path, Screen screen) {
            // HighScoreScreen may not need additional routes, so leave empty
        }
    }

