package com.example.NiceRun.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.NiceRun.DTO.DownloadTrack;
import com.example.NiceRun.DTO.Mytrack;
import com.example.NiceRun.DTO.Sns;
import com.example.NiceRun.DTO.Track;
import com.example.NiceRun.DTO.Trackinfo;
import com.example.NiceRun.MainActivity;
import com.example.NiceRun.R;
import com.example.NiceRun.lib.DialogLib;
import com.example.NiceRun.main.running.RunMainFragment;
import com.example.NiceRun.main.running.SavedTracksTab;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import static com.example.NiceRun.MainActivity.mContext;

public class RecommandTrackAdapter extends RecyclerView.Adapter<RecommandTrackAdapter.ViewHolder> {
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private int resource;

    static private List<Track> trackList;
    static private List<Boolean> savedList;
    private List<Sns> snsList;
    private List<Mytrack> mytrackList;
    private String email;

    private int count;

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage;
        public ImageView itemSave;
        public TextView itemDist;
        public TextView itemComment;
        public TextView itemNomoreSave;

        private ImageView tag1_img;
        private ImageView tag2_img;
        private ImageView tag3_img;
        private ImageView tag4_img;
        private ImageView tag5_img;
        private ImageView tag6_img;

        public ViewHolder(View itemView) {

            super(itemView);
            itemImage = (ImageView) itemView.findViewById(R.id.recommand_image);
            itemComment = (TextView) itemView.findViewById(R.id.recommand_comment);
            itemDist = (TextView) itemView.findViewById(R.id.recommand_dist);
            itemSave = (ImageView) itemView.findViewById(R.id.recommand_save);

            tag1_img = (ImageView) itemView.findViewById(R.id.tag1_img);
            tag2_img = (ImageView) itemView.findViewById(R.id.tag2_img);
            tag3_img = (ImageView) itemView.findViewById(R.id.tag3_img);
            tag4_img = (ImageView) itemView.findViewById(R.id.tag4_img);
            tag5_img = (ImageView) itemView.findViewById(R.id.tag5_img);
            tag6_img = (ImageView) itemView.findViewById(R.id.tag6_img);

            itemNomoreSave = (TextView) itemView.findViewById(R.id.NoMoreSaveData);
//            for (int i = 0; i < trackList.size(); i++) {
//                if(!savedList.get(i).booleanValue()) {
//                }
//            }
        }
    }

    public RecommandTrackAdapter(Context context, int resource, List<Track> tList, List<Sns> sList, List<Boolean> flagList, List<Mytrack> mList, String email) {
        this.context = context;
        this.resource = resource;
        this.trackList = tList;
        this.snsList = sList;
        this.savedList = flagList;
        this.mytrackList = mList;
        this.email = email;
    }

    public void setItme(Sns newItem) {
        for (int i = 0; i < snsList.size(); i++) {
            Sns item = snsList.get(i);
            if (item.getMytrackid() == (newItem.getMytrackid()) && !savedList.get(i) ) {
                snsList.set(i, newItem);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addItemlist(List<Track> tList, List<Sns> sList, List<Boolean> flagList, List<Mytrack> mList) {
        this.trackList.addAll(tList);
        this.snsList.addAll(sList);
        this.savedList.addAll(flagList);
        this.mytrackList.addAll(mList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return snsList.size();
    }

    @NonNull
    @Override
    public RecommandTrackAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_tab_recommandtracks_card, parent, false);
        return new ViewHolder(view);
    }

    private void changeItemDownload(int position, boolean b){
        for (int i = 0; i < savedList.size(); i++) {
            if(i == position){
                savedList.set(i,b);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecommandTrackAdapter.ViewHolder holder, int position) {
        //Log.d(TAG, "getView " + item);

        setImage(holder.itemImage, trackList.get(position).getTrackimg());
        holder.itemDist.setText("거리   "+String.valueOf(trackList.get(position).getDist()) + " Km");
        String comment_and_tags = snsList.get(position).getComment();
        StringTokenizer st = new StringTokenizer(comment_and_tags);
        String comment = st.nextToken();
        String[] tags = new String[6];
        Arrays.fill(tags,"");
        int idx = 0;
        while (st.hasMoreTokens()){
            tags[idx++] = st.nextToken();
        }

        for (int i = 0; i < tags.length; i++) {
            switch (tags[i]){
                case "평지":
                    holder.tag1_img.setVisibility(View.VISIBLE);
                    break;
                case "언덕":
                    holder.tag2_img.setVisibility(View.VISIBLE);
                    break;
                case "솔플":
                    holder.tag3_img.setVisibility(View.VISIBLE);
                    break;
                case "여럿이":
                    holder.tag4_img.setVisibility(View.VISIBLE);
                    break;
                case "산책":
                    holder.tag5_img.setVisibility(View.VISIBLE);
                    break;
                case "질주":
                    holder.tag6_img.setVisibility(View.VISIBLE);
                    break;
            }
        }

        holder.itemComment.setText(comment);

        Log.d(TAG,String.valueOf(savedList.get(position)) +  " " + position);
        if (savedList.get(position)) {
            holder.itemSave.setVisibility(View.GONE);
        }else{
            holder.itemSave.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogLib.getInstance().showDetailRecommandDialog(context, trackList.get(position).getTrackimg(), tags, comment);
            }
        });

        holder.itemSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogLib.getInstance().showMoveDownloadDialog(context, DownloadHandler, new DownloadTrack(email, mytrackList.get(position).getMytrackid(), 1), position);

            }
        });
    }

    private void setImage(ImageView imageView, String fileName) {
        String[] file = fileName.split("/image/");
        Log.d("iiiiiiiiiiii",fileName);
        Log.d("aaaaaaaaaaaa",file[1]);
        Picasso.get().load("https://k3b201.p.ssafy.io/image/"+file[1]).into(imageView);
    }

    Handler DownloadHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            changeItemDownload(msg.what, true);
        }
    };
}
