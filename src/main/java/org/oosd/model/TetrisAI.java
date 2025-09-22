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

        // Try all 4 rotations
        for (int rot = 0; rot < 4; rot++) {
            Tetromino rotated = current.rotated(rot - current.rotation); // rotate to this rotation

            // Compute column bounds for this piece
            int minCol = 0;
            int maxCol = boardWidth - rotated.spawnWidth(); // ensure it fits

            for (int col = minCol; col <= maxCol; col++) {
                // Drop piece to the lowest valid row
                int dropRow = getDropRow(board, boardHeight, boardWidth, rotated, col);
                if (dropRow < 0) continue;

                // Copy board and place piece
                int[][] boardCopy = copyBoard(board);
                placePiece(boardCopy, rotated, dropRow, col);

                // Evaluate
                int score = evaluator.evaluateBoard(boardCopy);

                // If you want, you could do 2-ply lookahead using 'next'

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
        int row = 0;
        Tetromino test = new Tetromino(piece.type, piece.rotation, row, col);
        while (canPlace(board, test)) {
            row++;
            test = new Tetromino(piece.type, piece.rotation, row, col);
        }
        return row - 1; // last valid row
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
}
