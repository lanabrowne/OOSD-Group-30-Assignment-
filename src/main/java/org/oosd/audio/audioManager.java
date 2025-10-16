package org.oosd.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;

public class audioManager {
    private static audioManager instance;

    private MediaPlayer musicPlayer;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;

    private boolean musicPaused = false;

    private final HashMap<String, AudioClip> sfxMap = new HashMap<>();

    private audioManager() {
        try {
            loadMusic("/audio/background.mp3");

            loadSFX("erase-line", "/audio/erase-line.wav");
            loadSFX("game-finish", "/audio/game-finish.wav");
            loadSFX("level-up", "/audio/level-up.wav");
            loadSFX("move-turn", "/audio/move-turn.wav");

        } catch (Exception e) {
            System.err.println("Error initializing audioManager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMusic(String path) {
        URL musicUrl = getClass().getResource(path);
        if (musicUrl != null) {
            Media music = new Media(musicUrl.toExternalForm());
            musicPlayer = new MediaPlayer(music);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        } else {
            System.err.println("Music file not found: " + path);
            musicPlayer = null;
        }
    }

    private void loadSFX(String name, String path) {
        URL sfxUrl = getClass().getResource(path);
        if (sfxUrl != null) {
            sfxMap.put(name, new AudioClip(sfxUrl.toExternalForm()));
        } else {
            System.err.println("SFX file not found: " + path);
        }
    }

    public static audioManager getInstance() {
        if (instance == null) {
            instance = new audioManager();
        }
        return instance;
    }

    public void playMusic() {
        if (musicEnabled && musicPlayer != null) {
            musicPlayer.play();
            musicPaused = false;
        } else if (musicPlayer == null) {
            System.err.println("Music player not available.");
        }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
            musicPaused = true;
        }
    }

    public void toggleMusic() {
        if (!musicEnabled) {
            musicEnabled = true;
            if (musicPaused && musicPlayer != null) {
                musicPlayer.play();
                musicPaused = false;
            } else {
                playMusic();
            }
        } else {
            musicEnabled = false;
            stopMusic();
        }
    }

    public void toggleSFX() {
        sfxEnabled = !sfxEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public void playSFX(String name) {
        if (sfxEnabled && sfxMap.containsKey(name)) {
            sfxMap.get(name).play();
        } else if (!sfxMap.containsKey(name)) {
            System.err.println("SFX not found: " + name);
        }
    }
}
