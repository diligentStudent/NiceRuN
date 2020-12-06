package com.example.NiceRun.adapter;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.NiceRun.API.CalendarApi;
import com.example.NiceRun.DTO.DownloadTrack;
import com.example.NiceRun.DTO.Trackinfo;
import com.example.NiceRun.R;
import com.example.NiceRun.lib.DialogLib;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DaytrackAdapter extends RecyclerView.Adapter<DaytrackAdapter.ViewHolder> {
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private int resource;
    private List<Trackinfo> itemList;
    List<Boolean> uploadList;
    List<Boolean> downloadList;
    private String email;



    public DaytrackAdapter(Context context, int resource, List<Trackinfo> itemList, List<Boolean> uploadList, List<Boolean> downloadList, String email) {
        this.context = context;
        this.resource = resource;
        this.itemList = itemList;
        this.uploadList = uploadList;
        this.downloadList = downloadList;
        this.email = email;
    }

    public void setItme(Trackinfo newItem){
        for(int i=0; i < itemList.size(); i++){
            Trackinfo item = itemList.get(i);
            if(item.getTrackinfoid() == newItem.getTrackinfoid()){
                itemList.set(i, newItem);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addItemlist(List<Trackinfo> itemList, List<Boolean> uploadList, List<Boolean> downloadList){
        this.itemList.addAll(itemList);
        this.downloadList.addAll(downloadList);
        this.uploadList.addAll(uploadList);
        notifyDataSetChanged();
    }
    private void changeItemUpload(int position, boolean b) {
        for (int i = 0; i < downloadList.size(); i++) {
            if(i == position){
                uploadList.set(i,b);
                notifyDataSetChanged();
                break;
            }
        }
    }

    private void changeItemDownload(int position, boolean b){
        for (int i = 0; i < downloadList.size(); i++) {
            if(i == position){
                downloadList.set(i,b);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_calendar_myinfo_card, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final Trackinfo item = itemList.get(position);
        final Boolean isUpload = uploadList.get(position);
        final Boolean isDownload = downloadList.get(position);
        Log.d(TAG, "getView " + item);

        /*notice case1 다른 사람이 공유한 트랙을 받아서 뛰는 경우
                 case2 내가 개척한 트랙인 경우
         */
        // notice 다른 사람이 공유한 && 내가 저장한 트랙을 받아서 뛰는 경우에는 공유 X 저장 X upload T download T
        if(isUpload && isDownload){
            viewHolder.itemUpload.setVisibility(View.GONE);
            viewHolder.itemDownload.setVisibility(View.GONE);
            // notice 내가 만들었는 데 내가 저장한 경우 공유 0 저장 x upload F download T
        }else if(!isUpload && isDownload){
            viewHolder.itemUpload.setVisibility(View.VISIBLE);
            viewHolder.itemDownload.setVisibility(View.GONE);
            // notice 내가 개척했는데 공유만 한 경우 공유 x 저장 0 upload T download F
        }else if(isUpload && !isDownload){
            viewHolder.itemUpload.setVisibility(View.GONE);
            viewHolder.itemDownload.setVisibility(View.VISIBLE);
        }

        String ExDate = item.getCreateat().substring(0,10);
        setImage(viewHolder.itemImage, item.getTrackimg());
        viewHolder.itemDay.setText("운동 날짜   "+ExDate);
        viewHolder.itemSpeed.setText("속도   "+String.valueOf(item.getSpeed()) +" Km/h");
        viewHolder.itemKcal.setText("소모 칼로리   "+String.valueOf(item.getKcal()) + " Kcal");
        viewHolder.itemDist.setText("거리   " + String.valueOf(item.getDist()) + " Km");
        int runningTime = item.getRunningtime();
        int sec = runningTime % 60;
        int min = (runningTime / 60) % 60;
        int hour = (runningTime / 60) / 60;
        // notice sec < 10 && min < 10
        if(sec < 10 && min < 10){
            viewHolder.itemTime.setText("운동 시간   "+ hour + " : 0" + min  + " : 0" + sec);
        }
        // notice sec > 10 && min < 10
        else if(sec > 10 && min < 10){
            viewHolder.itemTime.setText("운동 시간   "+ hour + " : 0" + min  + " : " + sec);
        }
        // notice sec < 10 && min > 10
        else if(sec < 10 && min > 10){
            viewHolder.itemTime.setText("운동 시간   "+ hour + " : " + min  + " : 0" + sec);
        }
        // notice sec > 10 && min > 10
        else if(sec > 10 && min > 10){
            viewHolder.itemTime.setText("운동 시간   "+ hour + " : " + min  + " : " + sec);
        }



        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogLib.getInstance().showDetailDialog(context, item.getTrackimg());
            }
        });


        viewHolder.itemDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    DialogLib.getInstance().showDownloadDialog(context, DownloadHandler, new DownloadTrack(email, item.getMytrackid(), 0), position);
            }
        });

        viewHolder.itemUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogLib.getInstance().showUploadDialog(context, UploadHandler, item.getMytrackid(), position);
            }
        });

    }

    private void setImage(ImageView imageView, String fileName){
        /* notice 서버 경로 입력
         */
        String[] file = fileName.split("/image/");
        Picasso.get().load("https://k3b201.p.ssafy.io/image/"+file[1]).into(imageView);
    }

    Handler UploadHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            changeItemUpload(msg.what, true);
        }
    };

    Handler DownloadHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            changeItemDownload(msg.what, true);
        }
    };

    static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView itemImage;
        public ImageView itemDownload;
        public ImageView itemUpload;
        public TextView itemDay;
        public TextView itemTime;
        public TextView itemSpeed;
        public TextView itemKcal;
        public TextView itemDist;

        public ViewHolder(View itemView){
            super(itemView);
            itemImage = (ImageView) itemView.findViewById(R.id.track_img);
            itemDownload = (ImageView) itemView.findViewById(R.id.save_on);
            itemUpload = (ImageView) itemView.findViewById(R.id.uplode_on);
            itemDay = (TextView) itemView.findViewById(R.id.createat);
            itemTime = (TextView) itemView.findViewById(R.id.time);
            itemSpeed = (TextView) itemView.findViewById(R.id.speed);
            itemKcal = (TextView) itemView.findViewById(R.id.Kal);
            itemDist = (TextView) itemView.findViewById(R.id.dist);
        }

    }




}