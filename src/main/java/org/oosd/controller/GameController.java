/****************************************************************
 PROGRAM:   Game Controller for Tetris
 AUTHOR:    IKKEI FUKUTA,

 LOGON ID:  s5339308  (Student Number)
 DUE DATE:  24th Aug 2025

 FUNCTION:  The Game control class to put functions into key
 program.

 INPUT:     GitHub\OOSD-Group-30-Assignment-
 disk

 OUTPUT:    l

 NOTES:     any relevant information that would be of
 additional help to someone looking at the program.
 ****************************************************************/


package org.oosd.controller;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import javafx.application.Platform;
import org.oosd.model.TetrominoType;

/**
 * This class is controller class to control the tetris game.This class maintains
 * tetris board and current tetromino conditions. And this class is setting
 * gravity fall by Animation Timer and move right, left rotate, drop fast
 * by bind key entry in the scene.
 * And making design of canvas (set 2 rows invisible to judge for game over)
 */

public class GameController {
    @FXML
    private Canvas gameCanvas;

    @FXML
    private VBox frameCanvas;

    @FXML
    private Button btnBack;


    /**
     * This is the number of rows that will be shown in the UI.Actual Board
     * is 22 lines but hide 2 lines to judge the game over
     */
    private static final int visibleRows = 20;
    /**
     * This is the number of hiding lines for the spawn. SO that
     * when i draw the UI, make up 2 lines.
     */
    private static final int hiddenRows = 2;

    /**
     * This is definition of free fall speed standard interval.When user
     * pressed down key, make this interval to 1 / 2
     */
    private long baseGravMs = 500;
    /**
     * This is the action of user input of down key. To switch the interval,
     * set false as default and when user pressed down key, it will be true.
     */
    private boolean downPressed = false;
    private GraphicsContext gc;

    /**
     * The time of last drop execution.Measure the interval by comparing
     * with now in the Animation Timer.
     */
    private long lastDropNs = 0L;
    //import Board class to user its methods
    private final Board board  = new Board(10,22);

    private Tetromino current;
    private Tetromino next;

    /**
     * This is the main loop and this is called every frame. Calling the stepGravity
     * method by current drop
     */
    private final AnimationTimer loop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            // If the block is the first since game starts,
            //Set last drop to now (initialise last drop)
            if(lastDropNs == 0)
            {
                lastDropNs = now;
            }

            // Set interval time
            // set if down key pressed, set interval to half (speed up)
            // if not pressed, set default speed.
            long interval = downPressed ? baseGravMs / 2 : baseGravMs;
            // Calculate passed time (ms) from last drop
            //this is nano sec so that divide by 1000000 to convert to milli sec
            long elapseMs = (now - lastDropNs) / 1_000_000L;

