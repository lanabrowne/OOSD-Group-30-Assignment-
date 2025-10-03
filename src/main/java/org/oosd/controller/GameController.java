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
import javafx.scene.text.Font;
import org.oosd.Main;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfig;
import org.oosd.external.ExternalPlayer;
import org.oosd.model.*;
import org.oosd.sound.SoundEffects;
import org.oosd.ui.Frame;
import org.oosd.ui.HighScoreScreen;
import javafx.scene.control.Label;

import java.awt.*;

public class GameController {

    @FXML private Canvas gameCanvas;
    @FXML private VBox frameCanvas;
    @FXML private javafx.scene.control.Button end;  // Button after game ends that goes to the highscore screen

    private Board board;
    private double cellSize;
    private Tetromino current;
    private Tetromino next;
    private Frame parent;

    private final TetrisAI ai = new TetrisAI();
    private Move currentAiMove = null;
    private boolean aiMoveExecuted = false;

    private long lastDropNs = 0;
    private int score = 0;

    private ExternalPlayer externalClient;

    @FXML
    private Label lblScore;

    /**
     * This is the number of hiding lines for the spawn. So that
     * when drawing the UI, make up 4 lines.
     */
    private static final int HIDDEN_ROWS = 4;

    /**
     * This is definition of free fall speed standard interval.
     * When user presses down key, make this interval 1/2
     */
    private long baseGravMs = 500;

    /**
     * This is the action of user input of down key.
     * To switch the interval, set false as default, and when user pressed down key, it will be true.
     */
    private boolean downPressed = false;
    private boolean paused = false;
    private boolean aiPlay = false;

    private GraphicsContext gc;

    /**
     * Palette of colors for tetromino IDs.
     * Index 0 is empty (no block)
     */
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

    /**
     * Main game loop
     */
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
        int blockSize = 30; // Pixels per block
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

    /**
     * Shows game over overlay
     */
    private void showGameOver() {
        loop.stop();
        SoundEffects.play("gameover");
    }

    /**
     * Setup key handlers for user input
     */
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

    /**
     * Start a new game
     */
    public void startGame() {
        current = null;
        next = null;
        spawnFirst();
        loop.start();
    }

    /**
     * Moves current piece down by one. Locks if cannot move further
     */
    private void stepGravity() {
        if (!tryMove(1, 0)) lockAndNext();
    }

    /**
     * Locks current piece and spawns next
     */
    private void lockAndNext() {
        board.lock(current);

        //capture how many lines were cleared
        int lines_cleared = board.clearFullLines();
        if(lines_cleared > 0) {
            //Update scoring
            addScore(lines_cleared);
        }
        downPressed = false;

        if (!spawnNext()) {
            loop.stop();
            drawGameOver();
        }
    }

    /**
     * Attempts to move current piece
     */
    private boolean tryMove(int dr, int dc) {
        Tetromino moved = current.moved(dr, dc);
        if (board.canPlace(moved)) {
            current = moved;
            return true;
        }
        return false;
    }

    /**
     * Attempts to rotate current piece
     */
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

    /**
     * Spawns first piece
     */
    private void spawnFirst() {
        next = randomTetromino();
        spawnNext();
    }

    /**
     * Spawns the next piece and optionally chooses AI move
     */
    private boolean spawnNext() {
        current = next;
        next = randomTetromino();
        current.row = 0;
        current.col = (board.w - current.spawnWidth()) / 2; // Center spawn

        if (!board.canPlace(current)) return false;

        if (aiPlay) {
            int[][] snap = board.snapshot();
            currentAiMove = ai.findBestMove(snap, board.h, board.w, current, next);
            aiMoveExecuted = false;

            if (currentAiMove != null) {
                System.out.println("AI move: col = " + currentAiMove.col +
                        ", rotation = " + currentAiMove.rotation);
            }
        }
        return true;
    }

    /**
     * Returns a random tetromino
     */
    private Tetromino randomTetromino() {
        TetrominoType[] types = TetrominoType.values();
        TetrominoType randomType = types[(int) (Math.random() * types.length)];
        return new Tetromino(randomType, 0, 0, 0);
    }

    /**
     * Executes AI move for current piece
     */
    private void executeAIMove(Move move) {
        if (move == null) return;

        // Rotate piece
        int rotations = (move.rotation - current.rotation + 4) % 4;
        for (int i = 0; i < rotations; i++) {
            tryRotate(1);
            System.out.println("Rotated: current.rotation = " + current.rotation + ", col = " + current.col);
        }

        // Move piece horizontally
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

    /**
     * Renders board and current piece
     */
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
                    gc.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
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

        // Draw grid
        gc.setStroke(Color.web("#222"));
        for (int x = 0; x <= board.w; x++) gc.strokeLine(x * cellSize, 0, x * cellSize, gameCanvas.getHeight());
        for (int y = 0; y <= board.h; y++) gc.strokeLine(0, y * cellSize, board.w * cellSize, y * cellSize);
    }

