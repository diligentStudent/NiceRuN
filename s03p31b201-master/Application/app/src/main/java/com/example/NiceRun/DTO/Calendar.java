package com.example.NiceRun.DTO;

import com.google.gson.annotations.SerializedName;

public class Calendar {
    @SerializedName("calendarid")
    private int calendarid;
    @SerializedName("email")
    private String email;
    @SerializedName("issucceeded")
    private boolean issucceeded;
    @SerializedName("totaldist")
    private double totaldist;
    @SerializedName("goaldist")
    private int goaldist;
    @SerializedName("today")
    private String today;

    public int getCalendarid() {
        return calendarid;
    }

    public void setCalendarid(int calendarid) {
        this.calendarid = calendarid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIssucceeded() {
        return issucceeded;
    }

    public void setIssucceeded(boolean issucceeded) {
        this.issucceeded = issucceeded;
    }

    public double getTotaldist() {
        return totaldist;
    }

    public void setTotaldist(double totaldist) {
        this.totaldist = totaldist;
    }

    public int getGoaldist() {
        return goaldist;
    }

    public void setGoaldist(int goaldist) {
        this.goaldist = goaldist;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    @Override
    public String toString() {
        return "Calendar{" +
                "calendarid=" + calendarid +
                ", email='" + email + '\'' +
                ", issucceeded=" + issucceeded +
                ", totaldist=" + totaldist +
                ", goaldist=" + goaldist +
                ", today='" + today + '\'' +
                '}';
    }
}
