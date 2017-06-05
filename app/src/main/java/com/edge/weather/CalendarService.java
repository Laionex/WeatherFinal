package com.edge.weather;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by kim on 2017. 5. 15..
 */

public class CalendarService extends Service {
    LocationManager mLM;
    SharedPreference sharedPreference = new SharedPreference();
    static final String TAG = "com.edge.weather.broad";
    static final String TAG2 = "com.weather";
    static final String TAG3 = "com.weather.daily";
    static final String TAG4 = "com.weather.widget";
    static final String TAG5 = "com.weather.widget.refresh";
    static final String TAG6 = "com.weather.week";
    static final int REQUEST_CODE = 11111;
    static final int REQUEST_CODE_DAILY = 10101;
    List<List<Event>> events = new ArrayList<>();
    static double longitude = 0;
    static double latitude = 0;
    static int hour = 7;
    static int minute = 0;
    long pushDate = 0;
    boolean push=false;
    boolean eventPush =false;
    boolean weekPush = false;
    Map<String, String> queryMap = new LinkedHashMap<>();
    ArrayList<int[]> iconMap2 = new ArrayList<>();
    Map<String, String> wthTextMap = new HashMap<>();
    Gson gson = new Gson();
    String eventData;
    TypedArray typedArray;
    boolean destroy = true;
    String liststring;
    Geocoder geocoder;
    GPSData gpsData;
    boolean stop = false;
    int iconNum = 0;
    static String finish = "refresh";
    String method = "widget";
    Timer time;
    String feature;
    TimerTask timerTask;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //서비스의 존재 여부에 따라 이벤트 버스 등록을 제어한다.
        if (destroy) {
            BusProvider.getInstance().register(this);
            destroy = false;
        } else {
            Log.d(TAG2, "create");
        }
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        hour = sharedPreference.getValue(getApplicationContext(),"hour",7);
        minute = sharedPreference.getValue(getApplicationContext(),"min",0);
        push = sharedPreference.getValue(getApplicationContext(),"Push",false);
        eventPush = sharedPreference.getValue(getApplicationContext(),"eventPush",false);
        weekPush = sharedPreference.getValue(getApplicationContext(),"weekPush",false);
        Log.d(TAG2, "command" + "," + intent + "," + push);
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {

                if (action.equals(TAG2)) {
                    //서비스 최초 실행

                    Log.d(TAG2, "start");
                    typedArray = getResources().obtainTypedArray(R.array.icon);
                    mLM = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
                    setIconMap();
                    setWthTextMap();
                    releaseAlarm(getApplicationContext());
                    releaseAlarmDialy();
                    if (eventPush){
                        getMyEvent();
                    }
                    if (push){
                        registerAlarmDialy(getApplicationContext());
                    }
                } else if (action.equals(TAG)) {
                    //캘린더 이벤트 알림 푸시 세팅
                    Log.d(TAG2,intent.getExtras()+",,");
                    Bundle bundle = intent.getBundleExtra("eventData");
                    eventData = bundle.getString("Data");
                    pushDate = bundle.getLong("Date", 0);
                    gpsData = (GPSData) bundle.getSerializable("gps");

                    releaseAlarm(getApplicationContext());
                    queryMap.put("version", ApiService.VERSION);
                    queryMap.put("lat", String.valueOf(gpsData.getLatitude()));
                    queryMap.put("lon", String.valueOf(gpsData.getLongitude()));
                    if (eventPush||push){
                        Log.d(TAG2, method+"zzzz");
                        method = "push";
                        getMinWeather(queryMap,method);
                    }
                    if (eventPush){
                        getMyEvent();
                    }
                } else if (action.equals(TAG3)) {
                    //매일 아침 7시 알림 세팅
                    method = "push";
                    Log.d(TAG2, "create2");
                    releaseAlarmDialy();
                    Bundle bundle = intent.getBundleExtra("dialy");
                    if (push){
                        pushDate = bundle.getLong("Date", 0);
                        registerLocationUpdates();
                        registerAlarmDialy(getApplicationContext());
                    }
                } else if (action.equals(TAG4)){
                    //위젯 세팅
                    method = "widget";
                    if (time!=null&&timerTask!=null){
                        time.cancel();
                        timerTask.cancel();
                        upDateTimeTask();
                    } else {
                        upDateTimeTask();
                    }
                    Log.d(TAG2,"getwidget");
                    mLM = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
                    //위도정보가 백그라운데 있을시 그정보를 이용하여 위젯셋팅을한다. 없을시에는 새로운 위치정보를 받아온다
                    if (longitude!=0&&latitude!=0){
                        queryMap.put("version", ApiService.VERSION);
                        queryMap.put("lat", String.valueOf(latitude));
                        queryMap.put("lon", String.valueOf(longitude));
                        if (eventPush||push){
                            getMinWeather(queryMap,method);
                        } else {
                            if (method.equals("widget")){
                                getMinWeather(queryMap,method);
                            }
                        }
                        getAddress(latitude,longitude);
                    } else {
                        registerLocationUpdates();
                    }
                } else if (action.equals(TAG5)){
                    //앱위젯 리프레쉬이다. 사용자가 위치정보가 다르거나 시간정보가 달라졋을시 리프레시 버튼을 통해 다시 세팅할수 있다.
                    //위와 따로 분류하는 이유는 리프레쉬 동작 애니메이션을 위해 finish 변수를 통해 구분한다. 돌아야 하는지 멈춰야 하는지 구분 변수
                    //메소드 변수는 날씨정보를 열람하는것이 위젯에서 하는것인지 푸시를 위한것인지 구분하기 위해 변수를 만들어 구분했다. 현재시간 날씨 정보는 위젯과 푸시 모두 이용하므로 구분이을 해야한다.
                    finish = "refresh";
                    method = "widget";
                    update();
                    if (time!=null&&timerTask!=null){
                        time.cancel();
                        timerTask.cancel();
                        upDateTimeTask();
                    } else {
                        upDateTimeTask();
                    }
                    if (finish.equals("refresh")){
                        mLM = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
                        registerLocationUpdates();
                        Log.d(TAG2, method+"zzefefzz");
                    }

                } else if (action.equals(TAG6)){
                    //이벤트 일주일전 알림이다. 알람매니저를 통해 받은 이벤트 데이터를 가지고 오늘 날짜와 이벤트 날짜 일을 비교하여 그 차이값을 가지고 일주일 일기 예보중 어느 날짜를 보여줘야 하는지 구분한다.
                    //날짜차이가 1~6까지로하여 24시간 단위로 해당 이벤트의 시간을 세팅한다
                    // 그 차이값을 주간 알람매니저 메소드로 넘겨준다
                    Calendar calendar = Calendar.getInstance();
                    Calendar current = Calendar.getInstance();
                    Bundle bundle = intent.getBundleExtra("eventData");
                    eventData = bundle.getString("Data");
                    pushDate = bundle.getLong("Date", 0);
                    gpsData = (GPSData) bundle.getSerializable("gps");

                    queryMap.put("version", ApiService.VERSION);
                    queryMap.put("lat", String.valueOf(gpsData.getLatitude()));
                    queryMap.put("lon", String.valueOf(gpsData.getLongitude()));
                    long time = bundle.getLong("Date");
                    calendar.setTimeInMillis(time);
                    current.setTimeInMillis(System.currentTimeMillis());
                    int eventDate = calendar.get(Calendar.DATE);
                    int currentDate = current.get(Calendar.DATE);
                    int dim = eventDate - currentDate;
                    if (dim==0){
                        releaseAlarmWeekly(time);
                    } else if (dim>0 && dim<7){
                        getPreWeahter(queryMap,dim);
                    }
                }
            }
        } else {
            typedArray = getResources().obtainTypedArray(R.array.icon);
            mLM = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
            setIconMap();
            setWthTextMap();
            getMyEvent();
            registerAlarmDialy(getApplicationContext());
            method = "widget";
            if (time!=null&&timerTask!=null){
                time.cancel();
                timerTask.cancel();
                upDateTimeTask();
            } else {
                upDateTimeTask();
            }
            Log.d(TAG2,"getwidget");
            mLM = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
            registerLocationUpdates();
        }

        return super.onStartCommand(intent, flags, startId);

    }
    public void upDateTimeTask (){
        time = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                update();
            }
        };
        time.schedule(timerTask,0,1000);
    }

    //캘린더의 이벤트 들을 불러온다
    private void getMyEvent() {
        ArrayList<String> list = new ArrayList<>();
        try {
            list = sharedPreference.loadList(getApplicationContext(), TAG2);
        } catch (NullPointerException ignored) {
        }
        List<Event> events2 = new ArrayList<>();
        List<ServiceEvent> sort = new ArrayList<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Event event = gson.fromJson(list.get(i), Event.class);
                events2.add(event);
                Log.d(TAG2, event.toString());
            }
            sort.clear();
            events.clear();
            events.add(events2);
            /*캘린더의 이벤트 값을 알람매니저에 활용하기위해 재배열한다.
            캘린더의 시간과 기록을 가져와 서비스 이벤트 클래스 내부에 시간순으로 재배열하는 인터페이스를 구현하여
            알람매니저에 캘린더의 이벤트 시간순대로 순차적으로 진행하게한다.
             */
            for (int i = 0; i < events.size(); i++) {
                for (int j = 0; j < events.get(i).size(); j++) {
                    LinkedTreeMap<String,Object>datas= (LinkedTreeMap<String, Object>) events.get(i).get(j).getData();
                    JsonObject jsonObject = gson.toJsonTree(datas).getAsJsonObject();
                    ServiceEvent serviceEvent = gson.fromJson(jsonObject,ServiceEvent.class);
                    sort.add(serviceEvent);
                }
            }
            /*
            재배열된 리스트를 통해 현재 시간기준으로 그 이후의 것중 첫번째 이벤트를 매니저에 등록하고 리턴한다.
             */
            for (int i = 0; i < sort.size(); i++) {
                long time = sort.get(i).getTimeInMillis();
                String data = sort.get(i).getData();
                if (time >= System.currentTimeMillis()) {
//                    Log.d(TAG2, time + "," + System.currentTimeMillis()+","+gpsData.getLatitude());
                    GPSData gpsData = sort.get(i).getGpsData();
                    registerAlarm(time, data,gpsData);
                    break;
                }
            }
            //주간 알림 받기 설정시에 주간알림 세팅
           if (weekPush){
               for (int i = 0; i < sort.size(); i++) {
                   long time = sort.get(i).getTimeInMillis();
                   String data = sort.get(i).getData();
                   releaseAlarmWeekly(time);
                   if (time >= System.currentTimeMillis()) {
                       GPSData gpsData = sort.get(i).getGpsData();
                       registerWeekAlarm(time,data,gpsData);
                   }
               }
           }
        }
    }


    //매일 아침 7시 알람
    public void registerAlarmDialy(Context context) {
        //현재 시간 날짜를 구한다
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        //현재 시간이 아침 7시 이후 시간이라면 내일 아침 7시값을 세팅한다.
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
        Log.d(TAG2, new Date(calendar.getTimeInMillis()) + "," + new Date(System.currentTimeMillis()));
        //세팅된 날짜의 값을 long type unix 타임으로 가져온다
        long time = calendar.getTimeInMillis();
        //알람매니저 등록
        Intent intent = new Intent(this, CalendarService.class);
        intent.setAction(TAG3);
        Bundle bundle = new Bundle();
        bundle.putLong("Date",calendar.getTimeInMillis());
        intent.putExtra("daily",bundle);
        intent.setPackage("com.edge.weather");
        PendingIntent sender = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_DAILY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, time, sender);
    }

    //매일 아침 알람 울린후 해제
    private void releaseAlarmDialy() {
        Log.d(TAG2, "releaseAlarm2");
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), CalendarService.class);
        PendingIntent sender = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_DAILY, intent, 0);
        alarmManager.cancel(sender);
    }

    //이벤트 시간에 맞는 알람 등록
    public void registerAlarm(long time, String data,GPSData gpsData) {
        Log.d(TAG2, "registerAlarm");
        Intent intent = new Intent(this, CalendarService.class);
        intent.setAction(TAG);
        Bundle bundle = new Bundle();
        bundle.putString("Data",data);
        bundle.putLong("Date",time);
        bundle.putSerializable("gps",gpsData);
        intent.putExtra("eventData",bundle);
        intent.setPackage("com.edge.weather");
        Log.d(TAG2,gpsData.toString()+","+gpsData.getLatitude());
        int interval = 24*60*60*1000;
        PendingIntent sender = PendingIntent.getService(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, time,interval, sender);
    }
    //알람해제 알람은 생명주기가 앱과 상관없이 os에서 다루기때문에 알람을 꼭 해제해주어야한다.
    private void releaseAlarm(Context context) {
        Log.d(TAG2, "releaseAlarm");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CalendarService.class);
        PendingIntent sender = PendingIntent.getService(context, REQUEST_CODE, intent, 0);
        sender.cancel();
        alarmManager.cancel(sender);
    }
    //이벤트 시간에 일주일전 부터 하루에 한번씩 반복알람
    //주간 알람매니저 메소드이고 시간값을 받아와 24시간단위로 세팅한다.
    //예를들면 6월 5일 15:30분 이벤트가 기록이 되있다면 오늘날짜 6월 1일 15:30 분을 기준으로 알람을 등록하고 매일 반복을 시작한다. 다만 오늘날짜의 경우 현재시간이 15:30분전이라면 알람이 울리고
    // 15:30분 이후라면 내일 날짜로 세팅한다.
    public void registerWeekAlarm(long time, String data,GPSData gpsData) {
        Log.d(TAG2, "registerAlarmweek");
        Intent intent = new Intent(this, CalendarService.class);
        intent.setAction(TAG6);
        Bundle bundle = new Bundle();
        bundle.putString("Data",data);
        bundle.putLong("Date",time);
        bundle.putSerializable("gps",gpsData);
        intent.putExtra("eventData",bundle);
        intent.setPackage("com.edge.weather");
        Calendar calendar = Calendar.getInstance();
        Calendar current= Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeInMillis(time);
        int date = current.get(Calendar.DATE);
        calendar.set(Calendar.DATE,date);
        Log.d(TAG2,calendar.getTime()+"");
        long interval = 24L*60L*60L*1000L;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int curHour = current.get(Calendar.HOUR_OF_DAY);
        int curMin = current.get(Calendar.MINUTE);
        if (hour>=curHour&&min>=curMin){
            PendingIntent sender = PendingIntent.getService(getApplicationContext(), (int) time, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC,calendar.getTimeInMillis(),interval, sender);
        } else {
            calendar.add(Calendar.DATE,1);
            PendingIntent sender = PendingIntent.getService(getApplicationContext(), (int) time, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC,calendar.getTimeInMillis(),interval, sender);
        }
    }
    //주간알람 매니저 취소 이다. 이메소드는 중복 알람 방지를 위해 필요한 메소드이다
    private void releaseAlarmWeekly(long time) {
        Log.d(TAG2, "releaseAlarm2");
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), CalendarService.class);
        PendingIntent sender = PendingIntent.getService(getApplicationContext(), (int)time, intent, 0);
        sender.cancel();
        alarmManager.cancel(sender);
    }
    // 날짜 차이 값을 가지고 구분하여 필요 메소드를 선정하고 실행한다.
    private void getPreWeahter(Map<String,String>map ,int dim){
        switch (dim){
            case 1:
                getSummaryWeather(map,dim);
                break;
            case 2:
                getSummaryWeather(map,dim);
                break;
            case 3:
                getWeek(map,dim);
                break;
            case 4:
                getWeek(map,dim);
                break;
            case 5:
                getWeek(map,dim);
                break;
            case 6:
                getWeek(map,dim);
                break;
        }
    }
    //현재위치 받아오기
    private void registerLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            try {
                mLM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        1000, 1, mLocationListener);
                mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000, 1, mLocationListener);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