    /**
     * Draws initial start screen
     */
    private void drawInitialScreen() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(28));
        gc.fillText("TETRIS", gameCanvas.getWidth() / 2 - 60, gameCanvas.getHeight() / 2 - 20);

        // Reset board state
        current = null;
        next = null;
        downPressed = false;
        lastDropNs = 0L;

        gc.setFont(Font.font(16));
        gc.fillText("Press any arrow key to start", gameCanvas.getWidth() / 2 - 100,
                gameCanvas.getHeight() / 2 + 20);
    }

    /**
     * @FXML
     *     public void initialize() {
     *         gc = gameCanvas.getGraphicsContext2D();
     *         // Added cell size var to be easily accessed
     *         int cellSize = 30;
     *         // change the gamecanvas based on config screen settings
     *         gameCanvas.setWidth(config.fieldWidth()*cellSize);
     *         gameCanvas.setHeight((config.fieldHeight()- hiddenRows)*cellSize);
     *
     *         drawInitialScreen();
     *
     *         soundEffects.init(sfxON);
     *
     *         if (musicON) {
     *             music.play("/background.mp3");
     *         }
     *
     *
     *
     *         // Set canvas to focusable and request focus
     *         gameCanvas.setFocusTraversable(true);
     *         Platform.runLater(() -> gameCanvas.requestFocus());
     *
     *         // Key event handling
     *         gameCanvas.sceneProperty().addListener((obs, oldSc, sc) -> {
     *             if (sc == null) return;
     *
     *             // Key pressed
     *             sc.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
     *                 if(current == null){
     *                     startGame();
     *                     return;
     *                 }
     *                 switch (e.getCode()) {
     *                     case LEFT   -> processCommand("LEFT");
     *                     case RIGHT  -> processCommand("RIGHT");
     *                     case UP     -> processCommand("ROTATE");
     *                     case DOWN   -> processCommand("DOWN");
     *                     case P      -> processCommand("PAUSE");
     *                 }
     *             });
     *
     *             // Key released
     *             sc.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
     *                 if (e.getCode() == KeyCode.DOWN) {
     *                     downPressed = false;
     *                 }
     *             });
     *         });
     *
     *     }
     */


    //Create command action method to pass the command to external
    public void processCommand(String command)
    {
        switch (command) {
            case "LEFT" -> {
                if (tryMove(0, -1)) render();
            }
            case "RIGHT" -> {
                if (tryMove(0, 1)) render();
            }
            case "ROTATE" -> {
                if (tryRotate(1)) render();
            }
            case "DOWN" -> downPressed = true;
            case "PAUSE" -> togglePause();
        }
    }


    // Class-level paused flag
// Class-level paused flag







    public void resetGame() {
        // Stop current game loop
        loop.stop();

        // Reset board
        current = null;
        next = null;
        downPressed = false;
        lastDropNs = 0L;

        gc.setFont(Font.font(16));
        gc.fillText("Press any arrow key to start", gameCanvas.getWidth() / 2 - 100,
                gameCanvas.getHeight() / 2 + 20);
    }

    /**
     * Draws game over overlay
     */
    private void drawGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(36));
        gc.fillText("GAME OVER", gameCanvas.getWidth() / 2 - 100, gameCanvas.getHeight() / 2);
    }

    /**
     * Toggle pause state
     */
    private void togglePause() {
        paused = !paused;
    }

    /**
     * Handle end button click
     */
    public void endClicked(ActionEvent e) {
        loop.stop();
        HighScoreScreen highScoreScreen = new HighScoreScreen((Main) parent);
        parent.showScreen(highScoreScreen);
    }

    //functions for score
    public int getScore() {
        return score;
    }
    private void addScore(int linesCleared) {
        switch (linesCleared) {
            case 1 -> score += 100;
            case 2 -> score += 300;
            case 3 -> score += 500;
            case 4 -> score += 800; // Tetris
        }
        updateScoreLabel();
    }

    private void updateScoreLabel() {
        lblScore.setText("Score: " + score);
    }

    // Getters for AI
    public int getBoardWidth() { return board.w; }
    public int getBoardHeight() { return board.h; }
    public Board getBoard() { return board; }

    public void setParent(Frame parent) {
        this.parent = parent;
    }
}
