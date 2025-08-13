package org.oosd.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * This class is setting the method of tetromino type, color, and shape
 * and block actions (Move Right, Left and Rotation)
 */

public class Tetromino {
    //write block type into Board grid with block Id
    public final TetrominoType type;
    //Set game playing row top is 0 and count increment by going down
    public int row;
    //Set col count increment by going right
    public int col;
    //Set rotation number 0 = default, +1 = rotate right side +2 = 180, +3 = left rotate
    public int rotation;

    /**
     * Initialize the type, color and shape for creating new instance
     * by creating constructor
     * Set parameters by type, color and 2d shape
     * @param type Type of blocks
     * @param rotation --> default (0)
     * @param row
     * @param col
     */
    public Tetromino(TetrominoType type,  int rotation, int row, int col)
    {
        this.type = type;
        this.rotation = rotation & 3;
        this.row = row;
        this.col = col;
    }

    /**
     * return the position of 4 cells by rotation
     * the value of position is [x row, y col]
     * @return
     */
    public int[][] cells()
    {
        return type.cells(rotation);
    }


    /**
     * return new instance that is moved relative to the current instance
     * @param dr (down row) +1
     * @param dc (down col) right = +1, left = -1
     * @return
     */
    public Tetromino moved(int dr, int dc)
    {
        return new Tetromino(type, rotation, row + dr, col + dc);
    }

    //return rotate request and All actions will be validated
    //At controller and Board class. here is just execute action
    //no matter its valid or invalid

    /**
     * Return the new instance which was rotated
     * @param dir +1 = rotate right, -1 = rotate left
     * @return
     */
    public Tetromino rotated(int dir)
    {
        return new Tetromino(type, rotation + (dir > 0 ? 1 : 3), row, col);
    }

    //Initial position is set to center by width

    /**
     * Set first drop block at Top of Center
     * @return
     */
    public  int spawnWidth()
    {
        //Calculate the width by current rotation to set blocks dropping from center
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int[] c : cells())
        {
            min = Math.min(min, c[0]);
            max = Math.max(max, c[0]);
        }
        return (max - min + 1);
    }

}
