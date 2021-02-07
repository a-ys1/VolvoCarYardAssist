package com.example.yardassist.classes;

import java.util.ArrayList;

public class rowOfCells {

    public ArrayList<Cell> cells;
    public int row;

    public rowOfCells() {
        cells = new ArrayList<Cell>();
    }

    public rowOfCells(ArrayList<Cell> cells) {
        this.cells = cells;
    }
}
