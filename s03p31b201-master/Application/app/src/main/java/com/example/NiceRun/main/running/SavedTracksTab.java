package com.example.NiceRun.main.running;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.NiceRun.API.RecommandTrackAPI;
import com.example.NiceRun.API.SavedTrackAPI;
import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.DTO.Track;
import com.example.NiceRun.DTO.Trackinfo;
import com.example.NiceRun.MyApp;
import com.example.NiceRun.R;
import com.example.NiceRun.Util.PreferenceManager;
import com.example.NiceRun.adapter.RecommandTrackAdapter;
import com.example.NiceRun.adapter.SavedTrackAdapter;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.NiceRun.MainActivity.mContext;

public class SavedTracksTab extends Fragment {
    Context context;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    SavedTrackAdapter adapter;

    private final String TAG = getClass().getSimpleName();
    public String baseUrl = "https://k3b201.p.ssafy.io/api/";
    private SavedTrackAPI savedTrackAPI;

    private List<Trackinfo> itemList = new ArrayList<Trackinfo>();



    JSONObject jObject;

    TextView noDataText;
    String id;

    public static SavedTracksTab newInstance(){
        return new SavedTracksTab();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tab_savedtracks, container, false);



        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"resume되는즁???");
        id = PreferenceManager.getString(mContext, "email");
        /*
        MyApp myApp = ((MyApp) mContext.getApplicationContext());
        List<Track> currentTrack = myApp.getTrack();
        if(adapter != null && currentTrack != null){
            adapter.setItemList(currentTrack);
            myApp.setTrack(null);
            if(adapter.getItemCount() == 0){
                noDataText.setVisibility(View.VISIBLE);
            }
        }

         */
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d(TAG,"재로딩되는중???");
            onResume();
            initMyAPI(baseUrl);
            listInfo();
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noDataText = (TextView) view.findViewById(R.id.saved_no_data);
        //recyclerView
        recyclerView = (RecyclerView) view.findViewById(R.id.saved_recycler_view);
        //set
        setRecyclerView();
        Log.d("aaa","saved Track call");
        // API
        //initMyAPI(baseUrl);
        //데이터 조회
        //listInfo();

    }


    private void setRecyclerView() {
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        //adapter
        //adapter = new RecommandTrackAdapter(context,R.layout.fragment_tab_recommandtracks_card, new ArrayList<>());
        adapter = new SavedTrackAdapter(getContext(),R.layout.fragment_tab_savedtracks_card, new ArrayList<Track>());
        //adapter = new SavedTrackAdapter(getContext(),R.layout.fragment_tab_savedtracks_card);
        recyclerView.setAdapter(adapter);
    }

    private void listInfo(){
        Log.d("444","들어옴??");
        //주소
//        gpsTracker = new GpsTracker(getActivity());
//        double latitude = gpsTracker.getLatitude();
//        double longitude = gpsTracker.getLongitude();
//        Log.d("주소",latitude+""+longitude);
        //String address = getCurrentAddress(new LatLng(latitude, longitude);

        Call<BasicResponse> postCall = savedTrackAPI.showtrack(id);
        Log.d("api",postCall.request().toString());

        postCall.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    BasicResponse dto = response.body();
                    if(dto.getData().equals("success")){
                        List<Track> list = response.body().getObject().getTrackList();
                        adapter.setItemList(list);
                        //adapter.addItemlist(list);
                        noDataText.setVisibility(View.GONE);
                    }else{
                        noDataText.setVisibility(View.VISIBLE);
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

    public void initMyAPI(String baseUrl) {
        Log.d(TAG, "initMyAPI : " + baseUrl);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()) // addConverterFactory로 gson converter를 생성한다. gson은 json을 자바 클래스로 바꾸는데 사용
                .build();
        savedTrackAPI = retrofit.create(SavedTrackAPI.class);
    }


}
