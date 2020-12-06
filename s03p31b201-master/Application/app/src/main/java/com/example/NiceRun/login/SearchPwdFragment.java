package com.example.NiceRun.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.NiceRun.API.UserAPI;
import com.example.NiceRun.BackPressHandler;
import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.DTO.Response.StandardResponse;
import com.example.NiceRun.DTO.User;
import com.example.NiceRun.LoginActivity;
import com.example.NiceRun.R;
import com.example.NiceRun.main.running.SavedTracksTab;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchPwdFragment extends Fragment {

    private static String TAG;
    LoginActivity activity;


    // API 요청
    private String baseUrl = "https://k3b201.p.ssafy.io/api/";
    private UserAPI userAPI;

    private ProgressDialog progressDialog;

    final String[] keyCheck = new String[1];

    static CountDownTimer countDownTimer;
    static final int MILLISINFUTURE = 300 * 1000; //총 시간 (300초 = 5분)
    static final int COUNT_DOWN_INTERVAL = 1000; //onTick 메소드를 호출할 간격 (1초)

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (LoginActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }


    public static SearchPwdFragment newInstance() {
        return new SearchPwdFragment();
    }

    Boolean inzbuttonconfirm;
    long emailAuthCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search_pwd, container, false);

        Button keyBtn = rootView.findViewById(R.id.keyBtn);
        Button passwordBtn = rootView.findViewById(R.id.passwordBtn);
        Button emailBtn = rootView.findViewById(R.id.emailBtn);

        EditText email = (EditText) rootView.findViewById(R.id.email);
        EditText password = (EditText) rootView.findViewById(R.id.password);
        EditText password2 = (EditText) rootView.findViewById(R.id.password2);
        EditText key = (EditText) rootView.findViewById(R.id.key);

        TextView time = (TextView) rootView.findViewById(R.id.time);

        initMyAPI(baseUrl);

        //이메일 인증 번호 전송
        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidEmail(email.getText().toString())) {
                    Toast myToast = Toast.makeText(getActivity().getApplicationContext(), "유효하지 않은 이메일입니다.", Toast.LENGTH_SHORT);
                    myToast.show();
                } else {
                    Call<BasicResponse> postCall = userAPI.findpassword(email.getText().toString());
                    Toast myToast1 = Toast.makeText(getActivity().getApplicationContext(), "잠시만 기다려주세요.", Toast.LENGTH_SHORT);
                    myToast1.show();
                    postCall.enqueue(new Callback<BasicResponse>() {
                        @Override
                        public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                            if (response.isSuccessful()) {
                                BasicResponse data = response.body();
                                try {
                                    if (data.getData().equals("fail")) {
                                        Toast myToast = Toast.makeText(getActivity().getApplicationContext(), "인증번호 전송에 실패했습니다.", Toast.LENGTH_SHORT);
                                        myToast.show();
                                    } else {
                                        key.setEnabled(true);
                                        keyBtn.setEnabled(true);
                                        keyCheck[0] = data.getData();
                                        countDownTimer.start();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
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
                }
            }
        });

        passwordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!password.getText().toString().equals(password2.getText().toString()) || password.getText().toString().length() < 6) {
                    Toast myToast = Toast.makeText(getActivity().getApplicationContext(), "비밀번호를 다시 확인해주세요.", Toast.LENGTH_SHORT);
                    myToast.show();
                    return;
                }
                Call<BasicResponse> postCall1 = userAPI.changepassword(email.getText().toString(), password.getText().toString());
                postCall1.enqueue(new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        if (response.isSuccessful()) {
                            BasicResponse data = response.body();
                            if (data.getData().equals("success")) {
                                Toast myToast = Toast.makeText(getActivity().getApplicationContext(), "비밀번호가 변경되엇습니다.", Toast.LENGTH_SHORT);
                                myToast.show();
                                activity.onFragmentChange(0);
                                email.setText(null);
                                password.setText(null);
                                password2.setText(null);
                                key.setText(null);
                                password.setEnabled(false);
                                password2.setEnabled(false);
                                passwordBtn.setEnabled(false);
                                keyBtn.setEnabled(false);
                                key.setEnabled(false);
                                countDownTimer.onFinish();
                            } else {
                                Toast myToast = Toast.makeText(getActivity().getApplicationContext(), "비밀번호가 변경에 실패했습니다.", Toast.LENGTH_SHORT);
                                myToast.show();
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
            }
        });

        keyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (key.getText().toString().equals(keyCheck[0])) {
                    password.setEnabled(true);
                    password2.setEnabled(true);
                    passwordBtn.setEnabled(true);
                    countDownTimer.onFinish();
                } else {
                    Toast myToast = Toast.makeText(getActivity().getApplicationContext(), "인증번호가 다릅니다", Toast.LENGTH_SHORT);
                    myToast.show();
                    key.setText(null);
                }
            }
        });

        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) { //(300초에서 1초 마다 계속 줄어듬)
                emailAuthCount = millisUntilFinished / 1000;
                if ((emailAuthCount - ((emailAuthCount / 60) * 60)) >= 10) { //초가 10보다 크면 그냥 출력
                    time.setText((emailAuthCount / 60) + " : " + (emailAuthCount - ((emailAuthCount / 60) * 60)));
                } else { //초가 10보다 작으면 앞에 '0' 붙여서 같이 출력. ex) 02,03,04...
                    time.setText((emailAuthCount / 60) + " : 0" + (emailAuthCount - ((emailAuthCount / 60) * 60)));
                }

            }

            @Override
            public void onFinish() { //시간이 다 되면 다이얼로그 종료
                key.setEnabled(false);
                keyBtn.setEnabled(false);
                key.setText(null);
                time.setText("0:00");
            }
        };

        return rootView;
    }

    private void initMyAPI(String baseUrl) {
        Log.d(TAG, "initMyAPI : " + baseUrl);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        userAPI = retrofit.create(UserAPI.class);
    }

    private static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if (m.matches()) {
            err = true;
        }
        return err;
    }

}