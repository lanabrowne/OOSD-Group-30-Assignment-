package org.oosd.HighScore;

public class PlayerScore {
    private final int score;
    private final String name;
    private final String config;

    public PlayerScore(int score, String name, String config) {
        this.score = score;
        this.name = name;
        this.config = config;
    }

    public int getScore()  { return score; }
    public String getName(){ return name;  }
    public String getConfig(){ return config; }
}
