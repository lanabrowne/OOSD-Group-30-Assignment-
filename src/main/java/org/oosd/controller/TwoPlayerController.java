package org.oosd.controller;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import org.oosd.HighScore.ConfigTagUtil;
import org.oosd.HighScore.HighScoreWriter;
import org.oosd.Main;
import org.oosd.config.ConfigService;
import org.oosd.config.PlayerType;
import org.oosd.config.TetrisConfig;
import org.oosd.external.ExternalPlayer;
import org.oosd.external.OpMove;
import org.oosd.model.*;
import org.oosd.ui.Frame;
import org.oosd.ui.HighScoreScreen;
import org.oosd.ui.Screen;

public class TwoPlayerController implements Screen {

    // ===== Config / Players =====
    private final TetrisConfig config = ConfigService.get();

    private ExternalPlayer externalLeft;
    private ExternalPlayer externalRight;

    private TetrisAI aiLeft;
    private TetrisAI aiRight;

    private volatile boolean connectionLostHandled = false;

    private OpMove lastExternalLeftMove  = null;
    private OpMove lastExternalRightMove = null;

    private final SharedPieceSequence sharedSequence = new SharedPieceSequence(12345L);
    private int leftPieceIndex  = 0;
    private int rightPieceIndex = 0;

    // ===== FXML =====
    @FXML private VBox leftColumn;
    @FXML private VBox rightColumn;
    @FXML private Label scoreLeft;
    @FXML private Label scoreRight;
    @FXML private VBox frameCanvas;
    @FXML private Canvas gameCanvasLeft;
    @FXML private Canvas gameCanvasRight;

    // ===== Boards / Pieces =====
    public Board boardLeft;
    public Board boardRight;

    private Tetromino currentPieceLeft;
    private Tetromino currentPieceRight;

    // ===== State =====
    private int scoreLeftValue  = 0;
    private int scoreRightValue = 0;

    private boolean paused   = false;
    private boolean gameOver = false;
    private boolean started  = false;

    private boolean nameDialogShowing = false;
    private boolean scoresSaved       = false;

    private AnimationTimer timer;
    private Frame parentFrame;

    // ===== Layout / Timing =====
    private static final int HIDDEN_ROWS = 4;
    private static final int CELL        = 25;
    private static final double MARGIN   = 8;
    private boolean fitting = false;

    private long lastTickLeft  = 0;
    private long lastTickRight = 0;
    private double dropIntervalMs = 600;
    private int    externalDownSteps = 2;

    private boolean isRunning = true;

    // ===== Palette =====
    private static final Color[] PALETTE = {
            Color.TRANSPARENT,
            Color.CYAN, Color.BLUE, Color.ORANGE,
            Color.YELLOW, Color.GREEN, Color.PURPLE, Color.RED
    };

    // ===== Public wiring (optional external clients) =====
    public void setExternalClients(ExternalPlayer left, ExternalPlayer right) {
        this.externalLeft  = left;
        this.externalRight = right;
    }

    public void setParent(Frame frame) { this.parentFrame = frame; }

