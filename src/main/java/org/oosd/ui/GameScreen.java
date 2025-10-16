package org.oosd.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.oosd.controller.GameController;

import java.io.IOException;

public class GameScreen implements ScreenWithGame{

        private final Frame parent;
        private VBox borderPane;
        private Screen mainScreen;
        private GameController controller; // Keeps reference to controller

        public GameScreen(Frame frame) {
            this.parent = frame;
            buildScreen();

        }
        public void onShow(){
            if(controller != null) {
                controller.startGame();
            }
        }

    private void buildScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.oosd/fxml/GameScreen.fxml"));
            borderPane = loader.load();

            // Grab controller for later use
            controller = loader.getController();

            //Inject frame reference (main instance)
            controller.setParent(parent);

            initializeGameController(controller);



//            // Wire back button navigation
//            controller.getBtnBack().setOnAction(e -> {
//                if (mainScreen != null) {
//                    parent.setScreen(mainScreen);
//                }
//            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        @Override
        public void initializeGameController(GameController controller) {
            this.controller = controller;
        }


    @Override
        public Parent getScreen() {
            return borderPane;
        }

        @Override
        public void setRoute(String path, Screen screen) {
            if ("back".equals(path)) {
                mainScreen = screen;
            }

        }
    }

