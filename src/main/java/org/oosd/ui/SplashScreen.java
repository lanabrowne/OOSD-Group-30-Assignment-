package org.oosd.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen {

    public static void show(Stage stage, Runnable onFinish, double width, double height) {
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setAlwaysOnTop(true);

        BorderPane splashScreen = new BorderPane();
        splashScreen.setStyle("-fx-background-color: white;");

        // Load images
        Image tetrisTopImg = new Image(SplashScreen.class.getResourceAsStream("/Images/TetrisSplashScreenTop.jpg"));
        Image tetrisBottomImg = new Image(SplashScreen.class.getResourceAsStream("/Images/TetrisSplashScreenBottom.jpg"));

        // Top banner
        ImageView topImg = new ImageView(tetrisTopImg);
        topImg.setPreserveRatio(true);
        splashScreen.setTop(topImg);
        BorderPane.setAlignment(topImg, Pos.CENTER);

        // Bottom banner
        ImageView bottomImg = new ImageView(tetrisBottomImg);
        bottomImg.setPreserveRatio(true);
        splashScreen.setBottom(bottomImg);
        BorderPane.setAlignment(bottomImg, Pos.CENTER);

        // Group information text
        VBox textBox = new VBox(10);
        textBox.setAlignment(Pos.CENTER);
        Text groupInfo = new Text(
                "Group ID: PG 30\n" +
                        "Members:\n" +
                        "s5340293, Lana Browne, 2805ICT\n" +
                        "s5350825, Taylor Brown, 2006ICT\n" +
                        "s5404819, Ria Rajesh, 2006ICT\n" +
                        "s5339308, Ikkei Fukuta, 2006ICT\n" +
                        "s5373939, Kosuke Sato, 2006ICT"
        );
        groupInfo.setStyle("-fx-fill: black; -fx-font-size: 22px;");
        textBox.getChildren().add(groupInfo);
        splashScreen.setCenter(textBox);

        // Scene and show
        Scene splashScene = new Scene(splashScreen, width, height);
        topImg.fitWidthProperty().bind(splashScene.widthProperty());
        bottomImg.fitWidthProperty().bind(splashScene.widthProperty());

        splashStage.setScene(splashScene);
        splashStage.centerOnScreen();
        splashStage.show();

        // Delay and then run callback
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(4000); // splash duration
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    splashStage.close();
                    onFinish.run(); // show main menu
                });
            }
        };
        new Thread(loadTask).start();
    }
}
