package com.example.NiceRun.login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.NiceRun.API.UserAPI;
import com.example.NiceRun.BackPressHandler;
import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.DTO.Response.StandardResponse;
import com.example.NiceRun.DTO.User;
import com.example.NiceRun.LoginActivity;
import com.example.NiceRun.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUpFragment extends Fragment {

    LoginActivity activity;

    private final  String TAG = getClass().getSimpleName();

    // API 요청
    private String baseUrl = "https://k3b201.p.ssafy.io/api/";
    private UserAPI userAPI;
    private EditText email;
    private EditText password;
    private EditText firstname;
    private EditText lastname;
    private EditText weight;
    private EditText height;
    private RadioGroup gender;
    private EditText goaldist;
    private EditText location;
    private EditText birthday;
    private Boolean clickGender;
    private RadioButton male;
    private RadioButton female;
    private String checkGender = "";
    private Boolean checkEmail = false;

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
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_sign_up , container, false);

        Button signUpBtn = rootView.findViewById(R.id.signUpBtn);
        // 이메일 형식, 중복 검사 버튼
        Button emailBtn = rootView.findViewById(R.id.emailBtn);

        email = rootView.findViewById(R.id.email);
        password = rootView.findViewById(R.id.password);
        firstname = rootView.findViewById(R.id.firstname);
        lastname = rootView.findViewById(R.id.lastname);
        weight = rootView.findViewById(R.id.weight);
        height = rootView.findViewById(R.id.height);
        gender = rootView.findViewById(R.id.gender);
        goaldist = rootView.findViewById(R.id.goaldist);
        location = rootView.findViewById(R.id.location);
        birthday = rootView.findViewById(R.id.birthday);
        male = rootView.findViewById(R.id.male);
        female = rootView.findViewById(R.id.female);

        final EditText email = (EditText) rootView.findViewById(R.id.email);
        final EditText password = (EditText) rootView.findViewById(R.id.password);
        final EditText password2 = (EditText) rootView.findViewById(R.id.password2);
        final EditText firstname = (EditText) rootView.findViewById(R.id.firstname);
        final EditText lastname = (EditText) rootView.findViewById(R.id.lastname);
        final EditText weight = (EditText) rootView.findViewById(R.id.weight);
        final EditText height = (EditText) rootView.findViewById(R.id.height);
        final EditText goaldist = (EditText) rootView.findViewById(R.id.goaldist);
        final EditText location = (EditText) rootView.findViewById(R.id.location);
        final EditText birthday = (EditText) rootView.findViewById(R.id.birthday);
        final RadioGroup gender = (RadioGroup) rootView.findViewById(R.id.gender);

        initMyAPI(baseUrl);

        //이메일 중복 검사
        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isValidEmail(email.getText().toString())){
                    Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"유효하지 않은 이메일입니다.",Toast.LENGTH_SHORT);
                    myToast.show();
                } else {
                    Call<StandardResponse> postCall = userAPI.emailvalidate(email.getText().toString());
                    postCall.enqueue(new Callback<StandardResponse>() {
                        @Override
                        public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                            if (response.isSuccessful()) {
                                StandardResponse data = response.body();
                                try {
                                    if(data.getData().equals("success")) {
                                        Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"사용 가능한 이메일 입니다.",Toast.LENGTH_SHORT);
                                        myToast.show();
                                        checkEmail = true;
                                    } else if(data.getData().equals("fail")) {
                                        Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"이미 사용되고 있는 이메일입니다.",Toast.LENGTH_SHORT);
                                        myToast.show();
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
                        public void onFailure(Call<StandardResponse> call, Throwable t) {
                            Log.d(TAG, "Fail msg : " + t.getMessage());
                        }
                    });
                }
            }
        });

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.male) {
                    clickGender = true;
                    checkGender = male.getText().toString();
                } else if (checkedId == R.id.female) {
                    clickGender = false;
                    checkGender = female.getText().toString();
                }
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEmail) {
                    Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"이메일 중복 검사를 해주세요.",Toast.LENGTH_SHORT);
                    myToast.show();
                    return;
                }
                if(!password.getText().toString().equals(password2.getText().toString())) {
                    Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"비밀번호가 다릅니다.",Toast.LENGTH_SHORT);
                    myToast.show();
                    return;
                } else if(birthday.getText().toString().length() != 8 || firstname.getText().toString().length() == 0
                        || weight.getText().toString().length() == 0 || height.getText().toString().length() == 0
                        || checkGender.equals("") || password.getText().toString().length() < 6
                        || goaldist.getText().toString().length() == 0) {
                    Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"가입 정보를 확인해주세요.",Toast.LENGTH_SHORT);
                    myToast.show();
                    return;
                }
                String year = birthday.getText().toString().substring(0,4);
                String month = birthday.getText().toString().substring(4,6);
                String day = birthday.getText().toString().substring(6);
                String birth = year + "-" + month + "-" + day;

                User user = new User();
                user.setEmail(email.getText().toString());
                user.setPassword(password.getText().toString());
                user.setFirstname(firstname.getText().toString());
                user.setLastname(lastname.getText().toString());
                user.setLocation(location.getText().toString());
                user.setBirthday(birth);
                user.setGender(clickGender);
                user.setGoaldist(Integer.parseInt(goaldist.getText().toString()));
                user.setHeight(Integer.parseInt(height.getText().toString()));
                user.setWeight(Integer.parseInt(weight.getText().toString()));
                user.setProfileimg("/home/ubuntu/project/backend/image/user.png");

                Call<BasicResponse> postCall1 = userAPI.post_signup(user);
                postCall1.enqueue(new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        if(response.isSuccessful()){
                            activity.onFragmentChange(0);
                            Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"회원가입이 완료되었습니다.",Toast.LENGTH_SHORT);
                            myToast.show();
                            email.setText(null);
                            password.setText(null);
                            password2.setText(null);
                            firstname.setText(null);
                            lastname.setText(null);
                            location.setText(null);
                            birthday.setText(null);
                            goaldist.setText(null);
                            height.setText(null);
                            weight.setText(null);
                            checkEmail = false;
                            checkGender = "";
                        }else {
                            Log.d(TAG,"Status Code : " + response.code());
                            Log.d(TAG,response.errorBody().toString());
                            Log.d(TAG,call.request().body().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {
                        Log.d(TAG,"Fail msg : " + t.getMessage());
                    }
                });
            }
        });

        return rootView;
    }

    private void initMyAPI(String baseUrl){
        Log.d(TAG,"initMyAPI : " + baseUrl);
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