package org.oosd.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoType;

public class GameController {
    @FXML
    private Canvas gameCanvas;

    @FXML
    private VBox frameCanvas;

    @FXML
    private Button btnBack;
    @FXML
    private Button btnRight;
    @FXML
    private Button btnLeft;
    @FXML
    private Button btnRotate;

    @FXML
    private void moveLeft(ActionEvent event)
    {
        //Write Method of moving tetromino to left in here.
    }

    @FXML
    private void moveRight(ActionEvent event)
    {
        //Write Method of moving tetromino to right in here.
    }
    @FXML
    private void rotateBlock(ActionEvent event)
    {
        //Write Method of rotating tetromino to 90 degrees in here.
    }

    private GraphicsContext gc;

    // Create initialize game method to start game
    // This will be called when GameScreen FXML is read
    @FXML
    public void initialize()
    {

        //Collect Graphics Context
        gc = gameCanvas.getGraphicsContext2D();

        drawInitialScreen();
    }

    //Create method which is initial condition of Tetris game screen
    private void drawInitialScreen()
    {
       gc.setFill(Color.BLACK);
       gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

       //put skyblue colour into outside frame
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0,0,frameCanvas.getWidth(), frameCanvas.getHeight());

       generateTetromino();

        gc.setFill(Color.WHITE);
        gc.fillText("Game Start!", 120, 100);
    }

    //Create generate tetromino method based on the model setting
    public void generateTetromino()
    {
        //Access to Tetromino definition class which is Tetromino
        //Then design of each shape of tetromino in this controller


        //Design of L
        Tetromino tL = new Tetromino(
                //TYPE Ln
                TetrominoType.L,
                Color.BLUE,
                //int [][] => int [y][x]
                new int[][]{
                        {1,0},
                        {1,0},
                        {1,1}
                }

        );


        //Design of J
        Tetromino tJ = new Tetromino(
                //TYPE J
                TetrominoType.J,
                Color.ORANGE,
                //int [][] => int [y][x]
                new int[][]{
                        {0,1},
                        {0,1},
                        {1,1}
                }

        );

        //T
        Tetromino tT = new Tetromino(
                //TYPE T
                TetrominoType.T,
                Color.GREEN,
                //int [][] => int [y][x]
                new int[][]{
                        {1,1,1},
                        {0,1,0},
                        {0,1,0},

                }

        );

        //O
        Tetromino tO = new Tetromino(
                //TYPE O
                TetrominoType.O,
                Color.RED,
                //int [][] => int [y][x]
                new int[][]{
                        {1,1},
                        {1,1}
                }

        );

        //I
        Tetromino tI = new Tetromino(
                //TYPE I
                TetrominoType.I,
                Color.YELLOW,
                //int [][] => int [y][x]
                new int[][]{
                        {1},
                        {1},
                        {1},
                        {1}
                }

        );

        //S
        Tetromino tS = new Tetromino(
                //TYPE S
                TetrominoType.S,
                Color.PURPLE,
                //int [][] => int [y][x]
                new int[][]{
                        {0,1,1},
                        {1,1,0}

                }

        );

        //Z
        Tetromino tZ = new Tetromino(
                //TYPE Z
                TetrominoType.Z,
                Color.GRAY,
                //int [][] => int [y][x]
                new int[][]{
                        {1,1,0},
                        {0,1,1}

                }

        );




        tL.draw(gc, 0,510 );
        tJ.draw(gc, 90,510 );
        tT.draw(gc, 150,510 );
        tO.draw(gc, 60,510 );
        tI.draw(gc, 240,480);
        tS.draw(gc, 0,450 );
        tZ.draw(gc, 150,450 );


    }


}
