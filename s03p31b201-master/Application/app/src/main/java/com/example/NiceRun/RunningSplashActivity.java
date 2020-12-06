package com.example.NiceRun;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Timer;
import java.util.TimerTask;

public class RunningSplashActivity extends AppCompatActivity {
    Handler mHandler = new Handler();
    ImageSwitcher mImageSwitcher;
    //    TimerTask mTimerTask;
//    Timer timer = new Timer();
    boolean mRunning;
    Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_splash);


        mImageSwitcher = (ImageSwitcher) findViewById(R.id.imageswitcher_countdown);
        mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView view = new ImageView(getApplicationContext());
                view.setBackgroundColor(Color.WHITE);
                view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                view.setLayoutParams(
                        new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                        )
                );
                return view;
            }
        });
        mImageSwitcher.setInAnimation(this, android.R.anim.fade_in);
        mImageSwitcher.setOutAnimation(this, R.anim.anim_scale_alpha);
        startAnimation();

        mHandler.postDelayed(new SplashHandler(), 2900); // 1초 후에 hd handler 실행  3000ms = 3초
    }

    private void startAnimation() {
        mImageSwitcher.setVisibility(View.VISIBLE);
        mThread = new ImageThread();
        mThread.start();
    }

    private class ImageThread extends Thread {
        int duration = 1000;
        final int images[] = {
                R.drawable.count_down_3,
                R.drawable.count_down_2,
                R.drawable.count_down_1
        };
        int cur = -1;

        public void run() {
            mRunning = true;
            while (mRunning) {
                synchronized (this) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mImageSwitcher.setImageResource(images[cur]);
                        }
                    });
                }
                cur++;
                if (cur == images.length) cur = 0;
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {

                }
            }
        }
    }

    private class SplashHandler implements Runnable {
        public void run() {
            Log.d("SplashHandler","this finish");
            mRunning= false;

            Intent intent = new Intent(new Intent(getApplication(), RunningActivity.class));
            startActivity(intent); //로딩이 끝난 후 이동
            RunningSplashActivity.this.finish(); // 로딩페이지 Activity stack에서 제거
        }
    }

    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }


}