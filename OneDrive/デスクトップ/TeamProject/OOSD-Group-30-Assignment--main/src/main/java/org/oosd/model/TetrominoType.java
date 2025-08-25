package org.oosd.model;

// Use enum to manage multiple constants into a type safe manager

/**
 * This class is used enum type to handle the 7 types of blocks
 * Set id for set into play game board and color id for reflect block colour.
 * So that this class is designing type of blocks and block id and colour id
 * to pass to the controller and Tetromino class to put functions
 */
public enum TetrominoType {
    // Make each block holds "id", and "Color Id"
    I(1, 1), O(2, 2), T(3, 3), S(4, 4), Z(5, 5), J(6, 6), L(7, 7);

    public final int id;
    public final int colorId;

    TetrominoType(int id, int colorId)
    {
        this.id = id;
        this.colorId = colorId;
    }

    /**
     * This  method is definition of shapes
     * This method is including all "Type", "rot" (rotation num), "cell", "coord" (row, col)
     */
    private static final int [][][][] SHAPES = {

            // I
            { { {0,0},{1,0},{2,0},{3,0} },  //default shape rotation no.0
                    { {0,0},{0,1},{0,2},{0,3} },  //Rotate to right No,1
                    { {0,0},{1,0},{2,0},{3,0} },  //Rotate 180 No.2
                    { {0,0},{0,1},{0,2},{0,3} }   //Rotate to Left No.3
            },
            // O
            { { {0,0},{1,0},{0,1},{1,1} },
                    { {0,0},{1,0},{0,1},{1,1} },
                    { {0,0},{1,0},{0,1},{1,1} },
                    { {0,0},{1,0},{0,1},{1,1} }
            },
            // T
            { { {0,0},{1,0},{2,0},{1,1} },
                    { {1,0},{1,1},{1,2},{0,1} },
                    { {0,1},{1,1},{2,1},{1,0} },
                    { {0,0},{0,1},{0,2},{1,1} }
            },
            // S
            { { {1,0},{2,0},{0,1},{1,1} },
                    { {0,0},{0,1},{1,1},{1,2} },
                    { {1,1},{2,1},{0,2},{1,2} },
                    { {1,0},{1,1},{2,1},{2,2} }
            },
            // Z
            { { {0,0},{1,0},{1,1},{2,1} },
                    { {1,0},{0,1},{1,1},{0,2} },
                    { {0,1},{1,1},{1,2},{2,2} },
                    { {2,0},{1,1},{2,1},{1,2} }
            },
            // J
            { { {0,0},{0,1},{1,1},{2,1} },
                    { {0,0},{0,1},{0,2},{1,0} },
                    { {0,0},{1,0},{2,0},{2,1} },
                    { {1,0},{1,1},{1,2},{0,2} }
            },
            // L
            { { {0,1},{1,1},{2,1},{2,0} },
                    { {0,0},{0,1},{0,2},{1,2} },
                    { {0,0},{1,0},{2,0},{0,1} },
                    { {1,0},{1,1},{1,2},{0,0} }
            }
    };

    /**
     * This returns the  4 cell relative coordinates
     * @param rotation -> this is rotation number 0, 1, 2, 3
     * @return
     */
    public int[][] cells(int rotation)
    {

        return SHAPES[ordinal()][rotation & 3];
    }

}
