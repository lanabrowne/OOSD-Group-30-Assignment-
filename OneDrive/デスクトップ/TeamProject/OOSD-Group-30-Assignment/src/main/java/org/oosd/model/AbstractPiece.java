package org.oosd.model;

public abstract class AbstractPiece {

    //Return the relative coordinates of cells based on the
    //current rotation

    /**
     * This method is returning 4 relative coordinates of cells
     * based on the current rotation condition
     * ex):
     * {0,0},{1,0},{0,1},{1,1} O piece
     * It could help to determine the cell location by user action
     * It will be used at canPlace or SpawnWidth etc
     * @return
     */
    public abstract int[][] cells();

    /**
     * This metjod is returning max, and min width by current user action
     * (rotation) using by results of cells
     * @return
     */
    public int spawnWidth()
    {

        // set initial value of min, max to find x position
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        //Using for loop to read all cells
        for (int[] c : cells())
        {
            // compare the minimum x and current x location
            //And store smaller one
            min = Math.min(min, c[0]);
            // compare the maximum x and current x of cell
            // and store larger one into max
            max = Math.max(max,c[0]);
        }
        //width is max - min - 1
        //ex) if x is 0..3 , width should be 3 - 0 + 1 because it is
        // number of row  include the edge so +1 need to be included
        return max - min + 1;


    }
}
