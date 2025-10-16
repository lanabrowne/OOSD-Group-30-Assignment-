package org.oosd.model;

import java.util.stream.IntStream;

public class BoardEvaluator {

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

  protected int getClearedLinesWeighted(int[][] board) {
    long cleared = IntStream.range(0, board.length)
            .filter(y -> IntStream.range(0, board[0].length)
                    .allMatch(x -> board[y][x] != 0))
            .count();

    return switch ((int) cleared) {
        case 1 -> 40;
        case 2 -> 100;
        case 3 -> 300;
        case 4 -> 800;
        default -> 0;
    };
}


  protected int getHoles(int[][] board) {
    return IntStream.range(0, board[0].length)
            .map(x -> {
                boolean[] blockFound = {false};
                return (int) IntStream.range(0, board.length)
                        .filter(y -> {
                            if (board[y][x] != 0) {
                                blockFound[0] = true;
                                return false;
                            } else return blockFound[0];
                        })
                        .count();
            })
            .sum();
}


    protected int getColumnHeight(int[][] board, int col) {
        for (int y = 0; y < board.length; y++) {
            if (board[y][col] != 0) return board.length - y;
        }
        return 0;
    }

  protected int getBumpiness(int[][] board) {
    return IntStream.range(0, board[0].length - 1)
            .map(x -> Math.abs(getColumnHeight(board, x) - getColumnHeight(board, x + 1)))
            .sum();
}


    protected int getColumnHeightPenalty(int[][] board) {
        int penalty = 0;
        int boardHeight = board.length;
        int boardWidth = board[0].length;

        for (int x = 0; x < boardWidth; x++) {
            int colHeight = getColumnHeight(board, x);
            if (colHeight > boardHeight * 0.85) {
                penalty += (colHeight - boardHeight * 0.85) * 5;
            }
            if (x == 0 || x == boardWidth - 1) {
                penalty /= 2;
            }
        }
        return penalty;
    }

   protected int getRowTransitions(int[][] board) {
    return IntStream.range(0, board.length)
            .map(y -> {
                int[] prev = {1};
                int transitions = (int) IntStream.range(0, board[0].length)
                        .filter(x -> {
                            int curr = board[y][x] == 0 ? 0 : 1;
                            boolean changed = curr != prev[0];
                            prev[0] = curr;
                            return changed;
                        })
                        .count();
                if (prev[0] == 0) transitions++;
                return transitions;
            })
            .sum();
}


    protected int getColumnTransitions(int[][] board) {
        int transitions = 0;
        for (int x = 0; x < board[0].length; x++) {
            int prev = 1;
            for (int y = 0; y < board.length; y++) {
                int curr = board[y][x] == 0 ? 0 : 1;
                if (curr != prev) transitions++;
                prev = curr;
            }
            if (prev == 0) transitions++;
        }
        return transitions;
    }

    protected int getWellDepths(int[][] board) {
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

    protected int getCoveredCells(int[][] board) {
        int covered = 0;
        int height = board.length;
        int width = board[0].length;

        for (int x = 0; x < width; x++) {
            boolean foundBlock = false;
            for (int y = 0; y < height; y++) {
                if (board[y][x] != 0) {
                    foundBlock = true;
                } else if (foundBlock) {
                    covered++;
                }
            }
        }
        return covered;
    }
}
