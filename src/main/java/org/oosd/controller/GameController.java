package org.oosd.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import org.oosd.Main;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfig;
import org.oosd.model.*;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoType;
import org.oosd.sound.Music;
import org.oosd.sound.SoundEffects;
import org.oosd.ui.Frame;
import org.oosd.ui.HighScoreScreen;

public class GameController {

    @FXML private Canvas gameCanvas;
    @FXML private VBox frameCanvas;

    private Board board;
    private double cellSize;
    private Tetromino current;
    private Tetromino next;
    private Frame parent;

    private final TetrisAI ai = new TetrisAI();
    private Move currentAiMove = null;
    private boolean aiMoveExecuted = false;

    private long lastDropNs = 0;
    @FXML
    private Button end;  // button after game ends that goes to the HS screen

    /**
     * This is the number of rows that will be shown in the UI.Actual Board
     * is 22 lines but hide 2 lines to judge the game over
     */
    //private static final int visibleRows = 30;
    /**
     * This is the number of hiding lines for the spawn. SO that
     * when i draw the UI, make up 4 lines.
     */
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
    private boolean paused = false;
    private boolean aiPlay = false;




    private GraphicsContext gc;

    private static final Color[] PALETTE = {
            Color.TRANSPARENT, // 0
            Color.CYAN,        // 1 - I
            Color.BLUE,        // 2 - J
            Color.ORANGE,      // 3 - L
            Color.YELLOW,      // 4 - O
            Color.GREEN,       // 5 - S
            Color.PURPLE,      // 6 - T
            Color.RED          // 7 - Z
    };
    private Tetromino current;
    private Tetromino next;

    private final AnimationTimer loop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (paused) return;

            if (lastDropNs == 0) lastDropNs = now;

            if (aiPlay && currentAiMove != null && !aiMoveExecuted) {
                executeAIMove(currentAiMove);
                System.out.println("AI move: col = " + currentAiMove.col +
                        ", rotation = " + currentAiMove.rotation);
                aiMoveExecuted = true;
            }

            long interval = downPressed ? 250 : 500;
            long elapsedMs = (now - lastDropNs) / 1_000_000L;

            if (elapsedMs >= interval) {
                stepGravity();
                lastDropNs = now;
            }

