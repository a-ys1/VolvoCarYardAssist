package com.example.yardassist.classes;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.IgnoreExtraProperties;

public class User {

    private String id, firstName, surname, email, userType;


    public User(String id, String email, String firstName, String surname, String userType){

        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.surname = surname;
        this.userType = userType;
    }

    public String getId() {
        return id;
    }

    public void setId1(String id1) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

}
