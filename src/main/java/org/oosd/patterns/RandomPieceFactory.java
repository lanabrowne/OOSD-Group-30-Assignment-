package org.oosd.patterns;

import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Concrete Factory: creates specific or random Tetromino instances.
 * Encapsulates spawn positioning logic (centered by spawn width).
 */
public final class RandomPieceFactory implements PieceFactory {

    @Override
    public Tetromino create(TetrominoType type, int boardWidth) {
        Tetromino t = new Tetromino(type, 0, 0, 0);
        // center spawn horizontally using piece spawn width
        int col = Math.max(0, (boardWidth - t.spawnWidth()) / 2);
        t.col = col;
        t.row = 0;
        return t;
    }

    @Override
    public Tetromino createRandom(int boardWidth) {
        TetrominoType[] types = TetrominoType.values();
        TetrominoType randomType = types[ThreadLocalRandom.current().nextInt(types.length)];
        return create(randomType, boardWidth);
    }
}