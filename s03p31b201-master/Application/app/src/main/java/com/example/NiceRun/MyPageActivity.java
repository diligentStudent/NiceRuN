package com.example.NiceRun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.NiceRun.API.TrackApi;
import com.example.NiceRun.API.UserAPI;
import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.DTO.User;
import com.example.NiceRun.Util.PreferenceManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import static com.example.NiceRun.MainActivity.mContext;

public class MyPageActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_FROM_CAMERA = 0; // 사진을 촬영하고 찍힌 이미지를 처리하는 부분
    private static final int PICK_FROM_ALBUM = 1; // 앨범에서 사진을 고르고 이미지를 처리하는 부분
    private static final int CROP_FROM_iMAGE = 2; // 이미지를 크롭하는 부분

    private Uri mImageCaptureUri;
    private ImageView iv_UserPhoto;
    private int id_view;
    private String absoultePath;
    private Bitmap photo;
    private RequestBody rqFile;
    private MultipartBody.Part mpFile;

    private EditText firstname;
    private EditText lastname;
    private EditText weight;
    private EditText height;
    private RadioGroup gender;
    private EditText goaldist;
    private EditText location;
    private EditText birthday;
    private Boolean clickGender;
    private RadioButton male;
    private RadioButton female;
    private String checkGender = "";
    private Boolean checkEmail = false;

    private String fileNameInLocal;
    private String imgName;


    private final  String TAG = getClass().getSimpleName();

    // API 요청
    private String baseUrl = "https://k3b201.p.ssafy.io/api/";
    private UserAPI userAPI;
    private TrackApi trackApi;

    private Context context;

    // 권한 확인
    private boolean externalStorageReadalbe;
    private boolean externalStorageWriteable;



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        iv_UserPhoto = (ImageView) this.findViewById(R.id.user_image);

        Button btn_agreeJoin = (Button) this.findViewById(R.id.btn_UploadPicture);
        btn_agreeJoin.setOnClickListener(this);

        Button updateBtn = (Button) this.findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(this);

        firstname = (EditText) findViewById(R.id.firstname);
        lastname = (EditText) findViewById(R.id.lastname);
        birthday = (EditText) findViewById(R.id.birthday);
        location = (EditText) findViewById(R.id.location);

        weight = (EditText) findViewById(R.id.weight);
        height = (EditText) findViewById(R.id.height);
        goaldist = (EditText) findViewById(R.id.goaldist);

        checkWritePermission();
        initMyAPI(baseUrl);

