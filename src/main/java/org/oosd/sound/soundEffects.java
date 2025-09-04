package org.oosd.sound;


import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

public class soundEffects {
    private static final Map<String, AudioClip> sounds = new HashMap<>();
    private static boolean enabled = true;

    /**
     * Initialize all sounds.
     * @param enableSfx whether sound effects are enabled
     */
    public static void init(boolean enableSfx) {
        enabled = enableSfx;
        System.out.println("Sound Effects Enabled: " + enabled);

        loadSound("move", "/sounds/move-turn.wav");
        loadSound("rotated", "/sounds/move-turn.wav");
        loadSound("lineclear", "/sounds/erase-line.wav");
        loadSound("gameover", "/sounds/game-finish.wav");

        System.out.println("Loaded Sounds: " + sounds.keySet());
    }

    /**
     * Load a single sound and store in the map.
     */
    private static void loadSound(String key, String path) {
        try {
            AudioClip clip = new AudioClip(soundEffects.class.getResource(path).toString());
            sounds.put(key, clip);
            System.out.println("Loaded sound: '" + key + "' from path: " + path);
        } catch (Exception e) {
            System.err.println("Could not load sound: " + path + " -> " + e.getMessage());
        }
    }

    /**
     * Play a sound if enabled.
     */
    public static void play(String key) {
        if (!enabled) return;

        AudioClip clip = sounds.get(key);
        if (clip != null) {
            System.out.println("Playing sound: " + key);
            clip.play();
        } else {
            System.err.println("Sound not found: " + key);
        }
    }

    /**
     * Enable or disable all sound effects.
     */
    public static void setEnabled(boolean on) {
        enabled = on;
    }
}
