package com.example.NiceRun.main.canlendar;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.NiceRun.API.CalendarApi;
import com.example.NiceRun.DTO.Response.BasicResponse;
import com.example.NiceRun.CalendarDecorator.EventDecorator;
import com.example.NiceRun.CalendarDecorator.OneDayDecorator;
import com.example.NiceRun.CalendarDecorator.SaturdayDecorator;
import com.example.NiceRun.CalendarDecorator.SundayDecorator;
import com.example.NiceRun.DTO.CalendarSuccess;
import com.example.NiceRun.DTO.Trackinfo;
import com.example.NiceRun.R;
import com.example.NiceRun.Util.PreferenceManager;
import com.example.NiceRun.adapter.DaytrackAdapter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CalendarMainFragment extends Fragment {
    TextView noDataText;

    MaterialCalendarView mCalendarView;
    TextView whenDate;
    String selecteddate;
    String selectedMonth;

    Context context;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    DaytrackAdapter adapter;


    private final String TAG = getClass().getSimpleName();
    public String baseUrl = "https://k3b201.p.ssafy.io/api/";
    private CalendarApi calendarApi;

    private List<Trackinfo> itemList = new ArrayList<Trackinfo>();
    private List<CalendarSuccess> result = new ArrayList<CalendarSuccess>();

    JSONObject jObject;
    String id;

    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        initCalendarApI(baseUrl);

        mCalendarView = (MaterialCalendarView) rootView.findViewById(R.id.calendarView);
        //ListView listView = (ListView) rootView.findViewById(R.id.ListInfo);
        whenDate = (TextView) rootView.findViewById(R.id.whenDate);
        Date currentTime = Calendar.getInstance().getTime();
        selecteddate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime);
        selectedMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentTime);
        whenDate.setText(selecteddate);

        mCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);

         id = PreferenceManager.getString(getActivity(), "email");

         Log.d("보내는 데이터", selectedMonth+" "+id);


        mCalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                String months = "";
                months = Year+"-"+Month;
                selectedMonth = months;
                checkMonthResult(selectedMonth, id);
            }
        });

        mCalendarView.setOnDateChangedListener(new OnDateSelectedListener() // 날짜 선택 이벤트
        {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();
                String dates = "";
                if(Day < 10){
                    dates = Year + "-" + Month + "-0" + Day;
                }else{
                    dates = Year + "-" + Month + "-" + Day;
                }
                selecteddate = dates;
                whenDate.setText(selecteddate); // 선택한 날짜로 설정
                setRecyclerView();
                listInfo(selecteddate, id);
            }
        });

        return rootView;
    }

    private void checkMonthResult(String selectedMonth, String id) {
        Call<BasicResponse> postCall = calendarApi.getmonthtrack(selectedMonth, id);

        postCall.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    BasicResponse dto = response.body();
                    result = dto.getObject().getDaycalendar();
                    Log.d("monthtrack",result.toString());
                    new ApiSimulator_RedDot(result).executeOnExecutor(Executors.newSingleThreadExecutor());
                    new ApiSimulator_GreenDot(result).executeOnExecutor(Executors.newSingleThreadExecutor());
                } else {
                    Log.d(TAG, "Status Code : " + response.code());
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "Fail msg : " + t.getMessage());
            }
        });
    }

    //날짜에 점찍기
    private class ApiSimulator_RedDot extends AsyncTask<Void, Void, List<CalendarDay>> {

        List<CalendarSuccess> Time_Result;

        ApiSimulator_RedDot(List<CalendarSuccess> Time_Result){
            this.Time_Result = Time_Result;
            Log.d("Time_Result", Time_Result.toString());
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            for(int i = 0 ; i < Time_Result.size() ; i ++){
                String date = Time_Result.get(i).getDate();
                int flag = Time_Result.get(i).getFlag();

                if(flag == 1){
                    StringTokenizer st = new StringTokenizer(date,"-");

                    int year = Integer.parseInt(st.nextToken());
                    int month = Integer.parseInt(st.nextToken());
                    int dayy = Integer.parseInt(st.nextToken());

                    calendar.set(year,month-1,dayy);
                    CalendarDay day = CalendarDay.from(calendar);
                    dates.add(day);
                }
            }
            return dates;
        }
        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            //if (isFinishing()) {return;}

            mCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays, getActivity()));
        }
    }
    private class ApiSimulator_GreenDot extends AsyncTask<Void, Void, List<CalendarDay>> {

        List<CalendarSuccess> Time_Result;

        ApiSimulator_GreenDot(List<CalendarSuccess> Time_Result){
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            for(int i = 0 ; i < Time_Result.size() ; i ++){
                String date = Time_Result.get(i).getDate();
                int flag = Time_Result.get(i).getFlag();

                if(flag == 2){
                    StringTokenizer st = new StringTokenizer(date,"-");

                    int year = Integer.parseInt(st.nextToken());
                    int month = Integer.parseInt(st.nextToken());
                    int dayy = Integer.parseInt(st.nextToken());

                    calendar.set(year,month-1,dayy);
                    CalendarDay day = CalendarDay.from(calendar);
                    dates.add(day);
                }
            }
            return dates;
        }
        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            //if (isFinishing()) {return;}

            mCalendarView.addDecorator(new EventDecorator(Color.GREEN, calendarDays, getActivity()));
        }
    }
    ///////////////

    @Override
    public void onResume() {
        super.onResume();
        /*MyApp myApp = ((MyApp) getActivity().getApplication());
        Trackinfo trackinfo = myApp.getTrackinfo();
        if(adapter != null && trackinfo != null){
            adapter.setItme(trackinfo);
            myApp.setTrackinfo(null);
        }
*/
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noDataText = (TextView) view.findViewById(R.id.no_data);
        //recyclerView
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        //set
        setRecyclerView();
        //데이터 조회

        checkMonthResult(selectedMonth, id);
        listInfo(selecteddate,id);
    }

    private void setRecyclerView() {
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        //adapter
        adapter = new DaytrackAdapter(getContext(),R.layout.fragment_calendar_myinfo_card, new ArrayList<Trackinfo>(),new ArrayList<Boolean>(),new ArrayList<Boolean>(),id);
        recyclerView.setAdapter(adapter);
    }

    private void listInfo(String date, String id){
        Log.d("보내는 값",date + " "+ id);
        Call<BasicResponse> getDayTrackCall = calendarApi.getdaytrack(date, id);

        getDayTrackCall.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {

                    String check = response.body().getData();
                    if(check.equals("success")){
                        BasicResponse dto = response.body();
                        adapter.addItemlist(dto.getObject().getTrackinfoList(), dto.getObject().getUploadList(), dto.getObject().getDownloadList());
                        noDataText.setVisibility(View.GONE);
                    }else{
                        noDataText.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(TAG, "Status Code : " + response.code());
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "Fail msg : " + t.getMessage());
            }
        });

    }

    public void initCalendarApI(String baseUrl) {

        Log.d(TAG, "calendarApi : " + baseUrl);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()) // addConverterFactory로 gson converter를 생성한다. gson은 json을 자바 클래스로 바꾸는데 사용
                .build();

        calendarApi = retrofit.create(CalendarApi.class);
    }

}
