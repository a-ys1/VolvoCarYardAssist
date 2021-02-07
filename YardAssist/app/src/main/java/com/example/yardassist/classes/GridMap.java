package com.example.yardassist.classes;

public class GridMap {
    private int mRows, mColumns;
    private Cell[][] mGrid;

    public GridMap() {

    }

    public GridMap(Cell[][] grid) {
        mGrid = grid;
    }
}
