package com.example.NiceRun.DTO;

import com.google.gson.annotations.SerializedName;

public class Track {
    @SerializedName("trackid")
    private String trackid;
    @SerializedName("filename")
    private String filename;
    @SerializedName("dist")
    private double dist;
    @SerializedName("trackimg")
    private String trackimg;
    @SerializedName("location")
    private String location;

    public String getTrackid() {
        return trackid;
    }

    public void setTrackid(String trackid) {
        this.trackid = trackid;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Track{" +
                "trackid='" + trackid + '\'' +
                ", filename='" + filename + '\'' +
                ", dist=" + dist +
                ", trackimg='" + trackimg + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
