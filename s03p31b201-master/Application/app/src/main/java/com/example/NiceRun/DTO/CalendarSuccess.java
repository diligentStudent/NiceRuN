package com.example.NiceRun.DTO;

import com.google.gson.annotations.SerializedName;

public class CalendarSuccess {
    @SerializedName("date")
    private String date;
    @SerializedName("flag")
    private int flag;

    public String getDate() {return date;}

    public void setDate(String date) {this.date = date;}

    public int getFlag() {return flag;}

    public void setFlag(int flag) {this.flag = flag;}
}
