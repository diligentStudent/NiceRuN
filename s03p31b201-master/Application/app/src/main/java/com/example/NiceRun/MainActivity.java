package com.example.NiceRun;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.NiceRun.Util.PreferenceManager;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    // SharePreferences를 위함
    private Context context;
    public static Context mContext;

    // BackPressHandler 객체 선언, 할당
    //private BackPressHandler backPressHandler = new BackPressHandler(this);
    String address;
    private ImageView profileimg;
    String profile;
    TextView name;
    String firstname;
    String lastname;

    View nav_header_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;

        // SharePreferences 검사
        context = this;
        String text = PreferenceManager.getString(context, "email");
        if (text.equals("")) {
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        }
//        navigationView.setNavigationItemSelectedListener(this);
        //Navigation Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        nav_header_view = navigationView.getHeaderView(0);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_running, R.id.nav_calendar, R.id.nav_extra)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void setImage(ImageView imageView, String fileName){
        System.out.println(fileName);
        String[] file = fileName.split("/image/");
        Picasso.get().load("https://k3b201.p.ssafy.io/image/"+file[1]).into(imageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        name = (TextView) nav_header_view.findViewById(R.id.name);
        profileimg = (ImageView) nav_header_view.findViewById(R.id.profileimg);

        firstname = PreferenceManager.getString(context, "firstname");
        lastname = PreferenceManager.getString(context, "lastname");
        if(!firstname.equals("") && !lastname.equals("")){
            name.setText(firstname+" "+lastname);
        }

        profile = PreferenceManager.getString(context,"profileimg");
        setImage(profileimg,profile);

    }

    @Override
    public void recreate() {
        super.recreate();
    }

    /**
     * 옵션 메뉴 생성과정
     * 1. /res/menu/ 경로에 Menu Resource 파일 생성
     * 2. Activity의 onCreateOptionMenu() 오버라이딩: 엑티비티가 호출될 때 단 한 번만 호출 -> MenuItem 생성과 초기화 작업
     * 3. Activity의 onPrepareOptionMenu() 오버라이딩: 추가한 옵션메뉴를 클릭할 때마다 호출(화면에 보여질 때마다 호출)
     * 4. Activity의 onOptionItemSelected() 오버라이딩: 특정 MenuItem을 선택했을 때마다 호출
     * 5. Activity의 onOptionMenuClosed() 오버라이딩: 옵션메뉴가 활성화된 상태에서 이전 버튼이나 엑티비티 다른 영역을 클릭했을 때 호출
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        // getMenuInflater()를 통해 MenuInflater 객체를 얻어와
        // MenuInflater의 inflater(Menu리소스 ID, Menu 객체)를 호출
        return true;
    }
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {

        switch(item.getItemId())
        {
            case R.id.menu_mypage:
                Intent intent = new Intent(this,MyPageActivity.class);
                startActivity(intent);
                break;
            case R.id.log_out:
                SharedPreferences pref = getApplicationContext().getSharedPreferences("login_info", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.clear();
                editor.commit(); // commit changes
                Intent intent2 = new Intent(this,LoginActivity.class);
                startActivity(intent2);
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    /*
     @Override
    public void onBackPressed() {
        // Default
        backPressHandler.onBackPressed();
        // Toast 메세지 사용자 지정
        backPressHandler.onBackPressed("뒤로가기 버튼 한번 더 누르면 종료");
        // 뒤로가기 간격 사용자 지정
        backPressHandler.onBackPressed(3000);
        // Toast, 간격 사용자 지정
        backPressHandler.onBackPressed("뒤로가기 버튼 한번 더 누르면 종료", 3000);
    }

*/
}