package com.example.NiceRun.API;

import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.DTO.UploadTrack;
import com.example.NiceRun.DTO.DownloadTrack;

import retrofit2.Call;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CalendarApi {
    /* fixme POST -> GET
     */
    @GET("calendar/getdaytrack")
    Call<BasicResponse> getdaytrack(@Query("day") String day, @Query("email") String email);

    @POST("calendar/getmonthtrack")
    Call<BasicResponse> getmonthtrack(@Query("date") String date, @Query("email") String email);

    @POST("sns/upload")
    Call<BasicResponse> postUploadMyTrack(@Body UploadTrack uploadTrack);
    //notice type 0
    @POST("sns/downloadtrack")
    Call<BasicResponse> postDownloadMyTrack(@Body DownloadTrack downloadTrack);
}
