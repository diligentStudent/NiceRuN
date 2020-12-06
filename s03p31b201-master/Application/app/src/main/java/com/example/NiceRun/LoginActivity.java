package com.example.NiceRun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.NiceRun.login.SearchPwdFragment;
import com.example.NiceRun.login.SignInFragment;
import com.example.NiceRun.login.SignUpFragment;
import com.example.NiceRun.Util.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity{

    private final  String TAG = getClass().getSimpleName();

    // SharedPreferences
    private Context context;

//    private BackPressHandler bph = new BackPressHandler(this);

    // login관련 fragment
    SignInFragment siFragment;
    SignUpFragment suFragment;
    SearchPwdFragment spFragment;

    // 서버에서 이미지 불러오기
    private ImageView imageView;
    private Bitmap bitmap;

    private TextToSpeech tts;
    private Button ttsBtn;


//    @Override
//    public void onBackPressed() {
//        bph.onBackPressed("뒤로가기 버튼을 한번 더 누르면 종료됩니다.", 3000);
//    }
    private long backKeyPressed;
    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() > backKeyPressed + 2000){
            onFragmentChange(0);
            backKeyPressed = System.currentTimeMillis();
            return;
        } else if(System.currentTimeMillis() <= backKeyPressed + 2000){
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        context = this;
        String text = PreferenceManager.getString(context, "email");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /*
        tts = new TextToSpeech(LoginActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //사용할 언어를 설정
                    int result = tts.setLanguage(Locale.KOREA);
                    //언어 데이터가 없거나 혹은 언어가 지원하지 않으면...
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.d("sss","error");
                        Toast.makeText(LoginActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        ttsBtn.setEnabled(true);
                        //음성 톤
                        tts.setPitch(0.7f);
                        //읽는 속도
                        tts.setSpeechRate(1.2f);
                        Log.d("tts","ok");
                    }
                }
            }
        });

        ttsBtn = findViewById(R.id.ttsBtn);
        ttsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tts.speak("전방 50미터 앞 5시방향 우회전입니다.", TextToSpeech.QUEUE_FLUSH,null);
                Log.d("tts2","ok");
            }
        });
        */
        if (text.equals("")) {
            imageView = findViewById(R.id.imview);

            siFragment = new SignInFragment();
            suFragment = new SignUpFragment();
            spFragment = new SearchPwdFragment();

//
//            Thread uThread = new Thread() {
//
//                @Override
//                public void run(){
//                    try{
//                        //서버에 올려둔 이미지 URL
//                        URL url = new URL("https://k3b201.p.ssafy.io/img/logo.PNG");
//                        //Web에서 이미지 가져온 후 ImageView에 지정할 Bitmap 만들기
//
//                    /* URLConnection 생성자가 protected로 선언되어 있으므로
//                     개발자가 직접 HttpURLConnection 객체 생성 불가 */
//                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//
//                    /* openConnection()메서드가 리턴하는 urlConnection 객체는
//                    HttpURLConnection의 인스턴스가 될 수 있으므로 캐스팅해서 사용한다*/
//                        conn.setDoInput(true); //Server 통신에서 입력 가능한 상태로 만듦
//                        conn.connect(); //연결된 곳에 접속할 때 (connect() 호출해야 실제 통신 가능함)
//
//                        InputStream is = conn.getInputStream(); //inputStream 값 가져오기
//                        bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 반환
//                    }catch (MalformedURLException e){
//                        e.printStackTrace();
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }
//                }
//            };
//            uThread.start(); // 작업 Thread 실행
//
//            try{
//                //메인 Thread는 별도의 작업을 완료할 때까지 대기한다!
//                //join() 호출하여 별도의 작업 Thread가 종료될 때까지 메인 Thread가 기다림
//                //join() 메서드는 InterruptedException을 발생시킨다.
//                uThread.join();
//
//                //작업 Thread에서 이미지를 불러오는 작업을 완료한 뒤
//                //UI 작업을 할 수 있는 메인 Thread에서 ImageView에 이미지 지정
//                imageView.setImageBitmap(bitmap);
//            }catch (InterruptedException e){
//                e.printStackTrace();
//            }


        onFragmentChange(0);
        } else{
            // activity 넘어가기
            Log.d("share", PreferenceManager.getString(context, "loginInfo"));
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish(); // MainActivity를 종료(메모리에서 제거)
        }

    }


    //프래그먼트와 프래그먼트끼리 직접접근을하지않는다. 프래그먼트와 엑티비티가 접근함
    public void onFragmentChange(int index){
        if(index == 0 ){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, siFragment).commit();
        }else if(index == 1){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, suFragment).commit();
        }else if(index == 2){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, spFragment).commit();
        }
    }
//    private void Speech() {
//        String text = "전방 50미터 앞 5시방향 우회전입니다.";
//        // QUEUE_FLUSH: Queue 값을 초기화한 후 값을 넣는다.
//        // QUEUE_ADD: 현재 Queue에 값을 추가하는 옵션이다.
//        // API 21
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
//            // API 20
//        else
//            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//
//    }
//
    // 메모리 누출을 방지하게 위해 TTS를 중지
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d("tts3","ok");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }





}