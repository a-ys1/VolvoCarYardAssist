package com.example.yardassist.classes;

import com.google.firebase.firestore.GeoPoint;

public class Vehicle extends Product {
    private String color;
    private String regNumber;
    private String model;
    private String year;
    private int col;
    private int row;


    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
    public Vehicle() {
    }

    public Vehicle(String color, String regNumber, String model, String year) {
        this.color = color;
        this.regNumber = regNumber;
        this.setId1(regNumber);
        this.model = model;
        this.year = year;
    }

    public Vehicle(String color, String regNumber, String model, String year, double latitude, double longitude) {
        this.color = color;
        this.regNumber = regNumber;
        this.setId1(regNumber);
        this.model = model;
        this.year = year;
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public void setDate(String date) {
        this.year = year;
    }
}
