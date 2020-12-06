package com.example.NiceRun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.NiceRun.API.RunningAPI;
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


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FindingSavedTrackActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLoadedCallback {
    /* toggle */
    boolean isSaveScreen = false;

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
    List<LatLng> mSavedTrack = new ArrayList<>();


    // API 요청
    RunningAPI runningAPI;
    private String baseUrl = "https://k3b201.p.ssafy.io/api/";

    // button
    ImageButton moveButton;

    // json file
    String jsonFileName;

    // tts
    private TextToSpeech tts;
    private boolean isSaid;
    private boolean ttsisReady = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_saved_track);

        isSaid = false;
        /* intent json*/
        Intent intent = getIntent();
        jsonFileName = intent.getStringExtra("filename");
        System.out.println("============jsonFileName===========");
        System.out.println(jsonFileName);
        ReadJSON(jsonFileName);
        /* tts */
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //사용할 언어를 설정
                    int result = tts.setLanguage(Locale.KOREA);
                    //언어 데이터가 없거나 혹은 언어가 지원하지 않으면...
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.d("sss", "error");
                        Toast.makeText(FindingSavedTrackActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        //음성 톤
                        tts.setPitch(0.7f);
                        //읽는 속도
                        tts.setSpeechRate(1.2f);
                        Log.d("tts", "ok");
                        ttsisReady=true;
                    }
                }
            }
        });

        /* 버튼 클릭 리스너 */
        ButtonOnClickListener onClickListener = new ButtonOnClickListener();
        moveButton = (ImageButton) findViewById(R.id.button_move_run_saved_track_activity);
        moveButton.setOnClickListener(onClickListener);



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
                .findFragmentById(R.id.map_find_saved_track);
        mapFragment.getMapAsync(this);

        checkReadPermission();

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

        // 트랙 그려버리기
        drawPolyLine(15, Color.BLUE, mSavedTrack);
        // 시작점 마커 찍기
        addStartMarker(mSavedTrack);


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
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                Log.d("시작점과 거리", String.valueOf(getDistance(currentLatLng, mSavedTrack.get(0))));
                System.out.println("====isSaid====");
                System.out.println(isSaid);
                if (isCloseBetweenPosition(currentLatLng, mSavedTrack.get(0))) {
                    if (!isSaid && ttsisReady) {
                        isSaid = true;
                        String text = "시작지점에 도착했습니다.";
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    //버튼 보이게
                    moveButton.setVisibility(View.VISIBLE);
                } else {
                    isSaid = false;
                    moveButton.setVisibility(View.GONE);
                }


                // 마지막 위치 넣을지 말지
                mGoogleMap.setOnMapLoadedCallback(FindingSavedTrackActivity.this);
            }
        }
    };

    public void ReadJSON(String filename) {
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NiceRuN/track/" + filename;
        System.out.println(filepath);
        System.out.println("=========================Running========================");
        StringBuffer strBuffer = new StringBuffer();
        String line = "";
        mSavedTrack.clear();
        try {
            InputStream is = new FileInputStream(filepath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                strBuffer.append(line + "\n");
            }

            org.json.JSONArray jsonArray = new org.json.JSONArray(strBuffer.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String lat = jsonObject.getString("lat");
                String lon = jsonObject.getString("lon");
                mSavedTrack.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));
            }

            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapLoaded() {
        if (mGoogleMap != null) {
            if (currentLatLng != null) {
                setCurrentLocation(currentLatLng);
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
            FrameLayout mapFrameLayout = (FrameLayout) findViewById(R.id.map_find_saved_track);
            int width = mapFrameLayout.getWidth();
            int height = mapFrameLayout.getHeight();
            int padding = (int) (width * 0.3); // 30%

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng l : mSavedTrack) {
                builder.include(l);
            }
            builder.include(currentLatLng);
            LatLngBounds bounds = builder.build();

            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    // true : 가깝다
    private boolean isCloseBetweenPosition(LatLng preLatLng, LatLng currentLatLng) {
        boolean result = false;
        double dist = getDistance(preLatLng, currentLatLng);
        // 10m 이하
        if (dist <= 0.01) {
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
            switch (view.getId()) {
                case R.id.button_move_run_saved_track_activity:
                    FindingSavedTrackActivity.this.finish(); // activity 종료

                    Intent intent = new Intent(getApplication(), RunningOSTSpashActivity.class);
                    intent.putExtra("filename", jsonFileName);
                    //RequestActivity 시작
                    startActivity(intent);
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


    private void addStartMarker(List<LatLng> mSavedTrack) {
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng ll = mSavedTrack.get(0);
        markerOptions.position(ll);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mGoogleMap.addMarker(markerOptions);
    }

    private void drawPolyLine(int thickness, int color, List track) {
        // 1. 점들을 선으로 만들어서 지도에 보여주기
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(track);
        polylineOptions.width(thickness).color(color);

        mGoogleMap.addPolyline(polylineOptions);
    }


    // m 반환
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

//        int distMeter = (int) (dist * 1000);
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


    @Override
    protected void onStop() {
        super.onStop();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFusedLocationClient != null) {
            Log.d("RA onStop : ", "call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }

        isSaid = false;

    }
}