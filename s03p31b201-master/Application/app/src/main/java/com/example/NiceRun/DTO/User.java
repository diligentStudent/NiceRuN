package com.example.NiceRun.DTO;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

public class User {
    //1.
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    //2.
    @SerializedName("lastname")
    private String lastname;
    @SerializedName("firstname")
    private String firstname;
    @SerializedName("height")
    private int height;
    @SerializedName("weight")
    private int weight;
    @SerializedName("gender")
    private boolean gender;
    //3.
    @SerializedName("goaldist")
    private int goaldist;
    //4.
    @SerializedName("location")
    private String location;
    @SerializedName("birthday")
    private String birthday;

    @SerializedName("profileimg")
    private String profileimg;
//     private Bitmap profileimg;

    public String getProfileimg() {
        return profileimg;
    }

    public void setProfileimg(String profileimg) {
        this.profileimg = profileimg;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public int getGoaldist() {
        return goaldist;
    }

    public void setGoaldist(int goaldist) {
        this.goaldist = goaldist;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", gender=" + gender +
                ", goaldist=" + goaldist +
                ", birthday='" + birthday + '\'' +
                ", location='" + location + '\'' +
                ", profileimg='" + profileimg + '\'' +
                '}';
    }
}
