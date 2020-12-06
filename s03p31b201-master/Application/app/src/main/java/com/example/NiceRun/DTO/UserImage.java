package com.example.NiceRun.DTO;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.File;

public class UserImage {
    @SerializedName("email")
    private String email;
    @SerializedName("profileimg")
    private Bitmap profileimg;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bitmap getProfileimg() {
        return profileimg;
    }

    public void setProfileimg(Bitmap profileimg) {
        this.profileimg = profileimg;
    }

    @Override
    public String toString() {
        return "Image{" +
                "email='" + email + '\'' +
                ", profileimg=" + profileimg +
                '}';
    }
}
