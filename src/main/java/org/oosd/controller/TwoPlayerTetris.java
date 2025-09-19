package org.oosd.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.Parent;

import org.oosd.sound.soundEffects;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfig;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import org.oosd.ui.Frame;
import org.oosd.ui.Screen;

public class TwoPlayerTetris extends BorderPane implements Screen {

    TetrisConfig config = ConfigService.get();

    Board boardLeft = new Board(config.fieldWidth(), config.fieldHeight());
    Board boardRight = new Board(config.fieldWidth(), config.fieldHeight());
    private static final int hiddenRows = 4;

    private Tetromino currentPieceLeft;
    private Tetromino currentPieceRight;

    private int scoreLeftValue = 0;
    private int scoreRightValue = 0;
    private final Text scoreLeft = new Text("Score: 0");
    private final Text scoreRight = new Text("Score: 0");

    private VBox leftColumn;
    private VBox rightColumn;

    private long lastTickLeft = 0;
    private long lastTickRight = 0;
    private double dropIntervalMs = 600;

    private AnimationTimer timer;
    private Frame parentFrame;

    // tickleft/right moves the players pieces down by one tick
    private void tickLeft() {tick(boardLeft, true);}
    private void tickRight() {tick(boardRight, false);}

    // constructor to initialise the two boards, set up UI components and start the game
    public TwoPlayerTetris(Frame frame) {
        this.parentFrame = frame;

        boardLeft = new Board(config.fieldWidth(), config.fieldHeight());
        boardRight = new Board(config.fieldWidth(), config.fieldHeight());

        scoreLeft.setFont(Font.font(18));
        scoreRight.setFont(Font.font(18));

        leftColumn = new VBox(8, scoreLeft, renderBoard(boardLeft, null));
        leftColumn.setAlignment(Pos.CENTER);
        leftColumn.setPadding(new Insets(8));

        rightColumn = new VBox(8, scoreRight, renderBoard(boardRight, null));
        rightColumn.setAlignment(Pos.CENTER);
        rightColumn.setPadding(new Insets(8));

        HBox center = new HBox(20, leftColumn, rightColumn);
        center.setAlignment(Pos.CENTER);
        setCenter(center);

        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);

        initialSpawnBoth();
        renderAllBoards();
        setupTimer();

        Platform.runLater(this::requestFocus);
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

        if (isLeft) currentPieceLeft = piece;
        else currentPieceRight = piece;

        renderAllBoards();
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
    handle player controls -> WASD for player 1 & Arrows for player 2
    move Left (A/Left key)
    move Right (D/Right key)
    Rotate piece (W/Up key)
    soft drop (S/Down key)
     */
    private void handleKeyPress(KeyEvent ev) {
        switch (ev.getCode()) {
            //player 1 controls (WASD)
            case A -> movePiece(boardLeft, true, 0, -1);
            case D -> movePiece(boardLeft, true, 0, 1);
            case W -> rotatePiece(boardLeft, true);
            case S -> softDrop(boardLeft, true);

            //player 2 controls (Arrows)
            case LEFT -> movePiece(boardRight, false, 0, -1);
            case RIGHT -> movePiece(boardRight, false, 0, 1);
            case UP -> rotatePiece(boardRight, false);
            case DOWN -> softDrop(boardRight, false);

            default -> {}
        }
    }

    // attempt to move the current tetromino by the given row (dRow) and column (dCol)
    private void movePiece(Board board, boolean isLeft, int dRow, int dCol){
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;
        Tetromino moved = piece.moved(dRow,dCol);
        if (board.canPlace(moved)){
            // update position if the move is valid
            if (isLeft) currentPieceLeft = moved;
            else currentPieceRight = moved;
            renderAllBoards();
        }
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
        renderAllBoards();
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
        renderAllBoards();
    }

    // stop the timer and declare game over/the winner
    private void endGame(String msg) {
        timer.stop();
        Text t = new Text(msg);
        t.setFont(Font.font(28));
        setBottom(t);
    }

    //refresh both board displays
    private void renderAllBoards() {
        leftColumn.getChildren().set(1, renderBoard(boardLeft, currentPieceLeft));
        rightColumn.getChildren().set(1, renderBoard(boardRight, currentPieceRight));
    }

    // create a gridpane showing the board's state (locked pieces and the current falling piece)
    // skips the hidden rows (not visible, used to load pieces)
    private GridPane renderBoard(Board board, Tetromino piece) {
        GridPane gridPane = new GridPane();
        int[][] grid = board.snapshot();

        //draw locked cells (placed tetromino blocks)
        for (int r = hiddenRows; r < board.h; r++) {
            for (int c = 0; c < board.w; c++) {
                Rectangle rect = new Rectangle(25, 25);
                rect.setStroke(Color.web("#222"));
                int id = grid[r][c];
                rect.setFill(id == 0 ? Color.BLACK
                        : id >= 0 && id < PALETTE.length ? PALETTE[id] : Color.BLACK);
                gridPane.add(rect, c, r - hiddenRows);
            }
        }

        //draw the current falling piece
        if (piece != null) {
            Color curColour = PALETTE[piece.type.colorId];
            for (int[] cell : piece.cells()) {
                int row = piece.row + cell[1];
                int col = piece.col + cell[0];

                if (row < hiddenRows || row >= board.h || col < 0 || col >= board.w) continue;

                // only the hidden part of the piece that is within visible bounds
                Rectangle rect = new Rectangle(25, 25);
                rect.setFill(curColour);
                rect.setStroke(Color.web("#222"));
                gridPane.add(rect, col, row - hiddenRows);
            }
        }
        return gridPane;
    }

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


    @Override
    public Parent getScreen() { return this; }

    @Override
    public void setRoute(String path, Screen screen) {}
}
