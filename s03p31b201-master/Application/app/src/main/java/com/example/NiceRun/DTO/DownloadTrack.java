package com.example.NiceRun.DTO;

import com.google.gson.annotations.SerializedName;

public class DownloadTrack {
    @SerializedName("email")
    private String email;
    @SerializedName("mytrackid")
    private int mytrackid;
    @SerializedName("type")
    private int type;

    public DownloadTrack(String email, int mytrackid, int type) {
        this.email = email;
        this.mytrackid = mytrackid;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMytrackid() {
        return mytrackid;
    }

    public void setMytrackid(int mytrackid) {
        this.mytrackid = mytrackid;
    }


    @Override
    public String toString() {
        return "SNS{" +
                "mytrackid=" + mytrackid +
                '}';
    }
}
