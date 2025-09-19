package org.oosd.model;

public class Move {
    public final int rotation; // Number of 90 degree rotations
    public final int col;
    public Move(int rotation, int col){
        this.rotation = rotation;
        this.col= col;
    }

}
