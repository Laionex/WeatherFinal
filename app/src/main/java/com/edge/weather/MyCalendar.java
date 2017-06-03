package com.edge.weather;

import android.app.AlarmManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by kim on 2017. 5. 13..
 */

public class MyCalendar extends AppCompatActivity implements View.OnClickListener {
    static final String TAG = "com.weather";
    CompactCalendarView compactCalendarView;
    Calendar calendar = Calendar.getInstance();
    List<List<Event>> events = new ArrayList<>();
    ArrayList<String> skycode = new ArrayList<>();
    ArrayList<String> namecode = new ArrayList<>();
    List<Event> deleteList = new ArrayList<>();
    ArrayList<WeekModel> weekArrayList = new ArrayList<>();
    WeekAdapter adapter;
    EventListAdapter eventListAdapter;
    SharedPreference sharedPreferences = new SharedPreference();
    Map<String, String> queryMap = new LinkedHashMap<>();
    Gson gson = new Gson();
    TextView month, year;
    TypedArray typedArray;
    ImageView cancel;
    FloatingActionButton button;
    String day;
    RecyclerView weekList, eventList;
    double longitude = 0;
    double latitude = 0;
    GPSData gpsData;
    static final int REQUEST_CODE = 1111;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mycalendar);
        //아이콘 배열 가져옴
        typedArray = getResources().obtainTypedArray(R.array.icon2);
        month = (TextView) findViewById(R.id.month);
        year = (TextView) findViewById(R.id.year);
        weekList = (RecyclerView) findViewById(R.id.weeklist);
        compactCalendarView = (CompactCalendarView) findViewById(R.id.calendar);
        eventList = (RecyclerView) findViewById(R.id.eventlist);
        eventList.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), eventList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onLongItemClick(View view, final int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MyCalendar.this);     // 여기서 this는 Activity의 this

