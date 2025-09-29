package org.oosd.model;

public class GameBoardAdapter {
    private final Board board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;

    public GameBoardAdapter(Board board) {
        this.board = board;
        this.currentPiece = Tetromino.random(board.getWidth());
        this.nextPiece = Tetromino.random(board.getWidth());
    }

    public Tetromino getCurrentPiece() {
        return currentPiece;
    }

    public Tetromino getNextPiece() {
        return nextPiece;
    }

    public int[][] getGrid() {
        return board.snapshot();
    }

    public int getHeight() {
        return board.snapshot().length;
    }

    public int getWidth() {
        return board.getWidth();
    }

    public void rotateTo(int rotation) {
        currentPiece.rotation = rotation & 3;
    }

    public void moveToColumn(int col) {
        currentPiece.col = col;
    }

    /** 
     * Move piece down by one row; lock if cannot move further.
     * Returns true if game is over.
     */
    public boolean step() {
        currentPiece.row++;

        if (!board.canPlace(currentPiece)) {
            currentPiece.row--; // undo move
            board.lock(currentPiece);
            board.clearFullLines();

            // check game over (blocks at top row)
            boolean gameOver = false;
            for (int col = 0; col < board.getWidth(); col++) {
                if (board.snapshot()[0][col] != 0) {
                    gameOver = true;
                    break;
                }
            }

            if (!gameOver) {
                spawnNextPiece();
            }

            return gameOver;
        }
        return false;
    }

    private void spawnNextPiece() {
        currentPiece = nextPiece;
        nextPiece = Tetromino.random(board.getWidth());
    }

    public Board getBoard() {
        return board;
    }
}