            //Once we are done the settings of dropping, execute dropping method
            // that we created (step Gravity)
            //Drop block 1 row if slapse Ms (passed time) was over than interval
            if(elapseMs >= interval)
            {
                // Drop the block and update drop time to now
                //Then reset count of dropping
                stepGravity();
                lastDropNs = now;
            }
            //Update frame to reflect changes
            render();
        }


    };

    private void fastDrop()
    {
        while(tryMove(1,0))
        {
            //Drop 1 row till block dropped
            //completely
        }
        //When dropped, set block, delete row if its filled
        //and show next mino
        lockAndNext();

    }

    /**
     * Dropping blocks by 1 step.If block cannot be set at 1 row below,
     * set block, and if row is filled, delete line and show next mino
     */
    private void stepGravity()
    {
        if(!tryMove(1, 0))
        {
            lockAndNext();
        }
    }

    /**
     * This method is setting current mino at game board and showing next
     * block
     */
    private void lockAndNext()
    {
        board.lock(current);
        board.clearFullLines();
        downPressed = false;
        if(!spawnNext())
        {
            loop.stop();
        }
    }

    /**
     *
     * @param dr --> row difference (down + 1)
     * @param dc --> col difference (right = +1, left = -1)
     * @return if block could be set, return to true and update current
     *         if it is out of range, return false and it is not updated / any changes
     */
    private boolean tryMove( int dr, int dc)
    {
        Tetromino t = current.moved(dr, dc);
        if (board.canPlace(t))
        {
            current = t;
            return true;
        }
        return false;
    }

    /**
     * This method is setting rotation of blocks
     * @param dir --> rotate block to right side = + 1, left side = -1
     * @return When success the rotation, return true.
     */
    private boolean tryRotate(int dir)
    {
        Tetromino rot = current.rotated(dir);
        for(int kick : new int []{0, -1, 1})
        {
            Tetromino t = new Tetromino(rot.type, rot.rotation, rot.row, rot.col + kick);
            if(board.canPlace(t))
            {
                current = t;
                return true;
            }

        }
        return false;
    }

    /**
     * This is the method of designing the canvas. Set background color,
     * draw the block setting at game board, current block, and grid lines.
     */
    private void render()
    {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        double cell = Math.floor(gameCanvas.getWidth() / board.w);
        double visibleHeight = cell * visibleRows;

        //Create Background colour of game screen'
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,gameCanvas.getWidth(), gameCanvas.getHeight());

        //Set Block if block hit bottom
        int[][] snap = board.snapshot();
        for( int row = hiddenRows; row < board.h; row++)
        {
            for (int col = 0; col < board.w; col++)
            {
                int id = snap[row][col];
                if(id != 0)
                {
                    double y = (row - hiddenRows) * cell;
                    gc.setFill(PALETTE[id]);
                    gc.fillRect(col * cell, y, cell -1, cell - 1);

                }
            }
        }

        //Current mino
        if(current != null)
        {
            gc.setFill(PALETTE[current.type.colorId]);
            for (int[] cols : current.cells())
            {
                int row = current.row + cols[1], col = current.col + cols[0];
                if(row >= hiddenRows)
                {
                    double y = (row - hiddenRows) * cell;
                    gc.fillRect(col * cell, y, cell -1, cell -1);
                }


            }
        }

        gc.setStroke(Color.web("#222"));
        for(int x = 0; x <= board.w; x++)
        {
            gc.strokeLine(x * cell, 0, x * cell, visibleHeight);
        }
        for(int y = 0; y<= visibleRows; y++)
        {
            gc.strokeLine(0, y * cell, board.w * cell, y * cell);
        }

    }

    /**
     * This method is setting the initial block and showing into UI.
     * generate next block by random and switch to current by next spawnNect method
     */
    private void spawnFirst()
    {
        next = randomTetromino();
        spawnNext();
    }

    /**
     * This method is showing next block when current block was set.
     * next is generating next block by random and set new current at top center
     * @return (if the block can be set at position, return true. if it was
     *          out from game frame or could not be set, judge to game over)
     */
    private boolean spawnNext()
    {
        current = next;
        next = randomTetromino();
        current.row = 0;
        current.col = Math.max(0, (board.w - current.spawnWidth()) / 2);
        return board.canPlace(current);
    }

    /**
     * This method is generating random blocks from Tetromino determination.
     * @return
     */
    private Tetromino randomTetromino()
    {
        TetrominoType[] a = TetrominoType.values();
        TetrominoType t = a[(int)(Math.random() * a.length)];

        return new Tetromino(t, 0, 0, 0);
    }


    /**
     * This is the initialization method when game fx screen is called.
     * Set initialized and start playing game.
     * Showing the first block and loop will be started.
     */
    @FXML
    public void initialize()
    {

        //Collect Graphics Context
        gc = gameCanvas.getGraphicsContext2D();
        drawInitialScreen();

        //Set canvas to available focus
        gameCanvas.setFocusTraversable(true);
        //Then when UI showed, take focus
        Platform.runLater(() -> gameCanvas.requestFocus());

        gameCanvas.sceneProperty().addListener((obs, oldSc, sc) -> {
            if(sc == null)
            {
                return;
            }
            //Set key and key action
            sc.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                switch (e.getCode())
                {
                    case LEFT -> {if (tryMove(0, -1)) render();}
                    case RIGHT -> {if (tryMove(0, 1)) render();}
                    case UP -> {if (tryRotate(1)) render();}
                    case DOWN -> downPressed = true;

                }
            });
            //Set when user left action key (Drop fast),
            //Back drop speed to default speed (Return false)

            sc.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
                //When user released down key,
                //Back dropping speed to default speed
                if(e.getCode() == KeyCode.DOWN)
                {
                    downPressed = false;
                }
            });
        });







        spawnFirst();
        loop.start();


    }

    /**
     * This is the basic initial design of game screen. (set background, title)
     * while user playing the game, render will update UI
     */
    private void drawInitialScreen()
    {
       gc.setFill(Color.BLACK);
       gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

       //put skyblue colour into outside frame
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0,0,frameCanvas.getWidth(), frameCanvas.getHeight());



        gc.setFill(Color.WHITE);
        gc.fillText("Game Start!", 120, 100);
    }


    private static final Color[] PALETTE = {
            null, Color.BLUE, Color.RED, Color.GREEN,
            Color.PURPLE, Color.GRAY, Color.YELLOW, Color.SKYBLUE
    };





}
