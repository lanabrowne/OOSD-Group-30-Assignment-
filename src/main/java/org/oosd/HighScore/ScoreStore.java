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

public class ScoreStore {
    private static final ObservableList<PlayerScore> scores = FXCollections.observableArrayList();
    private static final int MAX_ENTRIES = 10; // ← Top10

    public static ObservableList<PlayerScore> getScores() { return scores; }

    public static void clear() {
        scores.clear();
        ensurePadded();
    }

    public static class Entry {
        String name;
        int score;
        String config;
    }

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
    private static void ensurePadded() {
        while (scores.size() < MAX_ENTRIES) {
            scores.add(PlayerScore.placeholder());
        }
    }
}