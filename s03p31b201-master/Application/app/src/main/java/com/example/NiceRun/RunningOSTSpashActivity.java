package com.example.NiceRun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

public class RunningOSTSpashActivity extends AppCompatActivity {
    Handler mHandler = new Handler();
    ImageSwitcher mImageSwitcher;
    boolean mRunning;
    Thread mThread;
    String jsonFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_o_s_t_spash);

        Intent intent = getIntent();
        jsonFileName = intent.getStringExtra("filename");

        mImageSwitcher = (ImageSwitcher) findViewById(R.id.imageswitcher_countdown_ost);
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

        mHandler.postDelayed(new RunningOSTSpashActivity.SplashHandler(), 2900); // 1초 후에 hd handler 실행  3000ms = 3초
    }

    private void startAnimation() {
        mImageSwitcher.setVisibility(View.VISIBLE);
        mThread = new RunningOSTSpashActivity.ImageThread();
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
            Log.d("SplashHandler", "this finish");
            mRunning = false;

            RunningOSTSpashActivity.this.finish();

            Intent intent = new Intent(new Intent(getApplication(), RunningOnSavedTrackActivity.class));
            intent.putExtra("filename",jsonFileName);
            startActivity(intent); //로딩이 끝난 후 이동
        }
    }

    @Override
    public void onBackPressed() {
    }
}