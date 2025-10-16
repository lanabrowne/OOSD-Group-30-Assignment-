package org.oosd.HighScore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Low-level file I/O for the high score JSON.
 * NOTE: Works on a writable file on disk (not inside the JAR).
 */
public final class HighScoreWriter {

    // Writable path inside the project tree (adjust if you package the app)
    public static final Path SCORE_PATH = Path.of(
            System.getProperty("user.dir"),
            "src","main","resources","org","oosd","HighScore","JavaTetrisScore.json"
    );

    private HighScoreWriter() {}

    /** Ensure the JSON file exists; create an empty array "[]" if missing. */
    public static void ensureFile() {
        try {
            if (Files.exists(SCORE_PATH)) return;
            Files.createDirectories(SCORE_PATH.getParent());
            Files.writeString(SCORE_PATH, "[]",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Append a new score row (read-modify-write). */
    public static synchronized void append(String name, int score, String configTag) {
        try {
            ensureFile();
            String json = Files.readString(SCORE_PATH, StandardCharsets.UTF_8).trim();
            JSONArray arr = (json.isEmpty() ? new JSONArray() : new JSONArray(json));

            JSONObject o = new JSONObject()
                    .put("name", name)
                    .put("score", score)
                    .put("config", configTag);
            arr.put(o);

            Files.writeString(SCORE_PATH, arr.toString(2),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Truncate the file to an empty JSON array. */
    public static void clearFile() {
        clearFile(SCORE_PATH);
    }

    /** Truncate any given path to an empty JSON array. */
    public static synchronized void clearFile(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, "[]",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}