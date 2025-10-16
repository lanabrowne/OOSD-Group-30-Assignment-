package org.oosd.model;

import java.util.*;

public class PieceGenerator implements IPieceGenerator {
    private final Random rng;
    private final Deque<TetrominoType> bag = new ArrayDeque<>();

    public PieceGenerator(long seed)
    {
        this.rng = new Random(seed);
        refill();
    }

    private void refill()
    {
        List<TetrominoType> list = Arrays.asList(
                TetrominoType.I, TetrominoType.J, TetrominoType.L,
                TetrominoType.O, TetrominoType.S, TetrominoType.T, TetrominoType.Z
        );
        list = new ArrayList<>(list);
        Collections.shuffle(list, rng);
        bag.addAll(list);
    }

    @Override
    public TetrominoType nextType() {
        if (bag.isEmpty()) refill();
        return bag.removeFirst();
    }

    @Override
    public TetrominoType peekType() {
        if (bag.isEmpty()) refill();
        return bag.peekFirst();
    }


}
