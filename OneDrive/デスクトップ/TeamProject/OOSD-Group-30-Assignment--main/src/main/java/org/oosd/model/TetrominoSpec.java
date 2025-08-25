package org.oosd.model;

/**
 * This class stores all tetromino blocks information by record type
 * Store and manage all blocks by this class
 *
 * This class determines by records and use from TetrominoType class
 * //The coordinates are separated to Tetromino Shapes class to solve the
 * Reference forward problem.
 * @param rotations
 */
public record TetrominoSpec(int[][][] rotations) {


}
