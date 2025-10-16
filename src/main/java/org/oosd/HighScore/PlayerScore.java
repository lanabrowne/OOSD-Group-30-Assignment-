package org.oosd.HighScore;

/** Immutable score row model used by the TableView. */
public class PlayerScore {
    private final int score;
    private final String name;
    private final String config;
    private final boolean placeholder; // marks empty rows like "-,-,-"

    public PlayerScore(int score, String name, String config) {
        this(score, name, config, false);
    }

    public PlayerScore(int score, String name, String config, boolean placeholder) {
        this.score = score;
        this.name = name;
        this.config = config;
        this.placeholder = placeholder;
    }

    public int getScore() { return score; }
    public String getName() { return name; }
    public String getConfig() { return config; }
    public boolean isPlaceholder() { return placeholder; }

    /** Factory for a visual placeholder row. */
    public static PlayerScore placeholder() {
        return new PlayerScore(0, "—", "—", true);
    }
}