package com.example.NiceRun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;


public class TMapActivity extends AppCompatActivity {
    private Context mContext;

    private TextToSpeech tts;

    private TMapView tmapview;
    //    private static String mApiKey = "l7xxb9b86d78bbd04a24b067b17932901d82";
    private static String mApiKey = "l7xxc82f1041757740d6bd705aa86084e7ca";

    private TMapPoint tMapPointStart; // 출발지
    private TMapPoint tMapPointEnd; // 목적지

    private TMapCircle tcircle;

    private ArrayList<String> navi;
    private ArrayList<double[]> point;

    private String jsonFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t_map);

        navi = new ArrayList<>();
        point = new ArrayList<>();

        Intent intent = getIntent();
        jsonFileName = intent.getStringExtra("filename");
        ReadJSON(jsonFileName);

        mContext = this;

        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.map_view);
        tmapview = new TMapView(mContext);

        linearLayout.addView(tmapview);
        tmapview.setSKTMapApiKey(mApiKey);

        /* 현재 보는 방향*/
//        tmapview.setCompassMode(true);
        /* 현위치 아이콘표시 */
        tmapview.setIconVisibility(true);
        /*줌 레벨*/
        tmapview.setZoomLevel(15);
        /* 지도 타입 */
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        /*언어 설정 */
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        tmapview.setCenterPoint(127.298040,36.350062); // 출발지

        // tts
        tts = new TextToSpeech(TMapActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //사용할 언어를 설정
                    int result = tts.setLanguage(Locale.KOREA);
                    //언어 데이터가 없거나 혹은 언어가 지원하지 않으면...
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.d("sss","error");
                        Toast.makeText(TMapActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        //음성 톤
                        tts.setPitch(0.7f);
                        //읽는 속도
                        tts.setSpeechRate(1.2f);
                    }
                }
            }
        });

        final LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자(실내에선 NETWORK_PROVIDER 권장)
                1000, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mLocationListener);
    }

    public void find_path() {
        new Thread(new Runnable() {
            public void run() {
                //마커생성
                TMapMarkerItem markerItem1 = new TMapMarkerItem(); // 사용자 정의 풍선뷰와 애니메이션 지도 마커를 표시하기 위한 클래스
                TMapMarkerItem markerItem2 = new TMapMarkerItem();

                // 마커 아이콘
                Context context = getApplicationContext();
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin);
                Bitmap bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.end);

                // 3번째 파라미터 true == 지도 이동 Animation 사용
                tmapview.setCenterPoint(tMapPointStart.getLongitude(), tMapPointStart.getLatitude());

                //마커1(출발지)
                markerItem1.setIcon(bitmap); // 마커 아이콘 지정
                markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                markerItem1.setTMapPoint(tMapPointStart); // 마커의 좌표 지정
                markerItem1.setName("현재위치"); // 마커의 타이틀 지정

                //마커2(도착지)
                markerItem2.setIcon(bitmap2); // 마커 아이콘 지정
                markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                markerItem2.setTMapPoint(tMapPointEnd); // 마커의 좌표 지정
                markerItem2.setName("시작지점"); // 마커의 타이틀 지정

                //경로안내
                try {
                    TMapData tmapdata = new TMapData(); // TMapData: 지도 데이터를 관리하는 클래스
                    TMapPolyLine tMapPolyLine = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd); // findPathDataWithType: 보행자 경로 요청
                    tMapPolyLine.setLineColor(Color.BLUE); // TMapPolyLine: 지도 위에 선 그어주는 클래스
                    tMapPolyLine.setLineWidth(5);
                    ArrayList<TMapPoint> alTMapPoint = new ArrayList<TMapPoint>(); // TMapPoint: 위도, 경도 좌표를 나타내는 클래스
                    for (int i = 0; i < alTMapPoint.size(); i++) {
                        tMapPolyLine.addLinePoint(alTMapPoint.get(i));
                        Log.d("test",i+" "+ alTMapPoint.get(i).toString());
                    }
                    tmapview.addTMapPolyLine("Line1", tMapPolyLine);
                    tmapview.addMarkerItem("markerItem1", markerItem1); // 지도에 마커 추가
                    tmapview.addMarkerItem("markerItem2", markerItem2); // 지도에 마커 추가

                    if(isFirst){
                        naviGuide();
                        isFirst = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean isFirst = true;
    private static int index = 0;
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            //현재위치의 좌표를 알수있는 부분
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                tmapview.setCenterPoint(longitude, latitude);
                tmapview.setLocationPoint(longitude, latitude);
                tMapPointStart = new TMapPoint(latitude, longitude); // 출발지

                if(navi.size() !=0 && point.size() != 0 && navi.size() > index){
                    TMapPoint tempPoint = new TMapPoint(point.get(index)[1],point.get(index)[0]);

                    if(isCloseBetweenPosition(tMapPointStart,tempPoint)){ // 도착지점 여기서 다 처리해줌
                        tts.speak(navi.get(index), TextToSpeech.QUEUE_FLUSH,null);
                        Toast.makeText(mContext,navi.get(index).toString(),Toast.LENGTH_SHORT).show();

                        index++;

                        if(navi.size() == index){
                            Toast.makeText(mContext, "도착", Toast.LENGTH_SHORT).show();

                            Intent intent2 = new Intent(mContext,FindingSavedTrackActivity.class);
                            intent2.putExtra("filename",jsonFileName);
                            startActivity(intent2);

                            finish();
                        }

                        if(navi.size() > index){
                            TMapPoint tempPoint2 = new TMapPoint(point.get(index)[1],point.get(index)[0]);
                            tcircle = new TMapCircle();
                            tcircle.setCenterPoint(tempPoint2);
                            tcircle.setRadius(10); // 미터
                            tcircle.setLineColor(Color.BLUE);
                            tcircle.setAreaColor(Color.BLUE);
                            tcircle.setCircleWidth(2); // 선 두께
                            tcircle.setAreaAlpha(50); // 투명도
                            tcircle.setRadiusVisible(true);
                            tmapview.addTMapCircle("endPointCircle",tcircle);
                        }
                    }
                }
                find_path();
            }

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public void naviGuide() {

        new Thread(new Runnable() {
            public void run() {
                TMapData tmapdata2 = new TMapData();
                tmapview.zoomToTMapPoint ( tMapPointStart,tMapPointEnd );  // 자동 zoomlevel 조정

                tmapdata2.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataAllListenerCallback() {
                    @Override
                    public void onFindPathDataAll(Document document) {
                        Element root = document.getDocumentElement();
                        NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");

                        boolean first = true;
                        for (int i = 0; i < nodeListPlacemark.getLength(); i++) {
                            NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes(); // xml로 받는다

                            double[] p = new double[2];
                            String comment =  "";

                            for (int j = 0; j < nodeListPlacemarkItem.getLength(); j++) {
                                if (nodeListPlacemarkItem.item(j).getNodeName().equals("description")) {
                                    comment = nodeListPlacemarkItem.item(j).getTextContent().trim();
                                } else if (nodeListPlacemarkItem.item(j).getNodeName().equals("Point")) {
                                    String location[] = nodeListPlacemarkItem.item(j).getTextContent().trim().split(",");
                                    double longitude = Double.parseDouble(location[0]);
                                    double latitude = Double.parseDouble(location[1]);
                                    p[0] = longitude;
                                    p[1] = latitude;
                                    if (first) {
                                        TMapPoint tempPoint = new TMapPoint(p[1],p[0]);
                                        tcircle = new TMapCircle();
                                        tcircle.setCenterPoint(tempPoint);
                                        tcircle.setRadius(10); // 미터
                                        tcircle.setLineColor(Color.BLUE);
                                        tcircle.setAreaColor(Color.BLUE);
                                        tcircle.setCircleWidth(2); // 선 두께
                                        tcircle.setAreaAlpha(50); // 투명도
                                        tcircle.setRadiusVisible(true);
                                        tmapview.addTMapCircle("endPointCircle",tcircle);
                                        first = false;
                                    }
                                }
                            }

                            if(p[0] != 0.0){
                                point.add(new double[]{p[0],p[1]});
                                navi.add(comment);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    // m 반환
    private int getDistance(TMapPoint tMapPointStart, TMapPoint tMapPointEnd) {
        double startLat = tMapPointStart.getLatitude();
        double startLng = tMapPointStart.getLongitude();
        double endLat = tMapPointEnd.getLatitude();
        double endLng = tMapPointEnd.getLongitude();

        double theta = startLng - endLng;
        double dist = Math.sin(deg2rad(startLat)) * Math.sin(deg2rad(endLat)) + Math.cos(deg2rad(startLat)) * Math.cos(deg2rad(endLat)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        int distMeter = (int) (dist * 1000);
        return distMeter;
    }

    // Degree to Radian
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // Radian to Degree
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    // true : 가깝다
    private boolean isCloseBetweenPosition(TMapPoint tMapPointStart, TMapPoint tMapPointEnd) {
        boolean result = false;
        double dist = getDistance(tMapPointStart, tMapPointEnd);
        // 10m 이하
        if (dist <= 10) {
            result = true;
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public void ReadJSON(String filename) {
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NiceRuN/track/" + filename;

        StringBuffer strBuffer = new StringBuffer();
        String line="";
        try{
            InputStream is = new FileInputStream(filepath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while((line=reader.readLine())!=null){
                strBuffer.append(line+"\n");
            }
            reader.close();
            is.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        try {
            JSONArray jsonArray = new JSONArray(strBuffer.toString());

            String st = "";
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String lat = jsonObject.getString("lat");
            String lon = jsonObject.getString("lon");

            tMapPointEnd = new TMapPoint(Double.parseDouble(lat), Double.parseDouble(lon) ); // 목적지
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void checkWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (permissionCheck2 != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
    }

}