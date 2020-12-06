package com.example.NiceRun;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.NiceRun.API.RunningAPI;
import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.Util.PreferenceManager;
import com.example.NiceRun.main.running.GpsTracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RunningOnSavedTrackActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.SnapshotReadyCallback,
        GoogleMap.OnMapLoadedCallback {

    /* toggle */
    boolean isRunning;
    boolean isSaveScreen = false;
    /* 타이머 */
    TimerTask mTimerTask;
    Timer timer = new Timer();
    TextView timerBodyTextView;
    TextView timerHeaderTextView;
    int currentCount = 0;

    /* 지도 */
    GoogleMap mGoogleMap = null;

    private Location location;
    private LatLng currentLatLng;
    private Marker currentMarker = null;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;

    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    // camera
    boolean mRequestingLocationUpdates = false;
    boolean mMoveMapByUser = true;
    boolean mMoveMapByAPI = true;

    // Polyline
    Polyline mPolyline;
    List<LatLng> mTrack = new ArrayList<>();

    // 이동 거리
    private double currentDistance = 0L;
    private double roundedDistance;
    LatLng preFarLatlng;
    // 속도
    private double currentPace = 0L;

    // 시작 버튼 누른 시간
    String startTime;

    // API 요청
    RunningAPI runningAPI;
    private String baseUrl = "https://k3b201.p.ssafy.io/api/";

    // 요청 보낼 파일
    String jsonFileName;
    String imgFileName;

    // 칼로리 계산
    final double mLperMET = 3.5;
    double currentMET;
    private int currentKcal;

    /* TTS */
    private TextToSpeech tts;
    private int ttsIndex = 1;
    private static List<String> ttsList = new ArrayList<>();
    private boolean isSaid;

    /* Read Track */
    private static List<double[]> posList = new ArrayList<>(); //0은 lat 1은 long
    private static int target = 0;//idx1과 idx2는 currentPosition과 함께 사잇각을 구하기위한 변수 target은 유저가 다음 위치에 도착해야될 좌표
    //    private static boolean start = false;
    private static double[] targetPos = null;
    List<LatLng> mSavedTrack = new ArrayList<>();

    private boolean start;
    private boolean ttsisReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_on_saved_track);
        isSaid = false;
        start = true;

        Intent intent = getIntent();
        jsonFileName = intent.getStringExtra("filename");

        ReadJSON(jsonFileName);
        SaveTTS();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //사용할 언어를 설정
                    int result = tts.setLanguage(Locale.KOREA);
                    //언어 데이터가 없거나 혹은 언어가 지원하지 않으면...
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.d("sss", "error");
                        Toast.makeText(RunningOnSavedTrackActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        //음성 톤
                        tts.setPitch(0.7f);
                        //읽는 속도
                        tts.setSpeechRate(1.2f);
                        Log.d("tts", "ok");
                        ttsisReady = true;
                    }
                }
            }
        });


        isRunning = true;
        setStartTime();

        /* 버튼 클릭 리스너 */

        RunningOnSavedTrackActivity.ButtonOnClickListener onClickListener = new RunningOnSavedTrackActivity.ButtonOnClickListener();

        ImageButton startButton = (ImageButton) findViewById(R.id.button_start_OST);
        ImageButton pauseButton = (ImageButton) findViewById(R.id.button_pause_OST);
        ImageButton stopButton = (ImageButton) findViewById(R.id.button_stop_OST);
        startButton.setOnClickListener(onClickListener);
        pauseButton.setOnClickListener(onClickListener);
        stopButton.setOnClickListener(onClickListener);

        /* 타이머 */
        timerBodyTextView = (TextView) findViewById(R.id.textview_body_timer_OST);
        timerHeaderTextView = (TextView) findViewById(R.id.textview_header_timer_OST);
        startTimerTask();

        /* 지도 */
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_running_OST);
        mapFragment.getMapAsync(this);
        //  파일 권한 TODO: Main Activity로 분할
        checkWritePermission();
        checkReadPermission();

        initMyAPI(baseUrl);
    }

    public void ReadJSON(String filename) {
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NiceRuN/track/" + filename;
        System.out.println(filepath);
        System.out.println("=========================Running========================");
        StringBuffer strBuffer = new StringBuffer();
        String line = "";
        posList.clear();
        try {
            InputStream is = new FileInputStream(filepath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                strBuffer.append(line + "\n");
            }

            org.json.JSONArray jsonArray = new org.json.JSONArray(strBuffer.toString());

            String st = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
                String lat = jsonObject.getString("lat");
                String lon = jsonObject.getString("lon");
                System.out.println("lat : " + lat + " " + "long : " + lon);
                posList.add(new double[]{Double.parseDouble(lat), Double.parseDouble(lon)});
                mSavedTrack.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));
            }

            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SaveTTS() {
        int idx_1 = 0;
        int idx_2 = 2;
        int idx_3 = 4;
        double[] pos1 = null;
        double[] pos2 = null;
        double[] pos3 = null;
        ttsList.clear();
        for (int i = 0; i < 2; i++) {
            ttsList.add("");
        }
        for (int i = 2; i < posList.size() - 2; i++) {
            pos1 = posList.get(idx_1++);
            pos2 = posList.get(idx_2++);
            pos3 = posList.get(idx_3++);
            double degree = Math.atan((pos3[0] - pos1[0]) / (pos3[1] - pos1[1])) - Math.atan((pos2[0] - pos1[0]) / (pos2[1] - pos1[1]));
            degree *= 180 / Math.PI;

            if (Math.abs(degree) >= 100) {
                degree = degree < 0 ? degree + 180 : degree - 180;
            }


            if (Math.abs(degree) > 20) {
                //index2랑 index2 + 1
                // pos2 , posList.get(index_2 + 1);
                LatLng fromPos = new LatLng(pos2[0], pos2[1]);
                double[] temp = posList.get(idx_2);
                LatLng toPos = new LatLng(temp[0], temp[1]);
                double dist = getDistance(fromPos, toPos);
                int distMeter = (int) (dist * 1000);

                if (degree > 0) {
                    // "좌회전입니다" 그앞의 String 어딘가에 있다면
                    if (!ttsList.isEmpty() && ttsList.get(ttsList.size() - 1).indexOf("좌회전 입니다") != -1) {
                        ttsList.add("");
                        continue;
                    }
                    ttsList.add(distMeter + "미터 앞에서 좌회전 입니다");
                } else {
                    if (!ttsList.isEmpty() && ttsList.get(ttsList.size() - 1).indexOf("우회전 입니다") != -1) {
                        ttsList.add("");
                        continue;
                    }
                    ttsList.add(distMeter + "미터 앞에서 우회전 입니다");
                }
            } else {
                ttsList.add("");
            }
        }

        System.out.println("------------tts list --------");
        for (int i = 0; i < ttsList.size(); i++) {
            System.out.print(i + " ");
            System.out.println(ttsList.get(i));
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        // init
        GpsTracker gpsTracker = new GpsTracker(this);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        if (latitude == 0 && longitude == 0) {
            // 한밭대
            latitude = 36.350762;
            longitude = 127.300601;
        }
        setDefaultLocation(latitude, longitude);

        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        System.out.println("=======track size======");
        System.out.println(mSavedTrack.size());
        // 트랙 그리기
        drawPolyLine(15, Color.BLUE, mSavedTrack);

        // 도착지점 찍기

        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Log.d("onMyLocationButtonClick", "위치에 따른 카메라 이동 활성화");
                mMoveMapByAPI = true;
                return true;
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });
        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (mMoveMapByUser == true && mRequestingLocationUpdates) {
                    Log.d("onCameraMove :", "위치에 따른 카메라 이동 비활성화");
                    mMoveMapByAPI = false;
                }
                mMoveMapByUser = true;
            }
        });
        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
            }
        });
        //런타임 퍼미션 처리
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 위치 업데이트 시작
            startLocationUpdates();
        }

    }

    private void setDefaultLocation(double lat, double lng) {
        mMoveMapByUser = false;

        LatLng DEFAULT_LOCATION = new LatLng(lat, lng);

        // marker
        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.draggable(true);
        currentMarker = mGoogleMap.addMarker(markerOptions);

        // camera
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            targetPos = posList.get(target);
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                LatLng preLatLng = currentLatLng;
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                // 뛰는중이면 추적하기 위한 위치 배열에 추가
                if (isRunning) {

                    if (preLatLng != null) {
                        // 이동거리
                        setDistanceTextView(preLatLng, currentLatLng);
                        // 속도
                        setPaceTextView(currentDistance, currentCount);
                    } else { // currentLatLng의 이전 값이 null 일때 = 처음 좌표값 받아올때
                        preFarLatlng = currentLatLng;
                        mTrack.add(currentLatLng);
                    }

                    if (isFarBetweenPosition(preFarLatlng, currentLatLng)) {
                        preFarLatlng = currentLatLng;
                        mTrack.add(currentLatLng);
                    }
                }
                LatLng targetLatLng = new LatLng(targetPos[0], targetPos[1]);
                double distBtwnCurAndTar = getDistance(currentLatLng, targetLatLng);

                /*tts*/
                System.out.println("----start----");
                System.out.println(start);
                System.out.println("----distBtwnCurAndTar----");
                System.out.println(distBtwnCurAndTar);
                if (ttsisReady) {
                    if (start) {
                        String text = "안내를 시작합니다" + ttsList.get(ttsIndex++);
                        target++;
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                        start = false;
                    }

                    if (distBtwnCurAndTar < 0.01) {
                        // SSH동작
                        String text = null;

                        if (ttsIndex < ttsList.size()) {
                            text = ttsList.get(ttsIndex++);
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                            targetPos = posList.get(target++);
                        }

                    } else {//만약 도달하지 못했을경우 내가 경로에 다른 좌표위에 있는지 확인한후 다른좌표라면 해당 좌표로 내위치를 업데이트함
                        for (int i = target; i < posList.size() - 4; i++) {
                            double[] pos = posList.get(i);
                            LatLng tempLatLng = new LatLng(pos[0], pos[1]);
                            double distBtwnCurAndTemp = getDistance(currentLatLng, tempLatLng);
                            if (distBtwnCurAndTemp < 0.01) {
                                tts.speak("다른 목적지에 도달했습니다 현재 경로부터 시작합니다", TextToSpeech.QUEUE_FLUSH, null);
                                target = i;
                                ttsIndex = target + 1;

                                break;
                            }
                        }

                        //목표에 도달
                        if (target >= posList.size()) {
                            tts.speak("도착지점에 도달했습니다", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                }
                // 마지막 위치 넣을지 말지
                LatLng lastPos = mTrack.get(mTrack.size() - 1);
                if (isSaveScreen && getDistance(lastPos, currentLatLng) >= 0.004 && getDistance(lastPos, currentLatLng) <= 0.02) {
                    mTrack.add(currentLatLng);
                }

                mGoogleMap.setOnMapLoadedCallback(RunningOnSavedTrackActivity.this);
            }
        }
    };

    // true : 가깝지 않다. 꽤 멀다. good하다
    private boolean isFarBetweenPosition(LatLng preLatLng, LatLng currentLatLng) {
        boolean result = false;
        double dist = getDistance(preLatLng, currentLatLng);
        // 15m 이상
        if (dist >= 0.015) {
            result = true;
        }
        return result;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!mRequestingLocationUpdates) {
            Log.d("onResume :", " call startLocationUpdates");
            startLocationUpdates();
        }
    }


    private class ButtonOnClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            LinearLayout runningHeaderLayout = (LinearLayout) findViewById(R.id.layout_header_running_OST);
            LinearLayout runningBodyLayout = (LinearLayout) findViewById(R.id.layout_body_running_OST);
            LinearLayout runningFooterLayout = (LinearLayout) findViewById(R.id.layout_footer_running_OST);
            LinearLayout notRunningHeaderLayout = (LinearLayout) findViewById(R.id.layout_header_not_running_OST);
            LinearLayout notRunningBodyLayout = (LinearLayout) findViewById(R.id.layout_body_not_running_OST);
            LinearLayout notRunningFooterLayout = (LinearLayout) findViewById(R.id.layout_footer_not_running_OST);

            switch (view.getId()) {
                case R.id.button_pause_OST:

                    if (mGoogleMap != null) {
                        if (currentLatLng == null) {
                            setDefaultLocation(36.350762, 127.300601);
                        } else {
                            drawPolyLine(10, Color.RED, mTrack);
                            // camera update

                        }
                    }

                    stopTimerTask();

                    runningHeaderLayout.setVisibility(View.GONE);
                    runningBodyLayout.setVisibility(View.GONE);
                    runningFooterLayout.setVisibility(View.GONE);

                    notRunningHeaderLayout.setVisibility(View.VISIBLE);
                    notRunningBodyLayout.setVisibility(View.VISIBLE);
                    notRunningFooterLayout.setVisibility(View.VISIBLE);

                    isRunning = !isRunning;

                    break;


                case R.id.button_start_OST:
                    notRunningHeaderLayout.setVisibility(View.GONE);
                    notRunningFooterLayout.setVisibility(View.GONE);
                    notRunningBodyLayout.setVisibility(View.GONE);

                    runningHeaderLayout.setVisibility(View.VISIBLE);
                    runningFooterLayout.setVisibility(View.VISIBLE);
                    runningBodyLayout.setVisibility(View.VISIBLE);

                    isRunning = !isRunning;
                    startTimerTask();
                    break;

                case R.id.button_stop_OST:
                    stopRunning();
            }
        }
    }

    private void setStartTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startTime = sdfNow.format(date);
    }

    /* 종료 시 */
    private void stopRunning() {
        if (mTrack.size() < 5) {
            Toast.makeText(this, "운동 거리가 너무 짧습니다!", Toast.LENGTH_LONG).show();
            RunningOnSavedTrackActivity.this.finish(); // activity 종료
        } else {
            showFinishDialog();
        }
    }


    private void showFinishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("러닝 종료");
        builder.setMessage("러닝 기록을 저장하시겠습니까?");
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        calcurateKcal();
                        saveScreen();
                        RunningOnSavedTrackActivity.this.finish(); // activity 종료
                    }
                });

        builder.show();
    }

    private void calcurateKcal() {
        int weight = PreferenceManager.getInt(this, "weight");
        calcurateMET();
        int second = currentCount;

        double mL = (currentMET * mLperMET * weight * second) / 60;
        currentKcal = (int) ((mL / 1000) * 5);
    }

    private void calcurateMET() {
        if (currentPace < 3) {
            currentMET = 3;
        } else if (currentPace < 5) {
            currentMET = 3.5;
        } else if (currentPace < 6.5) {
            currentMET = 4;
        } else if (currentPace < 7) {
            currentMET = 5;
        } else if (currentPace < 8) {
            currentMET = 10;
        } else if (currentPace < 15) { // 자전거로 추측
            currentMET = 8;
        } else {
            currentMET = 12;
        }
    }


    private String makeCityName(String address) {
        String[] large = {"서울특별시", "부산광역시", "인천광역시", "대구광역시", "광주광역시", "대전광역시", "울산광역시"};

        StringTokenizer st = new StringTokenizer(address);
        String cityName = "";
        for (int i = 0; i < 2; i++) {
            cityName = st.nextToken();
        }

        boolean flag = true;
        for (int j = 0; j < large.length; j++) {
            if (cityName.equals(large[j])) {
                flag = false;
            }
        }
        if (flag) {
            cityName = st.nextToken();
        }
        return cityName;
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            //.makeText(mContext, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "대한민국 대전광역시";
        } catch (IllegalArgumentException illegalArgumentException) {
            //Toast.makeText(mContext, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "대한민국 대전광역시";

        }
        if (addresses == null || addresses.size() == 0) {
            //Toast.makeText(mContext, "주소 미발견", Toast.LENGTH_LONG).show();
            return "대한민국 대전광역시";

        } else {
            Address address = addresses.get(0);
            String showAdress = address.getAddressLine(0).toString();
            return showAdress;
        }
    }

    private void postTrackInfo() {
        String email = PreferenceManager.getString(this, "email");

        MultipartBody.Part jsonData = convertFile2MBP(jsonFileName, "filename");
        MultipartBody.Part imageData = convertFile2MBP(imgFileName, "trackimg");

        String location = makeCityName(getCurrentAddress(currentLatLng));
        boolean snsdownload = true;
        Call<BasicResponse> postCall = runningAPI.savetrack(
                startTime,
                roundedDistance,
                email,
                jsonData,
                currentKcal,
                location,
                currentCount,
                snsdownload,
                currentPace,
                imageData
        );
        String msg;
        postCall.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("success", "Status Code : " + response.code());
                } else {
                    Log.d("a", "Status Code : " + response.code());
                    Log.d("b", response.errorBody().toString());
                    Log.d("c", call.request().body().toString());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d("save track fail", "Fail msg : " + t.getMessage());
            }
        });
        Toast.makeText(this, "저장 성공 !", Toast.LENGTH_SHORT).show();
    }

    private MultipartBody.Part convertFile2MBP(String fileName, String formName) {
        RequestBody rqFile;
        MultipartBody.Part mpFile;
        File file = new File(fileName);
        rqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        mpFile = MultipartBody.Part.createFormData(formName, file.getName(), rqFile); // 킷값, 파일 이름, 데이터

        return mpFile;
    }

    private void initMyAPI(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        runningAPI = retrofit.create(RunningAPI.class);
    }

    /* 캡쳐 후 파일 저장 */
    private void saveScreen() {
        isSaveScreen = true;
        if (mGoogleMap != null) {
            mGoogleMap.setOnMapLoadedCallback(this);
        }
    }

    @Override
    public void onMapLoaded() {
        if (mGoogleMap != null) {
            if (currentLatLng != null) {
                setCurrentLocation(currentLatLng);
                if (isSaveScreen) {
                    if (currentMarker != null) currentMarker.remove();
                    mGoogleMap.snapshot(this);
                }
            }
        }
    }

    @Override
    public void onSnapshotReady(Bitmap bitmap) {
        try {
            FileOutputStream out;
            makeDirectory("/NiceRuN");
            makeDirectory("/NiceRuN/image");
            imgFileName = System.currentTimeMillis() + ".jpg";
            imgFileName = Environment.getExternalStorageDirectory().toString() + "/NiceRuN/image/" + imgFileName;
            out = new FileOutputStream(imgFileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            // 서버로 Post
            postTrackInfo();
        } catch (Exception e) {
            Log.e("bitmap error : ", e.toString() + " ");
        }
    }

    private File makeDirectory(String folderName) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    private void checkWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void checkReadPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    public void setCurrentLocation(LatLng latLng) {
        mMoveMapByUser = false;
        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);

        currentMarker = mGoogleMap.addMarker(markerOptions);
        // bounds to zoom camera
        if (mMoveMapByAPI) {
            CameraUpdate cameraUpdate;
            if (!mTrack.isEmpty()) {

                LinearLayout mapFrameLayout = (LinearLayout) findViewById(R.id.layout_body_not_running_OST);
                int width = mapFrameLayout.getWidth();
                int height = mapFrameLayout.getHeight();
                int padding = (int) (width * 0.12); // 12%

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng l : mTrack) {
                    builder.include(l);
                }
                for (LatLng l : mSavedTrack) {
                    builder.include(l);
                }
                LatLngBounds bounds = builder.build();

                cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            } else {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 20);
            }
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    private void drawPolyLine(int thickness, int color, List list) {
        // 1. 점들을 선으로 만들어서 지도에 보여주기
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(list);
        polylineOptions.width(thickness).color(color);

        mGoogleMap.addPolyline(polylineOptions);
    }

    /* timer */
    private void startTimerTask() {
        stopTimerTask();
        currentCount--;
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                currentCount++;
                timerBodyTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        String timerText = secondToMinuteText(currentCount);
                        timerBodyTextView.setText(timerText);
                    }
                });
            }
        };
        timer.schedule(mTimerTask, 0, 1000);
    }

    private String secondToMinuteText(int currentCount) {
        String result;
        int min = currentCount / 60;
        int sec = currentCount % 60;

        String minText = (min < 10) ? "0" + min : String.valueOf(min);
        String secText = (sec < 10) ? "0" + sec : String.valueOf(sec);

        result = minText + ":" + secText;
        return result;
    }

    private void stopTimerTask() {
        String timerText = secondToMinuteText(currentCount);
        timerHeaderTextView.setText(timerText);

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    /* 거리구하기 */

    private void setDistanceTextView(LatLng preLatLng, LatLng curLatLng) {

        TextView runningDistanceTextView = (TextView) findViewById(R.id.textview_distance_running_OST);
        TextView notRunningDistanceTextView = (TextView) findViewById(R.id.textview_distance_not_running_OST);

        double distance = getDistance(preLatLng, curLatLng);
        currentDistance += distance;
        setRoundedDistance();

        // UI
        runningDistanceTextView.setText(String.valueOf(roundedDistance));
        notRunningDistanceTextView.setText(String.valueOf(roundedDistance));
    }

    private void setRoundedDistance() {
        DecimalFormat form = new DecimalFormat("#.##");
        roundedDistance = Double.parseDouble(form.format(currentDistance));
    }

    // km 반환
    private double getDistance(LatLng startLatLng, LatLng endLatLng) {
        double startLat = startLatLng.latitude;
        double startLng = startLatLng.longitude;
        double endLat = endLatLng.latitude;
        double endLng = endLatLng.longitude;

        double theta = startLng - endLng;
        double dist = Math.sin(deg2rad(startLat)) * Math.sin(deg2rad(endLat)) + Math.cos(deg2rad(startLat)) * Math.cos(deg2rad(endLat)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        return dist;
    }

    // Degree to Radian
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // Radian to Degree
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    // 속도 구하기
    private void setPaceTextView(double currentDistance, int currentCount) {
        TextView runningPaceTextView = (TextView) findViewById(R.id.textview_pace_running_OST);
        TextView notRunningPaceTextView = (TextView) findViewById(R.id.textview_pace_not_running_OST);

        setCurrentPace(currentDistance, currentCount);
        runningPaceTextView.setText(String.valueOf(currentPace));
        notRunningPaceTextView.setText(String.valueOf(currentPace));
    }

    private void setCurrentPace(double currentDistance, int count) {
        double hour = (double) count / 3600;
        double pace = hour == 0 ? 0 : currentDistance / hour;

        DecimalFormat form = new DecimalFormat("#.##");
        currentPace = Double.parseDouble(form.format(pace));
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        timer.cancel();
        if (mFusedLocationClient != null) {
            Log.d("RA onStop : ", "call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
        isSaid = false;
        start = true;
    }

}