package org.oosd.controller;


import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.canvas.GraphicsContext;
import org.oosd.config.ConfigService;
import org.oosd.config.PlayerType;
import org.oosd.config.TetrisConfig;
import org.oosd.external.ExternalPlayer;
import org.oosd.external.OpMove;
import org.oosd.model.*;
import org.oosd.ui.Frame;
import org.oosd.ui.Screen;

import java.util.Optional;


public class TwoPlayerController implements Screen {

    TetrisConfig config = ConfigService.get();

    //Implement external client into both side player screen
    private ExternalPlayer externalLeft;
    private ExternalPlayer externalRight;

    //To migrate AI board that is created by Ria, set AI Board controller in here
    private TetrisAI aiLeft;
    private TetrisAI aiRight;

    private OpMove lastExternalLeftMove = null;
    private OpMove lastExternalRightMove = null;



    //Initialize external client
    public void setExternalClients (ExternalPlayer left, ExternalPlayer right)
    {
        this.externalLeft = left;
        this.externalRight = right;
    }
    @FXML
    //from TwoPlayerScreen.fxml
    private VBox leftColumn;
    @FXML private VBox rightColumn;
    @FXML private GridPane leftGrid;
    @FXML private GridPane rightGrid;
    @FXML private Label scoreLeft;
    @FXML private Label scoreRight;
    @FXML private Label lblWinner;
    @FXML private VBox frameCanvas;
    @FXML private Canvas gameCanvasLeft;
    @FXML private Canvas gameCanvasRight;



    public Board boardLeft;
    public Board boardRight;
    private Tetromino currentPieceLeft;
    private Tetromino currentPieceRight;

    private static final int hiddenRows = 4;

    private long lastTickLeft = 0;
    private long lastTickRight = 0;
    private double dropIntervalMs = 600;

    private boolean paused = false;
    private boolean gameOver = false;

    private AnimationTimer timer;
    private Frame parentFrame;

    // tickleft/right moves the players pieces down by one tick
    private void tickLeft() {
        if (aiLeft != null) {
            int[][] snap = boardLeft.snapshot();
            Move move = aiLeft.findBestMove(snap, boardLeft.h, boardLeft.w, currentPieceLeft, spawnFor(boardLeft));
            executeAIMove(boardLeft, true, move);
            softDrop(boardLeft, true); // 落とす
        } else {
            tick(boardLeft, true);
        }
    }
    private void tickRight() {
        if (aiRight != null) {
            int[][] snap = boardRight.snapshot();
            Move move = aiRight.findBestMove(snap, boardRight.h, boardRight.w, currentPieceRight, spawnFor(boardRight));
            executeAIMove(boardRight, false, move);
            softDrop(boardRight, false);
        } else {
            tick(boardRight, false);
        }
    }

    private int scoreLeftValue = 0;
    private int scoreRightValue = 0;

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

