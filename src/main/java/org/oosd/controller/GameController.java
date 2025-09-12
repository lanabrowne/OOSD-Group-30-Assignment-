/****************************************************************
 PROGRAM:   Game Controller for Tetris
 AUTHORS:   Ikkei Fukuta, Ria Rajesh, Taylor Brown, 
            Lana Browne, Kosuke Suto

 STUDENT ID: s5339308, s5404819, s350825, s5340293, s5373939
 DUE DATE:   27th Aug 2025

 FUNCTION: 
    This class controls the Tetris game. It manages the game 
    board, the current and next tetrominoes, and user inputs. 
    It also handles gravity, movement, rotation, soft drop, 
    fast drop, pause/resume, and game over detection. 
    Additionally, it renders the game state onto the canvas.

 INPUT:  
    User keyboard input (arrow keys, P key).
    Loaded from: GitHub\OOSD-Group-30-Assignment-

 OUTPUT: 
    Visual game rendering on the JavaFX Canvas.
    Game over screen and pause overlay.

 NOTES:  
    - The board has 22 rows, with the top 2 hidden for spawning. 
    - Game loop is handled via JavaFX AnimationTimer.
    - Designed for integration with the Main application class.
 ****************************************************************/


package org.oosd.controller;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import org.oosd.Main;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfig;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoType;
import javafx.scene.text.Font;
import org.oosd.sound.music;
import org.oosd.sound.soundEffects;
import org.oosd.ui.Frame;
import org.oosd.ui.HighScoreScreen;


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
    public Button getBtnBack(){
        return btnBack;
    }


     @FXML
    private Label lblGameOver;

    @FXML
    private Button end;  // button after game ends that goes to the HS screen

  
    private static final int visibleRows = 30;
    
    private static final int hiddenRows = 4;

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
   

    /**
     * The time of last drop execution.Measure the interval by comparing
     * with now in the Animation Timer.
     */
    private long lastDropNs = 0L;
    //import Board class to user its methods
    //public final Board board  = new Board(10,22);

    /**
     * all below command is reflecting user input value into game controller
     * So now you guys can start working on reflecting user input now
     * use
     */
    // Here is the collecting user input from Config class
    //Then reflecting all information to game board by put value
    //into board class
    TetrisConfig config = ConfigService.get();
    Board board = new Board(config.fieldWidth(), config.fieldHeight());
    int gameLevel = config.gameLevel();
    boolean musicON = config.music();
    boolean sfxON = config.sfx();
    boolean aiPlay = config.aiPlay();
    boolean extendMode = config.extendMode();



    private Tetromino current;
    private Tetromino next;

    /**
     * This is the main loop and this is called every frame. Calling the stepGravity
     * method by current drop
     */
private GraphicsContext gc;
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
        showGameOver();
        }
     
    }
