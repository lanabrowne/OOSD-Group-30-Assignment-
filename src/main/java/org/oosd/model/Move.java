package org.oosd.model;

import java.util.Comparator;

public class Move {
    public final int col;
    public final int rotation;
    public final int score; // optional

    public Move(int col, int rotation, int score) {
        this.col = col;
        this.rotation = rotation;
        this.score = score;
    }

    // Comparator to compare moves by score descending (higher score first)
    public static final Comparator<Move> SCORE_DESCENDING =
            (m1, m2) -> Integer.compare(m2.score, m1.score);

    // Comparator to compare moves by score ascending (lower score first)
    public static final Comparator<Move> SCORE_ASCENDING =
            Comparator.comparingInt(m -> m.score);
}
