package com.example.NiceRun.lib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.NiceRun.DTO.UploadTrack;
import com.example.NiceRun.DTO.DownloadTrack;
import com.example.NiceRun.MainActivity;
import com.example.NiceRun.R;
import com.example.NiceRun.RecommandDialog;
import com.example.NiceRun.adapter.SavedTrackAdapter;
import com.example.NiceRun.main.running.RunMainFragment;
import com.example.NiceRun.main.running.SavedTracksTab;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.StringTokenizer;

import static com.example.NiceRun.MainActivity.mContext;

/**
 * 다이얼로그와 관련된 메소드로 구성된 라이브러리
 */
public class DialogLib {
    public final String TAG = DialogLib.class.getSimpleName();
    private volatile static DialogLib instance;

    public static DialogLib getInstance() {
        if (instance == null) {
            synchronized (DialogLib.class) {
                if (instance == null) {
                    instance = new DialogLib();
                }
            }
        }
        return instance;
    }

    RecommandDialog recommandDialog;

    public void showUploadDialog(Context context, final Handler handler, int mytrackid, int position) {
        String[] checkItemsName = new String[7];
        Arrays.fill(checkItemsName, "");
        final EditText editText = new EditText(context);
        editText.setHint("후기");
        final CheckBox checkBox1 = new CheckBox(context);
        String[] tags = {"평지", "언덕", "솔플", "여럿이", "산책", "질주"};
        boolean[] checkedItems = {false, false, false, false, false, false};

        new AlertDialog.Builder(context)
                .setTitle(R.string.upload_insert)
                .setMultiChoiceItems(tags, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        //Toast.makeText(context, "words : " + animals[which], Toast.LENGTH_SHORT).show();
                        if (isChecked) {
                            checkItemsName[which + 1] = tags[which];
                        } else {
                            checkItemsName[which + 1] = "";
                        }
                    }
                })
                //.setMessage(R.string.upload_insert_message)
                .setView(editText)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String comment = editText.getText().toString();
                        StringTokenizer st = new StringTokenizer(comment);
                        StringBuilder sb = new StringBuilder();
                        while (st.hasMoreTokens()) {
                            sb.append(st.nextToken());
                        }
                        String extrackComment = sb.toString();
                        checkItemsName[0] = extrackComment;

                        String tag_and_comment = "";
                        for (int i = 0; i < 7; i++) {
                            if (checkItemsName != null) {
                                tag_and_comment += checkItemsName[i] + " ";
                            }
                        }
                        //Toast.makeText(context,showmethemoney, Toast.LENGTH_LONG).show();
                        UploadLib.getInstance().UploadTrack(handler, tag_and_comment, mytrackid, position);
                    }
                })
                .show();
    }

    public void showDownloadDialog(Context context, final Handler handler,
                                   DownloadTrack downloadTrack, int position) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.save_insert)
                .setMessage(R.string.save_insert_message)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadLib.getInstance().DownloadTrack(handler, downloadTrack, position);
                        ((MainActivity) mContext).recreate();
                    }
                })
                .show();
    }

    public void showMoveDownloadDialog(Context context, final Handler handler,
                                       DownloadTrack downloadTrack, int position) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.save_insert)
                .setMessage(R.string.save_insert_message)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadLib.getInstance().DownloadTrack(handler, downloadTrack, position);
                        //((MainActivity) mContext).recreate();

                    }
                })
                .show();
    }


    public void showDetailDialog(Context context, String trackimg) {
        final ImageView DetailImg = new ImageView(context);
        setImage(DetailImg, trackimg);
        new AlertDialog.Builder(context)
                .setTitle(R.string.Detail_view)
                .setView(DetailImg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //DownloadLib.getInstance().DownloadTrack(handler, downloadTrack, position);
                        //((MainActivity) mContext).recreate();

                    }
                })
                .show();
    }

    public void showDetailRecommandDialog(Context context, String trackimg, String[] tags, String comment) {

        recommandDialog = new RecommandDialog(context, trackimg, tags, comment);
        recommandDialog.show();
        recommandDialog.getWindow().setLayout((getWidth(context)), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void setImage(ImageView imageView, String fileName) {
        /* notice 서버 경로 입력
         */
        String[] file = fileName.split("/image/");
        Picasso.get().load("https://k3b201.p.ssafy.io/image/" + file[1]).into(imageView);
    }

    public static int getWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}
