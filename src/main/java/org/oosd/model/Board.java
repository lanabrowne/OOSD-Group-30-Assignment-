package org.oosd.model;

import java.util.Arrays;

/**
 *
 */
public class Board {

    //Define the number of width and height
    public final int w, h;
    //Set inside of grid
    private final int[][] grid;



    public Board(int w, int h)
    {
        //Initialize the w, h, grid
        this.w = w;
        this.h = h;
        this.grid = new int[h][w];
    }

    /**
     * This method is validating whether tetromino can be put at the location col, row,
     *
     * @param t --> tetromino
     * @return
     */
    public boolean canPlace(Tetromino t)
    {
        //Collect x, y position by for loop
        for(int [] c : t.cells())
        {
            // convert row and col to gaming position number
            // r (0) is top
            int r = t.row + c[1];
            //col (0) is left + is going to right
            int col = t.col + c[0];
            // If r is out of cell and col is also out of the cell,
            // return false
            if(r < 0 || r >= h || col < 0 || col >= w)
            {
                return false;
            }
            //  if there is already block at the position,
            // return false
            if(grid[r][col] != 0)
            {
                return false;
            }
        }
        // Else all cells are in the range and empty
        // return true.
        return true;
    }

    /**
     * This method is for setting block to game
     * @param t --> Blocks
     */
    public void lock(Tetromino t)
    {

        for (int[] c : t.cells())
        {
            //Change the position to gaming cell position
            //And write tetromino number
            grid[t.row + c[1]][t.col + c[0]] = t.type.id;
        }
    }

    /**
     * This method is validating the row is filled or not
     * and deleting row or multiple rows.
     * @return --> if both 4 cells are in the range and empty return true
     */
    public int clearFullLines()
    {
        //write make row going down when row was deleted
        int write = h - 1;
        // This is counter of the number of deleting rows
        int cleared = 0;

        // Accessing 2D array playing game screen by for loop
        //from bottom to top
        for(int r = h - 1; r >= 0; r--)
        {
            //Checking row is full or not
            boolean full = true;
            //We need to check col by for loop whether 0 or not
            //--> if col is 0 (empty) it is not full line.
            for(int c = 0; c < w; c++)
            {
                if(grid[r][c] == 0)
                {
                    //Col = 0 = not full
                    //so full = false
                    full = false;
                    break;
                }
            }
            //The row that is not full should be dropped down to next row
            //So copy the data and drop the number of row by write
            if(!full)
            {
                if(write != r)
                {
                    System.arraycopy(grid[r], 0, grid[write], 0, w);

                }
                write--;
            }
            //else (full line) is not copied and deleted
            else {
                cleared++;
            }
        }
        //Once finished accessing and checking full lines,
        // all rows which left above from write, clear to 0
        while (write >= 0)
        {
            Arrays.fill(grid[write--], 0);
        }
        return cleared;

    }

    /**
     * this method is for copying the game play screen to draw
     * @return
     */
    public int[][] snapshot()
    {
        //to avoid same access by UI and Model, create copy
        int[][] cp = new int[h][w];
        //copy the all rows and cols
        for(int r = 0; r < h; r++)
        {
            System.arraycopy(grid[r], 0, cp[r], 0, w);
        }
        // return
        return cp;
    }


}
