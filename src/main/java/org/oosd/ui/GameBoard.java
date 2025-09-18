package org.oosd.ui;

import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;

public class GameBoard {

    private final Board board;
    private Tetromino currentPiece;
    private int score = 0;

    private final GridPane gridPane;

    private final int cellSize = 25; // size of each block in pixels

    public GameBoard(int width, int height) {
        board = new Board(width, height);
        gridPane = new GridPane();
        spawnPiece();
        render();
    }

    public Node getNode() {
        return gridPane;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return board.lockAndCheckGameOver(currentPiece);
    }

    public void spawnPiece() {
        currentPiece = Tetromino.random(board.w);
        if (!board.canPlace(currentPiece)) {
            // Game over immediately if cannot place
            currentPiece = null;
        }
    }

    public boolean dropPiece() {
        Tetromino moved = currentPiece.moved(1, 0);
        if (board.canPlace(moved)) {
            currentPiece = moved;
            return false;
        } else {
            // lock piece and return true (landed)
            board.lock(currentPiece);
            return true;
        }
    }

    public void moveLeft() {
        Tetromino moved = currentPiece.moved(0, -1);
        if (board.canPlace(moved)) currentPiece = moved;
    }

    public void moveRight() {
        Tetromino moved = currentPiece.moved(0, 1);
        if (board.canPlace(moved)) currentPiece = moved;
    }

    public void softDrop() {
        if (!dropPiece()) score += 1; // soft drop = 1 point per cell
    }

    public void hardDrop() {
        int linesDropped = 0;
        while (!dropPiece()) linesDropped++;
        score += linesDropped * 2; // hard drop bonus
    }

    public void rotate() {
        Tetromino rotated = currentPiece.rotated(1);
        if (board.canPlace(rotated)) currentPiece = rotated;
    }

    public int clearLines() {
        int oldScore = score;
        board.clearFullLines();
        int clearedLines = (score - oldScore); // optional scoring logic
        return clearedLines;
    }

    public void addGarbage(int lines) {
        // Simple garbage: shift all rows up and add empty row at bottom
        // Implement more complex garbage as needed
        for (int i = 0; i < lines; i++) {
            board.clearFullLines(); // or implement proper garbage insertion
        }
    }

    public void render() {
        gridPane.getChildren().clear();
        int[][] snap = board.snapshot();
        for (int r = 0; r < board.h; r++) {
            for (int c = 0; c < board.w; c++) {
                Rectangle rect = new Rectangle(cellSize, cellSize);
                if (snap[r][c] != 0) rect.setFill(Color.BLUE);
                else rect.setFill(Color.LIGHTGRAY);
                rect.setStroke(Color.BLACK);
                gridPane.add(rect, c, r);
            }
        }

        // render current piece
        if (currentPiece != null) {
            for (int[] cell : currentPiece.cells()) {
                int r = currentPiece.row + cell[1];
                int c = currentPiece.col + cell[0];
                if (r >= 0 && r < board.h && c >= 0 && c < board.w) {
                    Rectangle rect = new Rectangle(cellSize, cellSize);
                    rect.setFill(Color.RED);
                    rect.setStroke(Color.BLACK);
                    gridPane.add(rect, c, r);
                }
            }
        }
    }
}
