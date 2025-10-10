package org.oosd.model;

public class TetrisAI {

    private final BoardEvaluator evaluator = new BoardEvaluator();

    /**
     * Tries every possible rotation and column for the current tetris piece
     * simulates dropping it, and uses the board evaluator to score the outcome
     * The move with the highest "Score" is returned as the "best move" for the AI to play
     */
    public Move findBestMove(int[][] board, int boardHeight, int boardWidth,
                             Tetromino current, Tetromino next) {

        // Start with no chosen move and worst possible score as the loop runs, this gets updated when better
        //placements are found
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        // Determine number of unique rotations
        int maxRotations = current.type.maxRotation();
        // Each piece has a limited num of rotations, O=1, I/S/Z=2, others=4

        for (int rot = 0; rot < maxRotations; rot++) {
            // Always start from a fresh piece with absolute rotation
            Tetromino rotated = new Tetromino(current.type, rot, 0, 0);

            //Figure out which columns are legal to be spawned in
            int minCol = 0;
            int maxCol = boardWidth - rotated.spawnWidth();
            // For each col, check where the piece would land if dropped down
            for (int col = minCol; col <= maxCol; col++) {
                int dropRow = getDropRow(board, boardHeight, boardWidth, rotated, col);
                if (dropRow < 0) continue;

                // Create copy of board to not effect gamestate
                int[][] boardCopy = copyBoard(board);
                placePiece(boardCopy, rotated, dropRow, col);

                int score;
                // If there is a "lookahead" piece score the board while considering
                // how good the next move will be.
                if (next != null) {
                    score = evaluateWithNext(boardCopy, boardHeight, boardWidth, next);
                } else {
                    score = evaluator.evaluateBoard(boardCopy);
                }
                // if no "lookahead" piece, just score board using boardevaluator class
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new Move(col, rot, score);
                }
            }
        }
        return bestMove;
    }


    // makes a copy of the board
    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[board.length][board[0].length];
        for (int r = 0; r < board.length; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, board[0].length);
        }
        return copy;
    }
    // places tetris piece on board
    private void placePiece(int[][] board, Tetromino piece, int row, int col) {
        for (int[] cell : piece.cells()) {
            int r = row + cell[1];
            int c = col + cell[0];
            if (r >= 0 && r < board.length && c >= 0 && c < board[0].length) {
                board[r][c] = 1; // or piece type
            }
        }
    }

    // Works out how far down a tetris piece can fall in a given col before collision
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

    //checks if piece fits in the board without any collisions
    private boolean canPlace(int[][] board, Tetromino piece) {
        for (int[] cell : piece.cells()) {
            // converts cells y-coordinate into a board row
            int r = piece.row + cell[1];
            // converts cells x-coordinate into a board col
            int c = piece.col + cell[0];
            // if any part of piece lies outside the board boundaries = invalid
            if (r < 0 || r >= board.length || c < 0 || c >= board[0].length) return false;
            // if target cell is already occupied return false
            if (board[r][c] != 0) return false;
        }
        return true;
    }
    //simulates if the next piece is placed in the best possible way what is
    // the score that comes back. The AI then uses this score to avoid short
    // sighted decisions like creating holes that block the next piece

    // prevents AI from making short-sighted decisions
    public int evaluateWithNext(int[][] board, int boardHeight, int boardWidth, Tetromino next) {
        int bestScore = Integer.MIN_VALUE;

        int maxRotations = next.type.maxRotation();
        // Each piece has a limited num of rotations, O=1, I/S/Z=2, others=4
        for (int rot = 0; rot < maxRotations; rot++) {
            // Try every rotation
            Tetromino rotated = new Tetromino(next.type, rot, 0, 0);
            int minCol = 0;
            int maxCol = boardWidth - rotated.spawnWidth();
            // Try every column left -> right
            for (int col = minCol; col <= maxCol; col++) {
                // find the lowest point a piece can land
                int dropRow = getDropRow(board, boardHeight, boardWidth, rotated, col);
                if (dropRow < 0) continue;

                // copy board + place rotated piece at (droprow, col)
                int[][] boardCopy = copyBoard(board);
                placePiece(boardCopy, rotated, dropRow, col);
                // run board evaluator to determine how good score is
                int score = evaluator.evaluateBoard(boardCopy);
                // Keep track of best outcome
                bestScore = Math.max(bestScore, score);
            }
        }

        return bestScore;
    }


}
