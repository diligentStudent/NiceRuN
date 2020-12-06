package com.example.NiceRun.API;

import com.example.NiceRun.DTO.DownloadTrack;
import com.example.NiceRun.DTO.Response.BasicResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RecommandTrackAPI {
    @GET("track/recomtrack")
    Call<BasicResponse> getRecommandtrack(@Query("email") String email, @Query("location") String location);

    //notice type 1
    @POST("sns/downloadtrack")
    Call<BasicResponse> postDownloadMyTrack(@Body DownloadTrack downloadTrack);
}
