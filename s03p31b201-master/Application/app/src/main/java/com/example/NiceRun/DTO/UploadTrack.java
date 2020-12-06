package com.example.NiceRun.DTO;

import com.google.gson.annotations.SerializedName;

public class UploadTrack {

    @SerializedName("comment")
    private String comment;
    @SerializedName("mytrackid")
    private int mytrackid;

    public UploadTrack(String comment, int mytrackid) {
        this.comment = comment;
        this.mytrackid = mytrackid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getMytrackid() {
        return mytrackid;
    }

    public void setMytrackid(int mytrackid) {
        this.mytrackid = mytrackid;
    }
}
