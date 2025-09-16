package org.oosd.ui;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import javafx.scene.Parent;

public class TwoPlayerTetris extends BorderPane implements Screen {

    private final Board boardLeft;
    private final Board boardRight;

    private Tetromino currentPieceLeft;
    private Tetromino currentPieceRight;
    private Tetromino nextPiece;

    private final Text scoreLeft = new Text("Score: 0");
    private final Text scoreRight = new Text("Score: 0");

    private long lastTickLeft = 0;
    private long lastTickRight = 0;
    private double dropIntervalMs = 600;

    private AnimationTimer timer;
    private Frame parentFrame;

    public TwoPlayerTetris(Frame frame) {
        this.parentFrame = frame;

        boardLeft = new Board(10, 20);
        boardRight = new Board(10, 20);

        scoreLeft.setFont(Font.font(18));
        scoreRight.setFont(Font.font(18));

        VBox leftColumn = new VBox(8, scoreLeft, renderBoard(boardLeft, null));
        leftColumn.setAlignment(Pos.CENTER);
        leftColumn.setPadding(new Insets(8));

        VBox rightColumn = new VBox(8, scoreRight, renderBoard(boardRight, null));
        rightColumn.setAlignment(Pos.CENTER);
        rightColumn.setPadding(new Insets(8));

        HBox center = new HBox(20, leftColumn, rightColumn);
        center.setAlignment(Pos.CENTER);
        setCenter(center);

        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);

        spawnNewPiece();
        renderAllBoards();
        setupTimer();
    }

    private void setupTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTickLeft == 0) lastTickLeft = now;
                if (lastTickRight == 0) lastTickRight = now;

                long elapsedLeft = (now - lastTickLeft) / 1_000_000;
                long elapsedRight = (now - lastTickRight) / 1_000_000;

                if (elapsedLeft >= dropIntervalMs) {
                    tick(boardLeft, currentPieceLeft);
                    lastTickLeft = now;
                }
                if (elapsedRight >= dropIntervalMs) {
                    tick(boardRight, currentPieceRight);
                    lastTickRight = now;
                }
            }
        };
        timer.start();
    }

    private void tick(Board board, Tetromino piece) {
        boolean landed = !board.canPlace(piece.moved(1, 0));

        if (!landed) piece.row++;
        else {
            board.lock(piece);
            int linesCleared = clearLines(board);
            // Optional: send garbage to other board if desired
            if (board == boardLeft) {
                if (!board.canPlace(currentPieceLeft)) endGame("Player 2 Wins!");
            } else {
                if (!board.canPlace(currentPieceRight)) endGame("Player 1 Wins!");
            }
            spawnNewPiece(); // spawn new synchronized piece
        }

        updateScores();
        renderAllBoards();
    }

    private void spawnNewPiece() {
        if (nextPiece == null) nextPiece = Tetromino.random(boardLeft.w);

        currentPieceLeft = nextPiece.copy();
        currentPieceRight = nextPiece.copy();

        currentPieceLeft.row = 0;
        currentPieceLeft.col = boardLeft.w / 2 - 1;

        currentPieceRight.row = 0;
        currentPieceRight.col = boardRight.w / 2 - 1;

        nextPiece = Tetromino.random(boardLeft.w);
    }

    private void handleKeyPress(KeyEvent ev) {
        KeyCode code = ev.getCode();

        // Player 1 (AWDS)
        if (code == KeyCode.A) movePiece(boardLeft, currentPieceLeft, 0, -1);
        else if (code == KeyCode.D) movePiece(boardLeft, currentPieceLeft, 0, 1);
        else if (code == KeyCode.W) rotatePiece(boardLeft, currentPieceLeft);
        else if (code == KeyCode.S) tick(boardLeft, currentPieceLeft);

        // Player 2 (Arrow Keys)
        else if (code == KeyCode.LEFT) movePiece(boardRight, currentPieceRight, 0, -1);
        else if (code == KeyCode.RIGHT) movePiece(boardRight, currentPieceRight, 0, 1);
        else if (code == KeyCode.UP) rotatePiece(boardRight, currentPieceRight);
        else if (code == KeyCode.DOWN) tick(boardRight, currentPieceRight);

        renderAllBoards();
    }

    private void movePiece(Board board, Tetromino piece, int dRow, int dCol) {
        Tetromino moved = piece.moved(dRow, dCol);
        if (board.canPlace(moved)) {
            if (board == boardLeft) currentPieceLeft = moved;
            else currentPieceRight = moved;
        }
    }

    private void rotatePiece(Board board, Tetromino piece) {
        Tetromino rotated = piece.rotated(1);
        if (board.canPlace(rotated)) {
            if (board == boardLeft) currentPieceLeft = rotated;
            else currentPieceRight = rotated;
        }
    }

    private int clearLines(Board board) {
        int before = board.snapshot().length;
        board.clearFullLines();
        int after = board.snapshot().length;
        return before - after;
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
        setLeft(renderBoard(boardLeft, currentPieceLeft));
        setRight(renderBoard(boardRight, currentPieceRight));
    }

    private GridPane renderBoard(Board board, Tetromino piece) {
        GridPane gridPane = new GridPane();
        int[][] grid = board.snapshot();
        for (int r = 0; r < board.h; r++) {
            for (int c = 0; c < board.w; c++) {
                Rectangle rect = new Rectangle(25, 25);
                rect.setStroke(Color.GRAY);
                rect.setFill(grid[r][c] == 0 ? Color.BLACK : Color.BLUE);
                gridPane.add(rect, c, r);
            }
        }

        if (piece != null) {
            for (int[] cell : piece.cells()) {
                int row = piece.row + cell[1];
                int col = piece.col + cell[0];
                if (row >= 0 && row < board.h && col >= 0 && col < board.w) {
                    Rectangle rect = new Rectangle(25, 25);
                    rect.setFill(Color.RED);
                    rect.setStroke(Color.GRAY);
                    gridPane.add(rect, col, row);
                }
            }
        }
        return gridPane;
    }

    @Override
    public Parent getScreen() { return this; }

    @Override
    public void setRoute(String path, Screen screen) {}
}
