package org.oosd.model;

public interface IPieceGenerator {
    TetrominoType nextType();
    TetrominoType peekType();

}
