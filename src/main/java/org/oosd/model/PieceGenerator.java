package org.oosd.model;

import java.util.*;

public class PierceGenerator implements PieceGenerator {
    private final Random rng;
    private final Deque<TetrominoType> bag = new ArrayDeque<>();

    public PieceGenerator(long seed)
    {
        this.rng = new Random(seed);
        refill();
    }

    private void refill()
    {
        List<TetrominoType> list = new ArrayList<>(Arrays.asList(TetrominoType.values()));
        list.removeIf(t -> t == TetrominoType.NONE);
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