    // ===== Initialize =====
    @FXML
    private void initialize() {

        boardLeft  = new Board(config.fieldWidth(),  config.fieldHeight() + HIDDEN_ROWS);
        boardRight = new Board(config.fieldWidth(),  config.fieldHeight() + HIDDEN_ROWS);


        if (config.leftPlayer() == PlayerType.AI) {
            aiLeft = new TetrisAI();
        } else if (config.leftPlayer() == PlayerType.EXTERNAL) {
            externalLeft = new ExternalPlayer(this, true);
        }

        if (config.rightPlayer() == PlayerType.AI) {
            aiRight = new TetrisAI();
        } else if (config.rightPlayer() == PlayerType.EXTERNAL) {
            externalRight = new ExternalPlayer(this, false);
        }


        int visibleH = boardLeft.h - HIDDEN_ROWS;
        gameCanvasLeft.setWidth (boardLeft.w  * CELL);
        gameCanvasLeft.setHeight(visibleH     * CELL);
        gameCanvasRight.setWidth (boardRight.w * CELL);
        gameCanvasRight.setHeight(visibleH     * CELL);

        VBox.setVgrow(leftColumn,  Priority.ALWAYS);
        VBox.setVgrow(rightColumn, Priority.ALWAYS);


        scoreLeft.setText("Score: 0");
        scoreRight.setText("Score: 0");


        Platform.runLater(() -> {
            drawWaitingOverlay(gameCanvasLeft,  "Press any key to start");
            drawWaitingOverlay(gameCanvasRight, "Press any key to start");


            attachScaleToScene();
            scheduleScaleFit();


            leftColumn.setFocusTraversable(true);
            leftColumn.requestFocus();
            leftColumn.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        });
    }

    // ===== Start / Pause =====
    private void startTwoPlayerGame() {
        if (started) return;
        started = true;

        scoreLeftValue = 0;
        scoreRightValue = 0;
        scoreLeft.setText("Score: 0");
        scoreRight.setText("Score: 0");
        gameOver = false;

        setupConnectionHandlers();


        tuneSpeedByLevel();


        initialSpawnBoth();
        renderBothBoards();


        setupTimer();
    }

    private void togglePause() {
        if (gameOver) return;
        if (paused) resumeGame(); else pauseGame();
        paused = !paused;
    }
    public void pauseGame()  { if (timer != null) timer.stop(); }
    public void resumeGame() { if (timer != null) timer.start(); }

    // ===== Frame scaling (fit to window) =====
    private void attachScaleToScene() {
        if (frameCanvas == null) return;

        frameCanvas.sceneProperty().addListener((o, oldSc, sc) -> {
            if (sc == null) return;
            sc.widthProperty().addListener((ox, a, b) -> scheduleScaleFit());
            sc.heightProperty().addListener((ox, a, b) -> scheduleScaleFit());
        });

        frameCanvas.widthProperty().addListener((o, a, b) -> scheduleScaleFit());
        frameCanvas.heightProperty().addListener((o, a, b) -> scheduleScaleFit());
        leftColumn.widthProperty().addListener((o, a, b) -> scheduleScaleFit());
        rightColumn.widthProperty().addListener((o, a, b) -> scheduleScaleFit());
    }

    private void scheduleScaleFit() {
        PauseTransition pt = new PauseTransition(Duration.millis(35));
        pt.setOnFinished(e -> fitFrameToScene());
        pt.play();
    }

    private void fitFrameToScene() {
        if (fitting) return;
        fitting = true;
        try {
            if (frameCanvas == null || frameCanvas.getScene() == null) return;

            double contentW = frameCanvas.getLayoutBounds().getWidth();
            double contentH = frameCanvas.getLayoutBounds().getHeight();
            if (contentW <= 0 || contentH <= 0) return;

            double sceneW = frameCanvas.getScene().getWidth();
            double sceneH = frameCanvas.getScene().getHeight();
            if (sceneW <= 0 || sceneH <= 0) return;

            double availW = Math.max(1, sceneW - MARGIN);
            double availH = Math.max(1, sceneH - MARGIN);

            double sx = availW / contentW;
            double sy = availH / contentH;
            double scale = Math.min(1.0, Math.min(sx, sy)); // 画面内に収まるようにだけ縮める

            if (Math.abs(scale - frameCanvas.getScaleX()) > 0.01) {
                frameCanvas.setScaleX(scale);
                frameCanvas.setScaleY(scale);
            }
        } finally {
            Platform.runLater(() -> fitting = false);
        }
    }

    // ===== Timer / Tick =====
    private void setupTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (paused || gameOver) return;

                if (lastTickLeft == 0)  lastTickLeft  = now;
                if (lastTickRight == 0) lastTickRight = now;

