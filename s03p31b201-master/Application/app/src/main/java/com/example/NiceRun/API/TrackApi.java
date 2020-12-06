package com.example.NiceRun.API;

import java.io.File;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TrackApi {

    @POST("track/gettrackfile")
    Call<File> gettrackfile(@Query("trackid") String trackid);

}
