package com.example.NiceRun.main.slideshow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.NiceRun.API.UserAPI;
import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.DTO.User;
import com.example.NiceRun.LoginActivity;
import com.example.NiceRun.MainActivity;
import com.example.NiceRun.Util.PreferenceManager;
import com.example.NiceRun.R;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class SlideshowFragment extends Fragment {

    MainActivity activity;
    private final String TAG = getClass().getSimpleName();

    // API 요청
    public String baseUrl = "https://k3b201.p.ssafy.io/api/";
    private UserAPI userAPI;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //프래그먼트 메인을 인플레이트해주고 컨테이너에 붙여달라는 뜻임
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_slideshow, container, false);

        ImageView image = (ImageView) rootView.findViewById(R.id.level_img);
        TextView dist = (TextView) rootView.findViewById(R.id.level_dist);
        ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.level_progress);
        TextView remain = (TextView) rootView.findViewById(R.id.level_remain);
        TextView count = (TextView) rootView.findViewById(R.id.level_count);

        TextView bronzeText = (TextView) rootView.findViewById(R.id.level_bronze);
        TextView silverText = (TextView) rootView.findViewById(R.id.level_silver);
        TextView goldText = (TextView) rootView.findViewById(R.id.level_gold);
        TextView niceText = (TextView) rootView.findViewById(R.id.level_nice);

        Double[] myDist = {0.0};
        String[] myCount = {"0"};
        int[] myRemain = {10};
        String[] myGrade = {""};
        int[] myProgress = {10};
        String[] myNext = {"실버"};

        final Double bronze = 0.0;
        final Double silver = 10.0;
        final Double gold = 50.0;
        final Double nice = 100.0;

        String email = PreferenceManager.getString(getActivity(), "email");
        initMyAPI(baseUrl);

        Call<BasicResponse> postCall = userAPI.total(email);

        postCall.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    BasicResponse data = response.body();
                    if(data.getData().equals("fail")) {
                        Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"운동기록이 없습니다.",Toast.LENGTH_SHORT);
                        myToast.show();

                        dist.setText(myDist[0] + "Km");
                        count.setText(myCount[0]);
                        remain.setText( myNext[0] + " 레벨까지 " + myRemain[0] + "Km 남았습니다");
                        progress.setMax(myProgress[0]);
                        progress.setProgress(myProgress[0] - myRemain[0]);
                        bronzeText.setTextColor(getResources().getColor(R.color.nicerun_blue));
                        image.setImageResource(R.drawable.bronze);

                    } else {

                        myDist[0] = Double.parseDouble(data.getObject().getTotal());
                        dist.setText(myDist[0] + "Km");
                        myCount[0] = data.getObject().getCount();
                        count.setText(myCount[0]);

                        if(myDist[0] < nice) {
                            if (myDist[0] < silver) {
                                myProgress[0] = (int) (silver - bronze);
                                myGrade[0] = "bronze";
                                myRemain[0] = (int) (silver - myDist[0]);
                                myNext[0] = "실버";
                                bronzeText.setTextColor(getResources().getColor(R.color.nicerun_blue));
                                image.setImageResource(R.drawable.bronze);
                            } else if (myDist[0] < gold) {
                                myProgress[0] = (int) (gold - silver);
                                myGrade[0] = "silver";
                                myRemain[0] = (int) (gold - myDist[0]);
                                myNext[0] = "골드";
                                silverText.setTextColor(getResources().getColor(R.color.nicerun_blue));
                                image.setImageResource(R.drawable.silver);
                            } else if (myDist[0] < nice) {
                                myProgress[0] = (int) (nice - gold);
                                myGrade[0] = "gold";
                                myRemain[0] = (int) (nice - myDist[0]);
                                myNext[0] = "나이스러너";
                                goldText.setTextColor(getResources().getColor(R.color.nicerun_blue));
                                image.setImageResource(R.drawable.gold);
                            }

                            remain.setText( myNext[0] + " 레벨까지 " + myRemain[0] + "Km 남았습니다");
                            progress.setMax(myProgress[0]);
                            progress.setProgress(myProgress[0] - myRemain[0]);

                        } else {
                            myGrade[0] = "nice";
                            remain.setText("당신은 진정한 나이스 러너");
                            progress.setMax(100);
                            progress.setProgress(100);
                            image.setImageResource(R.drawable.nice);
                            niceText.setTextColor(getResources().getColor(R.color.nicerun_blue));
                        }
                    }
                } else {
                    Log.d(TAG, "Status Code : " + response.code());
                    Log.d(TAG, response.errorBody().toString());
                    Log.d(TAG, call.request().body().toString());
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "Fail msg : " + t.getMessage());
            }
        });

//        myDist[0] = Double.parseDouble("70.0");
//        myCount[0] = "5";
//        count.setText(myCount[0]);

        return rootView;
    }

    public void initMyAPI(String baseUrl) {

        Log.d(TAG, "initMyAPI : " + baseUrl);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()) // addConverterFactory로 gson converter를 생성한다. gson은 json을 자바 클래스로 바꾸는데 사용
                .build();

        userAPI = retrofit.create(UserAPI.class);
    }
}