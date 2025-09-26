package org.oosd.model;

public class TetrisAI {

    private final BoardEvaluator evaluator = new BoardEvaluator();

    /**
     * Finds the best move for the current piece, optionally considering the next piece.
     */
    public Move findBestMove(int[][] board, int boardHeight, int boardWidth,
                             Tetromino current, Tetromino next) {

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        // Determine number of unique rotations
        int maxRotations = current.type.maxRotation(); // O=1, I/S/Z=2, others=4

        for (int rot = 0; rot < maxRotations; rot++) {
            // Always start from a fresh piece with absolute rotation
            Tetromino rotated = new Tetromino(current.type, rot, 0, 0);

            int minCol = 0;
            int maxCol = boardWidth - rotated.spawnWidth();

            for (int col = minCol; col <= maxCol; col++) {
                int dropRow = getDropRow(board, boardHeight, boardWidth, rotated, col);
                if (dropRow < 0) continue;

                int[][] boardCopy = copyBoard(board);
                placePiece(boardCopy, rotated, dropRow, col);

                int score;
                if (next != null) {
                    score = evaluateWithNext(boardCopy, boardHeight, boardWidth, next);
                } else {
                    score = evaluator.evaluateBoard(boardCopy);
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new Move(col, rot, score);
                }
            }
        }
        return bestMove;
    }



        private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[board.length][board[0].length];
        for (int r = 0; r < board.length; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, board[0].length);
        }
        return copy;
    }

    private void placePiece(int[][] board, Tetromino piece, int row, int col) {
        for (int[] cell : piece.cells()) {
            int r = row + cell[1];
            int c = col + cell[0];
            if (r >= 0 && r < board.length && c >= 0 && c < board[0].length) {
                board[r][c] = 1; // or piece type
            }
        }
    }

    private int getDropRow(int[][] board, int boardHeight, int boardWidth,
                           Tetromino piece, int col) {
        int lastValidRow = -1;

        for (int row = 0; row < boardHeight; row++) {
            Tetromino test = new Tetromino(piece.type, piece.rotation, row, col);
            if (canPlace(board, test)) {
                lastValidRow = row; // keep track of the lowest valid row
            } else {
                break; // as soon as it collides, stop
            }
        }

        return lastValidRow; // -1 means "can’t place here"
    }


    private boolean canPlace(int[][] board, Tetromino piece) {
        for (int[] cell : piece.cells()) {
            int r = piece.row + cell[1];
            int c = piece.col + cell[0];
            if (r < 0 || r >= board.length || c < 0 || c >= board[0].length) return false;
            if (board[r][c] != 0) return false;
        }
        return true;
    }

    private int evaluateWithNext(int[][] board, int boardHeight, int boardWidth, Tetromino next) {
        int bestScore = Integer.MIN_VALUE;

        int maxRotations = next.type.maxRotation();
        for (int rot = 0; rot < maxRotations; rot++) {
            Tetromino rotated = new Tetromino(next.type, rot, 0, 0);
            int minCol = 0;
            int maxCol = boardWidth - rotated.spawnWidth();

            for (int col = minCol; col <= maxCol; col++) {
                int dropRow = getDropRow(board, boardHeight, boardWidth, rotated, col);
                if (dropRow < 0) continue;

                int[][] boardCopy = copyBoard(board);
                placePiece(boardCopy, rotated, dropRow, col);

                int score = evaluator.evaluateBoard(boardCopy);
                bestScore = Math.max(bestScore, score);
            }
        }

        return bestScore;
    }


}
