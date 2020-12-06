package com.example.NiceRun.DTO;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Mytrack {
    @SerializedName("mytrackid")
    private int mytrackid;
    @SerializedName("email")
    private String email;
    @SerializedName("trackid")
    private String trackid;
    @SerializedName("isdownload")
    private boolean isdownload;
    @SerializedName("sharedtime")
    private String sharedtime;
    @SerializedName("isupload")
    private boolean isupload;

    @SerializedName("trackinfos")
    private List<Trackinfo> trackinfos;

    public int getMytrackid() {return mytrackid;}

    public void setMytrackid(int mytrackid) {this.mytrackid = mytrackid;}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTrackid() {
        return trackid;
    }

    public void setTrackid(String trackid) {
        this.trackid = trackid;
    }


    public boolean isIsdownload() {
        return isdownload;
    }

    public String getSharedtime() {
        return sharedtime;
    }

    public void setSharedtime(String sharedtime) {
        this.sharedtime = sharedtime;
    }

    public boolean getIsupload() {
        return isupload;
    }

    public boolean isIsupload() {
        return isupload;
    }

    public void setTrackinfos(List<Trackinfo> trackinfos) {
        this.trackinfos = trackinfos;
    }

    @Override
    public String toString() {
        return "Mytrack{" +
                "mytrackid=" + mytrackid +
                ", email='" + email + '\'' +
                ", trackid='" + trackid + '\'' +
                ", isdownload=" + isdownload +
                ", sharetime='" + sharedtime + '\'' +
                ", isupload=" + isupload +
                '}';
    }
}
