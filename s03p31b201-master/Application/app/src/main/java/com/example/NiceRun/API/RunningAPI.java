package com.example.NiceRun.API;

import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.DTO.Response.StandardResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RunningAPI {

    @Multipart
    @POST("track/savetrack")
    Call<BasicResponse> savetrack(
            @Query("createat") String createat,
            @Query("dist") Double dist,
            @Query("email") String email,
            @Part MultipartBody.Part filename,
            @Query("kcal") int kcal,
            @Query("location") String location,
            @Query("runningtime") int runningtime,
            @Query("snsdownload") boolean snsdownload,
            @Query("speed") double speed,
            @Part MultipartBody.Part trackimg
    );
}