    @FXML
    private void initialize() {
        // model setup
        boardLeft = new Board(config.fieldWidth(), config.fieldHeight());
        boardRight = new Board(config.fieldWidth(), config.fieldHeight());

        if (config.leftPlayer() == PlayerType.AI) {
            aiLeft = new TetrisAI();
            dropIntervalMs = 200;
        } else if (config.leftPlayer() == PlayerType.EXTERNAL) {
            externalLeft = new ExternalPlayer(this, true);
        }

        if (config.rightPlayer() == PlayerType.AI) {
            aiRight = new TetrisAI();
            dropIntervalMs = 200;
        } else if (config.rightPlayer() == PlayerType.EXTERNAL) {
            externalRight = new ExternalPlayer(this, false);
        }

        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        int blockSize = 25;
        gameCanvasLeft.setWidth(boardLeft.w * blockSize);
        gameCanvasLeft.setHeight((boardLeft.h - hiddenRows) * blockSize);

        gameCanvasRight.setWidth(boardRight.w * blockSize);
        gameCanvasRight.setHeight((boardRight.h - hiddenRows) * blockSize);


        Platform.runLater(() -> {
            javafx.stage.Screen fxScreen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = fxScreen.getVisualBounds();

            double screenW = bounds.getWidth() * 0.9;
            double screenH = bounds.getHeight() * 0.9;


            double actualW = frameCanvas.getLayoutBounds().getWidth();
            double actualH = frameCanvas.getLayoutBounds().getHeight();

            if (actualW == 0 || actualH == 0) {
                System.out.println("Size is not identified");
                return;
            }

            double scaleX = screenW / actualW;
            double scaleY = screenH / actualH;
            double scale = Math.min(1.0, Math.min(scaleX, scaleY));

            frameCanvas.setScaleX(scale);
            frameCanvas.setScaleY(scale);

            System.out.println("Frame area scaled: " + scale + " (W=" + actualW + ", H=" + actualH + ")");
        });


        //frameCanvas.setPrefWidth(boardLeft.w * blockSize + boardRight.w * blockSize + 400);

        initialSpawnBoth();
        renderBothBoards();

        //Set player type (LEFT side)
        if(config.leftPlayer() == PlayerType.AI)
        {
           aiLeft = new TetrisAI();

        }else if(config.leftPlayer() == PlayerType.EXTERNAL)
        {
            externalLeft = new ExternalPlayer(this, true);
        }


        //Set RIGHT side player type
        if(config.rightPlayer() == PlayerType.AI)
        {
            aiRight = new TetrisAI();
        } else if(config.rightPlayer() == PlayerType.EXTERNAL)
        {
            externalRight = new ExternalPlayer(this,false);
        }

        // UI
        scoreLeft.setText("Score: 0");
        scoreRight.setText("Score: 0");
        lblWinner.setText("");

        // input focus
        Platform.runLater(() -> {
            leftColumn.setFocusTraversable(true);
            leftColumn.requestFocus();
            leftColumn.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        });

        // start game

        initialSpawnBoth();
        renderBothBoards();
        setupTimer();
    }


    public void setParent(Frame frame){
        this.parentFrame = frame;
    }