// 여기서 부터는 알림창의 속성 설정
                builder .setMessage("삭제 하시 겠습니까?")        // 메세지 설정
                        .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            // 확인 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Event event = deleteList.get(position);
                                LinkedTreeMap<String,Object>datas= (LinkedTreeMap<String, Object>) event.getData();
                                JsonObject jsonObject = gson.toJsonTree(datas).getAsJsonObject();
                                ServiceEvent serviceEvent = gson.fromJson(jsonObject,ServiceEvent.class);
                                sharedPreferences.deleteEvent(getApplicationContext(), TAG, gson.toJson(new Event(event.getColor(),event.getTimeInMillis(),serviceEvent)));
                                compactCalendarView.removeEvent(deleteList.get(position), true);
                                eventListAdapter.notifyDataSetChanged();

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            // 취소 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton) {

                                dialog.cancel();
                            }
                        });

                final AlertDialog alertDialog = builder.create();    // 알림창 객체 생성
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#9AB4CF"));
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#ED837F"));
                    }
                });
                alertDialog.show();


            }
        }));
        cancel = (ImageView) findViewById(R.id.finish);
        cancel.setOnClickListener(this);
        //캘린더 시작 요일 설정
        compactCalendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        //위도 경도 정보 메인에서 받아옴
        latitude = getIntent().getDoubleExtra("lat", 0);
        longitude = getIntent().getDoubleExtra("lon", 0);
        gpsData = new GPSData(longitude,latitude);
        //월 폰트적용
        Typeface type = Typeface.createFromAsset(getApplicationContext().getAssets(), "BMJUA_otf.otf");

        //오늘 현재시간 파싱 후 최초 캘린더 상단에 연,월 값 세팅
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd");
        day = dayTime.format(new Date(System.currentTimeMillis()));
        String[] today = day.split("-");
        month.setText(today[1] + "월");
        year.setText(today[0]);
        month.setTypeface(type);
        year.setTypeface(type);

        //일정추가버튼
        button = (FloatingActionButton) findViewById(R.id.add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyCalendar.this, EventAddDialog.class);
                intent.putExtra("Date", day);
                intent.putExtra("gps",gpsData);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        //단말기에 저장한 이벤트들을 가져와서 캘린더에 세팅
        getMyEvent();
        //주간날씨정보
        getWeather();
        //오늘날짜 이벤트 값 세팅
        List<Event> events2 = compactCalendarView.getEvents(new Date());
        deleteList = events2;
        setEventList(events2);
        //캘린더뷰 리스너
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                //일정에 추가에 쓰일 날짜 값을 선택된 날짜로 세팅
                SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd");
                day = dayTime.format(new Date(dateClicked.getTime()));
                //캘린더 날짜에 기록된 이벤트 가져오기
                List<Event> events = compactCalendarView.getEvents(dateClicked);
                deleteList = events;
                setEventList(events);

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                //캘린더 연,월 값 세팅
                SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd");
                String a = dayTime.format(new Date(firstDayOfNewMonth.getTime()));
                String[] split = a.split("-");
                month.setText(split[1] + "월");
                year.setText(split[0]);
            }
        });

    }

    //캘린더에 해당 날짜에 이벤트 값 넣기
    private void addEvents(int month, int year) {
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = calendar.getTime();
        for (int i = 0; i < events.size(); i++) {
            //이벤트가 추가될 캘린더의 날짜 값 세팅
            calendar.setTime(firstDayOfMonth);
            if (month > -1) {
                calendar.set(Calendar.MONTH, month);
            }
            if (year > -1) {
                calendar.set(Calendar.ERA, GregorianCalendar.AD);
                calendar.set(Calendar.YEAR, year);
            }
            //이벤트 모델에 포함된 시간정보를 캘린더에 세팅하고 그 시간에 캘린더 이벤트를 추가
            for (int j = 0; j < events.get(i).size(); j++) {
                //이벤트 모델을 long 타입으로 저장하는데 그것을 date 형태로 파싱
                long millis = events.get(i).get(j).getTimeInMillis();
                SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd");
                String str = dayTime.format(new Date(millis));
                //date 부분에서 날짜 부분을 가져와 해당월 캘린더에 세팅
                String result = str.substring(str.length() - 2, str.length());
                calendar.add(Calendar.DATE, Integer.parseInt(result));
            }
            compactCalendarView.addEvents(events.get(i));
        }
    }

    private void setEventList(List<Event> events) {
        eventListAdapter = new EventListAdapter(getApplicationContext(), events);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        eventList.setLayoutManager(layoutManager);
        eventList.setAdapter(eventListAdapter);
    }

    //이벤트 모델을 단말기에 저장하기 위해 모델을 json형태의 string값으로 단말기에 저장
    private void storeMyEvent(Event event) {
        sharedPreferences.addList(getApplicationContext(), TAG, gson.toJson(event));
    }

    //단말기에서 string형태의 저장된 json을 불러와 event 모델로 매핑후 리스트형태로 만들어 캘린더에 세팅
    private void getMyEvent() {
        ArrayList<String> list = new ArrayList<>();
        try {
            list = sharedPreferences.loadList(getApplicationContext(), TAG);
        } catch (NullPointerException ignored) {
        }
        List<Event> events2 = new ArrayList<>();
        if (list != null) {
            Log.d(TAG, "aaaaaaaa");
            for (int i = 0; i < list.size(); i++) {
                Log.d(TAG, "aaaaaaaa");
                Event event = gson.fromJson(list.get(i), Event.class);
                events2.add(event);
                Log.d(TAG, event.toString());
            }
            events.clear();
            events.add(events2);
            compactCalendarView.removeAllEvents();
            addEvents(Calendar.MONTH, Calendar.YEAR);
        }
    }

    //주간 정보 query문을 map형태로 세팅후 request
    private void getWeather() {
        queryMap.put("version", ApiService.VERSION);
        queryMap.put("lat", String.valueOf(latitude));
        queryMap.put("lon", String.valueOf(longitude));
        get3Day(queryMap);
    }

    /*sk플래닛은 현재부터 3일 까지 정보와 3일 서부터 10일까지정보를 분리해서 주기때문에
      3일까지의 데이터 파싱하는 부분 - retrofit2를 이용하여 gson class mapping을
      하는것과 하드하게 파싱하는것과 차이에서 데이터 mapping을 하기위해선 class 파일이 상당히
       많이 생겨서 가독성은 떨어지지만 그냥 파싱했음.
    */
    private void day3JsonParsing(JsonObject object) throws Exception {
        JsonObject weather = (JsonObject) object.get("weather");
        JsonArray forecast3days = (JsonArray) weather.get("forecast3days");
        JsonObject jsonObject = (JsonObject) forecast3days.get(0);
        JsonObject jsonObject2 = (JsonObject) jsonObject.get("fcst3hour");
        JsonObject skyJson = (JsonObject) jsonObject2.get("sky");
        JsonObject temperatureJson = (JsonObject) jsonObject2.get("temperature");
        String iconNumSt = skyJson.get("code4hour").toString().replace("\"", "");
        String iconNumSt2 = skyJson.get("code28hour").toString().replace("\"", "");
        String iconNumSt3 = skyJson.get("code46hour").toString().replace("\"", "");
        String weatherSt = skyJson.get("name4hour").toString().replace("\"", "");
        String weatherSt2 = skyJson.get("name28hour").toString().replace("\"", "");
        String weatherSt3 = skyJson.get("name46hour").toString().replace("\"", "");
        //스카이 코드와 네임코드를 따른 배열로 일시적으로 저장해놓음.
        skycode.add(iconNumSt);
        skycode.add(iconNumSt2);
        skycode.add(iconNumSt3);
        Log.d("aaaa", iconNumSt3 + "");
        namecode.add(weatherSt);
        namecode.add(weatherSt2);
        namecode.add(weatherSt3);

    }

    /* 앞서말한 3~10 일까지인데 7주일까지로 맞췄음
     */
    private void weekJsonParsing(JsonObject object) throws Exception {
        JsonObject weather = (JsonObject) object.get("weather");
        JsonArray forecast3days = (JsonArray) weather.get("forecast6days");
        JsonObject jsonObject = (JsonObject) forecast3days.get(0);
        JsonObject skyJson = (JsonObject) jsonObject.get("sky");
        String iconNumSt = skyJson.get("pmCode4day").toString().replace("\"", "");
        String iconNumSt2 = skyJson.get("pmCode5day").toString().replace("\"", "");
        String iconNumSt3 = skyJson.get("pmCode6day").toString().replace("\"", "");
        String iconNumSt4 = skyJson.get("pmCode7day").toString().replace("\"", "");
        String weatherSt = skyJson.get("pmName4day").toString().replace("\"", "");
        String weatherSt2 = skyJson.get("pmName5day").toString().replace("\"", "");
        String weatherSt3 = skyJson.get("pmName6day").toString().replace("\"", "");
        String weatherSt4 = skyJson.get("pmName7day").toString().replace("\"", "");
        //스카이 코드와 네임코드를 따른 배열로 일시적으로 저장해놓음.
        skycode.add(iconNumSt);
        skycode.add(iconNumSt2);
        skycode.add(iconNumSt3);
        skycode.add(iconNumSt4);
        namecode.add(weatherSt);
        namecode.add(weatherSt2);
        namecode.add(weatherSt3);
        namecode.add(weatherSt4);
        calendar.setTime(new Date());
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        Log.d("aaaa22", weekday + "");
        //스카이코드와 네임코드는 항상 갯수가 같으므로 스카이 코드갯수 만큼 for문을 통하여 weekmodel을 배열로 만들어 오브젝트 배열에 값을 세팅
        for (int i = 0; i < skycode.size(); i++) {
            WeekModel model = new WeekModel();
            model.setCode(skycode.get(i));
            model.setName(namecode.get(i));
            model.setDayOfWeek(weekday);
            if (weekday == 8) {
                weekday = 1;
            }
            weekday++;
            weekArrayList.add(model);
        }
        setRecycler();
    }

    //날씨 리사이클러뷰를 이용한 1주일치 날씨정보 보여주기 horizontal 스크롤뷰가 간단할지 몰라도 메모리관리를 위해 recyclerview로 세팅
    private void setRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        weekList.setLayoutManager(layoutManager);
        adapter = new WeekAdapter(getApplicationContext(), weekArrayList, typedArray);
        weekList.setAdapter(adapter);
    }

    // 3일치 api통신
    private void get3Day(final Map<String, String> query) {
        Call<JsonObject> get3Day = SetRetrofit.setRetrofit(getApplicationContext()).shortWeather(ApiService.APP_KEY, query);
        get3Day.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    day3JsonParsing(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getWeek(query);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    //나머지 api통신
    private void getWeek(Map<String, String> query) {
        Call<JsonObject> get3Day = SetRetrofit.setRetrofit(getApplicationContext()).longWeather(ApiService.APP_KEY, query);
        get3Day.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    weekJsonParsing(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    //일정 추가부분에서 데이터를 받아와 다시 캘린더에 세팅함
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            int color = data.getIntExtra("Color", Color.parseColor("#703029"));
            long time = data.getLongExtra("Date", 0);
            String memo = data.getStringExtra("Memo");
            GPSData gpsData1 = (GPSData) data.getSerializableExtra("gps");
            ServiceEvent serviceEvent = new ServiceEvent(time,memo,gpsData1);
            Log.d(TAG, memo + "");
            Event ev3 = new Event(color, time, serviceEvent);
            storeMyEvent(ev3);
            List<Event> even = new ArrayList<>();
            even.add(ev3);
            events.add(even);
            compactCalendarView.removeAllEvents();
            addEvents(Calendar.MONTH, Calendar.YEAR);
            goService();
        }
    }

    private void goService(){
        Intent service = new Intent(MyCalendar.this, CalendarService.class);
        service.setPackage("com.edge.weather");
        service.setAction(TAG);
        startService(service);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish:
                finish();
                break;
        }
    }
}
