package org.oosd.sound;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class music {

    private static MediaPlayer mediaPlayer;

    public static void play(String path) {
        try {
            // Stop previous music if any
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            Media media = new Media(music.class.getResource(path).toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // loop indefinitely
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Failed to play music: " + e.getMessage());
        }
    }

    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public static void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public static void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }
}
