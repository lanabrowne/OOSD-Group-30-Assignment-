package org.oosd.model;



// Use enum to manage multiple constants into a type safe manager

/**
 * This class is used enum type to handle the 7 types of blocks
 * Set id for set into play game board and color id for reflect block colour.
 * So that this class is designing type of blocks and block id and colour id
 * to pass to the controller and Tetromino class to put functions
 */
public enum TetrominoType{

    // id, colorId, shapes(record)
    //To send to record type class
    I(1, 1, new TetrominoSpec(TetrominoShapes.SHAPES_I)),
    O(2, 2, new TetrominoSpec(TetrominoShapes.SHAPES_O)),
    T(3, 3, new TetrominoSpec(TetrominoShapes.SHAPES_T)),
    S(4, 4, new TetrominoSpec(TetrominoShapes.SHAPES_S)),
    Z(5, 5, new TetrominoSpec(TetrominoShapes.SHAPES_Z)),
    J(6, 6, new TetrominoSpec(TetrominoShapes.SHAPES_J)),
    L(7, 7, new TetrominoSpec(TetrominoShapes.SHAPES_L));

    public final int id;
    public final int colorId;
    //Hold record in this class
    private final TetrominoSpec spec;


    TetrominoType(int id, int colorId, TetrominoSpec spec)
    {
        this.id = id;
        this.colorId = colorId;
        this.spec = spec;
    }

    /**
     * Returns the number of unique rotations for this piece.
     * O = 1, I/S/Z = 2, others = 4
     */
    public int maxRotation() {
        return switch (this) {
            case O -> 1;
            case I, S, Z -> 2;
            default -> 4;
        };
    }






    /**
     * This returns the  4 cell relative coordinates
     * @param rotation -> this is rotation number 0, 1, 2, 3
     * @return
     */
    public int[][] cells(int rotation)
    {

        return spec.rotations()[rotation & 3];
    }

}
