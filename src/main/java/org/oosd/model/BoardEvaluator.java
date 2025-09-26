package org.oosd.model;

public class BoardEvaluator {

    /**
     * This class provides the heuristic functions of a Tetris game.
     * The AI uses these heuristics to evaluate the best possible move.
     * These weights were evaluated to heavily discourage holes and covered cells
     * It slightly discourages bumpiness and row/col transistions
     * and strongly rewards clearing multiple lines.
     *
     *
     */
    public int evaluateBoard(int[][] board) {
        int linesScore = getClearedLinesWeighted(board);
        int holes = getHoles(board);
        int bumpiness = getBumpiness(board);
        int rowTransitions = getRowTransitions(board);
        int columnTransitions = getColumnTransitions(board);
        int wellDepths = getWellDepths(board);
        int coveredCells = getCoveredCells(board);

        return
                - 50 * holes
                        - 10 * bumpiness
                        - 3 * rowTransitions
                        - 3 * columnTransitions
                        - 20 * wellDepths
                        - 25 * coveredCells
                        + linesScore
                        - getColumnHeightPenalty(board);
    }

    /**
     * Calculates the "weighted" points based on the number of lines cleared in the current board
     */
    private int getClearedLinesWeighted(int[][] board) {
        int cleared = 0;
        // Count how many rows are completely filled with blocks
        for (int y = 0; y < board.length; y++) {
            boolean full = true;
            for (int x = 0; x < board[0].length; x++) {
                if (board[y][x] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) cleared++;
        }
        // Scoring formula based on how many lines are cleared at once
        if (cleared == 1) return 40;
        if (cleared == 2) return 100;
        if (cleared == 3) return 300;
        if (cleared == 4) return 800; // Massive bonus for a Tetris(4 lines)
        return 0;
    }
    /*returns the number of holes in the board */
    private int getHoles(int[][] board) {
        int holes = 0;
        for (int x = 0; x < board[0].length; x++) {
            boolean blockFound = false;
            for (int y = 0; y < board.length; y++) {
                if (board[y][x] != 0) blockFound = true;
                else if (blockFound) holes++;
            }
        }
        return holes;
    }
    /* Returns column height */
    private int getColumnHeight(int[][] board, int col) {
        for (int y = 0; y < board.length; y++) {
            if (board[y][col] != 0) return board.length - y;
        }
        return 0;
    }
    // Measures how uneven the surface of this board
    private int getBumpiness(int[][] board) {
        int bump = 0;
        for (int x = 0; x < board[0].length - 1; x++) {
            bump += Math.abs(getColumnHeight(board, x) - getColumnHeight(board, x + 1));
        }
        return bump;
    }
    // Penalises AI for having columns that are too high, reduces penalty for edge cases
    private int getColumnHeightPenalty(int[][] board) {
        int penalty = 0;
        int boardHeight = board.length;
        int boardWidth = board[0].length;

        for (int x = 0; x < boardWidth; x++) {
            int colHeight = getColumnHeight(board, x);

            // Only penalize really tall columns (>85% of height)
            if (colHeight > boardHeight * 0.85) {
                penalty += (colHeight - boardHeight * 0.85) * 5;
            }

            // Slightly reduce penalty for edge columns
            if (x == 0 || x == boardWidth - 1) {
                penalty /= 2;
            }
        }
        return penalty;
    }
    // Measures how uneven a row is
    private int getRowTransitions(int[][] board) {
        int transitions = 0;
        for (int y = 0; y < board.length; y++) {
            int prev = 1; // treat left edge as filled
            for (int x = 0; x < board[0].length; x++) {
                int curr = board[y][x] == 0 ? 0 : 1;
                if (curr != prev) transitions++;
                prev = curr;
            }
            if (prev == 0) transitions++; // right edge as filled
        }
        return transitions;
    }
    // measures how jagged a column is
    private int getColumnTransitions(int[][] board) {
        int transitions = 0;
        for (int x = 0; x < board[0].length; x++) {
            int prev = 1; // treat top edge as filled
            for (int y = 0; y < board.length; y++) {
                int curr = board[y][x] == 0 ? 0 : 1;
                if (curr != prev) transitions++;
                prev = curr;
            }
            if (prev == 0) transitions++; // bottom edge as filled
        }
        return transitions;
    }
    // measures how many wells(empty cells surrounded by cells on either side) exist across the board
    private int getWellDepths(int[][] board) {
        int depth = 0;
        int width = board[0].length;
        int height = board.length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (board[y][x] == 0) {
                    if ((x == 0 || board[y][x-1] != 0) &&
                            (x == width-1 || board[y][x+1] != 0)) {
                        depth++;
                    }
                } else break;
            }
        }
        return depth;
    }
    // counts the empty spaces that have at least one block above them
    // These covered cells are pretty bad as they can not be directly filled
    private int getCoveredCells(int[][] board) {
        int covered = 0;
        int height = board.length;
        int width = board[0].length;

        for (int x = 0; x < width; x++) {
            boolean foundBlock = false;
            for (int y = 0; y < height; y++) {
                if (board[y][x] != 0) {
                    foundBlock = true;
                } else if (foundBlock) {
                    covered++; // empty cell with block above
                }
            }
        }
        return covered;
    }
}
