package com.example.NiceRun.DTO;

import com.google.gson.annotations.SerializedName;

public class Sns {
    @SerializedName("mytrackid")
    private int mytrackid;
    @SerializedName("createat")
    private String createat;
    @SerializedName("comment")
    private String comment;

    public int getMytrackid() {
        return mytrackid;
    }

    public void setMytrackid(int mytrackid) {
        this.mytrackid = mytrackid;
    }

    public String getCreateat() {
        return createat;
    }

    public void setCreateat(String createat) {
        this.createat = createat;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "SNS{" +
                "mytrackid=" + mytrackid +
                ", createat='" + createat + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
