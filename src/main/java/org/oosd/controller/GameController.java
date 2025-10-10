package org.oosd.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.oosd.HighScore.ConfigTagUtil;
import org.oosd.HighScore.HighScoreWriter;
import org.oosd.Main;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfig;
import org.oosd.external.ExternalPlayer;
import org.oosd.model.*;
import org.oosd.sound.SoundEffects;
import org.oosd.ui.Frame;
import org.oosd.ui.HighScoreScreen;
import org.oosd.patterns.ConfigManager;
import org.oosd.patterns.PieceFactory;
import org.oosd.patterns.RandomPieceFactory;

import java.util.Optional;

public class GameController {

    @FXML private Canvas gameCanvas;
    @FXML private VBox frameCanvas; // VBox that also contains score label / buttons
    @FXML private javafx.scene.control.Button end;
    @FXML private Label lblScore;

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

    private boolean scoreSaved = false;
    private boolean gameOver = false;
    private boolean nameDialogShowing = false;

    private static final int HIDDEN_ROWS = 4;
    private long baseGravMs = 500;

    private boolean downPressed = false;
    private boolean paused = false;
    private boolean aiPlay = false;

    private GraphicsContext gc;

    // Piece factory (Strategy/Factory) to decouple random piece creation
    private final PieceFactory pieceFactory = new RandomPieceFactory();

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

    private final AnimationTimer loop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (paused || gameOver) return;
            if (lastDropNs == 0) lastDropNs = now;

            if (aiPlay && currentAiMove != null && !aiMoveExecuted) {
                executeAIMove(currentAiMove);
                aiMoveExecuted = true;
            }

            long interval = downPressed ? 250 : 500;
            long elapsedMs = (now - lastDropNs) / 1_000_000L;

