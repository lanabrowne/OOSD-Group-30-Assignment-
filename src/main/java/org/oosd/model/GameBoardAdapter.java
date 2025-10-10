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

    public void setCurrentPiece(Tetromino t) { this.currentPiece = t; }
    public void setNextPiece(Tetromino t)    { this.nextPiece = t; }

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

    public boolean moveDownOne() {
        Tetromino cur = getCurrentPiece();
        if (cur == null) return false;

        Tetromino down = cur.moved(1, 0);

        // 1) 1マスだけ落とす
        if (board.canPlace(down)) {
            // ここは「位置の更新」だけにする
            setCurrentPiece(down);   // ← 無ければ後ろに示すセッターを追加
            return true;             // まだ落ち続けられる
        }else {
            // 2) もう落とせない → ロック & 行消し
            board.lock(cur);                   // ← Boardに委譲でもOK: board.lock(cur)
            board.clearFullLines();            // ← Boardに委譲でもOK: board.clearFullLines()

            return false;
        }
    }

    private void spawnNextPiece() {
        currentPiece = nextPiece;
        nextPiece = Tetromino.random(board.getWidth());
    }

    public Board getBoard() {
        return board;
    }

public void updateBoardAsync() {
    new Thread(() -> {
        System.out.println("Background thread running: " + Thread.currentThread().getName());


        try {
            Thread.sleep(500); // simulate delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

  
        javafx.application.Platform.runLater(() -> {
            System.out.println("UI safely updated from: " + Thread.currentThread().getName());
        });
    }).start();
}

}