//1000은 1초마다, 1은 1미터마다 해당 값을 갱신한다는 뜻으로, 딜레이마다 호출하기도 하지만
//위치값을 판별하여 일정 미터단위 움직임이 발생 했을 때에도 리스너를 호출 할 수 있다.
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                longitude = location.getLongitude();    //경도
                latitude = location.getLatitude();         //위도
                float accuracy = location.getAccuracy();        //신뢰도

                queryMap.put("version", ApiService.VERSION);
                queryMap.put("lat", String.valueOf(latitude));
                queryMap.put("lon", String.valueOf(longitude));
                if (eventPush||push){
                    getMinWeather(queryMap,method);
                } else {
                    if (method.equals("widget")){
                        getMinWeather(queryMap,method);
                    }
                }
                getAddress(latitude,longitude);
                mLM.removeUpdates(mLocationListener);
            } else {
                //Network 위치제공자에 의한 위치변화
                //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
                longitude = location.getLongitude();    //경도
                latitude = location.getLatitude();          //위도
                float accuracy = location.getAccuracy();        //신뢰도

                queryMap.put("version", ApiService.VERSION);
                queryMap.put("lat", String.valueOf(latitude));
                queryMap.put("lon", String.valueOf(longitude));
                if (eventPush||push){

                    getMinWeather(queryMap,method);
                }else {
                    if (method.equals("widget")){
                        getMinWeather(queryMap,method);
                    }
                }
                getAddress(latitude,longitude);
                mLM.removeUpdates(mLocationListener);
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    //날씨정보 받아오기
    private void getMinWeather(final Map<String, String> query, final String method) {

        Call<JsonObject> minWeather = SetRetrofit.setRetrofit(getApplicationContext()).minWeather(ApiService.APP_KEY, query);
        minWeather.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    if (method.equals("widget")){
                        jsonParsingWidget(response.body(),"minutely");
                    } else {
                        jsonParsing(query,response.body(),"minutely");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
    //내일 내일모레 날씨 받아오기 api
    private void getSummaryWeather(final Map<String,String> query,final int dim){
        Call<JsonObject> sumWeather = SetRetrofit.setRetrofit(getApplicationContext()).summaryWeather(ApiService.APP_KEY,query);
        sumWeather.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    jsonParsingSummary(response.body(),dim,query);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
    //시간단위 api 주로 쓰지 않지만 가령 분단위에서 스카이코드나 어떤 정보가 빠지기도한다 그때 오류를 캐치하여 시간단위 api로 대체하여 알려준다.
    private void getHourWeather(final Map<String, String> query, final String method) {
        Call<JsonObject> minWeather = SetRetrofit.setRetrofit(getApplicationContext()).hourWeather(ApiService.APP_KEY, query);
        minWeather.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    if (method.equals("widget")){
                        jsonParsingWidget(response.body(),"hourly");
                    } else {
                        jsonParsing(query,response.body(),"hourly");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
    //주간 api 날씨 받아오기
    private void getWeek(final Map<String, String> query, final int dim) {
        Call<JsonObject> get3Day = SetRetrofit.setRetrofit(getApplicationContext()).longWeather(ApiService.APP_KEY, query);
        get3Day.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    weekJsonParsing(response.body(),dim,query);
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

    //gps 데이터 주소정보 가져오기 지오코더 이용
    public void getAddress(double latitude, double longitude) {
        geocoder = new Geocoder(getApplicationContext());
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(
                    latitude, // 위도
                    longitude, // 경도
                    10); // 얻어올 값의 개수
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
        }
        if (list != null) {
            if (list.size() == 0) {
                String a1 = "주소를 찾지 못하였습니다.";
                feature = a1;
            } else {
                String a1 = list.get(0).getAddressLine(0);
                liststring = a1;
            }
        } else {
            String a1 = "주소를 찾지 못하였습니다.";
            liststring = a1;

        }
    }
    //날씨 정보 파싱
    private void jsonParsing(Map<String,String>map ,JsonObject object,String period) {
        try {
            JsonObject weather = (JsonObject) object.get("weather");
            JsonArray minutely = (JsonArray) weather.get(period);
            JsonObject jsonObject = (JsonObject) minutely.get(0);
            JsonObject skyJson = (JsonObject) jsonObject.get("sky");
            JsonObject temperatureJson = (JsonObject) jsonObject.get("temperature");
            String iconNumSt = skyJson.get("code").toString().replace("\"", "");
            String weatherSt = skyJson.get("name").toString().replace("\"", "");
            String temp = temperatureJson.get("tc").toString().replace("\"","");
            String tempMax = temperatureJson.get("tmax").toString().replace("\"","");
            String tempMin = temperatureJson.get("tmin").toString().replace("\"","");
            float f =0;
            if (temp.equals("")){
                f= Float.parseFloat(temp);
            } else {
                f = (Float.parseFloat(tempMax)+Float.parseFloat(tempMin))/2;
            }
            String tempRs = "현재 온도 " + String.valueOf(Math.round(f * 10.0) / 10.0) + "℃";
            String a = iconNumSt.substring(iconNumSt.length() - 2, iconNumSt.length());
            Log.d(TAG2, Integer.parseInt(a) + "");
            int[] num = iconMap2.get(Integer.parseInt(a));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            getAddress(Double.parseDouble(map.get("lat")),Double.parseDouble(map.get("lon")));
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour <= 18) {
                sendPush(num[0], wthTextMap.get(weatherSt));
            } else {
                if (num.length > 1) {
                    sendPush(num[1], wthTextMap.get(weatherSt));
                } else {
                    sendPush(num[0], wthTextMap.get(weatherSt));
                }

            }
        } catch (Exception e){
            e.printStackTrace();
            queryMap.put("version", ApiService.VERSION);
            queryMap.put("lat", String.valueOf(latitude));
            queryMap.put("lon", String.valueOf(longitude));
           // getHourWeather(queryMap,method);
        }

    }
    //내일 내일모레 날씨 파싱하여 푸시 던지기
    private void jsonParsingSummary(JsonObject object,int dim,Map<String,String> map){
        try {

            JsonObject weather= (JsonObject) object.get("weather");
            JsonArray summary = (JsonArray) weather.get("summary");
            JsonObject jsonObject = (JsonObject) summary.get(0);
            JsonObject tommorow = (JsonObject) jsonObject.get("tommorow");
            JsonObject dayAfterTommorow = (JsonObject) jsonObject.get("dayAfterTomorrow");
            getAddress(Double.parseDouble(map.get("lat")),Double.parseDouble(map.get("lon")));
            switch (dim){
                case 1:
                    sendPush(iconNum((JsonObject) tommorow.get("sky")),wthTextMap.get(weatherSt((JsonObject) tommorow.get("sky"))));
                    break;
                case 2:
                    sendPush(iconNum((JsonObject) dayAfterTommorow.get("sky")),wthTextMap.get(weatherSt((JsonObject) dayAfterTommorow.get("sky"))));
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //날씨 안내 메세지를 중복 코드를 피하기 위해 오브젝트를 인자로 받아 파싱후 리턴
    private String weatherSt(JsonObject object){
        return object.get("name").toString().replace("\"", "");
    }
    //날씨 아이콘세팅을 중보 코드를 피하기 위해 오브젝트를 인자로 받아 파싱후 현재 시간에 맞는 아이콘 넘버 리턴
    private int iconNum(JsonObject object){
        Calendar calendar = Calendar.getInstance();
        String iconNumSt = object.get("code").toString().replace("\"", "");

        //날씨 코드가 대략 "SKY_V01"이런식인데 뒤에 01 부분을 따와서 그부분을 상단에 아이콘 맵 세팅했던부분에 index로 넣어주고 그 value로 typearray에 아이콘을 가져오는 형식
        String a = iconNumSt.substring(iconNumSt.length() - 2, iconNumSt.length());
        int[] num = iconMap2.get(Integer.parseInt(a));
        //현재가 오후인지 오전인지 판단하여 날씨 아이콘이 달라짐
        calendar.setTime(new Date());
        int ampm = calendar.get(Calendar.HOUR_OF_DAY);
        if (ampm<=18){
            iconNum = num[0];
        }else {
            if (num.length > 1) {
                iconNum = num[1];
            } else {
                iconNum = num[0];
            }
        }
        return iconNum;
    }
    //위젯 날씨 정보 파싱후 위젯 업데이트 위젯에서 위에 finish 변수를 통하여 리프레쉬 여부를 세팅하는데 제이슨 파싱후 finish 변수를 "finish"로 세팅하여 날씨정보 받아오는것이 끝남을 알림
    private void jsonParsingWidget(JsonObject object,String period) {
        method = "push";
        try {
            Calendar calendar = Calendar.getInstance();
            JsonObject weather = (JsonObject) object.get("weather");
            JsonArray minutely = (JsonArray) weather.get(period);
            JsonObject jsonObject = (JsonObject) minutely.get(0);
            JsonObject skyJson = (JsonObject) jsonObject.get("sky");
            String iconNumSt = skyJson.get("code").toString().replace("\"", "");

            //날씨 코드가 대략 "SKY_V01"이런식인데 뒤에 01 부분을 따와서 그부분을 상단에 아이콘 맵 세팅했던부분에 index로 넣어주고 그 value로 typearray에 아이콘을 가져오는 형식
            String a = iconNumSt.substring(iconNumSt.length() - 2, iconNumSt.length());
            int[] num = iconMap2.get(Integer.parseInt(a));
            //현재가 오후인지 오전인지 판단하여 날씨 아이콘이 달라짐
            calendar.setTime(new Date());
            int ampm = calendar.get(Calendar.HOUR_OF_DAY);
            finish = "finish";
            mLM.removeUpdates(mLocationListener);
            update();
            if (ampm <= 18) {
                iconNum = num[0];
                completeRefresh(upDateView(liststring,iconNum,finish));
            } else {
                if (num.length > 1) {
                    iconNum = num[1];
                    completeRefresh(upDateView(liststring,iconNum,finish));
                } else {
                    iconNum = num[0];
                    completeRefresh(upDateView(liststring,iconNum,finish));
                }
            }
        } catch (Exception e){
            queryMap.put("version", ApiService.VERSION);
            queryMap.put("lat", String.valueOf(latitude));
            queryMap.put("lon", String.valueOf(longitude));
            getHourWeather(queryMap,method);
        }

    }
    //주간 날씨정보 파싱 날짜 차이값을 인자로 받아 주간날짜중 날짜 차이만큼 선택하여 해당 날씨를 푸시로 던져줌
    private void weekJsonParsing(JsonObject object,int dim,Map<String,String> map) throws Exception {
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
        getAddress(Double.parseDouble(map.get("lat")),Double.parseDouble(map.get("lon")));
        switch (dim){
            case 3:
                sendPush(weekIconNum(iconNumSt),wthTextMap.get(weatherSt));
                break;
            case 4:
                sendPush(weekIconNum(iconNumSt2),wthTextMap.get(weatherSt2));
                break;
            case 5:
                sendPush(weekIconNum(iconNumSt3),wthTextMap.get(weatherSt3));
                break;
            case 6:
                sendPush(weekIconNum(iconNumSt4),wthTextMap.get(weatherSt4));
                break;
        }
    }
    private int weekIconNum(String iconSt){
        Calendar calendar =Calendar.getInstance();
        calendar.setTime(new Date());
        String a = iconSt.substring(iconSt.length() - 2, iconSt.length());
        int[] num = iconMap2.get(Integer.parseInt(a));
        //현재가 오후인지 오전인지 판단하여 날씨 아이콘이 달라짐
        calendar.setTime(new Date());
        int ampm = calendar.get(Calendar.HOUR_OF_DAY);
        if (ampm<=18){
            iconNum = num[0];
        }else {
            if (num.length > 1) {
                iconNum = num[1];
            } else {
                iconNum = num[0];
            }
        }
        return iconNum;
    }

    //푸시 이벤트
    private void sendPush(int iconnum, String message) {
        java.text.SimpleDateFormat dayTime = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String a = dayTime.format(new Date(pushDate));
        Log.d(TAG2, a + "");
        String[] split = a.split("-");
        String month = split[1] + "월";
        String day = split[2] + "일";
        String [] location = liststring.split(" ");
        String result = location[2]+" "+location[3];
        NotificationManager notificationmanager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(typedArray.getResourceId(iconnum - 1, 0)).setWhen(System.currentTimeMillis())
                .setLargeIcon(iconBitmap(iconnum))
                .setContentTitle(month + day +" "+result +" 날씨")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message + "\n" + "메모 : " + eventData))
                .setContentText(message)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingIntent).setAutoCancel(true);
        notificationmanager.notify(1, builder.build());
    }

    //푸시 아이콘 생성
    private Bitmap iconBitmap(int num) {
        return BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(num - 1, 0));
    }

    //푸시에 쓰일 아이콘 맵핑
    private void setIconMap() {
        iconMap2.add(new int[]{38});
        iconMap2.add(new int[]{1, 8});
        iconMap2.add(new int[]{2, 9});
        iconMap2.add(new int[]{3, 10});
        iconMap2.add(new int[]{12, 40});
        iconMap2.add(new int[]{13, 41});
        iconMap2.add(new int[]{14, 42});
        iconMap2.add(new int[]{18});
        iconMap2.add(new int[]{21});
        iconMap2.add(new int[]{32});
        iconMap2.add(new int[]{4});
        iconMap2.add(new int[]{29});
        iconMap2.add(new int[]{4});
        iconMap2.add(new int[]{26});
        iconMap2.add(new int[]{27});
        iconMap2.add(new int[]{28});
    }

    //푸시 텍스트
    private void setWthTextMap() {
        wthTextMap.put("맑음", "기분 좋은 날씨에요 기모띠~!");
        wthTextMap.put("구름조금", "구름이 살짝 있어요");
        wthTextMap.put("구름많음", "구름이 많아요");
        wthTextMap.put("구름많고 비", "비가와요 우산 챙기세요");
        wthTextMap.put("구름많고 눈", "눈이내려와요 얼음길 조심하세요");
        wthTextMap.put("구름많고 비 또는 눈", "눈이나 비가내려요 감기조심하세요");
        wthTextMap.put("흐림", "날씨가 흐리지만 항상 밝게!");
        wthTextMap.put("흐리고 비", "비가와요 빗소리에 커피한잔 해봐요");
        wthTextMap.put("흐리고 비 또는 눈", "눈이나 비가내려요 감기조심하세요");
        wthTextMap.put("흐리고 낙뢰", "벼락 맞지 말고 돈벼락 맞으세요");
        wthTextMap.put("뇌우,비", "천둥 번개와 비가와요! 우루루쾅쾅!");
        wthTextMap.put("뇌우,눈", "거센 눈보라가 쳐요 감기조심하세요");
    }

    private void update()
    {

        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName weatherwidget = new ComponentName(this, WidgetProvider.class);
        int[]ids = manager.getAppWidgetIds(weatherwidget);
        final int N = ids.length;
        for (int i = 0; i < N; i++){
            int awID = ids[i];
            manager.updateAppWidget(awID,upDateView(liststring, iconNum,finish));

        }
    }

    public Bitmap buildUpdate(Context context, String time) {
        int bitmapX = time.length()*230;
        Bitmap myBitmap = Bitmap.createBitmap(bitmapX, 405, Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        Typeface clock = Typeface.createFromAsset(context.getAssets(), "BMJUA_otf.otf");
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(clock);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(280);
        paint.setTextAlign(Paint.Align.RIGHT);
        myCanvas.drawText(time, bitmapX, 330, paint);
        return myBitmap;
    }
    public Bitmap buildUpdateLocation(Context context, String address) {
        try {
            String [] location = address.split(" ");
            String result = location[2]+" "+location[3];
            int bitmapX = result.length()*40;
            Bitmap myBitmap = Bitmap.createBitmap(bitmapX, 55, Bitmap.Config.ARGB_8888);
            Canvas myCanvas = new Canvas(myBitmap);
            Paint paint = new Paint();
            Typeface clock = Typeface.createFromAsset(context.getAssets(), "BMJUA_otf.otf");
            paint.setAntiAlias(true);
            paint.setSubpixelText(true);
            paint.setTypeface(clock);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setTextSize(53);
            paint.setTextAlign(Paint.Align.LEFT);
            myCanvas.drawText(result,0,45, paint);
            return myBitmap;
        } catch (NullPointerException e){
            return null;
        }
    }
    public Bitmap buildUpdateLogo(Context context, String logo) {
        try {
            Bitmap myBitmap = Bitmap.createBitmap(140, 55, Bitmap.Config.ARGB_8888);
            Canvas myCanvas = new Canvas(myBitmap);
            Paint paint = new Paint();
            Typeface clock = Typeface.createFromAsset(context.getAssets(), "BMJUA_otf.otf");
            paint.setAntiAlias(true);
            paint.setSubpixelText(true);
            paint.setTypeface(clock);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setTextSize(53);
            paint.setTextAlign(Paint.Align.RIGHT);
            myCanvas.drawText(logo, 140,50, paint);
            return myBitmap;
        } catch (NullPointerException e){
            return null;
        }
    }
    //앱 위젯 커스텀뷰
    private RemoteViews upDateView(String address, int iconNum,String finish) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        typedArray = getApplicationContext().getResources().obtainTypedArray(R.array.icon);
        java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-hh-mm", Locale.KOREA);
        String[] date = simpleDateFormat.format(calendar.getTime()).split("-");
        String ymd = date[0] + "." + date[1] + "." + date[2];
        String time = date[3] + " : " + date[4];
        final RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);

        remoteViews.setImageViewBitmap(R.id.time, buildUpdate(getApplicationContext(), time));
        int ampm = calendar.get(Calendar.AM_PM);
        if (ampm == Calendar.AM) {
            remoteViews.setTextColor(R.id.pm, Color.parseColor("#20000000"));
            remoteViews.setTextColor(R.id.am, Color.parseColor("#000000"));
        } else {
            remoteViews.setTextColor(R.id.am, Color.parseColor("#20000000"));
            remoteViews.setTextColor(R.id.pm, Color.parseColor("#000000"));
        }
        remoteViews.setTextViewText(R.id.ymd, ymd);
        remoteViews.setProgressBar(R.id.refresh,100,0,true);
        if (iconNum < 43&&iconNum>0) {
            remoteViews.setImageViewResource(R.id.weathericon,typedArray.getResourceId(iconNum-1,0));
        }
        if (finish.equals("finish")){

            remoteViews.setViewVisibility(R.id.refresh,View.GONE);
            remoteViews.setViewVisibility(R.id.refreshback,View.VISIBLE);
        } else {

            remoteViews.setViewVisibility(R.id.refresh,View.VISIBLE);
            remoteViews.setViewVisibility(R.id.refreshback,View.GONE);
        }
        remoteViews.setImageViewBitmap(R.id.appName,buildUpdateLogo(getApplicationContext(),"웨더콕"));
        remoteViews.setImageViewBitmap(R.id.address,buildUpdateLocation(getApplicationContext(),address));
        goMain(remoteViews);
        goUpdate(remoteViews);
        return remoteViews;
    }
    public void goMain(RemoteViews remoteViews){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addCategory("android.intent.category.LAUNCHER");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.time,pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.weathericon,pendingIntent);

    }
    public void goUpdate(RemoteViews remoteViews){
        Intent intent1 = new Intent(getApplicationContext(),CalendarService.class);
        intent1.setPackage("com.edge.weather");
        intent1.setAction(TAG5);
        PendingIntent pendingIntent1 = PendingIntent.getService(getApplicationContext(),0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.refreshback,pendingIntent1);
    }
    public void reFresh(final RemoteViews remoteViews){
        remoteViews.setProgressBar(R.id.refresh,100,0,true);
        remoteViews.setViewVisibility(R.id.refresh,View.VISIBLE);
        remoteViews.setViewVisibility(R.id.refreshback,View.GONE);
    }
    public void completeRefresh(RemoteViews remoteViews){
        remoteViews.setViewVisibility(R.id.refresh,View.GONE);
        remoteViews.setViewVisibility(R.id.refreshback,View.VISIBLE);
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stop = true;

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy = true;
    }
}
