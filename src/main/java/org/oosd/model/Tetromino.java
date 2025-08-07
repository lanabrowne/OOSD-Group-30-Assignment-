package org.oosd.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

//This class is just setting the method of tetromino type, color, and shape
public class Tetromino {
    private TetrominoType type;
    private Color color;
    //This game is used 2d array so that integer should be [][]
    private int[][] shape;

    //Initialize the type, color and shape for creating new instance
    // by creating constructor
    //Set parameters by type, color and 2d shape
    public Tetromino(TetrominoType type, Color color, int[][] shape)
    {
        this.type = type;
        this.color = color;
        this.shape = shape;
    }

    //Now create the method to built new shape of tetromino
    //use Graphic Context for set color, set startX for determine X
    // set StartY to determine Y
    /**
     *
     * @param gc --> Graphic Context (Determine the Color)
     * @param startX --> Set start position (x)
     * @param startY --> Set start position (y)
     */
    public void draw(GraphicsContext gc, int startX, int startY)
    {
        //Set color option in here
        gc.setFill(color);
        //Increment row count by 1
        for (int row = 0; row < shape.length; row++)
        {
            //Increment col count by 1
            for (int col = 0; col < shape[row].length; col++)
            {
                //Then check there is blocks in the current position
                //ex 1 -> Block exists, 0 -> none
                //if 1, draw the block in there
                if(shape[row][col] == 1)
                {
                    //Then I set Game screen size to 300, 600 so that set sell size to 30
                    gc.fillRect(startX + col * 30, startY + row * 30, 30,30);
                }
            }
        }
    }
}
