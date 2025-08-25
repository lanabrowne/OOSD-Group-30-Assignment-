package org.oosd.model;

/**
 * This class is creating tetris blocks and sending to Tertromino Type class
 * to hold at Tetromino Spec record class.
 * I created this class to avoid Reference forward problem.
 */
final class TetrominoShapes {
    private TetrominoShapes()
    {

    }
    /**
     * This  method is definition of shapes
     * This method is including all "Type", "rot" (rotation num), "cell", "coord" (row, col)
     */
    static final int[][][] SHAPES_I = {
            { {0,0},{1,0},{2,0},{3,0} }, //default shape rotation no.0
            { {0,0},{0,1},{0,2},{0,3} }, //Rotate to right No,1
            { {0,0},{1,0},{2,0},{3,0} }, //Rotate 180 No.2
            { {0,0},{0,1},{0,2},{0,3} } //Rotate to Left No.3
    };

    static final int[][][] SHAPES_O = {
            { {0,0},{1,0},{0,1},{1,1} },
            { {0,0},{1,0},{0,1},{1,1} },
            { {0,0},{1,0},{0,1},{1,1} },
            { {0,0},{1,0},{0,1},{1,1} }
    };

    static final int[][][] SHAPES_T = {
            { {0,0},{1,0},{2,0},{1,1} },
            { {1,0},{1,1},{1,2},{0,1} },
            { {0,1},{1,1},{2,1},{1,0} },
            { {0,0},{0,1},{0,2},{1,1} }
    };

    static final int[][][] SHAPES_S = {
            { {1,0},{2,0},{0,1},{1,1} },
            { {0,0},{0,1},{1,1},{1,2} },
            { {1,1},{2,1},{0,2},{1,2} },
            { {1,0},{1,1},{2,1},{2,2} }
    };

    static final int[][][] SHAPES_Z = {
            { {0,0},{1,0},{1,1},{2,1} },
            { {1,0},{0,1},{1,1},{0,2} },
            { {0,1},{1,1},{1,2},{2,2} },
            { {2,0},{1,1},{2,1},{1,2} }
    };

    static final int[][][] SHAPES_J = {
            { {0,0},{0,1},{1,1},{2,1} },
            { {0,0},{0,1},{0,2},{1,0} },
            { {0,0},{1,0},{2,0},{2,1} },
            { {1,0},{1,1},{1,2},{0,2} }
    };

    static final int[][][] SHAPES_L = {
            { {0,1},{1,1},{2,1},{2,0} },
            { {0,0},{0,1},{0,2},{1,2} },
            { {0,0},{1,0},{2,0},{0,1} },
            { {1,0},{1,1},{1,2},{0,0} }
    };

}
