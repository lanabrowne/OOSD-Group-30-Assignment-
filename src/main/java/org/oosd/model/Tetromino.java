package org.oosd.model;

import java.util.Random;

public class Tetromino extends AbstractPiece {

    public final TetrominoType type;
    public int row;
    public int col;
    public int rotation;

    private static final TetrominoType[] TYPES = TetrominoType.values();
    private static final Random rand = new Random();

    public Tetromino(TetrominoType type, int rotation, int row, int col) {
        this.type = type;
        this.rotation = rotation & 3;
        this.row = row;
        this.col = col;
    }

    @Override
    public int[][] cells() {
        return type.cells(rotation);
    }

    public Tetromino moved(int dr, int dc) {
        return new Tetromino(type, rotation, row + dr, col + dc);
    }

    public Tetromino rotated(int dir) {
        return new Tetromino(type, rotation + (dir > 0 ? 1 : 3), row, col);
    }

    public int spawnWidth() {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int[] c : cells()) {
            min = Math.min(min, c[0]);
            max = Math.max(max, c[0]);
        }
        return (max - min + 1);
    }

    // ---- Correct random method ----
    public static Tetromino random(int boardWidth) {
        TetrominoType randomType = TYPES[rand.nextInt(TYPES.length)];
        int startCol = boardWidth / 2 - 1; // center start
        return new Tetromino(randomType, 0, 0, startCol);
    }
   public Tetromino copy() {
    return new Tetromino(this.type, this.rotation, this.row, this.col);
}
}
