package org.oosd.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import org.oosd.model.GameBoardAdapter;
import org.oosd.model.Move;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrisAI;
import org.oosd.ui.Frame;
import org.oosd.ui.Screen;

public class AIBoardController {

    private final GameBoardAdapter boardAdapter;
    private final Canvas canvas;
    private final TetrisAI ai;

    private static final int BLOCK_SIZE = 30;

    private Frame parentFrame;
    private Screen previousScreen;

    public AIBoardController(GameBoardAdapter boardAdapter, Canvas canvas) {
        this.boardAdapter = boardAdapter;
        this.canvas = canvas;
        this.ai = new TetrisAI();
    }

    public void setParentFrame(Frame frame) {
        this.parentFrame = frame;
    }

    public void setPreviousScreen(Screen screen) {
        this.previousScreen = screen;
    }

    public boolean step() {
        Tetromino current = boardAdapter.getCurrentPiece();
        Tetromino next = boardAdapter.getNextPiece();

        Move move = ai.findBestMove(
                boardAdapter.getGrid(),
                boardAdapter.getHeight(),
                boardAdapter.getWidth(),
                current,
                next
        );

        if (move != null) {
            boardAdapter.rotateTo(move.rotation);
            boardAdapter.moveToColumn(move.col);
        }

        boolean gameOver = boardAdapter.step();

        if (gameOver) {
            System.out.println("Game Over!");
        }

        render();
        return gameOver;
    }

    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
int boardPixelWidth = boardAdapter.getWidth() * BLOCK_SIZE;
int boardPixelHeight = boardAdapter.getHeight() * BLOCK_SIZE;

gc.setFill(Color.BLACK);
gc.fillRect(0, 0, boardPixelWidth, boardPixelHeight);


        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1);
        for (int r = 0; r <= boardAdapter.getHeight(); r++) {
            gc.strokeLine(0, r * BLOCK_SIZE, boardAdapter.getWidth() * BLOCK_SIZE, r * BLOCK_SIZE);
        }
        for (int c = 0; c <= boardAdapter.getWidth(); c++) {
            gc.strokeLine(c * BLOCK_SIZE, 0, c * BLOCK_SIZE, boardAdapter.getHeight() * BLOCK_SIZE);
        }

        int[][] grid = boardAdapter.getGrid();
        for (int r = 0; r < boardAdapter.getHeight(); r++) {
            for (int c = 0; c < boardAdapter.getWidth(); c++) {
                if (grid[r][c] != 0) {
                    gc.setFill(getColorFromId(grid[r][c]));
                    gc.fillRect(c * BLOCK_SIZE, r * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(c * BLOCK_SIZE, r * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }

        Tetromino current = boardAdapter.getCurrentPiece();
        if (current != null) {
            gc.setFill(getColorFromId(current.type.colorId));
            for (int[] cell : current.cells()) {
                int x = (current.col + cell[0]) * BLOCK_SIZE;
                int y = (current.row + cell[1]) * BLOCK_SIZE;

                gc.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);

                gc.setStroke(Color.BLACK);
                gc.strokeRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
    }

    private Color getColorFromId(int id) {
        return switch (id) {
            case 1 -> Color.CYAN;
            case 2 -> Color.BLUE;
            case 3 -> Color.ORANGE;
            case 4 -> Color.YELLOW;
            case 5 -> Color.GREEN;
            case 6 -> Color.PURPLE;
            case 7 -> Color.RED;
            default -> Color.GRAY;
        };
    }
}
