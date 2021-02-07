package com.example.yardassist.classes;

import androidx.collection.ArraySet;

import java.util.ArrayList;

public class Cell {

    public int mSize, mRowIndex, mColumnIndex;
    public boolean occupiedByVehicle, occupiedByOperator, isPath;
    public Coordinate topLeft, topRight, bottomLeft, bottomRight;
    public float mF, mG, mH;
    private ArrayList<Cell> mNeighbors;
    public Cell previous = null;
    public ArrayList<String> users;


    public Cell() {

    }
    public Cell(int rn, int cn, boolean ocByOp, boolean ocByVh) {
        mRowIndex = rn;
        mColumnIndex = cn;
        occupiedByOperator = ocByOp;
        occupiedByVehicle = ocByVh;
        mF = 0;
        mG = 0;
        mH = 0;
        mNeighbors = new ArrayList<Cell>();

    }

    public Cell(Coordinate topL, Coordinate topR, Coordinate botL, Coordinate botR) {
        topLeft = topL;
        topRight = topR;
        bottomLeft = botL;
        bottomRight = botR;
        mF = 0;
        mG = 0;
        mH = 0;
        mNeighbors = new ArrayList<Cell>();

    }

    public Cell(Coordinate topL, Coordinate topR, Coordinate botL, Coordinate botR, int row, int column) {
        topLeft = topL;
        topRight = topR;
        bottomLeft = botL;
        bottomRight = botR;
        mRowIndex = row;
        mColumnIndex = column;
        mF = 0;
        mG = 0;
        mH = 0;
        mNeighbors = new ArrayList<Cell>();
        users = new ArrayList<String>();

    }

    public Cell(int size, boolean occupiedByVehicle, boolean occupiedByOperator, Coordinate topL, Coordinate topR, Coordinate botL, Coordinate botR) {
        this.mSize = size;
        this.occupiedByVehicle = occupiedByVehicle;
        this.occupiedByOperator = occupiedByOperator;
        topLeft = topL;
        topRight = topR;
        bottomLeft = botL;
        bottomRight = botR;
        mF = 0;
        mG = 0;
        mH = 0;
        mNeighbors = new ArrayList<Cell>();
        users = new ArrayList<String>();
    }

    public void addNeighbor(docOfCells grid, Cell end) {
        mNeighbors = new ArrayList<Cell>();
        if (mRowIndex > 0){
            if(!grid.cells.get(mRowIndex - 1).cells.get(mColumnIndex).occupiedByVehicle || grid.cells.get(mRowIndex - 1).cells.get(mColumnIndex) == end)
            mNeighbors.add(grid.cells.get(mRowIndex - 1).cells.get(mColumnIndex));
        }

        if (mRowIndex < grid.rows - 1 ){
            if( !grid.cells.get(mRowIndex + 1).cells.get(mColumnIndex).occupiedByVehicle ||  grid.cells.get(mRowIndex + 1).cells.get(mColumnIndex) == end){
                Cell tmp = grid.cells.get(mRowIndex + 1).cells.get(mColumnIndex);
                mNeighbors.add(tmp);
            }

        }

        if(mColumnIndex > 0 ){
            if(!grid.cells.get(mRowIndex).cells.get(mColumnIndex-1).occupiedByVehicle || grid.cells.get(mRowIndex).cells.get(mColumnIndex-1) == end)
                mNeighbors.add(grid.cells.get(mRowIndex).cells.get(mColumnIndex-1));
        }

        if(mColumnIndex < grid.columns - 1 ){
            if(!grid.cells.get(mRowIndex).cells.get(mColumnIndex+1).occupiedByVehicle || grid.cells.get(mRowIndex).cells.get(mColumnIndex+1) == end)
                mNeighbors.add(grid.cells.get(mRowIndex).cells.get(mColumnIndex+1));
        }

    }

    public ArrayList<Cell> getNeighbors(){
        return mNeighbors;
    }

}