package org.oosd.HighScore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory store backing the TableView.
 * Keeps Top-10 and pads with placeholder rows so the table height is stable.
 */
public class ScoreStore {
    private static final ObservableList<PlayerScore> scores = FXCollections.observableArrayList();
    private static final int MAX_ENTRIES = 10; // Top 10 only

    public static ObservableList<PlayerScore> getScores() { return scores; }

    public static void clear() {
        scores.clear();
        ensurePadded();
    }

    // Gson DTO for resource-based loader (optional)
    public static class Entry {
        String name;
        int score;
        String config;
    }

    /** Load from a classpath resource (read-only). Kept for compatibility/tests. */
    public static void loadFromJsonResource(String resourcePath) {
        try (InputStream is = ScoreStore.class.getResourceAsStream(resourcePath);
             Reader reader = (is == null ? null : new InputStreamReader(is, StandardCharsets.UTF_8))) {

            scores.clear();

            if (reader == null) {
                System.err.println("JSON not found: " + resourcePath);
                ensurePadded();
                return;
            }

            List<Entry> list = new Gson().fromJson(reader, new TypeToken<List<Entry>>(){}.getType());
            if (list != null) {
                for (Entry e : list) {
                    if (e != null) scores.add(new PlayerScore(e.score, e.name, e.config));
                }
                scores.sort(Comparator.comparingInt(PlayerScore::getScore).reversed());
                if (scores.size() > MAX_ENTRIES) scores.remove(MAX_ENTRIES, scores.size());
            }
            ensurePadded();
        } catch (Exception ex) {
            ex.printStackTrace();
            scores.clear();
            ensurePadded();
        }
    }

    /** Ensure exactly MAX_ENTRIES rows by padding with placeholders. */
    private static void ensurePadded() {
        while (scores.size() < MAX_ENTRIES) {
            scores.add(PlayerScore.placeholder());
        }
    }

    /** Load from a writable file on disk, keep Top-10 and pad with placeholders. */
    public static void loadFromJsonFile(Path path) {
        final int MAX_ROWS = 10;
        try {
            if (!Files.exists(path)) {
                Files.writeString(path, "[]", UTF_8);
            }
            String json = Files.readString(path, UTF_8);
            JSONArray arr = json.isBlank() ? new JSONArray() : new JSONArray(json);

            List<PlayerScore> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String name = o.optString("name", "-");
                int score   = o.optInt("score", 0);
                String cfg  = o.optString("config", "-");

                list.add(new PlayerScore(score, name, cfg));
            }

            // Sort -> take Top-10 -> pad
            list.sort(Comparator.comparingInt(PlayerScore::getScore).reversed());
            if (list.size() > MAX_ROWS) {
                list = new ArrayList<>(list.subList(0, MAX_ROWS));
            }
            while (list.size() < MAX_ROWS) {
                list.add(PlayerScore.placeholder());
            }

            getScores().setAll(list);
        } catch (Exception e) {
            e.printStackTrace();
            // On error show 10 placeholders (table size stays stable)
            List<PlayerScore> ph = new ArrayList<>();
            for (int i = 0; i < 10; i++) ph.add(PlayerScore.placeholder());
            getScores().setAll(ph);
        }
    }

    /** Truncate the file to "[]" and clear the in-memory list. */
    public static void clearFile(Path path) {
        try {
            Files.writeString(path, "[]", UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            getScores().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}