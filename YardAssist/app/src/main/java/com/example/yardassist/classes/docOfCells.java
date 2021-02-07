package com.example.yardassist.classes;

import java.util.ArrayList;

public class docOfCells implements Cloneable {
    public int rows, columns;
    public ArrayList<rowOfCells> cells;

    @Override
    public docOfCells clone() throws CloneNotSupportedException {
        return (docOfCells) super.clone();
    }

    public docOfCells() {
        cells = new ArrayList<rowOfCells>();
    }

    public docOfCells(ArrayList<rowOfCells> cells) {
        this.cells = cells;
    }


}
