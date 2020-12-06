package com.example.NiceRun.API;

import com.example.NiceRun.DTO.Response.BasicResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SavedTrackAPI {
    @POST("track/showtrack")
    Call<BasicResponse> showtrack(@Query("email") String email);
}
