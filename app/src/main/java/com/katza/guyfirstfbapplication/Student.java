package com.katza.guyfirstfbapplication;

import androidx.annotation.NonNull;

public class Student {
    private String fullName, id;
    private int grade;

    public Student(){}

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;


    }

    @Override
    public String toString() {
        return "\n********** פרטי תלמיד **********\n"
                + "שם מלא: " + this.getFullName()
                + "\nתעודת זהות: " + this.getId()
                + "\nציון: " + this.getGrade()
                + "\n*******************************\n";
    }


}


