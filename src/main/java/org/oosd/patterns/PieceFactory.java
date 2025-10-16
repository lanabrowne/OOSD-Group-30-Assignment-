package org.oosd.patterns;

import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoType;

/** Factory interface for creating Tetromino pieces. */
public interface PieceFactory {
    Tetromino create(TetrominoType type, int boardWidth);
    Tetromino createRandom(int boardWidth);
}

