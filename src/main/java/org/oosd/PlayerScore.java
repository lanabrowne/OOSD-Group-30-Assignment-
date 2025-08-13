package org.oosd;

public class PlayerScore {
    private final int score;
    private final String name;

    public PlayerScore(int score, String name) {
        this.score = score;
        this.name = name;
    }

    public int getScore() { return score; }
    public String getName() { return name; }
}