            if (elapsedMs >= interval) {
                stepGravity();
                lastDropNs = now;
                if (gameOver) return;
            }
            render();
        }
    };

    @FXML
    public void initialize() {
        ConfigService.load();
        TetrisConfig config = ConfigManager.getInstance().get();
        this.board = new Board(config.fieldWidth(), config.fieldHeight() + HIDDEN_ROWS);
        this.aiPlay = config.aiPlay();
        this.gc = gameCanvas.getGraphicsContext2D();

        // Prevent VBox from force-growing canvas vertically
        VBox.setVgrow(gameCanvas, Priority.NEVER);

        // Defer until Scene/layout are alive
        Platform.runLater(() -> {
            fitCanvasToContainer();               // initial fit
            attachResizeListeners();              // keep fitting on resize
            drawInitialScreen();
            setupKeyHandlers();                   // safe: Scene exists now
            gameCanvas.setFocusTraversable(true);
            gameCanvas.requestFocus();
        });
    }

    // -------------------- auto-fit sizing --------------------

    /** Listen to parent size changes and refit canvas. */
    private void attachResizeListeners() {
        if (frameCanvas != null) {
            frameCanvas.widthProperty().addListener((o, ov, nv) -> fitCanvasToContainer());
            frameCanvas.heightProperty().addListener((o, ov, nv) -> fitCanvasToContainer());
        }
    }

    /**
     * Fit canvas into the remaining space of the VBox, **excluding** the height of
     * other children (score label, buttons) and VBox spacing/padding.
     */
    private void fitCanvasToContainer() {
        int visibleH = board.h - HIDDEN_ROWS;

        double availW = 0, availH = 0;

        if (frameCanvas != null && frameCanvas.getWidth() > 0 && frameCanvas.getHeight() > 0) {
            Insets in = frameCanvas.getInsets() == null ? Insets.EMPTY : frameCanvas.getInsets();
            double spacing = frameCanvas.getSpacing();
            int childCount = frameCanvas.getChildren().size();

            // Sum heights of non-canvas children currently laid out
            double reservedH = in.getTop() + in.getBottom();
            int gaps = Math.max(0, childCount - 1);
            reservedH += spacing * gaps;

            for (Node n : frameCanvas.getChildren()) {
                if (n == gameCanvas) continue;
                double h = n.getBoundsInParent().getHeight();
                if (h <= 0) {
                    // fallback to preferred height if bounds not realized yet
                    h = n.prefHeight(-1);
                }
                if (h > 0) reservedH += h;
            }

            availW = Math.max(1, frameCanvas.getWidth() - in.getLeft() - in.getRight());
            availH = Math.max(1, frameCanvas.getHeight() - reservedH);
        } else {
            // First pulse (before layout). Provide a sane fallback.
            availW = Math.max(1, gameCanvas.getWidth());
            availH = Math.max(1, gameCanvas.getHeight());
        }

        // Choose integer cell size to avoid blurry lines
        double s = Math.min(Math.floor(availW / board.w), Math.floor(availH / visibleH));
        if (s < 1) s = 1;
        cellSize = s;

        // Apply exact pixel size so grid lines are crisp
        gameCanvas.setWidth(board.w * cellSize);
        gameCanvas.setHeight(visibleH * cellSize);
    }

    // -------------------- input --------------------

    private void setupKeyHandlers() {
        if (gameCanvas.getScene() == null) {
            Platform.runLater(this::setupKeyHandlers);
            return;
        }
        gameCanvas.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (current == null) {
                startGame();
                e.consume();
                return;
            }
            if (aiPlay) return;

            KeyCode code = e.getCode();
            if (code == KeyCode.LEFT)  tryMove(0, -1);
            if (code == KeyCode.RIGHT) tryMove(0,  1);
            if (code == KeyCode.UP)    tryRotate(1);
            if (code == KeyCode.DOWN)  downPressed = true;
            if (code == KeyCode.P)     togglePause();
        });

        gameCanvas.getScene().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (!aiPlay && e.getCode() == KeyCode.DOWN) downPressed = false;
        });
    }

    // -------------------- gameplay --------------------

    public void startGame() {
        gameOver = false;
        scoreSaved = false;
        nameDialogShowing = false;
        downPressed = false;
        score = 0;
        updateScoreLabel();

        board = new Board(board.w, board.h);
        current = null;
        next = null;

        // Re-fit once again in case layout changed
        fitCanvasToContainer();

        spawnFirst();
        loop.start();
    }

    private void stepGravity() {
        if (!tryMove(1, 0)) lockAndNext();
    }

    private void lockAndNext() {
        board.lock(current);
        int cleared = board.clearFullLines();
        if (cleared > 0) addScore(cleared);
        downPressed = false;
        if (!spawnNext()) endGame();
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
        for (int kick : new int[]{0, -1, 1}) {
            Tetromino t = new Tetromino(rotated.type, rotated.rotation, rotated.row, rotated.col + kick);
            if (board.canPlace(t)) {
                current = t;
                return true;
            }
        }
        return false;
    }

    private void spawnFirst() {
        next = randomTetromino();
        spawnNext();
    }

    private boolean spawnNext() {
        current = next;
        next = randomTetromino();
        current.row = 0;
        current.col = (board.w - current.spawnWidth()) / 2;
        if (!board.canPlace(current)) return false;

        if (aiPlay) {
            int[][] snap = board.snapshot();
            currentAiMove = ai.findBestMove(snap, board.h, board.w, current, next);
            aiMoveExecuted = false;
        }
        return true;
    }

    private Tetromino randomTetromino() {
        return pieceFactory.createRandom(board.w);
    }

    private void executeAIMove(Move move) {
        if (move == null) return;
        int rotations = (move.rotation - current.rotation + 4) % 4;
        for (int i = 0; i < rotations; i++) tryRotate(1);

        int targetCol = Math.max(0, Math.min(move.col, board.w - current.spawnWidth()));
        while (current.col < targetCol && tryMove(0, 1)) {}
        while (current.col > targetCol && tryMove(0, -1)) {}
    }

    // -------------------- rendering --------------------

    private void render() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        int[][] snap = board.snapshot();
        int visibleH = board.h - HIDDEN_ROWS;

        // locked cells
        for (int r = HIDDEN_ROWS; r < board.h; r++) {
            for (int c = 0; c < board.w; c++) {
                int id = snap[r][c];
                if (id != 0) {
                    gc.setFill(PALETTE[id]);
                    gc.fillRect(c * cellSize, (r - HIDDEN_ROWS) * cellSize, cellSize, cellSize);
                }
            }
        }

        // current piece
        if (current != null) {
            gc.setFill(PALETTE[current.type.colorId]);
            for (int[] cell : current.cells()) {
                int r = current.row + cell[1];
                int c = current.col + cell[0];
                if (r >= HIDDEN_ROWS) {
                    gc.fillRect(c * cellSize, (r - HIDDEN_ROWS) * cellSize, cellSize, cellSize);
                }
            }
        }

        // grid
        gc.setStroke(Color.web("#222"));
        for (int x = 0; x <= board.w; x++) gc.strokeLine(x * cellSize, 0, x * cellSize, visibleH * cellSize);
        for (int y = 0; y <= visibleH; y++) gc.strokeLine(0, y * cellSize, board.w * cellSize, y * cellSize);
    }

    private void drawInitialScreen() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(28));
        gc.fillText("TETRIS", gameCanvas.getWidth() / 2 - 60, gameCanvas.getHeight() / 2 - 20);

        current = null;
        next = null;
        downPressed = false;
        lastDropNs = 0L;

        gc.setFont(Font.font(16));
        gc.fillText(
                "Press any arrow key to start",
                gameCanvas.getWidth() / 2 - 120,
                gameCanvas.getHeight() / 2 + 20
        );
    }

    // -------------------- end / score --------------------

    private void endGame() {
        if (gameOver) return;
        gameOver = true;
        loop.stop();

        drawGameOver();

        if (scoreSaved || nameDialogShowing) return;
        nameDialogShowing = true;

        final int finalScore = score;
        final String defaultName = "Player";

        Platform.runLater(() -> {
            TextInputDialog d = new TextInputDialog(defaultName);
            d.setTitle("High Score");
            d.setHeaderText("Enter your name");
            d.setContentText("Name:");
            d.setOnHidden(e -> {
                String name = d.getResult();
                if (name == null || name.trim().isEmpty()) name = defaultName;
                saveHighScore(finalScore, name);
                scoreSaved = true;
                nameDialogShowing = false;
            });
            d.show();
        });
    }

    private void drawGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(36));
        gc.fillText("GAME OVER", gameCanvas.getWidth() / 2 - 110, gameCanvas.getHeight() / 2);
    }

    private void togglePause() { paused = !paused; }

    public void endClicked(ActionEvent e) {
        loop.stop();
        HighScoreScreen highScoreScreen = new HighScoreScreen((Main) parent);
        parent.showScreen(highScoreScreen);
    }

    // -------------------- score utils --------------------

    public int getScore() { return score; }

    private void addScore(int linesCleared) {
        switch (linesCleared) {
            case 1 -> score += 100;
            case 2 -> score += 300;
            case 3 -> score += 500;
            case 4 -> score += 800;
        }
        updateScoreLabel();
    }

    private void updateScoreLabel() {
        if (lblScore != null) lblScore.setText("Score: " + score);
    }

    // -------------------- misc --------------------

    public int getBoardWidth()  { return board.w; }
    public int getBoardHeight() { return board.h; }
    public Board getBoard()     { return board; }
    public void setParent(Frame parent) { this.parent = parent; }

    private void saveHighScore(int finalScore, String playerName) {
        ConfigService.load();
        TetrisConfig cfg = ConfigService.get();
        String configTag = ConfigTagUtil.makeTagFrom(cfg);
        HighScoreWriter.append(playerName, finalScore, configTag);
    }

    public void processCommand(String command) {
        switch (command) {
            case "LEFT"   -> { if (tryMove(0, -1)) render(); }
            case "RIGHT"  -> { if (tryMove(0,  1)) render(); }
            case "ROTATE" -> { if (tryRotate(1))  render(); }
            case "DOWN"   -> downPressed = true;
            case "PAUSE"  -> togglePause();
        }
    }

    public void resetGame() {
        loop.stop();
        current = null;
        next = null;
        downPressed = false;
        lastDropNs = 0L;
        drawInitialScreen();
    }
}
