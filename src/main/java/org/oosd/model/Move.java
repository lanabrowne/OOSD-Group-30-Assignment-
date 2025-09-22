package org.oosd.model;

public class Move {
    public final int col;
    public final int rotation;
    public final int score; // optional

    public Move(int col, int rotation, int score) {
        this.col = col;
        this.rotation = rotation;
        this.score = score;
    }
}

