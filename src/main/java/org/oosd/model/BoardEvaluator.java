package org.oosd.model;

public class BoardEvaluator {

    /**
     * Evaluate a Tetris board.
     * Positive score = better board state.
     */
    public int evaluateBoard(int[][] board) {
        int linesCleared = getClearedLines(board);
        int holes = getHoles(board);
        int aggregateHeight = getAggregateHeight(board);
        int bumpiness = getBumpiness(board);
        int rowTransitions = getRowTransitions(board);
        int columnTransitions = getColumnTransitions(board);
        // Weighted scoring: lines cleared > holes > height > bumpiness
        return  -2 * aggregateHeight
                - 9 * holes
                - 1 * bumpiness
                - 1 * rowTransitions
                - 1 * columnTransitions
                + 20 * linesCleared
                - getColumnHeightPenalty(board); // extra
    }

    private int getClearedLines(int[][] board) {
        int cleared = 0;
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
        return cleared;
    }

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

    private int getAggregateHeight(int[][] board) {
        int total = 0;
        for (int x = 0; x < board[0].length; x++) {
            total += getColumnHeight(board, x);
        }
        return total;
    }

    private int getColumnHeight(int[][] board, int col) {
        for (int y = 0; y < board.length; y++) {
            if (board[y][col] != 0) return board.length - y;
        }
        return 0;
    }

    private int getBumpiness(int[][] board) {
        int bump = 0;
        for (int x = 0; x < board[0].length - 1; x++) {
            bump += Math.abs(getColumnHeight(board, x) - getColumnHeight(board, x + 1));
        }
        return bump;
    }
    private int getColumnHeightPenalty(int[][] board) {
        int penalty = 0;
        int boardHeight = board.length;
        int boardWidth = board[0].length;

        for (int x = 0; x < boardWidth; x++) {
            int colHeight = getColumnHeight(board, x);

            // Only penalize really tall columns (>85% of height)
            if (colHeight > boardHeight * 0.85) {
                penalty += (colHeight - boardHeight * 0.85) * 5; // reduced weight
            }

            // Optional: slightly reduce penalty for columns near edges to encourage full-board usage
            // e.g., columns 0 and boardWidth-1 get 50% of the penalty
            if (x == 0 || x == boardWidth - 1) {
                penalty /= 2;
            }
        }

        return penalty;
    }

    private int getRowTransitions(int[][] board) {
        int transitions = 0;
        for (int y = 0; y < board.length; y++) {
            int prev = 1; // treat left edge as filled
            for (int x = 0; x < board[0].length; x++) {
                int curr = board[y][x] == 0 ? 0 : 1;
                if (curr != prev) transitions++;
                prev = curr;
            }
            if (prev == 0) transitions++; // treat right edge as filled
        }
        return transitions;
    }

    private int getColumnTransitions(int[][] board) {
        int transitions = 0;
        for (int x = 0; x < board[0].length; x++) {
            int prev = 1; // treat top edge as filled
            for (int y = 0; y < board.length; y++) {
                int curr = board[y][x] == 0 ? 0 : 1;
                if (curr != prev) transitions++;
                prev = curr;
            }
            if (prev == 0) transitions++; // treat bottom edge as filled
        }
        return transitions;
    }
}
