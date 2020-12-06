package com.example.NiceRun;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

public class RecommandDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private String trackimg;
    private String[] tags;
    private String comment;

    private TextView DetailTitle;
    private ImageView Detail_track_img;

    private ImageView tag1_img;
    private TextView img1_text;
    private ImageView tag2_img;
    private TextView img2_text;
    private ImageView tag3_img;
    private TextView img3_text;
    private ImageView tag4_img;
    private TextView img4_text;
    private ImageView tag5_img;
    private TextView img5_text;
    private ImageView tag6_img;
    private TextView img6_text;

    private TextView dialog_comment;

    private Button okayButton;

    private String[] tagNames = {"편해요", "언덕이있어요", "솔플", "여럿이", "산책","질주"};
    public RecommandDialog(@NonNull Context context,String trackimg, String[] tags, String comment) {
        super(context);
        this.mContext = context;
        this.trackimg = trackimg;
        this.tags = tags;
        this.comment = comment;
    }


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommand_detail_dialog);

        DetailTitle = (TextView) findViewById(R.id.recommand_detail_title);
        Detail_track_img = (ImageView) findViewById(R.id.detail_track_img);

        tag1_img = (ImageView) findViewById(R.id.tag1_img);
        img1_text = (TextView) findViewById(R.id.img1_text);
        tag2_img = (ImageView) findViewById(R.id.tag2_img);
        img2_text = (TextView) findViewById(R.id.img2_text);
        tag3_img = (ImageView) findViewById(R.id.tag3_img);
        img3_text = (TextView) findViewById(R.id.img3_text);
        tag4_img = (ImageView) findViewById(R.id.tag4_img);
        img4_text = (TextView) findViewById(R.id.img4_text);
        tag5_img = (ImageView) findViewById(R.id.tag5_img);
        img5_text = (TextView) findViewById(R.id.img5_text);
        tag6_img = (ImageView) findViewById(R.id.tag6_img);
        img6_text = (TextView) findViewById(R.id.img6_text);

        dialog_comment = (TextView) findViewById(R.id.dialog_comment);

        okayButton = (Button) findViewById(R.id.okay_button);
        okayButton.setOnClickListener(this);

        setImage(Detail_track_img, trackimg);

        for (int i = 0; i < tags.length; i++) {
            switch (tags[i]){
                case "평지":
                    tag1_img.setVisibility(View.VISIBLE);
                    img1_text.setVisibility(View.VISIBLE);
                    break;
                case "언덕":
                    tag2_img.setVisibility(View.VISIBLE);
                    img2_text.setVisibility(View.VISIBLE);
                    break;
                case "솔플":
                    tag3_img.setVisibility(View.VISIBLE);
                    img3_text.setVisibility(View.VISIBLE);
                    break;
                case "여럿이":
                    tag4_img.setVisibility(View.VISIBLE);
                    img4_text.setVisibility(View.VISIBLE);
                    break;
                case "산책":
                    tag5_img.setVisibility(View.VISIBLE);
                    img5_text.setVisibility(View.VISIBLE);
                    break;
                case "질주":
                    tag6_img.setVisibility(View.VISIBLE);
                    img6_text.setVisibility(View.VISIBLE);
                    break;
            }
        }

        dialog_comment.setText(comment);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okay_button:
                dismiss();
                break;
        }
    }

    private void setImage(ImageView imageView, String fileName){
        /* notice 서버 경로 입력
         */
        String[] file = fileName.split("/image/");
        Picasso.get().load("https://k3b201.p.ssafy.io/image/"+file[1]).into(imageView);
    }

}