                long eL = (now - lastTickLeft)  / 1_000_000L;
                long eR = (now - lastTickRight) / 1_000_000L;


                if (eL >= dropIntervalMs) {
                    if (aiLeft != null) {
                        tickLeft();
                    } else if (externalLeft != null) {
                        tick(boardLeft, true);
                    } else {
                        tick(boardLeft, true);
                    }
                    lastTickLeft = now;
                }


                if (eR >= dropIntervalMs) {
                    if (aiRight != null) {
                        tickRight();
                    } else if (externalRight != null) {
                        tick(boardRight, false);
                    } else {
                        tick(boardRight, false);
                    }
                    lastTickRight = now;
                }


                if (externalLeft != null && externalLeft.pendingMove != null) {
                    if (!externalLeft.pendingMove.equals(lastExternalLeftMove)) {
                        applyExternalMove(externalLeft.pendingMove, true);
                        lastExternalLeftMove = externalLeft.pendingMove;
                    }
                    externalLeft.pendingMove = null;
                }
                if (externalRight != null && externalRight.pendingMove != null) {
                    if (!externalRight.pendingMove.equals(lastExternalRightMove)) {
                        applyExternalMove(externalRight.pendingMove, false);
                        lastExternalRightMove = externalRight.pendingMove;
                    }
                    externalRight.pendingMove = null;
                }
            }
        };
        timer.start();
    }

    private void tickLeft() {
        if (aiLeft != null) {
            int[][] snap = boardLeft.snapshot();
            Move move = aiLeft.findBestMove(snap, boardLeft.h, boardLeft.w,
                    currentPieceLeft, peekNext(boardLeft, true));
            executeAIMove(boardLeft, true, move);
            softDrop(boardLeft, true);
        } else {
            tick(boardLeft, true);
        }
    }

    private void tickRight() {
        if (aiRight != null) {
            int[][] snap = boardRight.snapshot();
            Move move = aiRight.findBestMove(snap, boardRight.h, boardRight.w,
                    currentPieceRight, peekNext(boardRight, false));
            executeAIMove(boardRight, false, move);
            softDrop(boardRight, false);
        } else {
            tick(boardRight, false);
        }
    }

    private void tick(Board board, boolean isLeft) {
        if ((isLeft && aiLeft != null) || (!isLeft && aiRight != null)) return;
        //if ((isLeft && externalLeft != null) || (!isLeft && externalRight != null)) return;

        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;

        Tetromino down = piece.moved(1, 0);
        boolean landed = !board.canPlace(down);

        if (!landed) {
            piece = down;
        } else {
            board.lock(piece);
            int lines = board.clearFullLines();
            if (lines > 0) {
                int pts = pointsFor(lines);
                if (isLeft) scoreLeftValue += pts; else scoreRightValue += pts;
                updateScoreLabels();
            }
            piece = spawnFor(board, isLeft);
            if (!board.canPlace(piece)) {
                endAndPromptNames();
                return;
            }
            notifyExternalOnSpawn(isLeft);
        }

        if (isLeft) currentPieceLeft = piece; else currentPieceRight = piece;
        renderBothBoards();
    }

    // ===== Input =====
    private void handleKeyPress(KeyEvent ev) {
        if (!started) {
            startTwoPlayerGame();
            return;
        }

        switch (ev.getCode()) {
            // P1 (WASD)
            case A -> {
                movePiece(boardLeft, true, 0, -1);
                if (externalLeft != null && externalLeft.isConnected()) externalLeft.sendAction();
            }
            case D -> {
                movePiece(boardLeft, true, 0, 1);
                if (externalLeft != null && externalLeft.isConnected()) externalLeft.sendAction();
            }
            case W -> {
                rotatePiece(boardLeft, true);
                if (externalLeft != null && externalLeft.isConnected()) externalLeft.sendAction();
            }
            case S -> {
                softDrop(boardLeft, true);
                if (externalLeft != null && externalLeft.isConnected()) externalLeft.sendAction();
            }

            // P2 (Arrows)
            case LEFT -> {
                movePiece(boardRight, false, 0, -1);
                if (externalRight != null && externalRight.isConnected()) externalRight.sendAction();
            }
            case RIGHT -> {
                movePiece(boardRight, false, 0, 1);
                if (externalRight != null && externalRight.isConnected()) externalRight.sendAction();
            }
            case UP -> {
                rotatePiece(boardRight, false);
                if (externalRight != null && externalRight.isConnected()) externalRight.sendAction();
            }
            case DOWN -> {
                softDrop(boardRight, false);
                if (externalRight != null && externalRight.isConnected()) externalRight.sendAction();
            }

            case P -> togglePause();
            default -> {}
        }
    }

    public void processCommand(String cmd, boolean isLeft) {
        switch (cmd.toUpperCase()) {
            case "LEFT"   -> movePiece(isLeft ? boardLeft : boardRight, isLeft, 0, -1);
            case "RIGHT"  -> movePiece(isLeft ? boardLeft : boardRight, isLeft, 0, 1);
            case "ROTATE" -> rotatePiece(isLeft ? boardLeft : boardRight, isLeft);
            case "DOWN"   -> softDrop(isLeft ? boardLeft : boardRight, isLeft);
            default -> System.out.println("Unknown command: " + cmd);
        }
    }

    public void movePiece(Board board, boolean isLeft, int dRow, int dCol) {
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;
        Tetromino moved = piece.moved(dRow, dCol);
        if (board.canPlace(moved)) {
            if (isLeft) currentPieceLeft = moved; else currentPieceRight = moved;
            renderBothBoards();
        }
    }

    private void rotatePiece(Board board, boolean isLeft) {
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;

        Tetromino rotate = piece.rotated(1);
        for (int kick : new int[]{0, -1, 1}) {
            Tetromino t = new Tetromino(rotate.type, rotate.rotation, rotate.row, rotate.col + kick);
            if (board.canPlace(t)) {
                if (isLeft) currentPieceLeft = t; else currentPieceRight = t;
                break;
            }
        }
        renderBothBoards();
    }

    private void softDrop(Board board, boolean isLeft) {
        Tetromino p = isLeft ? currentPieceLeft : currentPieceRight;
        if (p == null) return;

        Tetromino down = p.moved(1, 0);
        if (board.canPlace(down)) {
            p = down;
        } else {
            board.lock(p);
            int lines = board.clearFullLines();
            if (lines > 0) {
                int pts = pointsFor(lines);
                if (isLeft) scoreLeftValue += pts; else scoreRightValue += pts;
                updateScoreLabels();
            }
            p = spawnFor(board, isLeft);
            if (!board.canPlace(p)) {
                endAndPromptNames();
                return;
            }
            notifyExternalOnSpawn(isLeft);
        }

        if (isLeft) currentPieceLeft = p; else currentPieceRight = p;
        renderBothBoards();
    }

    private void executeAIMove(Board board, boolean isLeft, Move move) {
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (move == null || piece == null) return;

        int rotations = (move.rotation - piece.rotation + 4) % 4;
        for (int i = 0; i < rotations; i++) {
            Tetromino rotated = piece.rotated(1);
            if (board.canPlace(rotated)) piece = rotated;
        }

        int targetCol = Math.max(0, Math.min(move.col, board.w - piece.spawnWidth()));
        while (piece.col < targetCol && board.canPlace(piece.moved(0, 1)))  piece = piece.moved(0, 1);
        while (piece.col > targetCol && board.canPlace(piece.moved(0, -1))) piece = piece.moved(0, -1);

        if (isLeft) currentPieceLeft = piece; else currentPieceRight = piece;
        renderBothBoards();
    }

    // ===== Spawning / Next =====
    private void initialSpawnBoth() {
        TetrominoType firstLeft  = sharedSequence.pieceAt(leftPieceIndex++);
        TetrominoType firstRight = sharedSequence.pieceAt(rightPieceIndex++);
        currentPieceLeft  = new Tetromino(firstLeft,  boardLeft.w  / 2 - 1, 0, 0);
        currentPieceRight = new Tetromino(firstRight, boardRight.w / 2 - 1, 0, 0);

        notifyExternalOnSpawn(true);
        notifyExternalOnSpawn(false);
    }

    private Tetromino spawnFor(Board board, boolean isLeft) {
        int index = isLeft ? leftPieceIndex : rightPieceIndex;
        TetrominoType type = sharedSequence.pieceAt(index);
        if (isLeft) leftPieceIndex++; else rightPieceIndex++;
        //return new Tetromino(type, board.w / 2 - 1, 0, 0);
        int startRow = -2;
        int startCol = board.w / 2 - 2;

        return new Tetromino(type, startCol, startRow, 0);
    }

    private Tetromino peekNext(Board board, boolean isLeft) {
        int index = isLeft ? leftPieceIndex : rightPieceIndex;
        TetrominoType type = sharedSequence.pieceAt(index);
        return new Tetromino(type, board.w / 2 - 1, 0, 0);
    }

    private void notifyExternalOnSpawn(boolean isLeft) {
        if (isLeft && externalLeft != null) {
            TetrominoType nextType = sharedSequence.pieceAt(leftPieceIndex);
            Tetromino next = new Tetromino(nextType, 0, 0, 0);

            externalLeft.updateBoardState(boardLeft, currentPieceLeft, next);
            externalLeft.setCurrentPiece(currentPieceLeft);
            externalLeft.setNextPiece(next);
            externalLeft.sendAction();
        } else if (!isLeft && externalRight != null) {
            TetrominoType nextType = sharedSequence.pieceAt(rightPieceIndex);
            Tetromino next = new Tetromino(nextType, 0, 0, 0);

            externalRight.updateBoardState(boardRight, currentPieceRight, next);
            externalRight.setCurrentPiece(currentPieceRight);
            externalRight.setNextPiece(next);
            externalRight.sendAction();
        }
    }

    // ===== Speed (level-based) =====
    private void tuneSpeedByLevel() {
        final double BASE = 650;
        final double STEP = 80;
        final double MIN  = 80;

        int lv = Math.max(0, config.gameLevel());
        dropIntervalMs     = Math.max(MIN, BASE - STEP * lv);
        externalDownSteps  = Math.min(5, 2 + lv / 2);

        System.out.println("[Speed] level=" + lv +
                " dropIntervalMs=" + dropIntervalMs +
                " externalDownSteps=" + externalDownSteps);
    }


    public void applyExternalMove(OpMove move, boolean isLeft) {
        Board b = isLeft ? boardLeft : boardRight;
        Tetromino cur = isLeft ? currentPieceLeft : currentPieceRight;
        if (b == null || cur == null || move == null) return;


        for (int i = 0; i < move.opRotate(); i++) {
            Tetromino r = cur.rotated(1);
            if (b.canPlace(r)) cur = r;
        }


        int targetCol = Math.max(0, Math.min(move.opX(), b.w - cur.spawnWidth()));
        while (cur.col < targetCol && b.canPlace(cur.moved(0, 1)))  cur = cur.moved(0, 1);
        while (cur.col > targetCol && b.canPlace(cur.moved(0, -1))) cur = cur.moved(0, -1);


        boolean landed = false;
        for (int i = 0; i < externalDownSteps; i++) {
            Tetromino down = cur.moved(1, 0);
            if (b.canPlace(down)) {
                cur = down;
            } else {
                landed = true;
                break;
            }
        }


        if (landed) {
            b.lock(cur);
            int cleared = b.clearFullLines();
            if (cleared > 0) {
                int pts = pointsFor(cleared);
                if (isLeft) scoreLeftValue += pts;
                else scoreRightValue += pts;
                updateScoreLabels();
            }

            Tetromino next = spawnFor(b, isLeft);
            if (!b.canPlace(next)) {
                endAndPromptNames();
                return;
            }

            if (isLeft) currentPieceLeft = next;
            else currentPieceRight = next;

            notifyExternalOnSpawn(isLeft);
        } else {

            Tetromino down = cur.moved(1, 0);
            if (b.canPlace(down)) cur = down;

            if (isLeft) currentPieceLeft = cur;
            else currentPieceRight = cur;
        }


        Platform.runLater(this::renderBothBoards);
    }


    private void setupConnectionHandlers() {
        if (externalLeft != null) {
            externalLeft.setOnConnectionLost(() -> showConnectionLostAlert(true));
        }
        if (externalRight != null) {
            externalRight.setOnConnectionLost(() -> showConnectionLostAlert(false));
        }
    }

    private void showConnectionLostAlert(boolean isLeft) {
        synchronized (this) {
            if (connectionLostHandled) return;
            connectionLostHandled = true;
        }


        pauseGame();
        paused = true;


        Platform.runLater(() -> {

            if (!connectionLostHandled) return;

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Server Connection Error");
            alert.setHeaderText("Connection was cut");
            alert.setContentText("Please restart the server and reconnect.");
            alert.showAndWait();


            connectionLostHandled = true;
        });
    }

    // ===== Render =====
    private void renderBothBoards() {
        renderBoardInto(gameCanvasLeft,  boardLeft,  currentPieceLeft);
        renderBoardInto(gameCanvasRight, boardRight, currentPieceRight);

        if (gameOver) {
            drawGameOverOverlay(gameCanvasLeft);
            drawGameOverOverlay(gameCanvasRight);
        }
    }

    private void renderBoardInto(Canvas target, Board board, Tetromino piece) {
        GraphicsContext gc = target.getGraphicsContext2D();
        gc.clearRect(0, 0, target.getWidth(), target.getHeight());

        int[][] grid = board.snapshot();

        for (int r = HIDDEN_ROWS; r < board.h; r++) {
            for (int c = 0; c < board.w; c++) {
                int id = grid[r][c];
                Color fill = (id == 0) ? Color.BLACK
                        : (id >= 0 && id < PALETTE.length ? PALETTE[id] : Color.BLACK);

                gc.setFill(fill);
                gc.fillRect(c * CELL, (r - HIDDEN_ROWS) * CELL, CELL, CELL);

                gc.setStroke(Color.web("#222"));
                gc.strokeRect(c * CELL, (r - HIDDEN_ROWS) * CELL, CELL, CELL);
            }
        }

        if (piece != null) {
            Color colour = PALETTE[piece.type.colorId];
            for (int[] cell : piece.cells()) {
                int row = piece.row + cell[1];
                int col = piece.col + cell[0];
                if (row < HIDDEN_ROWS || row >= board.h || col < 0 || col >= board.w) continue;

                gc.setFill(colour);
                gc.fillRect(col * CELL, (row - HIDDEN_ROWS) * CELL, CELL, CELL);

                gc.setStroke(Color.web("#222"));
                gc.strokeRect(col * CELL, (row - HIDDEN_ROWS) * CELL, CELL, CELL);
            }
        }
    }

    private void drawWaitingOverlay(Canvas canvas, String text) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.setStroke(Color.web("#222"));
        for (double x = 0; x <= canvas.getWidth(); x += CELL) g.strokeLine(x, 0, x, canvas.getHeight());
        for (double y = 0; y <= canvas.getHeight(); y += CELL) g.strokeLine(0, y, canvas.getWidth(), y);

        g.setFill(Color.WHITE);
        g.setFont(Font.font(16));
        Text t = new Text(text);
        t.setFont(g.getFont());
        double tw = t.getLayoutBounds().getWidth();
        double th = t.getLayoutBounds().getHeight();
        double tx = Math.max(8, (canvas.getWidth() - tw) / 2.0);
        double ty = Math.max(th + 8, canvas.getHeight() * 0.5);
        g.fillText(text, tx, ty);
    }

    private void drawGameOverOverlay(Canvas canvas) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.rgb(0, 0, 0, 0.7));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFill(Color.WHITE);
        g.setFont(Font.font(28));
        String text = "GAME OVER";
        Text m = new Text(text);
        m.setFont(g.getFont());
        double tw = m.getLayoutBounds().getWidth();
        g.fillText(text, Math.max(8, (canvas.getWidth() - tw) / 2.0), canvas.getHeight() / 2.0);
    }

    // ===== End flow: prompt names, save scores, go to High Score =====
    public void endAndPromptNames() {

        if (gameOver) return;
        gameOver = true;
        if (timer != null) timer.stop();

        drawGameOverOverlay(gameCanvasLeft);
        drawGameOverOverlay(gameCanvasRight);

        if (scoresSaved || nameDialogShowing) return;
        nameDialogShowing = true;

        final int finalLeft  = scoreLeftValue;
        final int finalRight = scoreRightValue;

        Platform.runLater(() -> {
            final String defaultLeft  = "Left Player";
            final String defaultRight = "Right Player";

            TextInputDialog d1 = new TextInputDialog(defaultLeft);
            d1.setTitle("High Score");
            d1.setHeaderText("Left player name?");
            d1.setContentText("Name:");
            d1.setOnHidden(e1 -> {
                String res1 = d1.getResult();
                final String leftNameFinal =
                        (res1 == null || res1.trim().isEmpty()) ? defaultLeft : res1.trim();

                TextInputDialog d2 = new TextInputDialog(defaultRight);
                d2.setTitle("High Score");
                d2.setHeaderText("Right player name?");
                d2.setContentText("Name:");
                d2.setOnHidden(e2 -> {
                    String res2 = d2.getResult();
                    final String rightNameFinal =
                            (res2 == null || res2.trim().isEmpty()) ? defaultRight : res2.trim();

                    saveTwoScores(leftNameFinal, finalLeft, rightNameFinal, finalRight);
                    nameDialogShowing = false;
                });
                d2.show();
            });
            d1.show();
        });
        stopGame();
    }

    private void saveTwoScores(String leftName, int leftScore, String rightName, int rightScore) {
        try {
            ConfigService.load();
            TetrisConfig cfg = ConfigService.get();
            String tag = ConfigTagUtil.makeTagFrom(cfg);

            HighScoreWriter.append(leftName,  leftScore,  tag);
            HighScoreWriter.append(rightName, rightScore, tag);
            scoresSaved = true;

            Platform.runLater(() -> {
                if (parentFrame instanceof Main m) {
                    parentFrame.showScreen(new HighScoreScreen(m));
                } else if (parentFrame != null) {
                    parentFrame.showScreen(new HighScoreScreen((Main) parentFrame));
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ===== Utils =====
    private int pointsFor(int linesCleared) {
        return switch (linesCleared) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 600;
            case 4 -> 1000;
            default -> 0;
        };
    }

    private void updateScoreLabels() {
        scoreLeft.setText("Score: " + scoreLeftValue);
        scoreRight.setText("Score: " + scoreRightValue);
    }


    public boolean step()
    {
        if (!isRunning) {
            return true;
        }

        return true;

    }





    // ===== Screen =====
    @Override
    public Parent getScreen() {
        return leftColumn != null ? leftColumn.getScene().getRoot() : null;
    }

    @Override
    public void setRoute(String path, Screen screen) { /* no-op */ }

    public void stopGame() {
        if (timer != null) {
            timer.stop();
        }

        isRunning = false;
        paused = true;
        gameOver = true;

        System.out.println("Game stopped manually.");
    }
}
