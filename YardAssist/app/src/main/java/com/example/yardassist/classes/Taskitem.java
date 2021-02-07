package com.example.yardassist.classes;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;

public class Taskitem implements Serializable {
    private String id;
    //private double latitude;
    //private double longitude;
    private String time;
    private String comment;
    private int col, row;

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



    public Taskitem() {
    }

    public Taskitem(String id, String comment){
        this.id = id;
        this.comment = comment;
    }

    public Taskitem(String id, String time, String comment) {
        this.id = id;
        this.time = time;
        this.comment = comment;
    }
    public Taskitem(String id, String timestamp, String comment, int col, int row) {
        this.id = id;
        this.time = timestamp;
        this.comment = comment;
        this.col = col;
        this.row = row;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /* public double getLatitude() {
        return latitude;
    } */

   /* public void setLatitude(double latitude) {
        this.latitude = latitude;
    } */

    /*public double getLongitude() {
        return longitude;
    } *

   /* public void setLongitude(double longitude) {
        this.longitude = longitude;
    } */

    public String getComment(){
        return comment;
    }

    public String getTime(){
        return time;
    }

    public void setComment(){
        this.comment = comment;
    }

}
