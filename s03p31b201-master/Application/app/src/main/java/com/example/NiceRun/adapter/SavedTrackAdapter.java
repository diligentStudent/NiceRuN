package com.example.NiceRun.adapter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.NiceRun.DTO.Track;
import com.example.NiceRun.DTO.Trackinfo;
import com.example.NiceRun.FindingSavedTrackActivity;
import com.example.NiceRun.R;
import com.example.NiceRun.TMapActivity;
import com.example.NiceRun.lib.DialogLib;
import com.example.NiceRun.main.running.GpsTracker;
import com.google.android.gms.maps.model.LatLng;
import com.example.NiceRun.RunningOSTSpashActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.example.NiceRun.RunningActivity;
import com.example.NiceRun.RunningOnSavedTrackActivity;
import com.example.NiceRun.RunningSplashActivity;
import com.skt.Tmap.TMapPoint;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SavedTrackAdapter extends RecyclerView.Adapter<SavedTrackAdapter.ViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    public static final int SAVED_TRACK_ADAPTER = 3001;

    private Context mContext;
    private int resource;
    private Activity mActivity;

    private List<Track> trackList;

    private GpsTracker gpsTracker;
    private double latitude, longitude;
    private TMapPoint tMapPointStart; // 출발지
    private TMapPoint tMapPointEnd; // 목적지

    private JsonParser jsonParser;

    public SavedTrackAdapter(Context mContext, int resource, List<Track> trackList) {
        this.mContext = mContext;
        this.resource = resource;
        this.trackList = trackList;
    }

    public void setItemList(List<Track> itemList){
        this.trackList = itemList;
        notifyDataSetChanged();
    }


    // 0번 판 깔기
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_tab_savedtracks_card, parent, false);

        gpsTracker = new GpsTracker(mContext);
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        tMapPointStart = new TMapPoint(latitude,longitude); // lat, lon

        return new ViewHolder(view);
    }

    // 1. 판 깔기 - 레이아웃이랑 연결
    static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView track_img;
        public TextView dist;
        public TextView location;
        public Button startBtn;

        public ViewHolder(View itemView){
            super(itemView);
            track_img = (ImageView) itemView.findViewById(R.id.track_img);
            dist = (TextView) itemView.findViewById(R.id.dist);
            location = (TextView) itemView.findViewById(R.id.location);
            startBtn = (Button) itemView.findViewById(R.id.startBtn);

        }
    }


    // 2. 받아온 값 연결
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final Track item = trackList.get(position);
        Log.d("adapteritem",item.toString());

        viewHolder.dist.setText("거리   " + String.valueOf(item.getDist()) + " Km");
        viewHolder.location.setText("지역   "+ item.getLocation());


        setImage(viewHolder.track_img, item.getTrackimg());

        Log.d("position", String.valueOf(position));
        Log.d("iiiiiiiiiiii",item.getFilename());

        String[] trackFile = item.getFilename().split("/track/");
        Log.d("trackfile",trackFile[1]);


        tMapPointStart = new TMapPoint(latitude,longitude); // lat, lon



        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogLib.getInstance().showDetailDialog(mContext, item.getTrackimg());
            }
        });

        viewHolder.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();





                Thread uThread = new Thread() {
                    @Override
                    public void run(){
                        try{
                            //서버에 올려둔 이미지 URL
                            URL url = new URL("https://k3b201.p.ssafy.io/track/"+trackFile[1]); // 파일 없을 때 처리

                            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                            conn.setDoInput(true);
                            conn.connect();

                            InputStream is = conn.getInputStream();
                            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                            BufferedReader br = new BufferedReader(isr);
                            StringBuilder response = new StringBuilder();
                            String line = null;
                            while((line = br.readLine()) != null) {
                                response.append(line);
                            }

                            Log.d("response",response.toString());

                            br.close();
                            is.close();


                            JSONArray jsonArray = new JSONArray(response.toString());



                            makeDirectory("/NiceRuN");
                            makeDirectory("/NiceRuN/track");

                            String jsonFileName = Environment.getExternalStorageDirectory().toString() + "/NiceRuN/track/" + trackFile[1];

                            // file close 자동
                            try (FileWriter file = new FileWriter(jsonFileName)) {
                                file.write(jsonArray.toString());
                                file.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }catch (MalformedURLException e){
                            e.printStackTrace();
                        }catch (IOException e){
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                uThread.start(); // 작업 Thread 실행
                try{
                    uThread.join();
                }catch (InterruptedException e){

                    e.printStackTrace();

                }

                ReadJSON(trackFile[1]);

                if(isCloseBetweenPosition(tMapPointStart,tMapPointEnd)){  // 가까우면 true
                    Intent intent = new Intent(view.getContext(), RunningOSTSpashActivity.class);
                    intent.putExtra("filename",trackFile[1]);
                    context.startActivity(intent);
                } else{
                    Intent intent = new Intent(view.getContext(), TMapActivity.class);
                    intent.putExtra("filename",trackFile[1]);
                    context.startActivity(intent);
                }




            }
        });
    }
    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyAdapter", "onActivityResult");
    }

    // 3. 값 받아오기
    public void addItemlist(List<Track> trackList) {
        this.trackList.addAll(trackList);
        Log.d("adaptertracklist", trackList.toString());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.trackList.size();
    }

    private void setImage(ImageView imageView, String fileName){
        String[] file = fileName.split("/image/");
        Picasso.get().load("https://k3b201.p.ssafy.io/image/"+file[1]).into(imageView);
    }


    public void setItme(Track newItem){
        for(int i=0; i < trackList.size(); i++){
            Track item = trackList.get(i);
            if(item.getTrackid().equals(newItem.getTrackid())){
                trackList.remove(i);
                notifyItemChanged(i);
                break;
            }
        }
    }


    private File makeDirectory(String folderName) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
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

}
