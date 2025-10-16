package org.oosd.controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.oosd.external.ExternalPlayer;
import org.oosd.model.GameBoardAdapter;
import org.oosd.model.Move;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrisAI;
import org.oosd.ui.Frame;
import org.oosd.ui.Screen;

public class TwoPlayerAIController {

    private final GameBoardAdapter boardAdapter;
    private final Canvas canvas;
    private final TetrisAI ai;
    //NEW
    //Current best move
    private Move pendingMove = null;
    //Current stage 0 = rotation, 1 = move LEFT/RIGHT, 2 = DOWN
    private int moveStage = 0;

    private ExternalPlayer external;



    /**
     * NEW
     */
    private String lastAction;

    private static final int BLOCK_SIZE = 30;

    private Frame parentFrame;
    private Screen previousScreen;

    public TwoPlayerAIController(GameBoardAdapter boardAdapter, Canvas canvas) {
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

        if(current == null) return true;

        //To collect AI action one by one
        if(pendingMove == null)
        {
            //if pendingMove is not set yet, calculate optimize action
            pendingMove = ai.findBestMove(
                    boardAdapter.getGrid(),
                    boardAdapter.getHeight(),
                    boardAdapter.getWidth(),
                    current,
                    next
            );
            //reset move stafe
            moveStage = 0;
        }


        //NEW
        //Initialize last action
        String action = null;

        if (pendingMove != null) {
            switch (moveStage) {
                case 0 -> {
                    if (current.rotation != pendingMove.rotation) {
                        boardAdapter.rotateTo(pendingMove.rotation);
                        action = "ROTATE";
                    }
                    moveStage++;
                }

                case 1 -> {
                    if (current.col < pendingMove.col) {
                        boardAdapter.moveToColumn(current.col + 1);
                        action = "RIGHT";
                    } else if (current.col > pendingMove.col) {
                        boardAdapter.moveToColumn(current.col - 1);
                        action = "LEFT";
                    } else {
                        moveStage++;
                    }
                }

                case 2 -> {
                    boolean canFall = boardAdapter.moveDownOne();
                    action = "DOWN";

                    if (!canFall) {

                        pendingMove = null;
                        moveStage = 0;
                    }
                }
            }
        }

        if (action != null && external != null && external.isConnected()) {
            external.sendAction();
        }

        render();
        return false;
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




    /**
     * NEW
     */
    public String getLastAction()
    {
        return lastAction;
    }
}