private void showGameOver() {
    // Stop game loop immediately
    loop.stop();
    soundEffects.play("gameover");

    // Ensure gc is initialized
    if (gc == null) gc = gameCanvas.getGraphicsContext2D();

    // Use Platform.runLater to make sure drawing happens on the FX Application Thread
    Platform.runLater(() -> {
        double canvasWidth = gameCanvas.getWidth();
        double canvasHeight = gameCanvas.getHeight();

         Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(0, 0, 0, 0.8)),    // Top: dark, almost black
            new Stop(1, Color.rgb(255, 0, 0, 0.8))   // Bottom: dark red
        };
        LinearGradient lg = new LinearGradient(
            0, 0, 0, 1, // startX, startY, endX, endY (0-1 normalized)
            true,        // proportional
            CycleMethod.NO_CYCLE,
            stops
        );
          gc.setFill(lg);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Centered "GAME OVER" text
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 36));

        String message = "GAME OVER";
        Text tempText = new Text(message);
        tempText.setFont(Font.font("Arial", 36));
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();

        gc.fillText(message, (canvasWidth - textWidth) / 2, (canvasHeight + textHeight) / 2);
        Button end;
    });
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
// Palette of colors for tetromino IDs
// Index 0 is empty (no block)
private static final Color[] PALETTE = {
    Color.TRANSPARENT, // 0 - empty cell
    Color.CYAN,        // 1 - I
    Color.BLUE,        // 2 - J
    Color.ORANGE,      // 3 - L
    Color.YELLOW,      // 4 - O
    Color.GREEN,       // 5 - S
    Color.PURPLE,      // 6 - T
    Color.RED          // 7 - Z
};


    private void render()
    {

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        double cell = Math.floor(gameCanvas.getWidth() / board.w);

        int visibleRows = board.h;
        double visibleHeight = cell * visibleRows;

        //Create Background colour of game screen
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,gameCanvas.getWidth(), gameCanvas.getHeight());

        //Set Block if block hit bottom
        int[][] snap = board.snapshot();
        for(int row = hiddenRows; row < board.h; row++)
        {
            for (int col = 0; col < board.w; col++)
            {
                int id = snap[row][col];
                if(id != 0)
                {
                    double y = row * cell;
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
        for(int y = 0; y <= visibleRows; y++)
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
private void drawInitialScreen() {
    if (gc == null) gc = gameCanvas.getGraphicsContext2D();

    double w = gameCanvas.getWidth();
    double h = gameCanvas.getHeight();

    // Background
    gc.setFill(Color.BLACK);
    gc.fillRect(0, 0, w, h);

    // Title
    gc.setFill(Color.WHITE);
    gc.setFont(Font.font(28));
    String title = "TETRIS";
    gc.fillText(title, (w - title.length() * 14) / 2, h * 0.35);

    // Hint text
    gc.setFont(Font.font(16));
    gc.fillText("← → move   ↑ rotate   ↓ soft drop   P pause",
            (w * 0.5) - 170, h * 0.50);
    gc.fillText("Press any arrow key to start", (w * 0.5) - 120, h * 0.60);
}

   @FXML
public void initialize() {
    gc = gameCanvas.getGraphicsContext2D();
    // Added cell size var to be easily accessed
    int cellSize = 30;
    // change the gamecanvas based on config screen settings
    gameCanvas.setWidth(config.fieldWidth()*cellSize);
    gameCanvas.setHeight(config.fieldHeight()*cellSize);

    drawInitialScreen();

    soundEffects.init(sfxON);

       if (musicON) {
        music.play("/background.mp3"); 
    }



    // Set canvas to focusable and request focus
    gameCanvas.setFocusTraversable(true);
    Platform.runLater(() -> gameCanvas.requestFocus());

    // Key event handling
    gameCanvas.sceneProperty().addListener((obs, oldSc, sc) -> {
        if (sc == null) return;

        // Key pressed
        sc.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if(current == null){
                startGame();
                return;
            }
            switch (e.getCode()) {
                case LEFT -> {
                    if (tryMove(0, -1)) render();
                }
                case RIGHT -> {
                    if (tryMove(0, 1)) render();
                }
                case UP -> {
                    if (tryRotate(1)) render();
                }
                case DOWN -> downPressed = true;
                case P -> togglePause();
            }
        });

        // Key released
        sc.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.DOWN) {
                downPressed = false;
            }
        });
    });

}

// Class-level paused flag
// Class-level paused flag
private boolean paused = false;



// Display pause overlay
private void togglePause() {
    if (paused) {
        resumeGame();
    } else {
        pauseGame();
    }
}
public void startGame(){
    // Spawn first Tetromino and start the game loop
    resetGame();
    spawnFirst();
    loop.start();
}

    public void resetGame() {
        // Stop current game loop
        loop.stop();

        // Reset board
        //board.clear();   // assuming your Board class has a clear() method
        current = null;
        next = null;
        downPressed = false;
        lastDropNs = 0L;

        // Clear canvas
        if (gc != null) {
            gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
            drawInitialScreen();
        }
    }

private void pauseGame() {
    loop.stop();
    paused = true;

    double canvasWidth = gameCanvas.getWidth();
    double canvasHeight = gameCanvas.getHeight();

    // Semi-transparent dark overlay
    gc.setFill(Color.rgb(0, 0, 0, 0.7));
    gc.fillRect(0, 0, canvasWidth, canvasHeight);

    // Centered PAUSED text
    gc.setFill(Color.WHITE);
    gc.setFont(Font.font(36));
    gc.fillText("PAUSED", canvasWidth / 2 - 60, canvasHeight / 2);
}

private void resumeGame() {
    paused = false;
    loop.start();
    render(); // clears the overlay by redrawing board
}

private Frame parent;
public void setParent(Frame parent) {
    this.parent = parent;
}
@FXML
public void backClicked(ActionEvent e)
{
  loop.stop();
  parent.showExitConfirmation();
}
    public void endClicked(ActionEvent e)
    {
        loop.stop();
        // Show highscore screen via frame
        HighScoreScreen highScoreScreen = new HighScoreScreen((Main) parent);
        parent.showScreen(highScoreScreen);

    }
}
