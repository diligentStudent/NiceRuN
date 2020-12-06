package com.example.NiceRun.lib;

import android.os.Handler;
import android.util.Log;


import com.example.NiceRun.API.CalendarApi;
import com.example.NiceRun.DTO.DownloadTrack;
import com.example.NiceRun.DTO.Response.BasicResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 즐겨찾기 관련 라이브러리
 */
public class DownloadLib {
    public String baseUrl = "https://k3b201.p.ssafy.io/api/";
    private CalendarApi calendarApi;


    public final String TAG = DownloadLib.class.getSimpleName();
    private volatile static DownloadLib instance;

    public static DownloadLib getInstance() {
        if (instance == null) {
            synchronized (DownloadLib.class) {
                if (instance == null) {
                    instance = new DownloadLib();
                }
            }
        }
        return instance;
    }

    public void DownloadTrack(final Handler handler, DownloadTrack downloadTrack, int position) {
        initCalendarApI(baseUrl);
        Call<BasicResponse> postCall = calendarApi.postDownloadMyTrack(downloadTrack);

        postCall.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "downloadTrack " + response);
                    handler.sendEmptyMessage(position);
                } else { // 등록 실패
                    Log.d(TAG, "response error " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "no internet connectivity");
            }
        });
    }
    public void initCalendarApI(String baseUrl) {

        Log.d(TAG, "calendarApi : " + baseUrl);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()) // addConverterFactory로 gson converter를 생성한다. gson은 json을 자바 클래스로 바꾸는데 사용
                .build();

        calendarApi = retrofit.create(CalendarApi.class);
    }

}
