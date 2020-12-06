package com.example.NiceRun.main.running;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.NiceRun.API.RecommandTrackAPI;
import com.example.NiceRun.DTO.Mytrack;
import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.DTO.Sns;
import com.example.NiceRun.DTO.Track;
import com.example.NiceRun.DTO.Trackinfo;
import com.example.NiceRun.MyApp;
import com.example.NiceRun.R;
import com.example.NiceRun.Util.PreferenceManager;
import com.example.NiceRun.adapter.DaytrackAdapter;
import com.example.NiceRun.adapter.RecommandTrackAdapter;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecommandTracksTab extends Fragment {
    Context context;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecommandTrackAdapter adapter;

    TextView whereLocation;

    // init
    private final String TAG = getClass().getSimpleName();
    public String baseUrl = "https://k3b201.p.ssafy.io/api/";
    private RecommandTrackAPI recommandTrackAPI;

    private GpsTracker gpsTracker;

    JSONObject jObject;
    TextView noDataText;

    //    reponse.body()
    private List<Track> trackList = new ArrayList<>();
    private List<Boolean> savedList = new ArrayList<>();
    private List<Sns> snsList = new ArrayList<>();
    private List<Mytrack> mytrackList = new ArrayList<>();

    private String id;

    String currentLocalAddress;
    String cityName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tab_recommandtracks, container, false);
        initMyAPI(baseUrl);
        id = PreferenceManager.getString(getActivity(), "email");
        whereLocation = (TextView) rootView.findViewById(R.id.whereLocation);

        gpsTracker = new GpsTracker(getActivity());
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        if (latitude == 0 && longitude == 0) {
            latitude = 36.350762;
            longitude = 127.300601;
        }

        Log.d(TAG, "받아오는 주소값 " + latitude + " " + longitude);

        LatLng currentPosition
                = new LatLng(latitude, longitude);

        //String id = PreferenceManager.getString(getActivity(), "email");

        currentLocalAddress = getCurrentAddress(currentPosition);
        /*
        if(currentLocalAddress == null) {
            currentLocalAddress = "";
            cityName = makeCityName(currentLocalAddress);
        }else{
            cityName = makeCityName(currentLocalAddress);
        }
         */
        cityName = makeCityName(currentLocalAddress);
        whereLocation.setText(cityName);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        MyApp myApp = ((MyApp) getActivity().getApplication());
        Sns currentSns= myApp.getSns();
        if(adapter != null && currentSns != null){
            adapter.setItme(currentSns);
            myApp.setTrackinfo(null);
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noDataText = (TextView) view.findViewById(R.id.recommand_no_data);
        //recyclerView
        recyclerView = (RecyclerView) view.findViewById(R.id.recommand_recycler_view);
        //set
        setRecyclerView();
        //데이터 조회
        listInfo();
    }

    private void setRecyclerView() {
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        //adapter
        adapter = new RecommandTrackAdapter(getContext(), R.layout.fragment_tab_recommandtracks_card, new ArrayList<Track>(), new ArrayList<Sns>(), new ArrayList<Boolean>(), new ArrayList<Mytrack>(), id);
        recyclerView.setAdapter(adapter);
    }

    public String getCurrentAddress(LatLng latlng) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(context, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(context, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            //Toast.makeText(context, "주소 미발견", Toast.LENGTH_LONG).show();
            return "대한민국 대전광역시";

        } else {
            Address address = addresses.get(0);
            String showAdress = address.getAddressLine(0).toString();
            Log.d(TAG, showAdress);
            return showAdress;
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

    private void listInfo() {
        Log.d("여기는 lnfolist", "들어옴??");
        Log.d("보낼 값", cityName + " " + id);
        Log.d("여기는 이메일ㄹㄹㄹㄹ", id);

        Call<BasicResponse> getCall = recommandTrackAPI.getRecommandtrack(id, cityName);

        getCall.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    String flag = response.body().getData();
                    if (flag.equals("success")) {
                        BasicResponse data = response.body();
                        if(data.getData().equals("success")) {
                            trackList = data.getObject().getTrackList();
                            savedList = data.getObject().getSavedList();
                            snsList = data.getObject().getSnsList();
                            mytrackList = data.getObject().getMytrackList();
                            adapter.addItemlist(trackList, snsList, savedList, mytrackList);
                            noDataText.setVisibility(View.GONE);
                        }else{
                            Toast myToast = Toast.makeText(getActivity().getApplicationContext(), "해당지역에 공유된 트랙이 없습니다.", Toast.LENGTH_SHORT);
                            myToast.show();
                        }
                    } else {
                        noDataText.setVisibility(View.VISIBLE);

                    }
                } else {
                    Log.d("실퍀ㅋㅋㅋㅋㅋ", "Status Code : " + response.code());
                    Log.d(TAG, response.errorBody().toString());
                    Log.d(TAG, call.request().body().toString());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d("에렄ㅋㅋㅋㅋ", "Fail msg : " + t.getMessage());
            }
        });

    }


    public void initMyAPI(String baseUrl) {

        Log.d(TAG, "initMyAPI : " + baseUrl);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()) // addConverterFactory로 gson converter를 생성한다. gson은 json을 자바 클래스로 바꾸는데 사용
                .build();

        recommandTrackAPI = retrofit.create(RecommandTrackAPI.class);
    }

}