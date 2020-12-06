package com.example.NiceRun.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;


public class SignInFragment extends Fragment {

    LoginActivity activity;
    private final String TAG = getClass().getSimpleName();

    // API 요청
    public String baseUrl = "https://k3b201.p.ssafy.io/api/";
    private UserAPI userAPI;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //이 메소드가 호출될떄는 프래그먼트가 엑티비티위에 올라와있는거니깐 getActivity메소드로 엑티비티참조가능
        activity = (LoginActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //이제 더이상 엑티비티 참초가안됨
        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //프래그먼트 메인을 인플레이트해주고 컨테이너에 붙여달라는 뜻임
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_sign_in, container, false);
        Button signInBtn = rootView.findViewById(R.id.signInBtn);
        Button signUpBtn = rootView.findViewById(R.id.signUpBtn);
        Button searchPwdBtn = rootView.findViewById(R.id.searchPwdBtn);

        final EditText email = (EditText) rootView.findViewById(R.id.email);
        final EditText password = (EditText) rootView.findViewById(R.id.password);

        initMyAPI(baseUrl);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();
                if(email.getText().toString().length() == 0 || password.getText().toString().length() == 0) {
                    Toast myToast = Toast.makeText(getActivity().getApplicationContext(), "아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT);
                    myToast.show();
                    return;
                }
                user.setEmail(email.getText().toString());
                user.setPassword(password.getText().toString());

                Call<BasicResponse> postCall = userAPI.signin(email.getText().toString(), password.getText().toString());

                postCall.enqueue(new Callback<BasicResponse>() {

                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        if (response.isSuccessful()) {
                            BasicResponse dto = response.body();
                            if(dto.getData().equals("success")){
                                PreferenceManager.setString(getActivity(), "email", response.body().getObject().getUser().getEmail());
                                PreferenceManager.setString(getActivity(), "password", response.body().getObject().getUser().getPassword());
                                PreferenceManager.setString(getActivity(), "location", response.body().getObject().getUser().getLocation());
                                PreferenceManager.setInt(getActivity(), "height", response.body().getObject().getUser().getHeight());
                                PreferenceManager.setInt(getActivity(), "weight", response.body().getObject().getUser().getWeight());
                                PreferenceManager.setBoolean(getActivity(), "gender", response.body().getObject().getUser().isGender());
                                PreferenceManager.setInt(getActivity(), "goaldist", response.body().getObject().getUser().getGoaldist());
                                PreferenceManager.setString(getActivity(), "lastname", response.body().getObject().getUser().getLastname());
                                PreferenceManager.setString(getActivity(), "firstname", response.body().getObject().getUser().getFirstname());
                                PreferenceManager.setString(getActivity(), "profileimg", response.body().getObject().getUser().getProfileimg()); // 회원가입 시 temp
                                PreferenceManager.setString(getActivity(), "birthday", response.body().getObject().getUser().getBirthday());

                                Intent in = new Intent(getActivity(), MainActivity.class);
                                //in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(in);
                                activity.finish();
                            }else{
                                Toast myToast = Toast.makeText(getActivity().getApplicationContext(), "아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT);
                                myToast.show();
                                return;
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

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onFragmentChange(1);
            }
        });
        searchPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onFragmentChange(2);
            }
        });
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