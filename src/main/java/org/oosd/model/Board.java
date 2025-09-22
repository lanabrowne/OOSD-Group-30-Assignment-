//clears rows and moves the remaining tetromino down :)
package org.oosd.model;
import org.oosd.sound.soundEffects;

import java.util.Arrays;


public class Board {
    public final int w; // width 
    public final int h; // height 
    private final int[][] grid;

    public Board(int width, int height) {
        this.w = width;
        this.h = height;
        this.grid = new int[h][w]; // row-major grid
    }
    //locking the tetromino into the grid by marking their positions with the colorID
//In conclusion, this happens when the tetromino lands and can no longer move 
    public void lock(Tetromino t) {
        for (int[] pos : t.cells()) {
            int row = t.row + pos[1];
            int col = t.col + pos[0];
            if (row >= 0 && row < h && col >= 0 && col < w) {
                grid[row][col] = t.type.colorId;
            }
        }
    }

    /**
     *
     * @return
     */
    public int clearFullLines() {
        soundEffects.play("lineclear");
        int cleared = 0;
        for (int row = 0; row < h; row++) {
            boolean full = true;
            for (int col = 0; col < w; col++) { //checking if each column is filled, if its not then stop checking that row.
                if (grid[row][col] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                // Shift rows above down
                for (int r = row; r > 0; r--) {
                    System.arraycopy(grid[r - 1], 0, grid[r], 0, w);
                }
                // Clear top row
                Arrays.fill(grid[0], 0);
                cleared++;
                row--; // we're rechecking the same row
            }
        }
        return cleared;
    }

    public boolean lockAndCheckGameOver(Tetromino t) {
        lock(t);
        for (int col = 0; col < w; col++) {
            if (grid[0][col] != 0) {
                return true; // Game over
            }
        }
        return false;
    }

    public boolean canPlace(Tetromino t) {
        for (int[] pos : t.cells()) {
            int row = t.row + pos[1];
            int col = t.col + pos[0];
            if (row < 0 || row >= h || col < 0 || col >= w) return false;
            if (grid[row][col] != 0) return false;
        }
        return true;
    }

    public int[][] snapshot() {
        int[][] snap = new int[h][w];
        for (int i = 0; i < h; i++) {
            System.arraycopy(grid[i], 0, snap[i], 0, w);
        }
        return snap;
    }
}
