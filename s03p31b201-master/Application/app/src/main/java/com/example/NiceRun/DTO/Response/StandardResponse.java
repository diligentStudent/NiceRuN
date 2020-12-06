package com.example.NiceRun.DTO.Response;

import com.example.NiceRun.DTO.User;
import com.google.gson.annotations.SerializedName;

public class StandardResponse {
    @SerializedName("object")
    private Object object;
    @SerializedName("data")
    public String data;
    @SerializedName("status")
    public boolean status;


    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BasicResponse{" +
                "object=" + object +
                ", data='" + data + '\'' +
                ", status=" + status +
                '}';
    }

}