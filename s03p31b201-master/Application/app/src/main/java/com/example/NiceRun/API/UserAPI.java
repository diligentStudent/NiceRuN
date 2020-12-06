package com.example.NiceRun.API;

import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.DTO.Response.StandardResponse;
import com.example.NiceRun.DTO.User;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UserAPI {

    @POST("member/login")
    Call<BasicResponse> signin(@Query("email") String email, @Query("password") String password);

    @POST("member/signup")
    Call<BasicResponse> post_signup(@Body User user);

    @Multipart
    @POST("member/changeprofileimg")
    Call<BasicResponse> changeprofileimg(@Query("email") String email, @Part MultipartBody.Part file);

    @POST("member/emailvalidate")
    Call<StandardResponse> emailvalidate(@Query("email") String email);

    @POST("member/updatemyprofile")
    Call<BasicResponse> updatemyprofile(@Body User user);

    @POST("member/findpassword")
    Call<BasicResponse> findpassword(@Query("email") String email);

    @POST("member/changepassword")
    Call<BasicResponse> changepassword(@Query("email") String email, @Query("password") String password);

    @POST("member/total")
    Call<BasicResponse> total(@Query("email") String email);

}
