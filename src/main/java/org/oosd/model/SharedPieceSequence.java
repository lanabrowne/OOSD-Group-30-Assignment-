package org.oosd.model;

import java.util.*;

public class SharedPieceSequence {
    private final Random rng;
    private final List<TetrominoType> buffer = new ArrayList<>();
    private int currentIndex = 0;

    public SharedPieceSequence() {
        this.rng = new Random();
    }

    public SharedPieceSequence(long seed) {
        this.rng = new Random(seed);
    }


    public TetrominoType pieceAt(int index) {
        ensureSize(index + 1);
        return buffer.get(index);
    }


    public TetrominoType peek(int index) {
        return pieceAt(index);
    }

    private void ensureSize(int size) {
        while (buffer.size() < size) {
            // 7-Bag System: 全種類1回ずつシャッフルして補充
            List<TetrominoType> bag = new ArrayList<>(Arrays.asList(TetrominoType.values()));
            Collections.shuffle(bag, rng);
            buffer.addAll(bag);
        }
    }

    public TetrominoType nextType() {
        ensureSize(buffer.size() + 1);
        return buffer.get(buffer.size() - 1);
    }


}