    // set up and start the game timer to control intervals between piece drops
    private void setupTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTickLeft == 0) lastTickLeft = now;
                if (lastTickRight == 0) lastTickRight = now;

                long elapsedLeft = (now - lastTickLeft) / 1_000_000L;
                long elapsedRight = (now - lastTickRight) / 1_000_000L;



                if (elapsedLeft >= dropIntervalMs) {
                    tickLeft();
                    lastTickLeft = now;
                }
                if (elapsedRight >= dropIntervalMs) {
                    tickRight();
                    lastTickRight = now;
                }

                renderBothBoards();
            }
        };
        timer.start();
    }

    /*
    move the tetromino down the board
    once the piece has landed, lock it in place and spawn the next piece
    clear any full lines and update the score
    if a new piece cannot be placed, end game
     */
    private void tick(Board board, boolean isLeft) {

        if (isLeft && aiLeft != null) {
            int[][] snap = boardLeft.snapshot();
            Move move = aiLeft.findBestMove(snap, boardLeft.h, boardLeft.w, currentPieceLeft, spawnFor(boardLeft));
            if (move != null) {
                // move.rotation と move.col を元にピースを動かす
                executeAIMove(boardLeft, true, move);
                softDrop(boardLeft, true);
            }
        }
        //RIGHT SCREEN
        if(!isLeft && aiRight != null)
        {
            int[][] snap = boardRight.snapshot();
            Move move = aiRight.findBestMove(snap, boardRight.h, boardRight.w, currentPieceRight, spawnFor(boardRight));
            if (move != null) {
                executeAIMove(boardRight, false, move);
                softDrop(boardRight, false);
            }
        }

        //WHEN Player --> EXTERNAL
        if(isLeft && externalLeft != null)
        {
                externalLeft.updateBoardState(boardLeft, currentPieceLeft, spawnFor(boardLeft));
            //receive action from server and make action
                externalLeft.setCurrentPiece(currentPieceLeft);
                externalLeft.setNextPiece(spawnFor(boardLeft));
                externalLeft.sendAction();

            // if we got response from server, reflect that
            if (externalLeft.pendingMove != null) {
                if (!externalLeft.pendingMove.equals(lastExternalLeftMove)) {
                    applyExternalMove(externalLeft.pendingMove, true);
                    lastExternalLeftMove = externalLeft.pendingMove;
                } else {
                    System.out.println("Skipping duplicate left move");
                }
                externalLeft.pendingMove = null;
            }

            //externalLeft.updateBoardState(boardLeft, currentPieceLeft, null);


        }
        if(!isLeft && externalRight != null)
        {
            externalRight.updateBoardState(boardRight, currentPieceRight, spawnFor(boardRight));
            externalRight.setCurrentPiece(currentPieceRight);
            externalRight.setNextPiece(spawnFor(boardRight));
            externalRight.sendAction();

            if (externalRight.pendingMove != null) {
                if (!externalRight.pendingMove.equals(lastExternalRightMove)) {
                    applyExternalMove(externalRight.pendingMove, false);
                    lastExternalRightMove = externalRight.pendingMove;
                } else {
                    System.out.println("Skipping duplicate right move");
                }
                externalRight.pendingMove = null;
            }
            //externalRight.updateBoardState(boardRight, currentPieceRight, null);


        }



        //BELOW --> When player is USER
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;

        Tetromino down = piece.moved(1,0);
        boolean landed = !board.canPlace(down);

        if (!landed){
            // moves the piece downwards
            piece = down;
        } else {
            // if the piece has landed, lock it and clear any full lines
            board.lock(piece);
            int linesCleared = board.clearFullLines();

            if (linesCleared > 0){
                int points = pointsFor(linesCleared);

                if (isLeft) scoreLeftValue += points;
                else scoreRightValue += points;

                updateScoreLabels();
            }

            // spaw a new piece after locking the previous one
            piece = spawnFor(board);
            if (!board.canPlace(piece)) {
                // if a new piece cannot be placed, game over and the winner is announced
                endGame(isLeft ? "Player 2 Wins!" : "Player 1 Wins!");
                return;
            }
        }

        if (isLeft && externalLeft != null && externalLeft.pendingMove != null) {
            stepExternal(externalLeft, boardLeft, true);
        }
        if (!isLeft && externalRight != null && externalRight.pendingMove != null) {
            stepExternal(externalRight, boardRight, false);
        }

        if (isLeft) currentPieceLeft = piece;
        else currentPieceRight = piece;

        renderBothBoards();
    }

    private void stepExternal(ExternalPlayer ext, Board board, boolean isLeft) {
        OpMove move = ext.pendingMove;
        if (move == null) return;

        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;

        System.out.println("[DEBUG] External step: X=" + move.opX() + " rotate=" + move.opRotate());

        // --- 横移動 ---
        if (move.opX() != piece.col) {
            int diff = move.opX() - piece.col;
            int dir = diff > 0 ? 1 : -1;
            for (int i = 0; i < Math.abs(diff); i++) {
                Tetromino moved = piece.moved(0, dir);
                if (board.canPlace(moved)) piece = moved;
            }
        }

        // --- 回転処理 ---
        for (int i = 0; i < move.opRotate(); i++) {
            Tetromino rotated = piece.rotated(1);
            if (board.canPlace(rotated)) piece = rotated;
        }

        // --- 自然落下（1段 or 全部） ---
        Tetromino down = piece.moved(1, 0);
        if (board.canPlace(down)) piece = down;
        else {
            board.lock(piece);
            piece = spawnFor(board);
        }

        // ✅ 表示ピース更新（超重要）
        if (isLeft) currentPieceLeft = piece;
        else currentPieceRight = piece;

        // ✅ JavaFXスレッドで描画
        Platform.runLater(this::renderBothBoards);

        // 受信済みなのでリセット
        ext.pendingMove = null;
    }

    //calculate the number of points awarded per lines cleared
    private int pointsFor(int linesCleared) {
        return switch (linesCleared){
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 600;
            case 4 -> 1000;
            default -> 0;
        };
    }

    // update text labels to reflect current score
    private void updateScoreLabels(){
        scoreLeft.setText("Score: " + scoreLeftValue);
        scoreRight.setText("Score: " + scoreRightValue);
    }

    // spawn the initial tetromino piece for both l/r players
    private void initialSpawnBoth(){
        currentPieceLeft = spawnFor(boardLeft);
        currentPieceRight = spawnFor(boardRight);
   }

    // create a new random tetromino
    // positioned at the centre of the board
    private Tetromino spawnFor(Board board){
        Tetromino t = Tetromino.random(board.w);
        t.row = 0;
        t.col = board.w/2-1;
        return t;
   }

    /*
    handle player input -> WASD for player 1 & Arrows for player 2
    move Left (A/Left key)
    move Right (D/Right key)
    Rotate piece (W/Up key)
    soft drop (S/Down key)
     */
    private void handleKeyPress(KeyEvent ev) {
        switch (ev.getCode()) {
            //player 1 controls (WASD)
            /**
             * In here, sending user action to server will be implemented
             * for the case (Human vs External) or (External vs Human)
             */
            case A -> {
                movePiece(boardLeft, true, 0, -1);
                //if server is connected, send action to server
                if(externalLeft != null && externalLeft.isConnected())
                {
                    //Send left command to server
                    externalLeft.sendAction();
                }
            }
            case D -> {
                movePiece(boardLeft, true, 0, 1);
                //if server is connected, send action to server
                if(externalLeft != null && externalLeft.isConnected())
                {
                    //Send right command to server
                    externalLeft.sendAction();
                }
            }
            case W -> {
                rotatePiece(boardLeft, true);
                //if server is connected, send action to server
                if(externalLeft != null && externalLeft.isConnected())
                {
                    //Send rotate command to server
                    externalLeft.sendAction();
                }
            }
            case S -> {
                softDrop(boardLeft, true);
                //if server is connected, send action to server
                if(externalLeft != null && externalLeft.isConnected())
                {
                    //Send down command to server
                    externalLeft.sendAction();
                }
            }

            //player 2 controls (Arrows)
            case LEFT -> {
                movePiece(boardRight, false, 0, -1);
                //if server is connected, send action to server
                if(externalRight != null && externalRight.isConnected())
                {
                    //Send left command to server
                    externalRight.sendAction();
                }
            }
            case RIGHT -> {
                movePiece(boardRight, false, 0, 1);
                //if server is connected, send action to server
                if(externalRight != null && externalRight.isConnected())
                {
                    //Send right command to server
                    externalRight.sendAction();
                }
            }

            case UP -> {
                rotatePiece(boardRight, false);
                //if server is connected, send action to server
                if(externalRight != null && externalRight.isConnected())
                {
                    //Send rotate command to server
                    externalRight.sendAction();
                }
            }
            case DOWN ->{
                softDrop(boardRight, false);
                //if server is connected, send action to server
                if(externalRight != null && externalRight.isConnected())
                {
                    //Send down command to server
                    externalRight.sendAction();
                }
            }

            // 'p' for pause
            case P -> togglePause();

            default -> {}
        }
    }

    /**
     * Create public wrapper method to handle sent action from external player class.
     * @param cmd
     * @param isLeft
     */
    public void processCommand(String cmd, boolean isLeft)
    {
        switch(cmd.toUpperCase())
        {
            //using same method with handle key press method
            case "LEFT" -> movePiece(isLeft ? boardLeft : boardRight, isLeft, 0, -1);
            case "RIGHT" -> movePiece(isLeft ? boardLeft : boardRight, isLeft, 0, 1);
            case "ROTATE" -> rotatePiece(isLeft ? boardLeft : boardRight, isLeft);
            case "DOWN" -> softDrop(isLeft ? boardLeft : boardRight, isLeft);
            //set the error handling line for the case when controller received
            //exception command
            default -> System.out.println("Unknown command sent: " + cmd);
        }
    }


    // attempt to move the current tetromino by the given row (dRow) and column (dCol)

    /**
     * I set this private class to public class to use for applying action from server
     * @param board
     * @param isLeft
     * @param dRow
     * @param dCol
     */
    public void movePiece(Board board, boolean isLeft, int dRow, int dCol){
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;
        Tetromino moved = piece.moved(dRow,dCol);
        if (board.canPlace(moved)){
            // update position if the move is valid
            if (isLeft) currentPieceLeft = moved;
            else currentPieceRight = moved;
            renderBothBoards();
        }
    }

    private void executeAIMove(Board board, boolean isLeft, Move move) {
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (move == null || piece == null) return;

        // Rotation
        int rotations = (move.rotation - piece.rotation + 4) % 4;
        for (int i = 0; i < rotations; i++) {
            Tetromino rotated = piece.rotated(1);
            if (board.canPlace(rotated)) piece = rotated;
        }

        // move left / right
        int targetCol = Math.max(0, Math.min(move.col, board.w - piece.spawnWidth()));
        while (piece.col < targetCol && board.canPlace(piece.moved(0, 1))) {
            piece = piece.moved(0, 1);
        }
        while (piece.col > targetCol && board.canPlace(piece.moved(0, -1))) {
            piece = piece.moved(0, -1);
        }

        if (isLeft) currentPieceLeft = piece;
        else currentPieceRight = piece;

        renderBothBoards();
    }

    // pause game
    public void pauseGame() {
        if (timer != null) timer.stop();
    }

    // resume game, works alongside pause
    public void resumeGame() {
        if (timer != null) timer.start();
    }

    // soft drop the current piece
    // increases the falling speed, locks the piece into place
    private void softDrop(Board board, boolean isLeft){
        Tetromino p = isLeft ? currentPieceLeft : currentPieceRight;

        if (p == null) return;
        Tetromino down = p.moved(1,0);

        if (board.canPlace(down)) {
            // If it can move (not locked into place) update its position
            p = down;
        } else {
            // if the piece cant move down any further, lock it into place and check to clear lines
            board.lock(p);
            int linesCleared = board.clearFullLines();

            if (linesCleared > 0){
                int points = pointsFor(linesCleared);
                if (isLeft) scoreLeftValue += points;
                else scoreRightValue += points;
                updateScoreLabels();
            }

            // spawn a new piece after locking the previous one
            // otherwise call end game and announce the winner
            p = spawnFor(board);
            if (!board.canPlace(p)) {
                endGame(isLeft ? "Player 2 Wins!" : "Player 1 Wins!");
            }
        }

        if (isLeft) currentPieceLeft = p;
        else currentPieceRight = p;
        renderBothBoards();
    }

    //rotate the current piece clockwise
    private void rotatePiece(Board board, boolean isLeft) {
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;

        Tetromino rotate = piece.rotated(1); // rotate 90 degrees clockwise
        for (int kick : new int[]{0, -1, 1}) {
            Tetromino t = new Tetromino(rotate.type, rotate.rotation, rotate.row, rotate.col + kick);
            if (board.canPlace(t)) {
                // if this rotated position fits, use it
                if (isLeft) currentPieceLeft = t;
                else currentPieceRight = t;
                break;
            }
        }
        // redraw boards after rotation
        renderBothBoards();
    }

    public void focusGame(){
        Platform.runLater(() -> {
            if (leftColumn != null) {
               leftColumn.setFocusTraversable(true);
               leftColumn.requestFocus();
            }
        });
    }

    private void togglePause(){
        if (gameOver) return;
        if (paused) {resumeGame();}
        else  {pauseGame();}

        paused = !paused;
    }

    // stop the timer and declare game over/the winner
    private void endGame(String msg) {
        if (timer != null) timer.stop();
        lblWinner.setText(msg);
    }

    private void renderBothBoards() {
        renderBoardInto(gameCanvasLeft, boardLeft, currentPieceLeft);
        renderBoardInto(gameCanvasRight, boardRight, currentPieceRight);
    }

    private void renderBoardInto(Canvas target, Board board, Tetromino piece) {
        GraphicsContext gc = target.getGraphicsContext2D();
        gc.clearRect(0, 0, target.getWidth(), target.getHeight());

        int[][] grid = board.snapshot();
        int blockSize = 25;

        // Draw locked cells
        for (int r = hiddenRows; r < board.h; r++) {
            for (int c = 0; c < board.w; c++) {
                int id = grid[r][c];
                Color fill = (id == 0) ? Color.BLACK
                        : (id >= 0 && id < PALETTE.length ? PALETTE[id] : Color.BLACK);

                gc.setFill(fill);
                gc.fillRect(c * blockSize, (r - hiddenRows) * blockSize, blockSize, blockSize);

                gc.setStroke(Color.web("#222"));
                gc.strokeRect(c * blockSize, (r - hiddenRows) * blockSize, blockSize, blockSize);
            }

    }
        //draw the current falling piece
        if (piece != null) {
            Color colour = PALETTE[piece.type.colorId];
            for (int[] cell : piece.cells()) {
                int row = piece.row + cell[1];
                int col = piece.col + cell[0];

                boolean skipHidden = true;

                if ((target == gameCanvasLeft && externalLeft != null) ||
                        (target == gameCanvasRight && externalRight != null)) {
                    skipHidden = false;
                }

                if (row < hiddenRows || row >= board.h || col < 0 || col >= board.w) continue;
                if (skipHidden && row < hiddenRows) continue;

                // only the hidden part of the piece that is within visible bounds
                gc.setFill(colour);
                gc.fillRect(col * blockSize, (row - hiddenRows) * blockSize, blockSize, blockSize);

                gc.setStroke(Color.web("#222"));
                gc.strokeRect(col * blockSize, (row - hiddenRows) * blockSize, blockSize, blockSize);
            }
        }
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
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Connection Lost");
            alert.setHeaderText("⚠️ Connection to server was lost.");
            alert.setContentText((isLeft ? "Left" : "Right") + " player has lost connection.");

        });
    }

    /**
     * This method is applying operation result from External Player
     * @return
     */
    public void applyExternalMove(OpMove move, boolean isLeft) {
        Board targetBoard = isLeft ? boardLeft : boardRight;
        Tetromino targetPiece = isLeft ? currentPieceLeft : currentPieceRight;

        if (targetBoard == null || targetPiece == null) {
            System.out.println("⚠️ No board or piece for external move.");
            return;
        }

        System.out.println("🔄 Applying external move: X=" + move.opX() + ", Rotations=" + move.opRotate());

        // 1️⃣ 回転を適用
        Tetromino moved = targetPiece;
        for (int i = 0; i < move.opRotate(); i++) {
            Tetromino rotated = moved.rotated(1);
            if (targetBoard.canPlace(rotated)) {
                moved = rotated;
            }
        }

        // 2️⃣ 横移動を適用
        Tetromino shifted = moved.moved(0, move.opX());
        if (targetBoard.canPlace(shifted)) {
            moved = shifted;
        }

        // 3️⃣ ピースを更新（まだ固定しない！）
        if (isLeft) {
            currentPieceLeft = moved;
        } else {
            currentPieceRight = moved;
        }

        // 4️⃣ 描画更新
        renderBothBoards();
    }


    @Override
    public Parent getScreen() {
        return leftColumn != null ? leftColumn.getScene().getRoot() : null;
    }

    @Override
    public void setRoute(String path, Screen screen) {}
}
