package com.example.NiceRun;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.NiceRun.API.RunningAPI;
import com.example.NiceRun.API.TrackApi;
import com.example.NiceRun.API.UserAPI;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


//import org.json.JSONException;
import org.json.simple.*;
//JSONArray;
//import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
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

public class RunningActivity extends AppCompatActivity implements OnMapReadyCallback,
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        isRunning = true;
        setStartTime();

        /* 버튼 클릭 리스너 */
        ButtonOnClickListener onClickListener = new ButtonOnClickListener();

        ImageButton startButton = (ImageButton) findViewById(R.id.button_start);
        ImageButton pauseButton = (ImageButton) findViewById(R.id.button_pause);
        ImageButton stopButton = (ImageButton) findViewById(R.id.button_stop);
        startButton.setOnClickListener(onClickListener);
        pauseButton.setOnClickListener(onClickListener);
        stopButton.setOnClickListener(onClickListener);

        /* 타이머 */
        timerBodyTextView = (TextView) findViewById(R.id.textview_body_timer);
        timerHeaderTextView = (TextView) findViewById(R.id.textview_header_timer);
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
                .findFragmentById(R.id.map_running);
        mapFragment.getMapAsync(this);
        //  파일 권한 TODO: Main Activity로 분할
        checkWritePermission();
        checkReadPermission();

        initMyAPI(baseUrl);
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

                //                String markerTitle = getCurrentAddress(currentPosition);
                String markerTitle = "현재위치"; // custom
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d("RA onLocationResult : ", markerSnippet);

                // 마지막 위치 넣을지 말지
                if (mTrack.size() != 0) {
                    LatLng lastPos = mTrack.get(mTrack.size() - 1);
                    if (isSaveScreen && getDistance(lastPos, currentLatLng) >= 0.004 && getDistance(lastPos, currentLatLng) <= 0.02) {
                        mTrack.add(currentLatLng);
                    }
                }
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
            LinearLayout runningHeaderLayout = (LinearLayout) findViewById(R.id.layout_header_running);
            LinearLayout runningBodyLayout = (LinearLayout) findViewById(R.id.layout_body_running);
            LinearLayout runningFooterLayout = (LinearLayout) findViewById(R.id.layout_footer_running);
            LinearLayout notRunningHeaderLayout = (LinearLayout) findViewById(R.id.layout_header_not_running);
            LinearLayout notRunningBodyLayout = (LinearLayout) findViewById(R.id.layout_body_not_running);
            LinearLayout notRunningFooterLayout = (LinearLayout) findViewById(R.id.layout_footer_not_running);

            switch (view.getId()) {
                case R.id.button_pause:

                    if (mGoogleMap != null) {
                        if (currentLatLng == null) {
                            setDefaultLocation(36.350762, 127.300601);
                        } else {
                            int thickness = 10;
                            drawPolyLine(thickness);
                            // camera update
                            mGoogleMap.setOnMapLoadedCallback(RunningActivity.this);

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


                case R.id.button_start:
                    notRunningHeaderLayout.setVisibility(View.GONE);
                    notRunningFooterLayout.setVisibility(View.GONE);
                    notRunningBodyLayout.setVisibility(View.GONE);

                    runningHeaderLayout.setVisibility(View.VISIBLE);
                    runningFooterLayout.setVisibility(View.VISIBLE);
                    runningBodyLayout.setVisibility(View.VISIBLE);

                    isRunning = !isRunning;
                    startTimerTask();
                    break;

                case R.id.button_stop:
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
        if (mTrack.size() < 7) {
            Toast.makeText(this, "운동 거리가 너무 짧습니다!", Toast.LENGTH_LONG).show();
            RunningActivity.this.finish(); // activity 종료
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
                        savePoints();
                        saveScreen();
                        RunningActivity.this.finish(); // activity 종료
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

    // met 공식 : https://www.donga.com/news/It/article/all/20190209/94032667/1
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
        boolean snsdownload = false;
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

    private boolean savePoints() {
        if (mTrack.isEmpty()) {
            return false;
        }
        JSONArray jsonArray = new JSONArray();
        for (LatLng l : mTrack) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lat", l.latitude);
            jsonObject.put("lon", l.longitude);
            jsonArray.add(jsonObject);
        }

        makeDirectory("/NiceRuN");
        makeDirectory("/NiceRuN/track");

        jsonFileName = System.currentTimeMillis() + ".json";
        jsonFileName = Environment.getExternalStorageDirectory().toString() + "/NiceRuN/track/" + jsonFileName;

        // file close 자동
        try (FileWriter file = new FileWriter(jsonFileName)) {
            file.write(jsonArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
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
                    addMarker(mTrack.get(0), BitmapDescriptorFactory.HUE_BLUE);
                    addMarker(mTrack.get(mTrack.size() - 1), BitmapDescriptorFactory.HUE_RED);
                    mGoogleMap.snapshot(this);
                }
            }
        }
    }

    private void addMarker(LatLng latLng, float color) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));

        currentMarker = mGoogleMap.addMarker(markerOptions);
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

                LinearLayout mapFrameLayout = (LinearLayout) findViewById(R.id.layout_body_not_running);
                int width = mapFrameLayout.getWidth();
                int height = mapFrameLayout.getHeight();
                int padding = (int) (width * 0.12); // 12%

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng l : mTrack) {
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

    private void drawPolyLine(int thickness) {
        // 1. 점들을 선으로 만들어서 지도에 보여주기
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(mTrack);
        polylineOptions.width(thickness).color(Color.RED);

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

        TextView runningDistanceTextView = (TextView) findViewById(R.id.textview_distance_running);
        TextView notRunningDistanceTextView = (TextView) findViewById(R.id.textview_distance_not_running);

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
        TextView runningPaceTextView = (TextView) findViewById(R.id.textview_pace_running);
        TextView notRunningPaceTextView = (TextView) findViewById(R.id.textview_pace_not_running);

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
        timer.cancel();
        if (mFusedLocationClient != null) {
            Log.d("RA onStop : ", "call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

}