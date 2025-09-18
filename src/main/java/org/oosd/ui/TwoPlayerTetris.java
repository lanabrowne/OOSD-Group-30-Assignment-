package org.oosd.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.Parent;

import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfig;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;

public class TwoPlayerTetris extends BorderPane implements Screen {

    TetrisConfig config = ConfigService.get();
    int gameLevel = config.gameLevel();
    boolean musicON = config.music();
    boolean sfxON = config.sfx();
    boolean aiPlay = config.aiPlay();
    boolean extendMode = config.extendMode();

    Board boardLeft = new Board(config.fieldWidth(), config.fieldHeight());;
    Board boardRight = new Board(config.fieldWidth(), config.fieldHeight());;

    private Tetromino currentPieceLeft;
    private Tetromino currentPieceRight;

    private Tetromino nextPieceLeft;
    private Tetromino nextPieceRight;

    private final Text scoreLeft = new Text("Score: 0");
    private final Text scoreRight = new Text("Score: 0");

    private VBox leftColumn;
    private VBox rightColumn;

    private long lastTickLeft = 0;
    private long lastTickRight = 0;
    private double dropIntervalMs = 600;

    private AnimationTimer timer;
    private Frame parentFrame;

    private void tickLeft() {tick(boardLeft, true);}
    private void tickRight() {tick(boardRight, false);}


    public TwoPlayerTetris(Frame frame) {
        this.parentFrame = frame;

        boardLeft = new Board(10, 20);
        boardRight = new Board(10, 20);

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

    private void tick(Board board, boolean isLeft) {
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;

        Tetromino down = piece.moved(1,0);
        boolean landed = !board.canPlace(down);

        if (!landed){
            piece = down;
        } else {
            board.lock(piece);
            int linesCleared = board.clearFullLines();
            // Optional: send garbage to other board if desired

            piece = spawnFor(board);

            if (!board.canPlace(piece)) {
                endGame(isLeft ? "Player 2 Wins!" : "Player 1 Wins!");
                return;
            }
        }

        if (isLeft) currentPieceLeft = piece;
        else currentPieceRight = piece;

        updateScores();
        renderAllBoards();
    }

   private void initialSpawnBoth(){
        currentPieceLeft = spawnFor(boardLeft);
        currentPieceRight = spawnFor(boardRight);
   }

   private Tetromino spawnFor(Board board){
        Tetromino t = Tetromino.random(board.w);
        t.row = 0;
        t.col = board.w/2-1; //so the tetromino spawns in the centre of the board
        return t;
   }

    private void handleKeyPress(KeyEvent ev) {
        switch (ev.getCode()) {
            //player 1 (WASD)
            case A -> movePiece(boardLeft, true, 0, -1);
            case D -> movePiece(boardLeft, true, 0, 1);
            case W -> rotatePiece(boardLeft, true);
            case S -> softDrop(boardLeft, true);

            //player 2 (Arrows)
            case LEFT -> movePiece(boardRight, false, 0, -1);
            case RIGHT -> movePiece(boardRight, false, 0, 1);
            case UP -> rotatePiece(boardRight, false);
            case DOWN -> softDrop(boardRight, false);

            default -> {}
        }
    }

    private void movePiece(Board board, boolean isLeft, int dRow, int dCol){
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;
        Tetromino moved = piece.moved(dRow,dCol);
        if (board.canPlace(moved)){
            if (isLeft) currentPieceLeft = moved; else currentPieceRight = moved;
            renderAllBoards();
        }
    }

    private void softDrop(Board board, boolean isLeft){
        Tetromino p = isLeft ? currentPieceLeft : currentPieceRight;
        if (p == null) return;
        Tetromino down = p.moved(1,0);
        if (board.canPlace(down)) {
            p = down;
        } else {
            board.lock(p);
            int linesCleared = board.clearFullLines();
            p = spawnFor(board);
            if (!board.canPlace(p)) {endGame(isLeft ? "Player 2 Wins!" : "Player 1 Wins!");
            }
            if (isLeft) currentPieceLeft = p;
            else currentPieceRight = p;
            renderAllBoards();
        }
    }

    private void rotatePiece(Board board, boolean isLeft) {
        Tetromino piece = isLeft ? currentPieceLeft : currentPieceRight;
        if (piece == null) return;

        Tetromino rotate = piece.rotated(1);
        for (int kick : new int[]{0, -1, 1}) {
            Tetromino t = new Tetromino(rotate.type, rotate.rotation, rotate.row, rotate.col + kick);
            if (board.canPlace(t)) {
                if (isLeft) currentPieceLeft = t;
                else currentPieceRight = t;
                break;
            }
        }
        renderAllBoards();
    }

    private int clearLines(Board board) {
        int[][] grid = board.snapshot();
        int full = 0;
        for (int r = 0; r < board.w;  r++) {
            boolean all = true;
            for (int c = 0; c < board.w; c++) {
                if (grid[r][c] == 0) {
                    all = false;
                    break;
                }
            }
            if (all) full++;
        }
        board.clearFullLines();
        return full;
    }

    private void updateScores() {
        scoreLeft.setText("Score: " + countBlocks(boardLeft));
        scoreRight.setText("Score: " + countBlocks(boardRight));
    }

    private int countBlocks(Board board) {
        int count = 0;
        int[][] grid = board.snapshot();
        for (int r = 0; r < board.h; r++)
            for (int c = 0; c < board.w; c++)
                if (grid[r][c] != 0) count++;
        return count;
    }

    private void endGame(String msg) {
        timer.stop();
        Text t = new Text(msg);
        t.setFont(Font.font(28));
        setBottom(t);
    }

    private void renderAllBoards() {
        leftColumn.getChildren().set(1, renderBoard(boardLeft, currentPieceLeft));
        rightColumn.getChildren().set(1, renderBoard(boardRight, currentPieceRight));
    }

    private GridPane renderBoard(Board board, Tetromino piece) {
        GridPane gridPane = new GridPane();
        int[][] grid = board.snapshot();

        //draw locked cells
        for (int r = 0; r < board.h; r++) {
            for (int c = 0; c < board.w; c++) {
                Rectangle rect = new Rectangle(25, 25);
                rect.setStroke(Color.GRAY);
                int id = grid[r][c];
                rect.setFill(id == 0 ? Color.BLACK : PALETTE[id]);
                gridPane.add(rect, c, r);
            }
        }

        if (piece != null) {
            Color curColour = PALETTE[piece.type.colorId];
            for (int[] cell : piece.cells()) {
                int row = piece.row + cell[1];
                int col = piece.col + cell[0];
                if (row >= 0 && row < board.h && col >= 0 && col < board.w) {
                    Rectangle rect = new Rectangle(25, 25);
                    rect.setFill(curColour);
                    rect.setStroke(Color.GRAY);
                    gridPane.add(rect, col, row);
                }
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