            render();
        }
    };

    @FXML
    public void initialize() {
        TetrisConfig config = ConfigService.get();
        int blockSize = 30; // pixels pre block
        this.board = new Board(config.fieldWidth(), config.fieldHeight());
        System.out.println("Board Width = "+ board.w);
        this.aiPlay = config.aiPlay();

        // Resize canvas to match board
        gameCanvas.setWidth(config.fieldWidth() * blockSize);
        gameCanvas.setHeight(config.fieldHeight() * blockSize);
        gc = gameCanvas.getGraphicsContext2D();

        Platform.runLater(() -> {
            calculateCellSize();
            drawInitialScreen();
            setupKeyHandlers();
        });
    }

    private void calculateCellSize() {
        cellSize = Math.min(
                gameCanvas.getWidth() / board.w,
                gameCanvas.getHeight() / board.h
        );
    }

    private void showGameOver() {
        // Stop game loop immediately
        loop.stop();
        SoundEffects.play("gameover");
  }

    private void setupKeyHandlers() {
        gameCanvas.setFocusTraversable(true);
        gameCanvas.requestFocus();

        gameCanvas.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (current == null) {
                startGame();
                e.consume();
                return;
            }

            if (aiPlay) return;

            switch (e.getCode()) {
                case LEFT -> tryMove(0, -1);
                case RIGHT -> tryMove(0, 1);
                case UP -> tryRotate(1);
                case DOWN -> downPressed = true;
                case P -> togglePause();
            }
        });

        gameCanvas.getScene().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (!aiPlay && e.getCode() == KeyCode.DOWN) {
                downPressed = false;
            }
        });
    }

    public void startGame() {
        current = null;
        next = null;
        spawnFirst();
        loop.start();
    }

    private void stepGravity() {
        if (!tryMove(1, 0)) lockAndNext();
    }

    private void lockAndNext() {
        board.lock(current);
        board.clearFullLines();
        downPressed = false;

        if (!spawnNext()) {
            loop.stop();
            drawGameOver();
        }
    }

    private boolean tryMove(int dr, int dc) {
        Tetromino moved = current.moved(dr, dc);
        if (board.canPlace(moved)) {
            current = moved;
            return true;
        }
        return false;
    }

    private boolean tryRotate(int dir) {
        Tetromino rotated = current.rotated(dir);
        for (int kick : new int[] {0, -1, 1}) {
            Tetromino t = new Tetromino(
                    rotated.type, rotated.rotation,
                    rotated.row, rotated.col + kick
            );
            if (board.canPlace(t)) {
                current = t;
                return true;
            }
        }
        return false;
    }

    private void spawnFirst() {
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

        int visibleRows = board.h - hiddenRows;
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

    private boolean spawnNext() {
        current = next;
        next = randomTetromino();
        current.row = 0;
        current.col = (board.w - current.spawnWidth()) / 2; // Center spawn

        // Check if current piece can be placed
        if (!board.canPlace(current)) return false;

        if (aiPlay) {
            // Take a snapshot of the current board
            int[][] snap = board.snapshot();

            // Use 2-ply AI to pick the best move
            currentAiMove = ai.findBestMove(snap, board.h, board.w, current, next);

            aiMoveExecuted = false; // Reset for new piece
            if (currentAiMove != null) {
                System.out.println("AI move: col = " + currentAiMove.col +
                        ", rotation = " + currentAiMove.rotation);
            }
        }

        return true;
    }


    private Tetromino randomTetromino() {
        TetrominoType[] types = TetrominoType.values();
        TetrominoType randomType = types[(int) (Math.random() * types.length)];
        return new Tetromino(randomType, 0, 0, 0);
    }

    private void executeAIMove(Move move) {
        if (move == null) return;

        int rotations = (move.rotation - current.rotation + 4) % 4;
        for (int i = 0; i < rotations; i++) {
            tryRotate(1);
            System.out.println("Rotated: current.rotation = " + current.rotation + ", col = " + current.col);
        //SoundEffects.init(sfxON);

        if (musicON) {
            //Music.play("/background.mp3");
        }

        // Clamp column after rotation so piece fits in board
        int targetCol = Math.max(0, Math.min(move.col, board.w - current.spawnWidth()));
        while (current.col < targetCol) {
            if (!tryMove(0, 1)) break;
            System.out.println("Moved right: current.col = " + current.col);
        }
        while (current.col > targetCol) {
            if (!tryMove(0, -1)) break;
            System.out.println("Moved left: current.col = " + current.col);
        }

        System.out.println("Final AI position: col = " + current.col + ", rotation = " + current.rotation);
    }


    private void render() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Draw board
        int[][] snap = board.snapshot();
        for (int r = 0; r < board.h; r++) {
            for (int c = 0; c < board.w; c++) {
                if (snap[r][c] != 0) {
                    gc.setFill(PALETTE[snap[r][c]]);
                    gc.fillRect(
                            c * cellSize, r * cellSize,
                            cellSize, cellSize
                    );
                }
            }
        }

        // Draw current piece
        gc.setFill(PALETTE[current.type.colorId]);
        for (int[] cell : current.cells()) {
            int r = current.row + cell[1];
            int c = current.col + cell[0];
            gc.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
        }
        // Draws grid lines
        gc.setStroke(Color.web("#222"));
        for(int x = 0; x <= board.w; x++)
        {
            gc.strokeLine(x * cellSize, 0, x * cellSize, gameCanvas.getHeight());
        }
        for(int y = 0; y<= board.h; y++)
        {
            gc.strokeLine(0, y * cellSize, board.w * cellSize, y * cellSize);
        }

    }

    private void drawInitialScreen() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(28));
        gc.fillText(
                "TETRIS",
                gameCanvas.getWidth() / 2 - 60,
                gameCanvas.getHeight() / 2 - 20
        );
        // Reset board
        current = null;
        next = null;
        downPressed = false;
        lastDropNs = 0L;
        gc.setFont(Font.font(16));
        gc.fillText(
                "Press any arrow key to start",
                gameCanvas.getWidth() / 2 - 100,
                gameCanvas.getHeight() / 2 + 20
        );
    }

    private void drawGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(36));
        gc.fillText(
                "GAME OVER",
                gameCanvas.getWidth() / 2 - 100,
                gameCanvas.getHeight() / 2
        );
    }

    private void togglePause() {
        paused = !paused;
    }

    public void endClicked(ActionEvent e) {
        loop.stop();
        // Show highscore screen via frame
        HighScoreScreen highScoreScreen = new HighScoreScreen((Main) parent);
        parent.showScreen(highScoreScreen);
    }

    // Provide getters for AI
    public int getBoardWidth() { return board.w; }
    public int getBoardHeight() { return board.h; }
    public Board getBoard() { return board; }

    public void setParent(Frame parent) {
        this.parent = parent;
    }
}