//        ReadJSON("track1");
    }
    @Override
    public void onClick(View v) { // 1. 사진 선택 버튼 누를 시 AlertDialog.Builder()가 나오고 위의 클릭이벤트 발생
        id_view = v.getId();

        if(v.getId() == R.id.btn_UploadPicture) {
            DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doTakePhotoAction();
                }
            };

            DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doTakeAlbumAction();
                }
            };

            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };

            new AlertDialog.Builder(this)
                    .setTitle("업로드할 이미지 선택")
                    .setPositiveButton("사진촬영", cameraListener)
                    .setNeutralButton("앨범선택", albumListener)
                    .setNegativeButton("취소", cancelListener)
                    .show();
        } else if(v.getId() == R.id.updateBtn) {
            context = this;
            // 이메일은 수정 불가 (SharedPreference에서 이메일 값 불러오기)
            String emailSP = PreferenceManager.getString(context, "email");

            String passwordSP = PreferenceManager.getString(context, "password");
            String firstnameSP = PreferenceManager.getString(context, "firstname");
            String lastnameSP = PreferenceManager.getString(context, "lastname");
            String locationSP = PreferenceManager.getString(context, "location");
            boolean genderSP = PreferenceManager.getBoolean(context, "gender");
            String birthdaySP = PreferenceManager.getString(context, "birthday");
            String profileimgSP = PreferenceManager.getString(context, "profileimg");
            int goaldistSP = PreferenceManager.getInt(context,"goaldist");
            int heightSP = PreferenceManager.getInt(context,"height");
            int weightSP = PreferenceManager.getInt(context,"weight");

            Log.d("1", heightSP+" "+weightSP);

            User user = new User();
            user.setEmail(emailSP);
            user.setPassword(passwordSP);
            user.setFirstname(firstnameSP);
            user.setLastname(lastnameSP);
            user.setLocation(locationSP);
            user.setGender(genderSP);
            user.setProfileimg(profileimgSP);
            user.setBirthday(birthdaySP);
            user.setGoaldist(goaldistSP);
            user.setHeight(heightSP);
            user.setWeight(weightSP);

            Log.d("2",user.toString());


            if(firstname.getText().toString().trim().length() > 0){
                user.setFirstname(firstname.getText().toString());
            }
            if(lastname.getText().toString().trim().length() > 0){
                user.setLastname(lastname.getText().toString());
            }
            if(birthday.getText().toString().trim().length() > 0){
                user.setBirthday(birthday.getText().toString());
            }
            if(location.getText().toString().trim().length() > 0){
                user.setLocation(location.getText().toString());
            }
            if(height.getText().toString().trim().length() > 0){
                user.setHeight(Integer.parseInt(height.getText().toString()));
            }
            if(weight.getText().toString().trim().length() > 0){
                user.setWeight(Integer.parseInt(weight.getText().toString()));
            }
            if(goaldist.getText().toString().trim().length() > 0){
                user.setGoaldist(Integer.parseInt(goaldist.getText().toString()));
            }
            Log.d("3",user.toString());

            // 이미지 제외한 유저 정보 수정
            Call<BasicResponse> userupdateCall = userAPI.updatemyprofile(user);
            userupdateCall.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    if(response.isSuccessful()){
                        Log.d("updatemyprofile",user.toString());

                        if(firstname.getText().toString().trim().length() > 0){
                            PreferenceManager.setString(getApplication(), "firstname", firstname.getText().toString());
                        }
                        if(lastname.getText().toString().trim().length() > 0){
                            PreferenceManager.setString(getApplication(), "lastname", lastname.getText().toString());
                        }
                        if(birthday.getText().toString().trim().length() > 0){
                            PreferenceManager.setString(getApplication(), "birthday", birthday.getText().toString());
                        }
                        if(location.getText().toString().trim().length() > 0){
                            PreferenceManager.setString(getApplication(), "location", location.getText().toString());
                        }
                        if(goaldist.getText().toString().trim().length() > 0){
                            PreferenceManager.setInt(getApplication(), "goaldist", Integer.parseInt(goaldist.getText().toString()));
                        }
                        if(height.getText().toString().trim().length() > 0){
                            PreferenceManager.setInt(getApplication(), "height", Integer.parseInt(height.getText().toString()));
                        }
                        if(weight.getText().toString().trim().length() > 0){
                            PreferenceManager.setInt(getApplication(), "weight", Integer.parseInt(weight.getText().toString()));
                        }

                        Log.d("updatemyprofile", "success user update");
                    }else{
                        Log.d("updatemyprofile","fail user update");
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    Log.d("updatemyprofile", "connect fail");
                }
            });

            if(mpFile != null){
                Call<BasicResponse> postCall = userAPI.changeprofileimg(emailSP, mpFile);
                postCall.enqueue(new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        if(response.isSuccessful()){
                            imgName =  response.body().getObject().getUser().getProfileimg();
                            PreferenceManager.setString(getApplication(), "profileimg", imgName);

                            Log.d("aaaaaaaaa",imgName);
                            Log.d("mpfile",mpFile.body().toString());
                            Log.d("success","image update");

                        }else {
                            Log.d("fail", "image faile");
                            Log.d(TAG,"Status Code : " + response.code());
                            Log.d(TAG,response.errorBody().toString());
                            Log.d(TAG,call.request().body().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {
                        Log.d(TAG,"Fail msg : " + t.getMessage());
                    }
                });

            }

            ((MainActivity) mContext).recreate();
            //Toast.makeText(this, "회원정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    public static String getExtension(String fileStr) { return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length()); }

    /**
     * Activity의 결과를 받으려면 호출할 때 startActivity() 대신 startActivityForResult() 메소드를 사용한다.
     * 새로 호출된 Activity에서는 setResult()를 통해 돌려줄 결과를 저장하고 finish()로 Activity를 종료한다.
     * 이후 결과는 호출했던 Activity의 onActivityResult() 메소드를 통해 전달한다.
     */
    // 카메라에서 사진 촬영
    public void doTakePhotoAction() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성kiki
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

        // startActivityForResult(intent, 상수)에서 상수는 onActivityResult()에 동일한 값이 전달되며
        // 이를 통해 하나의 onActivityResult()에서 여러 개의 startActivutyForResult()를 구분할 수 있다.
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    // 앨범에서 이미지 가져오기
    public void doTakeAlbumAction() {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        Log.d("doTakeAlbumAction","성공");
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);
        if(resultCode != RESULT_OK)
            return;

        switch(requestCode){
            case PICK_FROM_ALBUM:
            {
                // 이후의 처리가 카메라와 같으므로 일단 break없이 진행한다.
                mImageCaptureUri = data.getData();
            }
            case PICK_FROM_CAMERA:
            {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정한다.
                // 이후에 이미지 크롭 어플리케이션을 호출하게 된다.
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");

                // CROP할 이미지를 200*200 크기로 저장
                intent.putExtra("outputX", 200); // CROP한 이미지의 x축 크기
                intent.putExtra("outputY", 200); // CROP한 이미지의 y축 크기
                intent.putExtra("aspectX", 1); // CROP 박스의 X축 비율
                intent.putExtra("aspectY", 1); // CROP 박스의 Y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_iMAGE); // CROP_FROM_iMAGE case문 이동

                break;
            }
            case CROP_FROM_iMAGE:
            {
                // 크롭이 된 이후의 이미지를 넘겨 받는다.
                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에 임시 파일을 삭제한다.
                if(resultCode != RESULT_OK) {
                    return;
                }

                final Bundle extras = data.getExtras();

                String state = Environment.getExternalStorageState();
                if(state.equals(Environment.MEDIA_MOUNTED)){
                    if(state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
                        externalStorageReadalbe = true;
                        externalStorageWriteable = false;
                    } else{
                        externalStorageReadalbe = true;
                        externalStorageWriteable = true;
                    }
                } else{
                    externalStorageReadalbe = externalStorageWriteable = false;
                }
                // Log.d("PERMISSION111111", externalStorageReadalbe+ " "+externalStorageWriteable);

                // CROP된 이미지를 저장하기 위한 FILE 경로
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/NiceRuN/";
                fileNameInLocal = System.currentTimeMillis() +".jpeg";

                String totalFilePath = filePath+fileNameInLocal;

                if(extras != null)
                {
                    photo = extras.getParcelable("data"); // CROP된 BITMAP
                    iv_UserPhoto.setImageBitmap(photo); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌
                    // iv_UserPhoto는 이미지 뷰

                    storeCropImage(photo, totalFilePath); // CROP된 이미지를 외부저장소, 앨범에 저장한다.
                    absoultePath = totalFilePath;
                    break;
                }

                // 임시 파일 삭제
                File f = new File(mImageCaptureUri.getPath());
                if(f.exists())
                {
                    f.delete();
                }
            }
        }
    }

    /*
     * Bitmap을 저장하는 부분
     */
    private void storeCropImage(Bitmap bitmap, String filePath) {
        // NiceRuN 폴더를 생성하여 이미지를 저장하는 방식이다.
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NiceRuN";

        Log.d("filepath",filePath);
        Log.d("dirpath",dirPath);

        // 폴더 생성
        File directory = new File(dirPath); // 바로 파일 생성이 되는 것이 아니라, 그 디렉터리를 가진 객체를 생성
        if(!directory.exists()) { // directory_NiceRuN 디렉터리에 폴더가 없다면 (새로 이미지를 저장할 경우에 속한다.)
            directory.mkdir();
        }

        File copyFile = new File(filePath); // filepath: CROP된 이미지를 저장하기 위한 FILE 경로
        BufferedOutputStream out = null;
        Log.d("copyfile",copyFile.toString());

        try {
            copyFile.createNewFile();  // 자동으로 빈 파일을 생성합니다.


            out = new BufferedOutputStream(new FileOutputStream(copyFile)); // 파일을 쓸 수 있는 스트림을 준비합니다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // compress 함수를 사용해 스트림에 비트맵을 저장합니다.

            // sendBroadcast를 통해 Crop된 사진을 앨범에 보이도록 갱신한다.
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));

            // request 데이터로 변환
            rqFile = RequestBody.create(MediaType.parse("multipart/form-data"), copyFile);
            mpFile = MultipartBody.Part.createFormData("mf", copyFile.getName(), rqFile); // 킷값, 파일 이름, 데이터

            out.flush();
            out.close();
        } catch (Exception e) {
            Log.d("rot",dirPath);
            e.printStackTrace();
        }
    }

    private void initMyAPI(String baseUrl){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        userAPI = retrofit.create(UserAPI.class);
        trackApi = retrofit.create(TrackApi.class);
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
            int permissionCheck3 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck3 != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            int permissionCheck4 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION);
            if (permissionCheck3 != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_MEDIA_LOCATION}, 1);
            }

        }
    }

}