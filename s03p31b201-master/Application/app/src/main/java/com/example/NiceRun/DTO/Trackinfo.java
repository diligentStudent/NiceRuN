package com.example.NiceRun.DTO;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Trackinfo {
    @SerializedName("trackinfoid")
    private int trackinfoid;
    @SerializedName("mytrackid")
    private int mytrackid;
    @SerializedName("createat")
    private String createat;
    @SerializedName("runningtime")
    private int runningtime;
    @SerializedName("speed")
    private double speed;
    @SerializedName("kcal")
    private int kcal;
    @SerializedName("dist")
    private double dist;
    @SerializedName("trackimg")
    private String trackimg;
    @SerializedName("mytrack")
    private Mytrack mytrack;

    public int getTrackinfoid() {
        return trackinfoid;
    }

    public void setTrackinfoid(int trackinfoid) {
        this.trackinfoid = trackinfoid;
    }

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

    public int getRunningtime() {
        return runningtime;
    }

    public void setRunningtime(int runningtime) {
        this.runningtime = runningtime;
    }


    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getKcal() {
        return kcal;
    }

    public void setKcal(int kcal) {
        this.kcal = kcal;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public String getTrackimg() {
        return trackimg;
    }

    public void setTrackimg(String trackimg) {
        this.trackimg = trackimg;
    }

    public Mytrack getMytrack() {
        return mytrack;
    }

    public void setMytrack(Mytrack mytrack) {
        this.mytrack = mytrack;
    }

    @Override
    public String toString() {
        return "Trackinfo{" +
                "trackinfoid=" + trackinfoid +
                ", mytrackid=" + mytrackid +
                ", createat='" + createat + '\'' +
                ", runningtime=" + runningtime +
                ", speed=" + speed +
                ", kcal=" + kcal +
                ", dist=" + dist +
                ", trackimg='" + trackimg + '\'' +
                ", mytrack=" + mytrack +
                '}';
    }
}
